package org.jqassistant.contrib.plugin.uml.impl;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import lombok.extern.slf4j.Slf4j;
import org.jqassistant.contrib.plugin.uml.api.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

/**
 * Implementation of a scanner for XMI files containing a UML model.
 */
@Slf4j
@ScannerPlugin.Requires(FileDescriptor.class)
public class XMIFileScannerPlugin extends AbstractScannerPlugin<FileResource, XMIFileDescriptor> {

    /**
     * Defines the prefix for XMI namespaces.
     */
    private static final Set<String> UML_NAMESPACE_URI_PREFIXES = Stream.of("http://www.omg.org/spec/UML/", "http://schema.omg.org/spec/UML/", "http://www.eclipse.org/uml2/3.0.0/UML").collect(toSet());

    /**
     * Defines the prefix for UML namespaces.
     */
    private static final Set<String> XMI_NAMESPACE_URI_PREFIXES = Stream.of("http://www.omg.org/spec/XMI/", "http://schema.omg.org/spec/XMI/").collect(toSet());

    private XMLInputFactory inputFactory;

    @Override
    public void initialize() {
        inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    }

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) {
        return path.toLowerCase().endsWith(".xmi");
    }

    @Override
    public XMIFileDescriptor scan(FileResource fileResource, String path, Scope scope, Scanner scanner) throws IOException {
        ScannerContext context = scanner.getContext();
        Store store = context.getStore();
        FileDescriptor fileDescriptor = context.peek(FileDescriptor.class);
        XMIFileDescriptor xmiFileDescriptor = store.addDescriptorType(fileDescriptor, XMIFileDescriptor.class);
        try (InputStream stream = fileResource.createStream()) {
            XMLStreamReader streamReader = inputFactory.createXMLStreamReader(stream);
            XMLParser xmlParser = new XMLParser(streamReader);
            xmlParser.process(() -> {
                String namespaceURI = xmlParser.getName().getNamespaceURI();
                String xmiNamespace = null;
                if (xmlParser.getName().getLocalPart().equals("XMI") && XMI_NAMESPACE_URI_PREFIXES.stream().anyMatch(p -> namespaceURI.startsWith(p))) {
                    xmiNamespace = namespaceURI;
                } else if (xmlParser.getName().getLocalPart().equals("Documentation") && XMI_NAMESPACE_URI_PREFIXES.stream().anyMatch(p -> namespaceURI.startsWith(p))) {
                    xmiFileDescriptor.setDocumentation(processDocumentation(xmlParser, store));
                } else if (xmlParser.getName().getLocalPart().equals("Model") && UML_NAMESPACE_URI_PREFIXES.stream().anyMatch(p -> namespaceURI.startsWith(p))) {
                    xmiFileDescriptor.setModel(processModel(xmiNamespace, xmlParser, store));
                }
            });
        } catch (XMLStreamException e) {
            log.warn("Cannot parse document '" + path + "'.", e);
        }
        return xmiFileDescriptor;
    }

    /**
     * Process a xmi:Documentation element.
     *
     * @param parser The {@link XMLParser}.
     * @param store  The {@link Store}.
     * @return The {@link XMIDocumentationDescriptor}.
     */
    private XMIDocumentationDescriptor processDocumentation(XMLParser parser, Store store) {
        XMIDocumentationDescriptor documentationDescriptor = store.create(XMIDocumentationDescriptor.class);
        parser.getAttribute("exporter").ifPresent(exporter -> documentationDescriptor.setExporter(exporter));
        parser.getAttribute("exporterVersion").ifPresent(exporterVersion -> documentationDescriptor.setExporterVersion(exporterVersion));
        parser.getAttribute("exporterID").ifPresent(exporterID -> documentationDescriptor.setExporterID(exporterID));
        return documentationDescriptor;
    }

    /**
     * Process a uml:model element.
     *
     * @param xmiNamespace The XMI namespace.
     * @param xmlParser    The {@link XMLParser}.
     * @param store        The {@link Store}.
     * @return The {@link UMLModelDescriptor}.
     * @throws XMLStreamException If the {@link XMLParser} fails.
     */
    private UMLModelDescriptor processModel(String xmiNamespace, XMLParser xmlParser, Store store) throws XMLStreamException {
        UMLModelDescriptor umlModel = store.create(UMLModelDescriptor.class);
        UMLELementResolver umlElementResolver = new UMLELementResolver(umlModel, xmiNamespace, store);
        xmlParser.process(() -> {
            if ("packagedElement".equals(xmlParser.getName().getLocalPart())) {
                processPackagedElement(umlModel, umlElementResolver, xmlParser);
            }
        });
        return umlModel;
    }

    /**
     * Process a uml:packagedElement element.
     *
     * @param parent          The parent {@link UMLElementDescriptor}.
     * @param elementResolver The {@link UMLELementResolver}.
     * @param xmlParser       The {@link XMLParser}.
     * @throws XMLStreamException If the {@link XMLParser} fails.
     */
    private void processPackagedElement(UMLElementDescriptor parent, UMLELementResolver elementResolver, XMLParser xmlParser) throws XMLStreamException {
        UMLPackagedElementDescriptor packagedElement = createUMLElement(parent, elementResolver, UMLPackagedElementDescriptor.class, xmlParser);
        xmlParser.getAttribute("supplier").ifPresent(supplierId -> packagedElement.setSupplier(elementResolver.resolve(supplierId)));
        xmlParser.getAttribute("client").ifPresent(clientId -> packagedElement.setClient(elementResolver.resolve(clientId)));
        xmlParser.getAttribute("visibility").ifPresent(visibility -> packagedElement.setVisibility(visibility));
        xmlParser.process(() -> {
            switch (xmlParser.getName().getLocalPart()) {
                case "packagedElement":
                    processPackagedElement(packagedElement, elementResolver, xmlParser);
                    break;
                case "provided":
                    createUMLElement(packagedElement, elementResolver, UMLProvidedInterfaceDescriptor.class, xmlParser);
                    break;
                case "required":
                    createUMLElement(packagedElement, elementResolver, UMLRequiredInterfaceDescriptor.class, xmlParser);
                    break;
                case "ownedAttribute":
                    processOwnedElement(packagedElement, elementResolver, UMLOwnedAttributeDescriptor.class, xmlParser);
                    break;
                case "ownedEnd":
                    processOwnedElement(packagedElement, elementResolver, UMLOwnedEndDescriptor.class, xmlParser);
                    break;
                default:
                    break;
            }
        });
    }

    /**
     * Process a owned element, i.e. ownedEnd or ownedAttribute.
     *
     * @param parent          The parent {@link UMLElementDescriptor}.
     * @param elementResolver The {@link UMLELementResolver}.
     * @param type            The label type to create.
     * @param xmlParser       The {@link XMLParser}.
     * @param <T>             The label type to create.
     * @throws XMLStreamException If the {@link XMLParser} fails.
     */
    private <T extends UMLOwnedElementTemplate & UMLElementDescriptor> void processOwnedElement(UMLElementDescriptor parent, UMLELementResolver elementResolver, Class<T> type, XMLParser xmlParser) throws XMLStreamException {
        T ownedElement = createUMLElement(parent, elementResolver, type, xmlParser);
        xmlParser.getAttribute("aggregation").ifPresent(aggregation -> ownedElement.setAggregation(aggregation));
        xmlParser.getAttribute("association").ifPresent(associationId -> ownedElement.setAssociation(elementResolver.resolve(associationId)));
        xmlParser.getAttribute("type").ifPresent(typeId -> ownedElement.setOfType(elementResolver.resolve(typeId)));
        xmlParser.process(() -> {
            if ("type".equals(xmlParser.getName().getLocalPart())) {
                xmlParser.getAttribute(elementResolver.getXmiNamespace(), "idref").ifPresent(idref -> ownedElement.setOfType(elementResolver.resolve(idref)));
            }
        });
    }

    /**
     * Create a {@link UMLElementDescriptor}.
     *
     * @param parent          The parent {@link UMLElementDescriptor}.
     * @param elementResolver The {@link UMLELementResolver}.
     * @param elementType     The label element type to create.
     * @param xmlParser       The {@link XMLParser}.
     * @param <T>             The label elementType to create.
     * @return The create {@link UMLElementDescriptor}.
     * @throws XMLStreamException If parsing fails.
     */
    private <T extends UMLElementDescriptor> T createUMLElement(UMLElementDescriptor parent, UMLELementResolver elementResolver, Class<T> elementType, XMLParser xmlParser) throws XMLStreamException {
        T umlElement = elementResolver.create(xmlParser.getMandatoryAttribute(elementResolver.getXmiNamespace(), "id"), elementType, parent);
        xmlParser.getAttribute(elementResolver.getXmiNamespace(), "type").ifPresent(type -> umlElement.setXmiType(type));
        xmlParser.getAttribute("name").ifPresent(name -> umlElement.setName(name));
        return umlElement;
    }

}
