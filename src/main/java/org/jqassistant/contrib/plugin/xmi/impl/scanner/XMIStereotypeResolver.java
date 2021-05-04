package org.jqassistant.contrib.plugin.xmi.impl.scanner;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.jqassistant.contrib.plugin.xmi.api.XMIFileDescriptor;
import org.jqassistant.contrib.plugin.xmi.api.XMIStereotypeDescriptor;

import javax.xml.namespace.QName;

/**
 * A caching resolver for stereotypes.
 */
@RequiredArgsConstructor
public class XMIStereotypeResolver {

    private final XMIFileDescriptor xmiFileDescriptor;

    private final Cache<QName, XMIStereotypeDescriptor> stereotypes = Caffeine.newBuilder().softValues().build();

    /**
     * Resolve a {@link XMIStereotypeDescriptor} by the given XML {@link QName}.
     *
     * @param name The {@link QName}.
     * @return The {@link XMIStereotypeDescriptor}.
     */
    public XMIStereotypeDescriptor resolve(QName name) {
        return stereotypes.get(name, key -> xmiFileDescriptor.resolveStereotype(name.getNamespaceURI(), name.getLocalPart(), name.getPrefix()));
    }

}
