package org.jqassistant.contrib.plugin.uml.api;

import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Describes an owned element.
 */
public interface UMLOwnedElementTemplate {

    /**
     * Return the aggregation, e.g. "composite".
     * @return The aggregation.
     */
    String getAggregation();

    /**
     * Set the aggregation.
     * @param aggregation The aggregation.
     */
    void setAggregation(String aggregation);

    /**
     * Return the association referenced by this owned element.
     * @return The association.
     */
    @Relation("FOR_ASSOCIATION")
    UMLElementDescriptor getAssociation();

    /**
     * Set the association referenced by this owned element.
     * @param association The association.
     */
    void setAssociation(UMLElementDescriptor association);

    /**
     * Return the type of this owned element.
     * @return The type.
     */
    @Relation("OF_TYPE")
    UMLElementDescriptor getOfType();

    /**
     * Set the type of this owned element.
     * @param ofType The type.
     */
    void setOfType(UMLElementDescriptor ofType);
}
