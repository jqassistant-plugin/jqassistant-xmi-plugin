package org.jqassistant.plugin.xmi.impl.scanner;

import com.buschmais.jqassistant.core.store.api.Store;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jqassistant.plugin.xmi.api.UMLElementDescriptor;
import org.jqassistant.plugin.xmi.api.UMLModelDescriptor;
import org.jqassistant.plugin.xmi.api.XMIElementDescriptor;
import org.jqassistant.plugin.xmi.api.XMIFileDescriptor;

/**
 * A caching resolver for UML elements.
 */
@Getter
@RequiredArgsConstructor
class XMIElementResolver {

    /**
     * The {@link UMLModelDescriptor} declaring all resolved {@link UMLElementDescriptor}s.
     */
    private final XMIFileDescriptor xmiFileDescriptor;

    /**
     * The {@link Store}.
     */
    private final Store store;

    /**
     * The {@link Cache}.
     */
    private final Cache<String, XMIElementDescriptor> cache = Caffeine.newBuilder().softValues().build();

    /**
     * Create a {@link UMLElementDescriptor}.
     *
     * @param xmiId  The XMI id of the {@link UMLElementDescriptor}.
     * @param type   The requested type of the {@link UMLElementDescriptor}.
     * @param parent The parent of the {@link UMLElementDescriptor}.
     * @param <T>    The type of the {@link UMLElementDescriptor}.
     * @return The created {@link UMLElementDescriptor}.
     */
    <T extends UMLElementDescriptor> T create(String xmiId, Class<T> type, UMLElementDescriptor parent) {
        T umlElement = store.addDescriptorType(resolve(xmiId), type);
        umlElement.setParent(parent);
        return umlElement;
    }

    /**
     * Resolve a {@link UMLElementDescriptor}, even if it has not been created yet.
     *
     * @param xmiIdRef The XMI id referencing a {@link UMLElementDescriptor}.
     * @return The resolved {@link UMLElementDescriptor}.
     */
    XMIElementDescriptor resolve(String xmiIdRef) {
        return cache.get(xmiIdRef, key -> xmiFileDescriptor.resolveElement(key));
    }

}
