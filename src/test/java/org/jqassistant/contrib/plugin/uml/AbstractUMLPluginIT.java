package org.jqassistant.contrib.plugin.uml;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import org.jqassistant.contrib.plugin.uml.api.UMLElementDescriptor;
import org.jqassistant.contrib.plugin.uml.api.XMIFileDescriptor;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.util.List;

import static com.buschmais.jqassistant.core.scanner.api.DefaultScope.NONE;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Abstract base class for ITs using the XMI file "uml-elements.xmi".
 */
abstract class AbstractUMLPluginIT extends AbstractPluginIT {

    protected static final String UML_ELEMENTS_XMI_FILE = "/uml-elements.xmi";

    /**
     * Scan the resource
     */
    @BeforeEach
    void scan() {
        File xmiFile = new File(getClassesDirectory(XMIFileScannerPluginIT.class), UML_ELEMENTS_XMI_FILE);
        Descriptor descriptor = getScanner().scan(xmiFile, UML_ELEMENTS_XMI_FILE, NONE);
        assertThat(descriptor).isInstanceOf(XMIFileDescriptor.class);
    }

    /**
     * Map the given list of {@link UMLElementDescriptor}s to their names.
     *
     * @param umlElements The {@link UMLElementDescriptor}s.
     * @return The names of the {@link UMLElementDescriptor}s.
     */
    protected List<String> toNames(List<? extends UMLElementDescriptor> umlElements) {
        return umlElements.stream().map(umlElement -> umlElement.getName()).collect(toList());
    }

}
