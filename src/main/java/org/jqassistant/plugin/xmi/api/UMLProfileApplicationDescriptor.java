package org.jqassistant.plugin.xmi.api;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Represents a profile application.
 */
@Label("ProfileApplication")
public interface UMLProfileApplicationDescriptor extends UMLElementDescriptor {

    /**
     * Return the {@link UMLAppliedProfileDescriptor}.
     *
     * @return The {@link UMLAppliedProfileDescriptor}.
     */
    @Relation("APPLIES_PROFILE")
    UMLAppliedProfileDescriptor getAppliedProfile();

    /**
     * Set the {@link UMLAppliedProfileDescriptor}
     *
     * @param appliedProfile The {@link UMLAppliedProfileDescriptor}.
     */
    void setAppliedProfile(UMLAppliedProfileDescriptor appliedProfile);

}
