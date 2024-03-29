package org.jqassistant.plugin.xmi.api;

import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Describes an owned attribute.
 */
@Label("OwnedAttribute")
public interface UMLOwnedAttributeDescriptor extends UMLElementDescriptor, UMLOwnedElementTemplate {
}
