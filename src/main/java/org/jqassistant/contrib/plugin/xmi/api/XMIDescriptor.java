package org.jqassistant.contrib.plugin.xmi.api;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Describes a XMI element.
 */
@Abstract
@Label("XMI")
public interface XMIDescriptor extends Descriptor {
}
