package org.jqassistant.plugin.xmi;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlElementDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlFileDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
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

    //@Test TODO plugin it currently does not support .jqassistant.yml configuration (needed for apoc)
    void umlPackageHierarchy() throws RuleException {
        Result<Concept> conceptResult = applyConcept("uml:ResolveHierarchy");
        store.beginTransaction();
        assertThat(conceptResult.getStatus()).isEqualTo(Result.Status.SUCCESS);
        List<List<NamedDescriptor>> hierarchies = query("MATCH " +
                "(:XMI:File)-[:CONTAINS_UML_MODEL]->(model:UML:Model)-[:HAS_CHILD]->(root:UML:PackageableElement), " + // identify root package
                "hierarchy=((root)-[:HAS_CHILD*0..]->(child:UML:PackageableElement:Package)) " + // identify all hierarchies starting from the root package & limit results to uml:Package elements
                "RETURN nodes(hierarchy) as hierarchy ").getColumn("hierarchy");
        assertThat(hierarchies.stream().map(this::toNames).collect(toList())).containsExactlyInAnyOrder(
                asList("Model"),
                asList("Model", "Components"),
                asList("Model", "Components", "Aggregations & Dependencies"),
                asList("Model", "Components", "Ports & Interfaces"),
                asList("Model", "Components", "Information Flow"));
        store.commitTransaction();
    }

}
