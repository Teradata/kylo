package com.thinkbiganalytics.policies;

import com.thinkbiganalytics.annotations.AnnotatedFieldProperty;
import com.thinkbiganalytics.annotations.AnnotationFieldNameResolver;
import com.thinkbiganalytics.feedmgr.rest.model.LabelValue;
import com.thinkbiganalytics.feedmgr.rest.model.schema.BaseUiPolicyRule;
import com.thinkbiganalytics.feedmgr.rest.model.schema.FieldRuleProperty;
import com.thinkbiganalytics.feedmgr.rest.model.schema.FieldRulePropertyBuilder;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by sr186054 on 4/21/16.
 */
public abstract class BasePolicyAnnotationTransformer<U extends BaseUiPolicyRule, P extends FieldPolicyItem, A extends Annotation>
    implements PolicyTransformer<U, P, A> {


  private List<FieldRuleProperty> getUiProperties(P policy) {
    AnnotationFieldNameResolver annotationFieldNameResolver = new AnnotationFieldNameResolver(PolicyProperty.class);
    List<AnnotatedFieldProperty> list = annotationFieldNameResolver.getProperties(policy.getClass());
    List<FieldRuleProperty> properties = new ArrayList<>();
    if (hasConstructor(policy.getClass())) {

      for (AnnotatedFieldProperty<PolicyProperty> annotatedFieldProperty : list) {
        PolicyProperty prop = annotatedFieldProperty.getAnnotation();
        String value = null;
        try {
          Object fieldValue = FieldUtils.readField(annotatedFieldProperty.getField(), policy, true);
          if (fieldValue != null) {
            value = fieldValue.toString();
          }
        } catch (IllegalAccessException e) {

        }
        FieldRuleProperty rule = new FieldRulePropertyBuilder(prop.name()).displayName(
            StringUtils.isNotBlank(prop.displayName()) ? prop.displayName() : prop.name()).hint(prop.hint())
            .type(FieldRulePropertyBuilder.PROPERTY_TYPE.valueOf(prop.type().name())).value(prop.value())
            .objectProperty(annotatedFieldProperty.getName())
            .value(value)
            .addSelectableValues(convertToLabelValue(prop.selectableValues())).build();
        properties.add(rule);
      }
    }
    return properties;
  }

  public abstract U buildUiModel(A annotation, P policy, List<FieldRuleProperty> properties);

  public abstract Class<A> getAnnotationClass();

  @Override
  public U toUIModel(P standardizationPolicy) {
    Annotation annotation = standardizationPolicy.getClass().getAnnotation(getAnnotationClass());
    List<FieldRuleProperty> properties = getUiProperties(standardizationPolicy);
    U rule = buildUiModel((A) annotation, standardizationPolicy, properties);
    return rule;
  }

  @Override
  public P fromUiModel(U rule)
      throws PolicyTransformException {
    try {
      P standardizationPolicy = createClass(rule);

      if (hasConstructor(standardizationPolicy.getClass())) {

        for (FieldRuleProperty property : rule.getProperties()) {
          String field = property.getObjectProperty();
          String value = property.getValue();
          Field f = FieldUtils.getField(standardizationPolicy.getClass(), field, true);
          Object objectValue = convertStringToObject(value, f.getType());
          BeanUtils.setProperty(standardizationPolicy, field, objectValue);
        }
      }
      return standardizationPolicy;
    } catch (Exception e) {
      throw new PolicyTransformException(e);
    }
  }

  private Object getPropertyValue(BaseUiPolicyRule rule, Class<P> standardizationPolicyClass, PolicyPropertyRef reference) {

    for (FieldRuleProperty property : rule.getProperties()) {
      String name = property.getName();
      if (name.equalsIgnoreCase(reference.name())) {
        String field = property.getObjectProperty();
        String value = property.getValue();
        Field f = FieldUtils.getField(standardizationPolicyClass, field, true);
        Object objectValue = convertStringToObject(value, f.getType());
        return objectValue;
      }
    }
    return null;
  }


  private boolean hasConstructor(Class<?> policyClass) {
    return policyClass.getConstructors().length > 0;
  }

  private P createClass(BaseUiPolicyRule rule)
      throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException,
             IllegalAccessException {
    P standardizationPolicy = null;
    String classType = rule.getObjectClassType();
    Class<P> standardizationPolicyClass = (Class<P>) Class.forName(classType);

    Constructor constructor = null;
    Object[] paramValues = null;
    boolean hasConstructor = false;
    for (Constructor con : standardizationPolicyClass.getConstructors()) {
      hasConstructor = true;
      int parameterSize = con.getParameterTypes().length;
      paramValues = new Object[parameterSize];
      for (int p = 0; p < parameterSize; p++) {
        Type pgtype = con.getGenericParameterTypes()[p];
        Annotation[] annotations = con.getParameterAnnotations()[p];
        Object paramValue = null;
        for (Annotation a : annotations) {
          if (a instanceof PolicyPropertyRef) {
            // this is the one we want
            if (constructor == null) {
              constructor = con;
            }
            //find the value associated to this property
            paramValue = getPropertyValue(rule, standardizationPolicyClass, (PolicyPropertyRef) a);

          }
        }
        paramValues[p] = paramValue;
      }

    }

    if (constructor != null) {
      //call that constructor
      standardizationPolicy = ConstructorUtils.invokeConstructor(standardizationPolicyClass, paramValues);
    } else {
      //if the class has no public constructor then attempt to call the static instance method
      if (!hasConstructor) {
        //if the class has a static "instance" method on it then call that
        try {
          standardizationPolicy = (P) MethodUtils.invokeStaticMethod(standardizationPolicyClass, "instance", null);
        } catch (NoSuchMethodException | SecurityException | InvocationTargetException e) {
          standardizationPolicy = standardizationPolicyClass.newInstance();
        }
      } else {
        //attempt to create a new instance
        standardizationPolicy = standardizationPolicyClass.newInstance();
      }
    }

    return standardizationPolicy;

  }


  private List<LabelValue> convertToLabelValue(String[] values) {
    if (values != null) {
      List<LabelValue> list = new ArrayList<>();
      for (String value : values) {
        LabelValue labelValue = new LabelValue();
        labelValue.setLabel(value);
        labelValue.setValue(value);
        list.add(labelValue);
      }
      return list;
    }
    return null;
  }


  private LabelValue convertToLabelValue(PropertyLabelValue propertyLabelValue) {
    if (propertyLabelValue != null) {
      LabelValue labelValue = new LabelValue();
      labelValue.setLabel(propertyLabelValue.label());
      labelValue.setValue(propertyLabelValue.value());
      return labelValue;
    }
    return null;
  }

  private List<LabelValue> convertToLabelValue(PropertyLabelValue[] propertyLabelValues) {
    List<LabelValue> labelValues = null;
    if (propertyLabelValues != null) {
      for (PropertyLabelValue propertyLabelValue : propertyLabelValues) {
        if (labelValues == null) {
          labelValues = new ArrayList<>();
        }
        LabelValue labelValue = convertToLabelValue((propertyLabelValue));
        if (labelValue != null) {
          labelValues.add(labelValue);
        }
      }
    }
    return labelValues;
  }


  public Object convertStringToObject(String value, Class type) {
    if (type.isEnum()) {
      return Enum.valueOf(type, value);
    } else if (StringUtils.isBlank(value)) {
      return null;
    }
    if (String.class.equals(type)) {
      return value;
    } else if (Number.class.equals(type)) {
      return NumberUtils.createNumber(value);
    } else if (Integer.class.equals(type)) {
      return new Integer(value);
    } else if (Long.class.equals(type)) {
      return Long.valueOf(value);
    } else if (Double.class.equals(type)) {
      return Double.valueOf(value);
    } else if (Float.class.equals(type)) {
      return Float.valueOf(value);
    } else if (Pattern.class.equals(type)) {
      return Pattern.compile(value);
    } else {
      throw new IllegalArgumentException("Unable to convert the value " + value + " to an object of type " + type.getName());
    }

  }

}
