package org.jqassistant.contrib.plugin.xmi.api;

import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Describes a XMI documentation.
 */
@Label("Documentation")
public interface XMIDocumentationDescriptor extends XMIDescriptor {

    /**
     * Return the exporter.
     *
     * @return The exporter.
     */
    String getExporter();

    /**
     * Set the exporter.
     *
     * @param exporter The exporter.
     */
    void setExporter(String exporter);

    /**
     * Return the exporter version.
     *
     * @return The exporter version.
     */
    String getExporterVersion();

    /**
     * Set the exporter version.
     *
     * @param exporterVersion The exporter version.
     */
    void setExporterVersion(String exporterVersion);

    /**
     * Return the exporter ID.
     *
     * @return The exporter ID.
     */
    String getExporterID();

    /**
     * Set the exporter ID.
     *
     * @param exporterID The exporterID.
     */
    void setExporterID(String exporterID);

}
