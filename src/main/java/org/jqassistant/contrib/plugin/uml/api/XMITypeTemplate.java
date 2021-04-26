package org.jqassistant.contrib.plugin.uml.api;

/**
 * A template for typed XMI elements.
 */
public interface XMITypeTemplate {

    /**
     * Return the XMI type of the element, e.g. "uml:Component".
     *
     * @return The XMI type.
     */
    String getXmiType();

    /**
     * Set the XMI type of the element.
     *
     * @param type The XMI type.
     */
    void setXmiType(String type);

}
