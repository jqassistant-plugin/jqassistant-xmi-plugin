package org.jqassistant.contrib.plugin.xmi.api;

import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Describes an UML interface.
 */
@Abstract
@Label("Interface")
public interface UMLInterfaceDescriptor extends UMLElementDescriptor {
}