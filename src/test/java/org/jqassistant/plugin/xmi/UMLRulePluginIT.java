package org.jqassistant.plugin.xmi;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contains test verifying the rules in the xmi:Default group.
 */
class UMLRulePluginIT extends AbstractUMLPluginIT {

    @Test
    void umlModelNode() throws RuleException {
        Result<Concept> conceptResult = applyConcept("uml:ModelNode");
        store.beginTransaction();
        assertThat(conceptResult.getStatus()).isEqualTo(Result.Status.SUCCESS);
        List<Descriptor> xmiFiles = query("MATCH (xmiFile:XMI:Root)-[:CONTAINS_UML_MODEL]->(m:UML:Element:Model{name: 'EA_Model'}) RETURN m").getColumn("m");
        assertThat(xmiFiles).hasSize(1);
        store.commitTransaction();
    }

    // already migrated, wait for jQA update to support apoc plugin properly
    //@Test
    //void umlPackageHierarchy() throws RuleException {
    //    Result<Concept> conceptResult = applyConcept("uml:ResolveHierarchy");
    //   store.beginTransaction();
    //    assertThat(conceptResult.getStatus()).isEqualTo(Result.Status.SUCCESS);
    //    List<List<String>> hierarchies = query("MATCH " +
    //            "(:XMI:Root)-[:CONTAINS_UML_MODEL]->(model:UML:Model)-[:HAS_CHILD]->(root:UML:PackageableElement), " + // identify root package
    //            "hierarchy=((root)-[:HAS_CHILD*0..]->(child:UML:PackageableElement:Package)) " + // identify all hierarchies starting from the root package & limit results to uml:Package elements
    //            "WITH nodes(hierarchy) as hierarchy " +
    //            "RETURN [n in hierarchy | n.name] as names").getColumn("names");
    //    assertThat(hierarchies).containsExactlyInAnyOrder(
    //            asList("Model"),
    //            asList("Model", "Components"),
    //            asList("Model", "Components", "Aggregations & Dependencies"),
    //            asList("Model", "Components", "Ports & Interfaces"),
    //            asList("Model", "Components", "Information Flow"));
    //    store.commitTransaction();
    //}

}
