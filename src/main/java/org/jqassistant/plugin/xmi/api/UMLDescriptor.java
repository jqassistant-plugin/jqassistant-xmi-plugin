package org.jqassistant.plugin.xmi.api;

import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Describes an UML element.
 */
@Abstract
@Label("UML")
public interface UMLDescriptor extends XMIDescriptor {
}
