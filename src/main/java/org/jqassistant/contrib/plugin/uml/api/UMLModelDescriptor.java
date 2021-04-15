package org.jqassistant.contrib.plugin.uml.api;

import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.Cypher;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

/**
 * Describes a UML model.
 */
@Label("Model")
public interface UMLModelDescriptor extends UMLElementDescriptor {

    @Relation("CONTAINS_XMI_ELEMENT")
    List<UMLElementDescriptor> getUMLElements();

    /**
     * Resolve a UML element within the model identified by an id.
     *
     * @param xmiId The XMI id.
     * @return The resolved UML element.
     */
    @ResultOf
    @Cypher("MATCH (model:XMI:UML:Model) WHERE id(model)=$this MERGE (model)-[:CONTAINS_XMI_ELEMENT]->(element:XMI:UML:Element{xmiId:$xmiId}) RETURN element")
    UMLElementDescriptor resolveElement(@Parameter("xmiId") String xmiId);

}
