/**
 * 
 */
package com.thinkbiganalytics.nifi.v2.spark;

import com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.annotation.behavior.DynamicProperty;
import org.apache.nifi.annotation.behavior.DynamicRelationship;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.PropertyDescriptor.Builder;
import org.apache.nifi.components.ValidationContext;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.components.Validator;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
@CapabilityDescription("Execute one or more Spark jobs sharing a single context.")
@DynamicProperty(name = "Spark app-specific property", value = "Spark job argument", supportsExpressionLanguage = true, description = "Specifies Spark application arguments in the form 'app.class', app.arg.<n>, app.arg.<name>,...")
@DynamicRelationship(name = "Name from Dynamic Property", description = "FlowFiles that match the Dynamic Property's Attribute Expression Language")
public class ExecuteMultiSparkJobs extends AbstractNiFiProcessor {
    
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

    
    private static final List<PropertyDescriptor> STATIC_PROP_DESCR = Collections.unmodifiableList(Arrays.asList(
                     ExecuteSparkJob.DRIVER_MEMORY,
                     ExecuteSparkJob.EXECUTOR_MEMORY,
                     ExecuteSparkJob.NUMBER_EXECUTORS,
                     ExecuteSparkJob.EXECUTOR_CORES,
                     ExecuteSparkJob.NETWORK_TIMEOUT,
                     ExecuteSparkJob.HADOOP_CONFIGURATION_RESOURCES,
                     ExecuteSparkJob.SPARK_CONFS,
                     ExecuteSparkJob.SPARK_CONFS,
                     ExecuteSparkJob.EXTRA_JARS,
                     ExecuteSparkJob.EXTRA_SPARK_FILES,
                     ExecuteSparkJob.PROCESS_TIMEOUT,
                     ExecuteSparkJob.METADATA_SERVICE,
                     ExecuteSparkJob.DATASOURCES));

    
    private List<PropertyDescriptor> propDescriptors;
    private PropertyDescriptor appNamesPropDescriptor;
    private final AtomicReference<Set<Relationship>> relationships = new AtomicReference<>(Collections.emptySet());
    private final AtomicReference<Set<String>> appNames = new AtomicReference<>(Collections.emptySet());
    private final Map<String, AppCommand> appCommands = new ConcurrentHashMap<>();

    /**
     * 
     */
    public ExecuteMultiSparkJobs() {
        final Set<Relationship> r = new HashSet<>();
        r.add(JOB_SUCCESS);
        r.add(JOB_FAILURE);
        relationships.set(Collections.unmodifiableSet(r));
    }
    
    @Override
    public Set<Relationship> getRelationships() {
        return relationships.get();
    }
    
    /* (non-Javadoc)
     * @see org.apache.nifi.processor.AbstractProcessor#onTrigger(org.apache.nifi.processor.ProcessContext, org.apache.nifi.processor.ProcessSession)
     */
    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }
        
        session.transfer(flowFile, ExecuteSparkJob.REL_SUCCESS);
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
            
            parseNamedArg(descriptor.getName()).ifPresent(arg -> {
                namedArgModified(arg, oldValue, newValue);
                return;
            });
            
            parseAppRelationshipArg(descriptor.getName()).ifPresent(arg -> {
                appRelationshipModified(arg, oldValue, newValue);
                return;
            });
            
            parsePositionalArg(descriptor.getName()).ifPresent(arg -> {
                positionalArgModified(arg, oldValue, newValue);
            });
        }
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor#init(org.apache.nifi.processor.ProcessorInitializationContext)
     */
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.nifi.processor.AbstractNiFiProcessor#init(org.apache.nifi.processor.ProcessorInitializationContext)
     */
    @Override
    protected void init(ProcessorInitializationContext context) {
        super.init(context);
        
        this.appNamesPropDescriptor = createAppNamesProperty();
        List<PropertyDescriptor> list = Stream.concat(Stream.of(this.appNamesPropDescriptor), 
                                                      STATIC_PROP_DESCR.stream())
                      .collect(Collectors.toList());
        this.propDescriptors = Collections.unmodifiableList(list);
    }

    /* (non-Javadoc)
     * @see org.apache.nifi.components.AbstractConfigurableComponent#getSupportedPropertyDescriptors()
     */
    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return this.propDescriptors;
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
            String trimmed = listValue.trim();
            
            parseAppNames(trimmed).stream()
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
            this.relationships.getAndUpdate(set -> {
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
            this.relationships.getAndUpdate(set -> {
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
    
    protected Optional<String> getMatched(String value, Pattern pattern, int groupIdx) {
        Matcher matcher = pattern.matcher(value);
        if (matcher.matches()) {
            return Optional.of(matcher.group(groupIdx));
        } else {
            return Optional.empty();
        }
    }

    private List<String> parseAppNames(String listValue) {
        return Arrays.stream(listValue.split("\\s*,\\s*")).collect(Collectors.toList());
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
        private volatile Relationship successeRelationship;
        private volatile Relationship failureRelationship;
        private Map<PositionalArgument, String> positionalArgs = new ConcurrentHashMap<>();
        private Map<NamedArgument, String> namedArgs = new ConcurrentHashMap<>();
        
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
