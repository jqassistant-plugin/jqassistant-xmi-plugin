package org.jqassistant.plugin.xmi;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlElementDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlFileDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contains test verifying the rules in the xmi:Default group.
 */
class XMIRulePluginIT extends AbstractUMLPluginIT {

    /**
     * Verifies the top-level file structure of an XMI file with an XMI root element.
     */
    @Test
    void xmiFile() throws RuleException {

        Result<Concept> result = applyConcept("xmi:File");
        store.beginTransaction();
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        List<Row> rows = result.getRows();
        assertThat(rows).hasSize(1);
        Descriptor file = (Descriptor) rows.get(0).getColumns().get("File").getValue();
        assertThat(file).isInstanceOf(XmlFileDescriptor.class);
        assertThat(((XmlFileDescriptor) file).getFileName()).isEqualTo("/uml-elements.xmi");
        Descriptor rootElement = (Descriptor) rows.get(0).getColumns().get("RootElement").getValue();
        assertThat(rootElement).isInstanceOf(XmlElementDescriptor.class);
        assertThat(((XmlElementDescriptor) rootElement).getName()).isEqualTo("XMI");
        store.commitTransaction();
    }

    // TODO extract documentation fields
    // private void verifyDocumentation(XMIDocumentationDescriptor documentation) {
    //    assertThat(documentation).isNotNull();
    //    assertThat(documentation.getExporter()).isEqualTo("Enterprise Architect");
    //    assertThat(documentation.getExporterID()).isEqualTo("1558");
    //    assertThat(documentation.getExporterVersion()).isEqualTo("6.5");
    //}

}
