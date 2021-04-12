package org.jqassistant.contrib.plugin.uml.api;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
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
public interface UMLElementDescriptor extends UMLDescriptor, NamedDescriptor {

    /**
     * Return the unique id of the element.
     *
     * @return The id.
     */
    @Indexed
    String getId();

    /**
     * Set the unique id of the element.
     *
     * @param id The id.
     */
    void setId(String id);

    /**
     * Return the type of the element, e.g. "uml:Component".
     *
     * @return The type.
     */
    String getType();

    /**
     * Set the type of the element.
     *
     * @param type The type.
     */
    void setType(String type);

    /**
     * Return the child elements of this element.
     *
     * @return The child elements.
     */
    @Outgoing
    @Relation("HAS_CHILD")
    List<UMLElementDescriptor> getChildren();

    /**
     * Return the parent element of this element.
     * @return The parent element.
     */
    @Incoming
    @Relation("HAS_CHILD")
    UMLElementDescriptor getParent();

    /**
     * Set the parent element.
     * @param parent The parent element.
     */
    void setParent(UMLElementDescriptor parent);
}
