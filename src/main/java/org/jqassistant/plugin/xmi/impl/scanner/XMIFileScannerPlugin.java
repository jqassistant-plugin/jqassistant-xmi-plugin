package org.jqassistant.plugin.xmi.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import lombok.extern.slf4j.Slf4j;
import org.jqassistant.plugin.xmi.api.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation of a scanner for XMI files containing a UML model.
 */
@Slf4j
@ScannerPlugin.Requires(FileDescriptor.class)
public class XMIFileScannerPlugin extends AbstractScannerPlugin<FileResource, XMIFileDescriptor> {

    public static final String XMI_NS_PREFIX = "xmi";
    public static final String UML_NS_PREFIX = "uml";

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
        XMIElementResolver elementResolver = new XMIElementResolver(xmiFileDescriptor, store);
        XMIStereotypeResolver stereotypeResolver = new XMIStereotypeResolver(xmiFileDescriptor);
        try (InputStream stream = fileResource.createStream()) {
            log.info("Scanning XMI file '{}'.", path);
            XMLStreamReader streamReader = inputFactory.createXMLStreamReader(stream);
            XMLParser xmlParser = new XMLParser(streamReader);
            xmlParser.process(() -> {
                QName name = xmlParser.getName();
                if (name.getPrefix().equals(XMI_NS_PREFIX)) {
                    if (name.getLocalPart().equals("Documentation")) {
                        xmiFileDescriptor.setDocumentation(processDocumentation(xmlParser, store));
                    } else if (name.getLocalPart().equals("Extension")) {
                        xmlParser.process(() -> {
                            //  skip extensions
                        });
                    }
                } else if (name.getPrefix().equals(UML_NS_PREFIX)) {
                    if (name.getLocalPart().equals("Model")) {
                        xmiFileDescriptor.setModel(processModel(xmlParser, elementResolver, store));
                    }
                } else {
                    processStereotype(xmlParser, elementResolver, stereotypeResolver, store);
                }
            });
        } catch (XMLStreamException e) {
            log.warn("Cannot parse XMI document '" + path + "'.", e);
        } finally {
            log.info("Finished scan of XMI file '{}'.", path);
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
     * @param xmlParser       The {@link XMLParser}.
     * @param elementResolver The {@link XMIElementResolver}.
     * @param store           The {@link Store}.
     * @return The {@link UMLModelDescriptor}.
     * @throws XMLStreamException If the {@link XMLParser} fails.
     */
    private UMLModelDescriptor processModel(XMLParser xmlParser, XMIElementResolver elementResolver, Store store) throws XMLStreamException {
        log.info("Starting scan of UML model (line {}).", xmlParser.getXmlStreamReader().getLocation().getLineNumber());
        UMLModelDescriptor umlModel = store.create(UMLModelDescriptor.class);
        processUMLElementAttributes(xmlParser, umlModel);
        xmlParser.process(() -> {
            String localPart = xmlParser.getName().getLocalPart();
            if ("packagedElement".equals(localPart)) {
                processPackagedElement(xmlParser, umlModel, elementResolver);
            } else if ("profileApplication".equals(localPart)) {
                processProfileApplication(xmlParser, umlModel, elementResolver, store);
            }
        });
        log.info("Finished scan of UML model (line {}).", xmlParser.getXmlStreamReader().getLocation().getLineNumber());
        return umlModel;
    }

    /**
     * Process a uml:packagedElement element.
     *
     * @param xmlParser       The {@link XMLParser}.
     * @param parent          The parent {@link UMLElementDescriptor}.
     * @param elementResolver The {@link XMIElementResolver}.
     * @throws XMLStreamException If the {@link XMLParser} fails.
     */
    private void processPackagedElement(XMLParser xmlParser, UMLElementDescriptor parent, XMIElementResolver elementResolver) throws XMLStreamException {
        UMLPackagedElementDescriptor packagedElement = createUMLElement(xmlParser, UMLPackagedElementDescriptor.class, parent, elementResolver);
        xmlParser.getAttribute("supplier").ifPresent(supplierId -> packagedElement.setSupplier(elementResolver.resolve(supplierId)));
        xmlParser.getAttribute("client").ifPresent(clientId -> packagedElement.setClient(elementResolver.resolve(clientId)));
        xmlParser.getAttribute("informationSource").ifPresent(supplierId -> packagedElement.setInformationSource(elementResolver.resolve(supplierId)));
        xmlParser.getAttribute("informationTarget").ifPresent(clientId -> packagedElement.setInformationTarget(elementResolver.resolve(clientId)));
        xmlParser.getAttribute("visibility").ifPresent(visibility -> packagedElement.setVisibility(visibility));
        xmlParser.process(() -> {
            String localPart = xmlParser.getName().getLocalPart();
            if ("packagedElement".equals(localPart)) {
                processPackagedElement(xmlParser, packagedElement, elementResolver);
            } else if ("ownedAttribute".equals(localPart)) {
                processOwnedElement(xmlParser, UMLOwnedAttributeDescriptor.class, packagedElement, elementResolver);
            } else if ("ownedEnd".equals(localPart)) {
                processOwnedElement(xmlParser, UMLOwnedEndDescriptor.class, packagedElement, elementResolver);
            } else {
                processInterfaceElements(xmlParser, packagedElement, elementResolver);
            }
        });
    }

    /**
     * Process a owned element, i.e. ownedEnd or ownedAttribute.
     *
     * @param <T>             The label type to create.
     * @param xmlParser       The {@link XMLParser}.
     * @param type            The label type to create.
     * @param parent          The parent {@link UMLElementDescriptor}.
     * @param elementResolver The {@link XMIElementResolver}.
     * @throws XMLStreamException If the {@link XMLParser} fails.
     */
    private <T extends UMLOwnedElementTemplate & UMLElementDescriptor> void processOwnedElement(XMLParser xmlParser, Class<T> type, UMLElementDescriptor parent, XMIElementResolver elementResolver) throws XMLStreamException {
        T ownedElement = createUMLElement(xmlParser, type, parent, elementResolver);
        xmlParser.getAttribute("aggregation").ifPresent(aggregation -> ownedElement.setAggregation(aggregation));
        xmlParser.getAttribute("association").ifPresent(associationId -> ownedElement.setAssociation(elementResolver.resolve(associationId)));
        xmlParser.getAttribute("type").ifPresent(typeId -> ownedElement.setOfType(elementResolver.resolve(typeId)));
        xmlParser.process(() -> {
            if ("type".equals(xmlParser.getName().getLocalPart())) {
                xmlParser.getAttribute(XMI_NS_PREFIX, "idref").ifPresent(idref -> ownedElement.setOfType(elementResolver.resolve(idref)));
            } else {
                processInterfaceElements(xmlParser, ownedElement, elementResolver);
            }
        });
    }

    /**
     * Process provided or required interface elements.
     *
     * @param xmlParser       The {@link XMLParser}.
     * @param parent          The parent {@link UMLElementDescriptor}.
     * @param elementResolver The {@link XMIElementResolver}.
     * @throws XMLStreamException If the {@link XMLParser} fails.
     */
    private void processInterfaceElements(XMLParser xmlParser, UMLElementDescriptor parent, XMIElementResolver elementResolver) throws XMLStreamException {
        if ("provided".equals(xmlParser.getName().getLocalPart())) {
            createUMLElement(xmlParser, UMLProvidedInterfaceDescriptor.class, parent, elementResolver);
        } else if ("required".equals(xmlParser.getName().getLocalPart())) {
            createUMLElement(xmlParser, UMLRequiredInterfaceDescriptor.class, parent, elementResolver);
        }
    }

    /**
     * Process a profile application.
     *
     * @param xmlParser       The {@link XMLParser}.
     * @param umlModel        The {@link UMLModelDescriptor}.
     * @param elementResolver The {@link XMIElementResolver}.
     * @param store           The {@link Store}.
     * @throws XMLStreamException If the {@link XMLParser} fails.
     */
    private void processProfileApplication(XMLParser xmlParser, UMLModelDescriptor umlModel, XMIElementResolver elementResolver, Store store) throws XMLStreamException {
        UMLProfileApplicationDescriptor profileApplication = createUMLElement(xmlParser, UMLProfileApplicationDescriptor.class, umlModel, elementResolver);
        xmlParser.process(() -> {
            if ("appliedProfile".equals(xmlParser.getName().getLocalPart())) {
                UMLAppliedProfileDescriptor appliedProfile = store.create(UMLAppliedProfileDescriptor.class);
                processUMLElementAttributes(xmlParser, appliedProfile);
                xmlParser.getAttribute("href").ifPresent(href -> appliedProfile.setHref(href));
                profileApplication.setAppliedProfile(appliedProfile);
            }
        });
    }

    /**
     * Create a {@link UMLElementDescriptor}.
     *
     * @param <T>             The label elementType to create.
     * @param xmlParser       The {@link XMLParser}.
     * @param elementType     The label element type to create.
     * @param parent          The parent {@link UMLElementDescriptor}.
     * @param elementResolver The {@link XMIElementResolver}.
     * @return The create {@link UMLElementDescriptor}.
     * @throws XMLStreamException If parsing fails.
     */
    private <T extends UMLElementDescriptor> T createUMLElement(XMLParser xmlParser, Class<T> elementType, UMLElementDescriptor parent, XMIElementResolver elementResolver) throws XMLStreamException {
        String xmiId = xmlParser.getMandatoryAttribute(XMI_NS_PREFIX, "id");
        T umlElement = elementResolver.create(xmiId, elementType, parent);
        processUMLElementAttributes(xmlParser, umlElement);
        return umlElement;
    }

    /**
     * Process core UML element attributes, i.e. "name" and "type".
     *
     * @param xmlParser  The The {@link XMLParser}.
     * @param umlElement The {@link UMLElementDescriptor}.
     * @param <T>        The {@link UMLElementDescriptor} type.
     */
    private <T extends UMLElementDescriptor> void processUMLElementAttributes(XMLParser xmlParser, T umlElement) {
        xmlParser.getAttribute("name").ifPresent(name -> umlElement.setName(name));
        xmlParser.getAttribute(XMI_NS_PREFIX, "type").ifPresent(type -> umlElement.setXmiType(type));
    }

    /**
     * Process the applied stereotype represented by the current XML element.
     *
     * @param xmlParser          The {@link XMLParser}.
     * @param elementResolver    The {@link XMIElementResolver}.
     * @param stereotypeResolver The {@link XMIStereotypeResolver}.
     * @param store              The {@link Store}
     */
    private void processStereotype(XMLParser xmlParser, XMIElementResolver elementResolver, XMIStereotypeResolver stereotypeResolver, Store store) {
        QName name = xmlParser.getName();
        XMIStereotypeDescriptor stereotype = stereotypeResolver.resolve(name);
        XMIAppliedStereotypeDescriptor appliedStereotype = store.create(XMIAppliedStereotypeDescriptor.class);
        appliedStereotype.setStereotype(stereotype);
        for (int i = 0; i < xmlParser.getXmlStreamReader().getAttributeCount(); i++) {
            String attributeName = xmlParser.getXmlStreamReader().getAttributeLocalName(i);
            String attributeValue = xmlParser.getXmlStreamReader().getAttributeValue(i);
            if (attributeName.startsWith("base_")) {
                appliedStereotype.setAppliedToElement(elementResolver.resolve(attributeValue));
            } else {
                XMITaggedValueDescriptor xmiAttribute = store.create(XMITaggedValueDescriptor.class);
                xmiAttribute.setName(attributeName);
                xmiAttribute.setValue(attributeValue);
                appliedStereotype.getTaggedValues().add(xmiAttribute);
            }
        }
    }
}
