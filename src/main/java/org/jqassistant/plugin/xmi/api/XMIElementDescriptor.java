package org.jqassistant.plugin.xmi.api;

import com.buschmais.xo.neo4j.api.annotation.Indexed;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import java.util.List;

/**
 * Describes an UML element within an XMI file.
 */
@Label("Element")
public interface XMIElementDescriptor extends XMIDescriptor {

    /**
     * Return the unique XMI id of the element.
     *
     * @return The XMI id.
     */
    @Indexed
    String getXmiId();

    /**
     * Set the unique XMI id of the element.
     *
     * @param xmiId The XMI id.
     */
    void setXmiId(String xmiId);

    /**
     * Return the XMI type of the element, e.g. "uml:Component".
     *
     * @return The XMI type.
     */
    String getXmiType();

    /**
     * Set the XMI type of the element.
     *
     * @param type The XMI type.
     */
    void setXmiType(String type);
    
    /**
     * Return the child elements of this element.
     *
     * @return The child elements.
     */
    @Outgoing
    @Relation("HAS_CHILD")
    List<XMIElementDescriptor> getChildren();

    /**
     * Return the parent element of this element.
     *
     * @return The parent element.
     */
    @Incoming
    @Relation("HAS_CHILD")
    XMIElementDescriptor getParent();

    /**
     * Set the parent element.
     *
     * @param parent The parent element.
     */
    void setParent(XMIElementDescriptor parent);
}
