package org.jqassistant.contrib.plugin.uml;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import org.jqassistant.contrib.plugin.uml.api.UMLOwnedAttributeDescriptor;
import org.jqassistant.contrib.plugin.uml.api.UMLPackagedElementDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contains tests verifying the rules provided by the plugin.
 */
class UMLRulesPluginIT extends AbstractUMLPluginIT {

    /**
     * Verifies the concept "xmi:UMLPackage".
     *
     * @throws RuleException If the concept cannot be applied.
     */
    @Test
    void umlPackage() throws RuleException {
        Result<Concept> result = applyConcept("xmi:UMLPackage");
        store.beginTransaction();
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("Packages")).isEqualTo(5L);
        List<UMLPackagedElementDescriptor> packages = query("MATCH (package:UML:Package:PackagedElement) RETURN package").getColumn("package");
        assertThat(toNames(packages)).containsExactlyInAnyOrder("Model", "Components", "Aggregations & Dependencies", "Ports & Interfaces", "Information Flow");
        store.commitTransaction();
    }

    /**
     * Verifies the concept "xmi:UMLComponent".
     *
     * @throws RuleException If the concept cannot be applied.
     */
    @Test
    void umlComponent() throws RuleException {
        Result<Concept> result = applyConcept("xmi:UMLComponent");
        store.beginTransaction();
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("Components")).isEqualTo(7L);
        List<UMLPackagedElementDescriptor> components = query("MATCH (component:UML:Component:PackagedElement) RETURN component").getColumn("component");
        assertThat(toNames(components)).containsExactlyInAnyOrder("Component A", "Component B", "Aggregate", "Dependent", "Dependency", "Information Source", "Information Target");
        store.commitTransaction();
    }

    /**
     * Verifies the concept "xmi:UMLProperty".
     *
     * @throws RuleException If the concept cannot be applied.
     */
    @Test
    void umlProperty() throws RuleException {
        Result<Concept> result = applyConcept("xmi:UMLProperty");
        store.beginTransaction();
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("Properties")).isEqualTo(4L);
        List<UMLPackagedElementDescriptor> componentsOwningAttributes = query("MATCH (component:PackagedElement)-[:HAS_CHILD]->(property:UML:Property:OwnedAttribute) RETURN component").getColumn("component");
        assertThat(toNames(componentsOwningAttributes)).containsExactlyInAnyOrder("Dependent", "Dependency");
        List<UMLPackagedElementDescriptor> componentsOwningEnds = query("MATCH (property:UML:Property:OwnedEnd)-[:OF_TYPE]->(component:PackagedElement) RETURN component").getColumn("component");
        assertThat(toNames(componentsOwningEnds)).containsExactlyInAnyOrder("Dependent", "Dependency");
        store.commitTransaction();
    }

    /**
     * Verifies the concept "xmi:UMLPort".
     *
     * @throws RuleException If the concept cannot be applied.
     */
    @Test
    void umlPort() throws RuleException {
        Result<Concept> result = applyConcept("xmi:UMLPort");
        store.beginTransaction();
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("Ports")).isEqualTo(2L);
        List<UMLOwnedAttributeDescriptor> ports = query("MATCH (port:UML:Port:OwnedAttribute) RETURN port").getColumn("port");
        assertThat(toNames(ports)).containsExactlyInAnyOrder("Port A", "Port B");
        store.commitTransaction();
    }

    /**
     * Verifies the concept "xmi:UMLAssociation".
     *
     * @throws RuleException If the concept cannot be applied.
     */
    @Test
    void umlAssociation() throws RuleException {
        Result<Concept> result = applyConcept("xmi:UMLAssociation");
        store.beginTransaction();
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("Associations")).isEqualTo(2L);
        List<UMLOwnedAttributeDescriptor> associations = query("MATCH (association:UML:Association:PackagedElement)-[:HAS_CHILD]->(:OwnedEnd)-[:OF_TYPE]->(component:PackagedElement) RETURN component").getColumn("component");
        assertThat(toNames(associations)).containsExactlyInAnyOrder("Dependent", "Dependency");
        store.commitTransaction();
    }

    /**
     * Verifies the concept "xmi:UMLDependency".
     *
     * @throws RuleException If the concept cannot be applied.
     */
    @Test
    void umlDependency() throws RuleException {
        Result<Concept> result = applyConcept("xmi:UMLDependency");
        store.beginTransaction();
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("Dependencies")).isEqualTo(1L);
        assertThat(query("MATCH (dependency:UML:Dependency:PackagedElement) RETURN dependency").getColumn("dependency")).hasSize(1);
        List<Map<String, Object>> dependencies = query("MATCH (dependent:UML:PackagedElement)-[:HAS_DEPENDENCY]->(dependency:UML:PackagedElement) RETURN dependent.name as dependentName, dependency.name as dependencyName").getRows();
        assertThat(dependencies).hasSize(1);
        assertThat(dependencies.get(0).get("dependentName")).isEqualTo("Dependent");
        assertThat(dependencies.get(0).get("dependencyName")).isEqualTo("Dependency");
        store.commitTransaction();
    }

    /**
     * Verifies the concept "xmi:UMLUsage".
     *
     * @throws RuleException If the concept cannot be applied.
     */
    @Test
    void umlUsage() throws RuleException {
        Result<Concept> result = applyConcept("xmi:UMLUsage");
        store.beginTransaction();
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("Usages")).isEqualTo(1L);
        assertThat(query("MATCH (usage:UML:Usage:PackagedElement) RETURN usage").getColumn("usage")).hasSize(1);
        List<Map<String, Object>> uses = query("MATCH (required:UML:Interface:Required)-[:USES]->(provided:UML:Interface:Provided) RETURN required.name as requiredName, provided.name as providedName").getRows();
        assertThat(uses).hasSize(1);
        assertThat(uses.get(0).get("requiredName")).isEqualTo("Required B");
        assertThat(uses.get(0).get("providedName")).isEqualTo("Provided B");
        store.commitTransaction();
    }

    /**
     * Verifies the concept "xmi:InformationFlow".
     *
     * @throws RuleException If the concept cannot be applied.
     */
    @Test
    void umlInformationFlow() throws RuleException {
        Result<Concept> result = applyConcept("xmi:UMLInformationFlow");
        store.beginTransaction();
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("InformationFlows")).isEqualTo(1L);
        assertThat(query("MATCH (informationFlow:UML:InformationFlow:PackagedElement) RETURN informationFlow").getColumn("informationFlow")).hasSize(1);
        store.commitTransaction();
    }

    /**
     * Verifies the concept "xmi:UMLProvidesInterface".
     *
     * @throws RuleException If the concept cannot be applied.
     */
    @Test
    void umlProvidesInterface() throws RuleException {
        Result<Concept> result = applyConcept("xmi:UMLProvidesInterface");
        store.beginTransaction();
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("ProvidesInterfaces")).isEqualTo(2L);
        List<Map<String, Object>> componentProvidesInterfaces = query("MATCH (element:UML:PackagedElement)-[:PROVIDES_INTERFACE]->(provided:UML:Interface) RETURN element.name as elementName, provided.name as interfaceName").getRows();
        assertThat(componentProvidesInterfaces).hasSize(1);
        assertThat(componentProvidesInterfaces.get(0).get("elementName")).isEqualTo("Component B");
        assertThat(componentProvidesInterfaces.get(0).get("interfaceName")).isEqualTo("Provided B");
        List<Map<String, Object>> portProvidesInterfaces = query("MATCH (element:UML:OwnedAttribute)-[:PROVIDES_INTERFACE]->(provided:UML:Interface) RETURN element.name as elementName, provided.name as interfaceName").getRows();
        assertThat(portProvidesInterfaces).hasSize(1);
        assertThat(portProvidesInterfaces.get(0).get("elementName")).isEqualTo("Port A");
        assertThat(portProvidesInterfaces.get(0).get("interfaceName")).isEqualTo("Public A");
        store.commitTransaction();
    }

    /**
     * Verifies the concept "xmi:UMLRequiresInterface".
     *
     * @throws RuleException If the concept cannot be applied.
     */
    @Test
    void umlRequiresInterface() throws RuleException {
        Result<Concept> result = applyConcept("xmi:UMLRequiresInterface");
        store.beginTransaction();
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("RequiresInterfaces")).isEqualTo(2L);
        List<Map<String, Object>> componentRequiresInterfaces = query("MATCH (element:UML:PackagedElement)-[:REQUIRES_INTERFACE]->(provided:UML:Interface) RETURN element.name as elementName, provided.name as interfaceName").getRows();
        assertThat(componentRequiresInterfaces).hasSize(1);
        assertThat(componentRequiresInterfaces.get(0).get("elementName")).isEqualTo("Component A");
        assertThat(componentRequiresInterfaces.get(0).get("interfaceName")).isEqualTo("Required B");
        List<Map<String, Object>> portRequiresInterfaces = query("MATCH (element:UML:OwnedAttribute)-[:REQUIRES_INTERFACE]->(provided:UML:Interface) RETURN element.name as elementName, provided.name as interfaceName").getRows();
        assertThat(portRequiresInterfaces).hasSize(1);
        assertThat(portRequiresInterfaces.get(0).get("elementName")).isEqualTo("Port B");
        assertThat(portRequiresInterfaces.get(0).get("interfaceName")).isEqualTo("Public B");
        store.commitTransaction();
    }

    /**
     * Verifies the group "xmi:UMLDefault".
     *
     * @throws RuleException If the group cannot be executed.
     */
    @Test
    void uml() throws RuleException {
        executeGroup("xmi:UML");
        List<String> conceptIds = query("MATCH (concept:Concept) RETURN concept.id as id").getColumn("id");
        assertThat(conceptIds).containsExactlyInAnyOrder(
                "xmi:UMLPackage",
                "xmi:UMLComponent",
                "xmi:UMLProperty",
                "xmi:UMLPort",
                "xmi:UMLAssociation",
                "xmi:UMLDependency",
                "xmi:UMLUsage",
                "xmi:UMLInformationFlow",
                "xmi:UMLProvidesInterface",
                "xmi:UMLRequiresInterface");
    }
}
