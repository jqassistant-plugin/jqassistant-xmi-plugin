package org.jqassistant.plugin.xmi;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import org.junit.jupiter.api.BeforeEach;

import static com.buschmais.jqassistant.core.scanner.api.DefaultScope.NONE;
import static java.util.stream.Collectors.toList;

/**
 * Abstract base class for ITs using the XMI file "uml-elements.xmi".
 */
abstract class AbstractUMLPluginIT extends AbstractPluginIT {

    protected static final String UML_ELEMENTS_XMI_FILE = "/uml-elements.xmi";

    @Override
    protected Map<String, Object> getScannerProperties() {
        Map<String, Object> map = new HashMap<>();
        map.put("xml.file.include", "/uml-elements.xmi");
        return map;
    }

    /**
     * Scan the resource
     */
    @BeforeEach
    void scan() {
        File xmiFile = new File(getClassesDirectory(XMIFileScannerPluginIT.class), UML_ELEMENTS_XMI_FILE);
        Descriptor descriptor = getScanner().scan(xmiFile, UML_ELEMENTS_XMI_FILE, NONE);
        //assertThat(descriptor).isInstanceOf(XMIFileDescriptor.class);
    }

    protected List<String> toNames(List<? extends NamedDescriptor> elements) {
        return elements.stream().map(NamedDescriptor::getName).collect(toList());
    }

}
