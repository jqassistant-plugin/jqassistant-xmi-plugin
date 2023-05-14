package org.jqassistant.plugin.xmi.api;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Represents a Stereotype.
 */
@Label("Stereotype")
public interface XMIStereotypeDescriptor extends XMIDescriptor, NamedDescriptor {

    /**
     * Return the namespace URI as used in the XMI document.
     *
     * @return The namespace URI.
     */
    String getNamespaceUri();

    /**
     * Return the namespace prefix as used in the XMI document.
     *
     * @return The namespace prefix.
     */
    String getNamespacePrefix();

}
