package org.jqassistant.contrib.plugin.xmi.impl.scanner;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;

/**
 * A XML parser simplifying the StAX-API.
 */
@RequiredArgsConstructor
@Getter
public class XMLParser {

    /**
     * The {@link XMLStreamReader} to use.
     */
    private final XMLStreamReader xmlStreamReader;

    /**
     * The currenty stack of processed elements.
     */
    private final Deque<QName> stack = new LinkedList<>();

    /**
     * Process an element.
     * <p>
     * An {@link XMLElementConsumer} is taken as parameter which will be notified on each element start event.
     * The method returns if the element (i.e. sub-tree of the XML document) has been processed.
     *
     * @param elementConsumer The {@link XMLElementConsumer}.
     * @throws XMLStreamException If parsing fails.
     */
    public void process(XMLElementConsumer elementConsumer) throws XMLStreamException {
        int depth = stack.size();
        while (stack.size() >= depth && xmlStreamReader.hasNext()) {
            int next = xmlStreamReader.next();
            if (next == XMLStreamConstants.START_ELEMENT) {
                stack.push(xmlStreamReader.getName());
                elementConsumer.onStartElement();
            } else if (next == XMLStreamConstants.END_ELEMENT) {
                stack.pop();
            }
        }
    }

    /**
     * Return the current element name.
     *
     * @return The current element name.
     */
    public QName getName() {
        return xmlStreamReader.getName();
    }


    /**
     * Return the value for an optional attribute identified by the given name.
     *
     * @param attributeName The attribute name.
     * @return An {@link Optional} with the attribute value.
     */
    public Optional<String> getAttribute(String attributeName) {
        return getAttribute(null, attributeName);
    }

    /**
     * Return the value for an optional attribute identified by the given name.
     *
     * @param namespacePrefix The namespace URI.
     * @param attributeName   The attribute name.
     * @return An {@link Optional} with the attribute value.
     */
    public Optional<String> getAttribute(String namespacePrefix, String attributeName) {
        String namespaceUri = namespacePrefix != null ? xmlStreamReader.getNamespaceURI(namespacePrefix) : "";
        return Optional.ofNullable(xmlStreamReader.getAttributeValue(namespaceUri, attributeName));
    }


    /**
     * Return the value for an mandatory attribute identified by the given name.
     *
     * @param namespacePrefix The namespace URI.
     * @param attributeName   The attribute name.
     * @return The attribute value.
     * @throws XMLStreamException If the attribute does not exist.
     */
    public String getMandatoryAttribute(String namespacePrefix, String attributeName) throws XMLStreamException {
        String attributeValue = xmlStreamReader.getAttributeValue(xmlStreamReader.getNamespaceURI(namespacePrefix), attributeName);
        if (attributeValue == null) {
            throw new XMLStreamException("Cannot find id attribute of UML element at " + xmlStreamReader.getLocation());
        }
        return attributeValue;
    }

    /**
     * A consumer which will be notified on each element start event.
     */
    public interface XMLElementConsumer {

        /**
         * Notify an element start event.
         *
         * @throws XMLStreamException If parsing fails.
         */
        void onStartElement() throws XMLStreamException;

    }
}
