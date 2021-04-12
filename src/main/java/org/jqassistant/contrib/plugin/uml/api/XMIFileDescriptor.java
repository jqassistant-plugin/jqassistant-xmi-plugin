package org.jqassistant.contrib.plugin.uml.api;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Describes a XMI file.
 */
public interface XMIFileDescriptor extends FileDescriptor, XMIDescriptor {

    /**
     * Return the documentation.
     *
     * @return The documentation.
     */
    @Relation("CONTAINS_DOCUMENTATION")
    XMIDocumentationDescriptor getDocumentation();

    /**
     * Set the documentation.
     *
     * @param documentationDescriptor The documentation.
     */
    void setDocumentation(XMIDocumentationDescriptor documentationDescriptor);

    /**
     * Return the contained UML model.
     *
     * @return The UML model.
     */
    @Relation("CONTAINS_MODEL")
    UMLModelDescriptor getModel();

    /**
     * Set the contained UML model.
     *
     * @param umlModel The UML model.
     */
    void setModel(UMLModelDescriptor umlModel);
}
