/**
 * 
 */
package com.thinkbiganalytics.nifi.v2.spark;

/*-
 * #%L
 * kylo-nifi-spark-processors
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.spark.multiexec.MultiSparkExecArguments;
import com.thinkbiganalytics.spark.multiexec.SparkApplicationCommand;
import com.thinkbiganalytics.spark.multiexec.SparkApplicationCommandsBuilder;
import com.thinkbiganalytics.spark.multiexec.SparkApplicationCommandsBuilder.SparkCommandBuilder;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.annotation.behavior.DynamicProperty;
import org.apache.nifi.annotation.behavior.DynamicRelationship;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.PropertyValue;
import org.apache.nifi.components.PropertyDescriptor.Builder;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.components.Validator;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.util.StandardValidators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jersey.repackaged.com.google.common.base.Objects;

@EventDriven
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@Tags({"spark", "thinkbig"})
@CapabilityDescription("Execute one or more Spark apps sharing a single context.")
@DynamicProperty(name = "Spark app-specific property", value = "Spark app argument", supportsExpressionLanguage = true, description = "Specifies Spark application arguments in the form '<appname>.class', <appname>.arg.<n>, <appname>.arg.<name>,...")
@DynamicRelationship(name = "Name from Dynamic Property", description = "FlowFiles that match the Dynamic Property's Attribute Expression Language")
public class ExecuteSparkApps extends ExecuteSparkJob {
    
    protected static final Pattern APP_JARS_PATTERN = Pattern.compile("^\\s*(\\w+)\\.jars");
    protected static final Pattern APP_CLASS_PATTERN = Pattern.compile("^\\s*(\\w+)\\.class");
    protected static final Pattern APP_REL_PATTERN = Pattern.compile("^\\s*(\\w+)\\.(success|failure)");
    protected static final Pattern APP_ARGS_PATTERN = Pattern.compile("^\\s*(\\w+)\\.args");
    protected static final Pattern APP_POS_ARG_PATTERN = Pattern.compile("^\\s*(\\w+)\\.arg\\.(\\d+)");
    protected static final Pattern APP_NAMED_ARG_PATTERN = Pattern.compile("^\\s*(\\w+)\\.arg\\.([a-zA-Z][\\w-]*)");

    // Relationships
    public static final Relationship JOB_SUCCESS = new Relationship.Builder()
        .name("Spark job success")
        .description("Success of all apps within the spark job")
        .build();
    
    public static final Relationship JOB_FAILURE = new Relationship.Builder()
        .name("Spark job failure")
        .description("Failure of at least one app within the spark job")
        .build();

    // These property descriptors replace the ones installed in the superclass.
    public static final PropertyDescriptor APPLICATION_JAR = new PropertyDescriptor.Builder()
        .name("ApplicationJAR")
        .description("Path to the JAR file containing the multi-app executing Spark job application - normally not changed.")
        .required(true)
        .defaultValue("${nifi.home}/current/lib/app/kylo-spark-multi-exec-jar-with-dependencies.jar")
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor MAIN_CLASS = new PropertyDescriptor.Builder()
        .name("MainClass")
        .description("Qualified classname of the multi-app executing Spark job application class - normally not changed.")
        .defaultValue("com.thinkbiganalytics.spark.multiexec.MultiSparkExecApp")
        .required(true)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .expressionLanguageSupported(true)
        .build();
    public static final PropertyDescriptor MAIN_ARGS = new PropertyDescriptor.Builder()
        .name("MainArgs")
        .description("Comma separated set of addional arguments to be passed, allong with the --apps argument, to the Spark job application main method.")
        .required(false)
        .expressionLanguageSupported(true)
        .build();
    
    private PropertyDescriptor appNamesPropDescriptor;
    private final AtomicReference<Set<Relationship>> dynamicRelationships = new AtomicReference<>(Collections.emptySet());
    private final AtomicReference<Set<String>> appNames = new AtomicReference<>(Collections.emptySet());
    private final Map<String, AppCommand> appCommands = new ConcurrentHashMap<>();
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.v2.spark.ExecuteSparkJob#addProperties(java.util.Set)
     */
    @Override
    protected void addProperties(Set<PropertyDescriptor> set) {
        set.add(this.appNamesPropDescriptor);
        
        super.addProperties(set);
        
        set.remove(ExecuteSparkJob.APPLICATION_JAR);
        set.remove(ExecuteSparkJob.MAIN_CLASS);
        set.remove(ExecuteSparkJob.MAIN_ARGS);
        
        set.add(ExecuteSparkApps.APPLICATION_JAR);
        set.add(ExecuteSparkApps.MAIN_CLASS);
        set.add(ExecuteSparkApps.MAIN_ARGS);
    }
    
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor#init(org.apache.nifi.processor.ProcessorInitializationContext)
     */
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor#init(org.apache.nifi.processor.ProcessorInitializationContext)
     */
    @Override
    protected void init(ProcessorInitializationContext context) {
        this.appNamesPropDescriptor = createAppNamesProperty();
    
        super.init(context);
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.v2.spark.ExecuteSparkJob#getApplicationJar(org.apache.nifi.processor.ProcessContext, org.apache.nifi.flowfile.FlowFile)
     */
    @Override
    protected String getApplicationJar(ProcessContext context, FlowFile flowFile) {
        String jar = context.getProperty(APPLICATION_JAR).evaluateAttributeExpressions(flowFile).getValue().trim();
        getLog().info("Spark app jar: {}", new Object[] { jar });
        return jar;
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.v2.spark.ExecuteSparkJob#getMainClass(org.apache.nifi.processor.ProcessContext, org.apache.nifi.flowfile.FlowFile)
     */
    @Override
    protected String getMainClass(ProcessContext context, FlowFile flowFile) {
        String mainClass = context.getProperty(MAIN_CLASS).evaluateAttributeExpressions(flowFile).getValue().trim();
        getLog().info("Spark app class: {}", new Object[] { mainClass });
        return mainClass;
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.v2.spark.ExecuteSparkJob#getMainArgs(org.apache.nifi.processor.ProcessContext, org.apache.nifi.flowfile.FlowFile)
     */
    @Override
    protected String[] getMainArgs(ProcessContext context, FlowFile flowFile) {
        SparkApplicationCommandsBuilder listBldr = new SparkApplicationCommandsBuilder();
        String namesCsv = context.getProperty(this.appNamesPropDescriptor).evaluateAttributeExpressions(flowFile).getValue();
        
        for (String appName : parseAppNames(namesCsv)) {
            AppCommand cmd = this.appCommands.get(appName);
            SparkCommandBuilder cmdBldr = listBldr.application(cmd.name);
            PropertyValue classProp = context.newPropertyValue(cmd.appClass);
            cmdBldr.className(classProp.evaluateAttributeExpressions(flowFile).getValue());
            
            for (Entry<NamedArgument, String> entry : cmd.namedArgs.entrySet()) {
                PropertyValue valueProp = context.newPropertyValue(entry.getValue());
                cmdBldr.addArgument(entry.getKey().argName, valueProp.evaluateAttributeExpressions(flowFile).getValue());
            }
            
            cmd.positionalArgs.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e1.getKey().position, e2.getKey().position))
                .map(entry -> context.newPropertyValue(entry.getValue()))
                .forEach(prop -> cmdBldr.addArgument(prop.evaluateAttributeExpressions(flowFile).getValue()));
            
            cmdBldr.add();
        }
        
        List<SparkApplicationCommand> commands = listBldr.build();
        
        String[] args = MultiSparkExecArguments.createCommandLine(commands);
        getLog().info("Spark main args: {} {}", args);
        return args;
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.v2.spark.ExecuteSparkJob#getExtraJars(org.apache.nifi.processor.ProcessContext, org.apache.nifi.flowfile.FlowFile)
     */
    @Override
    protected String getExtraJars(ProcessContext context, FlowFile flowFile) {
        StringBuilder propJars = new StringBuilder(super.getExtraJars(context, flowFile));
        String allAppJars = this.appCommands.values().stream()
            .map(app -> context.newPropertyValue(app.appJars))
            .map(prop -> prop.evaluateAttributeExpressions(flowFile).getValue())
            .collect(Collectors.joining(","));
        return StringUtils.isBlank(propJars) ? allAppJars : propJars + "," + allAppJars;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.processor.BaseProcessor#getRelationships()
     */
    @Override
    public Set<Relationship> getRelationships() {
        return Stream.concat(super.getRelationships().stream(), this.dynamicRelationships.get().stream())
                        .collect(Collectors.toSet());
    }

    /* (non-Javadoc)
     * @see org.apache.nifi.components.AbstractConfigurableComponent#onPropertyModified(org.apache.nifi.components.PropertyDescriptor, java.lang.String, java.lang.String)
     */
    @Override
    public void onPropertyModified(PropertyDescriptor descriptor, String oldValue, String newValue) {
        if (descriptor.equals(this.appNamesPropDescriptor)) {
            appNamesModified(newValue);
        } else if (descriptor.isDynamic()) {
            parseAppClassArg(descriptor.getName()).ifPresent(arg -> {
                appClassModified(arg, oldValue, newValue);
                return;
            });
            
            parseAppJarsArg(descriptor.getName()).ifPresent(arg -> {
                appJarsModified(arg, oldValue, newValue);
                return;
            });
            
            parseAppArgs(descriptor.getName()).ifPresent(arg -> {
                appArgsModified(arg, oldValue, newValue);
                return;
            });
            
            parseNamedArg(descriptor.getName()).ifPresent(arg -> {
                namedArgModified(arg, oldValue, newValue);
                return;
            });
            
            parsePositionalArg(descriptor.getName()).ifPresent(arg -> {
                positionalArgModified(arg, oldValue, newValue);
                return;
            });
            
            parseAppRelationshipArg(descriptor.getName()).ifPresent(arg -> {
                appRelationshipModified(arg, oldValue, newValue);
            });
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.nifi.components.AbstractConfigurableComponent#getSupportedDynamicPropertyDescriptor(java.lang.String)
     */
    @Override
    protected PropertyDescriptor getSupportedDynamicPropertyDescriptor(String propName) {
        return parseAppArg(propName)
                .map(appArg -> createAppArgPropertyDescriptor(appArg))
                .orElse(createUnknownArgDescriptor(propName));
    }
    
    private Optional<AppArgument> parseAppArg(final String propName) {
        return Stream.<Supplier<Optional<? extends AppArgument>>>of(() -> parseAppClassArg(propName), 
                                                                    () -> parseAppJarsArg(propName), 
                                                                    () -> parseAppArgs(propName), 
                                                                    () -> parseAppRelationshipArg(propName), 
                                                                    () -> parseNamedArg(propName), 
                                                                    () -> parsePositionalArg(propName))
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .findFirst()
                .map(Optional::get);
    }

    private AppCommand ensureAppCommand(String appName) {
        return this.appCommands.computeIfAbsent(appName, name -> new AppCommand(name));
    }

    private void appNamesModified(String listValue) {
        Set<String> newList = new LinkedHashSet<>();
        
        if (StringUtils.isNotBlank(listValue)) {
            parseAppNames(listValue).stream()
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .forEach(appName -> {
                    ensureAppCommand(appName);
                    newList.add(appName);
                });
        }
        this.appNames.set(newList);
        
    }

    private void appJarsModified(AppArgument arg, String oldValue, String newValue) {
        if (StringUtils.isBlank(newValue)) {
            ensureAppCommand(arg.appName).appJars = null;
        } else {
            ensureAppCommand(arg.appName).appJars = newValue;
        }
    }
    
    private void appArgsModified(AppArgument arg, String oldValue, String newValue) {
        if (StringUtils.isBlank(newValue)) {
            ensureAppCommand(arg.appName).args = null;
        } else {
            ensureAppCommand(arg.appName).args = newValue;
        }
    }

    private void appClassModified(AppArgument arg, String oldValue, String newValue) {
        if (StringUtils.isBlank(newValue)) {
            ensureAppCommand(arg.appName).appClass = null;
        } else {
            ensureAppCommand(arg.appName).appClass = newValue;
        }
    }
    
    private void appRelationshipModified(RelationshipArgument arg, String oldValue, String newValue) {
        AppCommand command = ensureAppCommand(arg.appName);
        
        if (newValue == null) {
            removeRelationship(arg, command);
        } else {
            addRelationship(arg, command, newValue);
        }
    }
    
    private void addRelationship(RelationshipArgument arg, AppCommand command, String newValue) {
        final Relationship rel = new Relationship.Builder()
                        .name(command.name + " - " + (arg.successful ? "success" : "failure"))
                        .description("App " + command.name + " completed successfullly")
                        .build();
        
        if (rel != null) {
            this.dynamicRelationships.getAndUpdate(set -> {
                Set<Relationship> newSet = new HashSet<>(set);
                newSet.add(rel);
                return Collections.unmodifiableSet(newSet);
            });
        }
        
        if (arg.successful) {
            command.successeRelationship = rel;
            command.successValue = newValue;
        } else {
            command.failureRelationship = rel;
            command.failureValue = newValue;
        }
    }

    private void removeRelationship(RelationshipArgument arg, AppCommand command) {
        final Relationship rel = arg.successful ? command.successeRelationship : command.failureRelationship;
        
        if (rel != null) {
            this.dynamicRelationships.getAndUpdate(set -> {
                Set<Relationship> newSet = new HashSet<>(set);
                newSet.remove(rel);
                return Collections.unmodifiableSet(newSet);
            });
        }
        
        if (arg.successful) {
            command.successeRelationship = null;
            command.successValue = null;
        } else {
            command.failureRelationship = null;
            command.failureValue = null;
        }
    }

    private void namedArgModified(NamedArgument arg, String oldValue, String newValue) {
        if (StringUtils.isBlank(newValue)) {
            ensureAppCommand(arg.appName).namedArgs.remove(arg);
        } else {
            ensureAppCommand(arg.appName).namedArgs.put(arg, newValue);
        }
    }
    
    private void positionalArgModified(PositionalArgument arg, String oldValue, String newValue) {
        if (StringUtils.isBlank(newValue)) {
            ensureAppCommand(arg.appName).positionalArgs.remove(arg);
        } else {
            ensureAppCommand(arg.appName).positionalArgs.put(arg, newValue);
        }
    }

    protected Optional<NamedArgument> parseNamedArg(String propName) {
        Matcher matcher = APP_NAMED_ARG_PATTERN.matcher(propName);
        
        if (matcher.matches()) {
            String appName = matcher.group(1);
            String argName = matcher.group(2);
            
            return Optional.of(new NamedArgument(appName, argName));
        } else {
            return Optional.empty();
        }
    }
    
    protected Optional<PositionalArgument> parsePositionalArg(String propName) {
        Matcher matcher = APP_POS_ARG_PATTERN.matcher(propName);
        
        if (matcher.matches()) {
            String appName = matcher.group(1);
            String argId = matcher.group(2);
            
            try {
                int pos = Integer.parseInt(argId);
                return Optional.of(new PositionalArgument(appName, pos));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }
    
    protected Optional<RelationshipArgument> parseAppRelationshipArg(String propName) {
        Matcher matcher = APP_REL_PATTERN.matcher(propName);
        if (matcher.matches()) {
            boolean success = matcher.group(2).equals("success");
            return Optional.of(new RelationshipArgument(matcher.group(1), success));
        } else {
            return Optional.empty();
        }
    }
    
    protected Optional<AppArgument> parseAppClassArg(String propName) {
        return getMatched(propName, APP_CLASS_PATTERN, 1).map(appName -> new AppArgument(appName, "class"));
    }
    
    protected Optional<AppArgument> parseAppJarsArg(String propName) {
        return getMatched(propName, APP_JARS_PATTERN, 1).map(appName -> new AppArgument(appName, "jars"));
    }
    
    protected Optional<AppArgument> parseAppArgs(String propName) {
        return getMatched(propName, APP_ARGS_PATTERN, 1).map(appName -> new AppArgument(appName, "args"));
    }
    
    protected Optional<String> getMatched(String value, Pattern pattern, int groupIdx) {
        Matcher matcher = pattern.matcher(value);
        if (matcher.matches()) {
            return Optional.of(matcher.group(groupIdx));
        } else {
            return Optional.empty();
        }
    }

    protected List<String> parseAppNames(String listValue) {
        String trimmed = listValue.trim();
        return Arrays.stream(trimmed.split("\\s*,\\s*")).collect(Collectors.toList());
    }

    private PropertyDescriptor createAppArgPropertyDescriptor(AppArgument arg) {
        final PropertyDescriptor.Builder builder = new PropertyDescriptor.Builder()
                        .required(false)
                        .name(arg.getPropertyName())
                        .dynamic(true)
                        .expressionLanguageSupported(true);
        addValidation(arg, builder);
        
        return builder.build();
    }
    
    private PropertyDescriptor createUnknownArgDescriptor(String propName) {
        return new PropertyDescriptor.Builder()
                        .required(false)
                        .name(propName)
                        .dynamic(true)
                        .expressionLanguageSupported(true)
                        .addValidator(new Validator() {
                            @Override
                            public ValidationResult validate(String subject, String input, ValidationContext context) {
                                return new ValidationResult.Builder()
                                                .subject(subject)
                                                .input(input)
                                                .valid(false)
                                                .explanation("it is an unrecognized app property type: " + propName)
                                                .build();
                            }
                        })
                        .build();
    }

    private void addValidation(AppArgument arg, Builder builder) {
//        builder.required(arg.argType.equals("class") || arg.argType.equals("jars"));
        createArgumentValidators(arg).forEach(validator -> builder.addValidator(validator));
    }

    private List<Validator> createArgumentValidators(final AppArgument arg) {
        List<Validator> validators = new ArrayList<>();
        validators.add(new Validator() {
            @Override
            public ValidationResult validate(String subject, String input, ValidationContext context) {
                if (appNames.get().contains(arg.appName)) {
                    return new ValidationResult.Builder().subject(subject).input(input).explanation("valid argument").valid(true).build();
                } else {
                    return new ValidationResult.Builder().subject(subject).input(input).explanation("it contains an unrecognized app name: \"" + arg.appName + "\"").valid(false).build();
                }
            }
        });
        
        if (arg.argType.equals("class") || arg.argType.equals("jars")) {
            validators.add(StandardValidators.NON_EMPTY_VALIDATOR);
        }
        
        return validators;
    }

    private PropertyDescriptor createAppNamesProperty() {
        return new PropertyDescriptor.Builder()
            .name("Application Names")
            .description("A comma-separated list of names for applications to be used for later command configuration.  The listed applications will be excecuted in the order given.")
            .required(true)
            .addValidator(createRequiredAppPropsValidator())
            .expressionLanguageSupported(true)
            .build();
    }

    private Validator createRequiredAppPropsValidator() {
        return new Validator() {
            @Override
            public ValidationResult validate(String subject, String input, ValidationContext context) {
                ValidationResult.Builder builder = new ValidationResult.Builder().subject(subject).input(input);
                StringBuilder explBuilder = new StringBuilder();
                Set<String> incompleteApps = new LinkedHashSet<>();
                
                parseAppNames(input).forEach(appName -> {
                    AppCommand command = appCommands.get(appName);
                    
                    if (command == null || StringUtils.isBlank(command.appClass)) {
                        incompleteApps.add(appName);
                    }
                });
                
                return builder
                        .valid(incompleteApps.size() == 0)
                        .explanation(incompleteApps.size() > 0 ? "there is a missing required parameter \"<app>.class\" for the app(s): " + incompleteApps : "valid app names")
                        .build();
            }
        };
    }
    
    // Comparators grouping alphabetically by app name and arg type, where arg type "class", "success", and "failure" highest precedence.
    private static final List<Comparator<AppArgument>> ARG_COMPARITORS = Arrays.asList((a1, a2) -> a1.appName.compareTo(a2.appName), 
                                                                                       (a1, a2) -> a1.argType.equals("class") ? -1 : 0,
                                                                                       (a1, a2) -> a1.argType.equals("jars") ? -1 : 0,
                                                                                       (a1, a2) -> a1.argType.equals("success") ? -1 : 0,
                                                                                       (a1, a2) -> a1.argType.equals("failure") ? -1 : 0,
                                                                                       (a1, a2) -> a1.argType.compareTo(a2.argType));
    // Comparators with added positional arg grouping.
    private static final List<Comparator<AppArgument>> POSISITIONAL_COMPARITORS 
        = Stream.concat(ARG_COMPARITORS.stream(), 
                        Stream.of((a1, a2) -> a2 instanceof PositionalArgument 
                                      ? Integer.compare(((PositionalArgument) a1).position, ((PositionalArgument) a2).position)
                                      : -1))
                    .collect(Collectors.toList());
    
    // Comparators with added named arg grouping.
    private static final List<Comparator<AppArgument>> NAMED_COMPARITORS 
        = Stream.concat(ARG_COMPARITORS.stream(), 
                        Stream.of((a1, a2) -> a2 instanceof PositionalArgument 
                                      ? 1
                                      : ((NamedArgument) a1).argName.compareTo(((NamedArgument) a2).argName)))
                    .collect(Collectors.toList());
    
    private static class AppCommand {
        
        private final String name;
        private volatile String appClass;
        private volatile String appJars;
        private volatile String successValue;
        private volatile String failureValue;
        private volatile String args;
        private Map<PositionalArgument, String> positionalArgs = new ConcurrentHashMap<>();
        private Map<NamedArgument, String> namedArgs = new ConcurrentHashMap<>();
        private volatile Relationship successeRelationship;
        private volatile Relationship failureRelationship;
        
        public AppCommand(String appName) {
            this.name = appName;
        }
        
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(this.name);
            if (this.appClass != null) builder.append(" class: ").append(this.appClass);
            if (this.appJars != null) builder.append(" jars: ").append(this.appJars);
            this.namedArgs.forEach((arg, val) -> builder.append(" ").append(arg.getPropertyName()).append("=").append(val));
            this.positionalArgs.forEach((arg, val) -> builder.append(" ").append(arg.getPropertyName()).append("=").append(val));
            return builder.toString();
        }
    }
    
    private static class AppArgument implements Comparable<AppArgument> {
        protected String appName;
        protected String argType;
        
        public AppArgument(String appName, String type) {
            this.appName = appName;
            this.argType = type;
        }
        
        public String getPropertyName() {
            return this.appName + "." + this.argType;
        }

        @Override
        public int compareTo(AppArgument arg) {
            if (arg instanceof NamedArgument) {
                return -1;
            } else {
                return comparatorStream()
                    .map(comp -> comp.compare(this, arg))
                    .filter(i -> i != 0)
                    .findFirst()
                    .orElse(0);
            }
        }
        
        @Override
        public boolean equals(Object obj) {
            if (getClass().isInstance(obj)) {
                AppArgument that = (AppArgument) obj;
                return Objects.equal(this.appName, that.appName) && Objects.equal(this.argType, that.argType);
            } else {
                return false;
            }
        }
        
        @Override
        public int hashCode() {
            return Objects.hashCode(getClass(), this.appName, this.argType);
        }
        
        @Override
        public String toString() {
            return getPropertyName();
        }
        
        protected Stream<Comparator<AppArgument>> comparatorStream() {
            return ARG_COMPARITORS.stream();
        }
    }
    
    private static class PositionalArgument extends AppArgument {

        private int position;
        
        public PositionalArgument(String appName, int pos) {
            super(appName, "arg");
            this.position = pos;
        }
        
        public String getPropertyName() {
            return super.getPropertyName() + "." + this.position;
        }
        
        @Override
        public boolean equals(Object obj) {
            return super.equals(obj) && Objects.equal(this.position, ((PositionalArgument) obj).position);
        }
        
        @Override
        public int hashCode() {
            return super.hashCode() ^ Integer.hashCode(this.position);
        }
        
        protected Stream<Comparator<AppArgument>> comparatorStream() {
            return POSISITIONAL_COMPARITORS.stream();
        }
    }
    
    private static class NamedArgument extends AppArgument implements Comparable<AppArgument> {
        
        private String argName;
        
        public NamedArgument(String appName, String argName) {
            super(appName, "arg");
            this.argName = argName;
        }
        
        public String getPropertyName() {
            return super.getPropertyName() + "." + this.argName;
        }
        
        @Override
        public boolean equals(Object obj) {
            return super.equals(obj) && Objects.equal(this.argName, ((NamedArgument) obj).argName);
        }
        
        @Override
        public int hashCode() {
            return super.hashCode() ^ this.argName.hashCode();
        }

        protected Stream<Comparator<AppArgument>> comparatorStream() {
            return NAMED_COMPARITORS.stream();
        }
    }
    
    private static class RelationshipArgument extends AppArgument implements Comparable<AppArgument> {
        
        private boolean successful;
        private transient Relationship relationship;
        
        public RelationshipArgument(String appName, boolean success) {
            this(appName, success, null);
        }
        
        public RelationshipArgument(String appName, boolean success, Relationship relationship) {
            super(appName, success ? "success" : "failure");
            this.successful = success;
            this.relationship = relationship;
        }
        
        @Override
        public boolean equals(Object obj) {
            return super.equals(obj) && Objects.equal(this.successful, ((RelationshipArgument) obj).successful);
        }
        
        @Override
        public int hashCode() {
            return super.hashCode() ^ Boolean.hashCode(this.successful);
        }
    }
}
