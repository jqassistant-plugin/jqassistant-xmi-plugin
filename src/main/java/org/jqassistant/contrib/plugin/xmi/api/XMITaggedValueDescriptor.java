package org.jqassistant.contrib.plugin.xmi.api;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("TaggedValue")
public interface XMITaggedValueDescriptor extends XMIDescriptor, NamedDescriptor {

    /**
     * Return the attribute value.
     *
     * @return The attribute value.
     */
    String getValue();

    /**
     * Set the attribute value.
     *
     * @param value The attribute value.
     */
    void setValue(String value);

}
