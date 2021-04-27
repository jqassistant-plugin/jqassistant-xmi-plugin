package org.jqassistant.contrib.plugin.uml.api;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.Cypher;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

/**
 * Describes a XMI file.
 */
public interface XMIFileDescriptor extends XMIDescriptor, FileDescriptor {

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
    @Relation("CONTAINS_UML_MODEL")
    UMLModelDescriptor getModel();

    /**
     * Set the contained UML model.
     *
     * @param umlModel The UML model.
     */
    void setModel(UMLModelDescriptor umlModel);

    /**
     * Return the {@link XMIStereotypeDescriptor}s.
     *
     * @return The {@link XMIStereotypeDescriptor}.
     */
    @Relation("CONTAINS_STEREOTYPE")
    List<XMIStereotypeDescriptor> getStereotypes();

    /**
     * Resolve a {@link XMIElementDescriptor} identified by an id.
     *
     * @param xmiId The XMI id.
     * @return The resolved {@link XMIElementDescriptor}.
     */
    @ResultOf
    @Cypher("MATCH (xmiFile:XMI:File) WHERE id(xmiFile)=$this MERGE (xmiFile)-[:CONTAINS_XMI_ELEMENT]->(element:XMI:Element{xmiId:$xmiId}) RETURN element")
    XMIElementDescriptor resolveElement(@Parameter("xmiId") String xmiId);


    /**
     * Resolve a {@link XMIStereotypeDescriptor} identified by the XML namespace URI and element name.
     *
     * @param namespaceUri    The namespace URI.
     * @param name            The element name.
     * @param namespacePrefix The namespace prefix as used in the XMI document.
     * @return The resolved {@link XMIStereotypeDescriptor}.
     */
    @ResultOf
    @Cypher("MATCH (xmiFile:XMI:File) WHERE id(xmiFile)=$this MERGE (xmiFile)-[:CONTAINS_STEREOTYPE]->(stereotype:XMI:Stereotype{namespaceUri:$namespaceUri,name:$name}) ON CREATE SET stereotype.namespacePrefix=$namespacePrefix RETURN stereotype")
    XMIStereotypeDescriptor resolveStereotype(@Parameter("namespaceUri") String namespaceUri, @Parameter("name") String name, @Parameter("namespacePrefix") String namespacePrefix);
}
