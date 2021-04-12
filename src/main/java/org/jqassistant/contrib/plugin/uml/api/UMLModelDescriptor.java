package org.jqassistant.contrib.plugin.uml.api;

import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.Cypher;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Describes a UML model.
 */
@Label("Model")
public interface UMLModelDescriptor extends UMLDescriptor, UMLElementDescriptor {

    /**
     * Resolve a UML element within the model identified by an id.
     *
     * @param id The id.
     * @return The resolved UML element.
     */
    @ResultOf
    @Cypher("MATCH (model:XMI:UML:Model) WHERE id(model)=$this MERGE (model)-[:DECLARES_ELEMENT]->(element:XMI:UML:Element{id:$id}) RETURN element")
    UMLElementDescriptor resolveElement(@Parameter("id") String id);

}
