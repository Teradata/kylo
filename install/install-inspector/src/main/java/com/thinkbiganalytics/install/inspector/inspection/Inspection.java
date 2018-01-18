package com.thinkbiganalytics.install.inspector.inspection;


/**
 * Kylo configuration inspection, which is executed upon user requests.
 * It should provide two types which will be autowired with kylo-services and kylo-ui properties, e.g.
 * <pre>
 * class ServicesProperties {
 *    {@literal @}Value("${property-name}")
 *     private String property;
 * }
 * </pre>
 *
 * At the moment these types can only be autowired with <code>String</code> values.
 *
 * @param <SP> type which defines Kylo-Services properties
 * @param <UP> type which defines Kylo-UI properties
 */
public interface Inspection<SP, UP> {

    /**
     * @return unique id
     */
    int getId();

    void setId(int id);

    /**
     * @return human readable name
     */
    String getName();

    /**
     * @return human readable description
     */
    String getDescription();

    /**
     * This method is executed to inspect provided kylo-services and kylo-ui properties
     * @param servicesProperties kylo-services properties
     * @param uiProperties kylo-ui properties
     * @return inspection status
     */
    InspectionStatus inspect(SP servicesProperties, UP uiProperties);

    /**
     * @return a new blank instance of kylo-services properties which are of interest to this configuration
     */
    SP getServicesProperties();

    /**
     * @return a new blank instance of kylo-ui properties which are of interest to this configuration
     */
    UP getUiProperties();
}
