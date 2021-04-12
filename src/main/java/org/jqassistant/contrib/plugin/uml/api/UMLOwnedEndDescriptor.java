package org.jqassistant.contrib.plugin.uml.api;

import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Describes an owned end.
 */
@Label("OwnedEnd")
public interface UMLOwnedEndDescriptor extends UMLElementDescriptor, UMLOwnedElementTemplate {
}
