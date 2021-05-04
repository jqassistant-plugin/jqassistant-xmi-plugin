package org.jqassistant.contrib.plugin.xmi.api;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Describes a packaged element.
 */
@Label("PackagedElement")
public interface UMLPackagedElementDescriptor extends UMLElementDescriptor {

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
    XMIElementDescriptor getSupplier();

    /**
     * Set the supplier for this element.
     *
     * @param supplier The supplier.
     */
    void setSupplier(XMIElementDescriptor supplier);

    /**
     * Return the client of this element, e.g. for "uml:Association".
     *
     * @return The client.
     */
    @Relation("HAS_CLIENT")
    XMIElementDescriptor getClient();

    /**
     * Set the client of this element.
     *
     * @param client The client.
     */
    void setClient(XMIElementDescriptor client);

    /**
     * Return the information source of this element, i.e. for "uml:InformationFlow".
     *
     * @return The information source.
     */
    @Relation("HAS_INFORMATION_SOURCE")
    XMIElementDescriptor getInformationSource();

    /**
     * Set the information source of this element.
     *
     * @param informationSource The information source.
     */
    void setInformationSource(XMIElementDescriptor informationSource);

    /**
     * Return the information target of this element, i.e. for "uml:InformationFlow".
     *
     * @return The information target.
     */
    @Relation("HAS_INFORMATION_TARGET")
    XMIElementDescriptor getInformationTarget();

    /**
     * Set the information target of this element.
     *
     * @param informationTarget The information target.
     */
    void setInformationTarget(XMIElementDescriptor informationTarget);
}
