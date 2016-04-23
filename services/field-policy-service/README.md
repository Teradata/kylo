Field Policies
==========

### Overview
Design Custom Standardization and Validation Rules that can be applied to a Table Field.
Standardization and Validation rules are written as simple Java POJO along with custom Annotations that allow the system to Convert Rules to/from the UI.

### Annotations
#### @PolicyProperty
**Purpose:** Annotate any Parameter that is needed to be captured the User Interface in order to create the Standardization or Validation Policy.

| Attribute              | Required  | Default Value | Description      | 
| -----------------      | --------- | -------------    |------------|
| name                   | Y         |                  | Unique Name for the User Inerface and Domain to match. <br/> **NOTE** This will also become the User Interface Display Name, (**displayName attribute**) if one is not specified |
| displayName            | N         | name value above | The display name on the UI |
| value                  | N         |                  | default value on the UI |
| placeholder            | N         |                  | Html Placeholder attribute |
| type                   | N         |  string          | Render Types. Available Options (number, string, select, regex, date) |
| selectableValues       | N         |                  | If type is **select** this is the Array of Strings that will be in the list<br/> Optionally use the property below for a label/value render |
| labelValues            | N         |                  | If type is **select** this is the Array of Label/Values that will be in the list |


#### @PolicyPropertyRef
**Purpose:** Annotate any Constructor Parameter that references a given 
**@PolicyProperty**

| Attribute     | Required |  Description |
| ----------    | -------- | ----------   |
| name          | Y        | This name should match the @PolicyProperty name |


#### @Standardizer
**Purpose:** Annotate the Class to inform the system and the User Interface that this is a Standardizer

| Attribute     | Required |  Description |
| ----------    | -------- | ----------   |
| name          | Y        | This name of the Standardizer.  This will be displayed on the User Interface |
| description   | N        | Short Description of the Standardizer. This will be displayed on the User Interface  |

#### @FieldValidator
**Purpose:** Annotate the Class to inform the system and the User Interface that this is a Validator

| Attribute     | Required |  Description |
| ----------    | -------- | ----------   |
| name          | Y        | This name of the Validator.  This will be displayed on the User Interface |
| description   | N        | Short Description of the Validator. This will be displayed on the User Interface  |


How To
=======

### Standardization

1. Create a new Java Class that implements the **StandardizationPolicy** class
2. Augment your Class with the **@Standardizer** annotation

```java
@Standardizer(name = "Default Value", description = "Applies a default value if null")
public class DefaultValueStandardizer implements StandardizationPolicy, AcceptsEmptyValues {

 ...

}
```

3. If fields are needed to be captured by the User interface annotate the fields with the **@PolicyProperty** annotation
    
```java
@Standardizer(name = "Default Value", description = "Applies a default value if null")
public class DefaultValueStandardizer implements StandardizationPolicy, AcceptsEmptyValues {

 @PolicyProperty(name = "Default Value", hint = "If the value is null it will use this supplied value")
  private String defaultStr;
  
  ...
  }
```

4.  Either supply a no arg Constructor, or supply a Constructor with the **@PolicyPropertyRef** indicating which property the parameter refers to. <br>**Note:** The Name attribute of *@PolicyPropertyRef* matches on the name attribute of *@PolicyProperty*

     ```java
            @Standardizer(name = "Default Value", description = "Applies a default value if null")
            public class DefaultValueStandardizer implements StandardizationPolicy, AcceptsEmptyValues {
            
              @PolicyProperty(name = "Default Value", hint = "If the value is null it will use this supplied value")
              private String defaultStr;
            
            
              public DefaultValueStandardizer(@PolicyPropertyRef(name = "Default Value") String defaultStr) {
                this.defaultStr = defaultStr;
              }
     ```

**Note:** If your StandardizationPolicy does not require inputs you can make it a Singelton class and the System will use the singelton if you implement a static method **instance**

     ```java
            @Standardizer(name = "Strip Non Numeric", description = "Remove any characters that are not numeric")
            public class StripNonNumeric extends SimpleRegexReplacer {
            
              private static final StripNonNumeric instance = new StripNonNumeric();
            
              public static StripNonNumeric instance() {
                return instance;
              }
            
              private StripNonNumeric() {
                super("[^\\d.]", "");
              }
            
              @Override
              public String convertValue(String value) {
                return super.convertValue(value);
              }
            
              
            }
     ```

Notice above the default constructor is private, but there is a static **instance** method. 


### Validation



### Default Implementations



