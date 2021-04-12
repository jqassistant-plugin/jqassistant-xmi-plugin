package org.jqassistant.contrib.plugin.uml.api;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Describes a packaged element.
 */
@Label("PackagedElement")
public interface UMLPackagedElementDescriptor extends UMLDescriptor, UMLElementDescriptor {

    /**
     * Return the visibility, e.g. "public".
     *
     * @return The visibility.
     */
    String getVisibility();

    /**
     * Set the visibility.
     *
     * @param visibility The visibility.
     */
    void setVisibility(String visibility);

    /**
     * Return the supplier for this element, e.g. for "uml:Association".
     *
     * @return The supplier.
     */
    @Relation("HAS_SUPPLIER")
    UMLElementDescriptor getSupplier();

    /**
     * Set the supplier for this element.
     *
     * @return The supplier.
     */
    void setSupplier(UMLElementDescriptor supplier);

    /**
     * Return the client of this element, e.g. for "uml:Association".
     *
     * @return The client.
     */
    @Relation("HAS_CLIENT")
    UMLElementDescriptor getClient();

    /**
     * Set the client of this element.
     *
     * @param client The client.
     */
    void setClient(UMLElementDescriptor client);

}
