package org.jqassistant.contrib.plugin.uml.api;

import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Represents an applied UML profile.
 */
@Label("AppliedProfile")
public interface UMLAppliedProfileDescriptor extends UMLDescriptor, XMITypeTemplate {

    /**
     * Return the HREF of the applied profile.
     *
     * @return The HREF.
     */
    String getHref();

    /**
     * Set the HREF of the applied profile.
     *
     * @param href The HREF.
     */
    void setHref(String href);

}
