package org.jqassistant.contrib.plugin.xmi;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import org.jqassistant.contrib.plugin.xmi.api.*;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contains test verifying the {@link org.jqassistant.contrib.plugin.xmi.impl.scanner.XMIFileScannerPlugin}.
 */
class XMIFileScannerPluginIT extends AbstractUMLPluginIT {

    /**
     * Verifies the top-level file structure of an XMI file with an XMI root element.
     */
    @Test
    void xmiFile() {
        store.beginTransaction();
        List<Descriptor> xmiFiles = query("MATCH (xmiFile:XMI:File) RETURN xmiFile").getColumn("xmiFile");
        assertThat(xmiFiles).hasSize(1);
        Descriptor descriptor = xmiFiles.get(0);
        assertThat(descriptor).isInstanceOf(XMIFileDescriptor.class);
        XMIFileDescriptor xmiFile = (XMIFileDescriptor) descriptor;
        assertThat(xmiFile.getFileName()).isEqualTo("/uml-elements.xmi");
        verifyDocumentation(xmiFile.getDocumentation());
        verifyModel(xmiFile.getModel());
        store.commitTransaction();
    }

    private void verifyDocumentation(XMIDocumentationDescriptor documentation) {
        assertThat(documentation).isNotNull();
        assertThat(documentation.getExporter()).isEqualTo("Enterprise Architect");
        assertThat(documentation.getExporterID()).isEqualTo("1558");
        assertThat(documentation.getExporterVersion()).isEqualTo("6.5");
    }

    private void verifyModel(UMLModelDescriptor model) {
        assertThat(model).isNotNull();
        assertThat(model.getName()).isEqualTo("EA_Model");
    }

    /**
     * Verifies the hierarchy of "uml:Package" elements contained in the model.
     */
    @Test
    void umlPackageHierarchy() {
        store.beginTransaction();
        List<List<UMLPackagedElementDescriptor>> hierarchies = query("MATCH " +
                "(:XMI:File)-[:CONTAINS_UML_MODEL]->(model:UML:Model)-[:HAS_CHILD]->(root:UML:PackagedElement), " + // identify root package
                "hierarchy=((root)-[:HAS_CHILD*0..]->(child:UML:PackagedElement)) " + // identify all hierarchies starting from the root package
                "WHERE child.xmiType='uml:Package'" + // limit result to 'uml:Package' elements
                "RETURN nodes(hierarchy) as hierarchy ").getColumn("hierarchy");
        assertThat(hierarchies.stream().map(hierarchy -> toNames(hierarchy)).collect(toList())).containsExactlyInAnyOrder(
                asList("Model"),
                asList("Model", "Components"),
                asList("Model", "Components", "Aggregations & Dependencies"),
                asList("Model", "Components", "Ports & Interfaces"),
                asList("Model", "Components", "Information Flow"));
        store.commitTransaction();
    }

    /**
     * Verifies ports and interfaces provided/required by components.
     */
    @Test
    void portsAndInterfaces() {
        store.beginTransaction();
        UMLPackagedElementDescriptor componentA = getComponent("Ports & Interfaces", "Component A");
        List<UMLOwnedAttributeDescriptor> portsA = getChildrenByType(componentA, UMLOwnedAttributeDescriptor.class);
        assertThat(portsA).hasSize(1);
        UMLOwnedAttributeDescriptor portA = portsA.get(0);
        assertThat(portA.getXmiType()).isEqualTo("uml:Port");
        List<UMLProvidedInterfaceDescriptor> providedInterfacesA = getChildrenByType(portA, UMLProvidedInterfaceDescriptor.class);
        assertThat(providedInterfacesA).hasSize(1);
        UMLProvidedInterfaceDescriptor publicA = providedInterfacesA.get(0);
        assertThat(publicA.getName()).isEqualTo("Public A");

        UMLPackagedElementDescriptor componentB = getComponent("Ports & Interfaces", "Component B");
        List<UMLOwnedAttributeDescriptor> portsB = getChildrenByType(componentB, UMLOwnedAttributeDescriptor.class);
        assertThat(portsB).hasSize(1);
        UMLOwnedAttributeDescriptor portB = portsB.get(0);
        assertThat(portB.getXmiType()).isEqualTo("uml:Port");
        List<UMLRequiredInterfaceDescriptor> requiredInterfacesB = getChildrenByType(portB, UMLRequiredInterfaceDescriptor.class);
        UMLRequiredInterfaceDescriptor publicB = requiredInterfacesB.get(0);
        assertThat(publicB.getName()).isEqualTo("Public B");
        store.commitTransaction();
    }

    /**
     * Verifies interfaces required/provided by components with an existing "uml:Usage" relation.
     */
    @Test
    void componentInterfaces() {
        store.beginTransaction();
        UMLPackagedElementDescriptor componentA = getComponent("Ports & Interfaces", "Component A");
        List<UMLRequiredInterfaceDescriptor> requiredInterfaces = getChildrenByType(componentA, UMLRequiredInterfaceDescriptor.class);
        assertThat(requiredInterfaces).hasSize(1);
        UMLRequiredInterfaceDescriptor requiredB = requiredInterfaces.get(0);
        assertThat(requiredB.getName()).isEqualTo("Required B");

        UMLPackagedElementDescriptor componentB = getComponent("Ports & Interfaces", "Component B");
        List<UMLProvidedInterfaceDescriptor> providedInterfaces = getChildrenByType(componentB, UMLProvidedInterfaceDescriptor.class);
        assertThat(providedInterfaces).hasSize(1);
        UMLProvidedInterfaceDescriptor providedB = providedInterfaces.get(0);
        assertThat(providedB.getName()).isEqualTo("Provided B");

        Map<String, Object> params = new HashMap<>();
        params.put("requiredB", requiredB);
        params.put("providedB", providedB);
        List<UMLPackagedElementDescriptor> usages = query("MATCH (requiredB)<-[:HAS_CLIENT]-(usage:PackagedElement{xmiType:'uml:Usage'})-[:HAS_SUPPLIER]->(providedB) " +
                "WHERE id(requiredB)=$requiredB and id(providedB)=$providedB " +
                "RETURN usage", params).getColumn("usage");

        assertThat(usages).hasSize(1);

        store.commitTransaction();
    }

    @Test
    void componentDependencies() {
        store.beginTransaction();
        UMLPackagedElementDescriptor dependent = getComponent("Aggregations & Dependencies", "Dependent");
        assertThat(dependent).isNotNull();
        UMLPackagedElementDescriptor dependency = getComponent("Aggregations & Dependencies", "Dependency");
        assertThat(dependency).isNotNull();

        Map<String, Object> params = new HashMap<>();
        params.put("dependentComponent", dependent);
        params.put("dependencyComponent", dependency);
        List<UMLPackagedElementDescriptor> dependencies = query("MATCH (dependentComponent)<-[:HAS_CLIENT]-(dependency:PackagedElement{xmiType:'uml:Dependency'})-[:HAS_SUPPLIER]->(dependencyComponent) " +
                "WHERE id(dependentComponent)=$dependentComponent and id(dependencyComponent)=$dependencyComponent " +
                "RETURN dependency", params).getColumn("dependency");
        assertThat(dependencies).hasSize(1);
        store.commitTransaction();
    }

    /**
     * Verifies information flow relations between packaged elements.
     */
    @Test
    void informationFlow() {
        store.beginTransaction();
        UMLPackagedElementDescriptor informationSupplier = getComponent("Information Flow", "Information Source");
        UMLPackagedElementDescriptor informationConsumer = getComponent("Information Flow", "Information Target");
        Map<String, Object> params = new HashMap<>();
        params.put("informationSource", informationSupplier);
        params.put("informationTarget", informationConsumer);

        List<UMLPackagedElementDescriptor> informationFlow = query("MATCH (informationSource)<-[:HAS_INFORMATION_SOURCE]-(informationFlow:PackagedElement{xmiType:'uml:InformationFlow'})-[:HAS_INFORMATION_TARGET]->(informationTarget) " +
                "WHERE id(informationSource)=$informationSource and id(informationTarget)=$informationTarget " +
                "RETURN informationFlow", params).getColumn("informationFlow");

        assertThat(informationFlow).hasSize(1);
        store.commitTransaction();
    }

    /**
     * Verifies {@link UMLPackagedElementDescriptor}s of type "uml:Association" for "composite" aggregations.
     */
    @Test
    void componentCompositeAssociations() {
        store.beginTransaction();
        UMLPackagedElementDescriptor aggregate = getComponent("Aggregations & Dependencies", "Aggregate");
        assertThat(aggregate).isNotNull();
        UMLPackagedElementDescriptor dependent = getComponent("Aggregations & Dependencies", "Dependent");
        assertThat(dependent).isNotNull();
        UMLPackagedElementDescriptor dependency = getComponent("Aggregations & Dependencies", "Dependency");
        assertThat(dependency).isNotNull();
        assertThat(getCompositeAssociations(aggregate, dependent)).hasSize(1);
        assertThat(getCompositeAssociations(aggregate, dependency)).hasSize(1);
        store.commitTransaction();
    }

    /**
     * Return the composite associations between the given {@link UMLPackagedElementDescriptor}s.
     *
     * @param aggregate The aggregate.
     * @param child     The child.
     * @return The {@link UMLPackagedElementDescriptor} representing the "uml:Association".
     */
    private List<UMLPackagedElementDescriptor> getCompositeAssociations(UMLPackagedElementDescriptor aggregate, UMLPackagedElementDescriptor child) {
        Map<String, Object> params = new HashMap<>();
        params.put("aggregateComponent", aggregate);
        params.put("childComponent", child);
        return query("MATCH (association:PackagedElement{xmiType:'uml:Association'}), " +
                "(association)<-[:FOR_ASSOCIATION]-(:OwnedAttribute)-[:OF_TYPE]->(aggregateComponent), " +
                "(association)<-[:FOR_ASSOCIATION]-(:OwnedEnd{aggregation:'composite'})-[:OF_TYPE]->(childComponent) " +
                "WHERE id(aggregateComponent)=$aggregateComponent and id(childComponent)=$childComponent " +
                "RETURN association", params).getColumn("association");
    }

    /**
     * Verifies stereotypes and their application to UML elements.
     */
    @Test
    void stereotypes() {
        store.beginTransaction();
        List<XMIStereotypeDescriptor> stereotypes = query("MATCH (:XMI:File)-[:CONTAINS_STEREOTYPE]->(stereotype:XMI:Stereotype) RETURN stereotype").getColumn("stereotype");
        assertThat(stereotypes).hasSize(1);
        XMIStereotypeDescriptor stereotype = stereotypes.get(0);
        assertThat(stereotype.getName()).isEqualTo("REST");
        assertThat(stereotype.getNamespacePrefix()).isEqualTo("thecustomprofile");
        assertThat(stereotype.getNamespaceUri()).isEqualTo("http://www.sparxsystems.com/profiles/thecustomprofile/1.0");
        List<String> elements = query("MATCH (port:UML:OwnedAttribute)<-[:APPLIED_TO]-(appliedStereotype:XMI:AppliedStereotype)-[:OF_STEREOTYPE]->(stereotype:XMI:Stereotype{name:'REST'}) RETURN port.name as portName").getColumn("portName");
        assertThat(elements).containsExactlyInAnyOrder("Port A", "Port B");
        store.commitTransaction();
    }

    /**
     * Verifies profiles and their applications.
     */
    @Test
    void profileApplications() {
        store.beginTransaction();
        List<UMLProfileApplicationDescriptor> profileApplications = query("MATCH (:XMI:File)-[:CONTAINS_XMI_ELEMENT]->(profileApplication:ProfileApplication) RETURN profileApplication").getColumn("profileApplication");
        assertThat(profileApplications).hasSize(1);
        UMLProfileApplicationDescriptor profileApplication = profileApplications.get(0);
        assertThat(profileApplication.getXmiType()).isEqualTo("uml:ProfileApplication");
        UMLAppliedProfileDescriptor appliedProfile = profileApplication.getAppliedProfile();
        assertThat(appliedProfile).isNotNull();
        assertThat(appliedProfile.getXmiType()).isEqualTo("uml:Profile");
        assertThat(appliedProfile.getHref()).isEqualTo("http://www.sparxsystems.com/profiles/thecustomprofile/1.0#thecustomprofile");
        store.commitTransaction();
    }

    /**
     * Executes a query for a {@link UMLPackagedElementDescriptor} of type "uml:Component" which is a child of a {@link UMLPackagedElementDescriptor} of type "uml:Package".
     *
     * @param packageName   The name of the UML package.
     * @param componentName The name of the UML component.
     * @return The {@link UMLPackagedElementDescriptor} representing the UML component.
     */
    private UMLPackagedElementDescriptor getComponent(String packageName, String componentName) {
        Map<String, Object> params = new HashMap<>();
        params.put("package", packageName);
        params.put("component", componentName);
        List<UMLPackagedElementDescriptor> components = query("MATCH (package:UML:PackagedElement{xmiType:'uml:Package',name:$package}), " +
                "(package)-[:HAS_CHILD]->(component:UML:PackagedElement{xmiType:'uml:Component',name:$component}) " +
                "RETURN component", params).getColumn("component");
        assertThat(components).hasSize(1);
        return components.get(0);
    }

    private <T extends UMLElementDescriptor> List<T> getChildrenByType(XMIElementDescriptor parent, Class<T> type) {
        return parent.getChildren().stream()
                .filter(child -> type.isAssignableFrom(child.getClass()))
                .map(child -> type.cast(child))
                .collect(toList());
    }

}
