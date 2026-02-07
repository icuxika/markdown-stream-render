package com.icuxika.markdown.stream.render.core.ast;

import java.util.HashMap;
import java.util.Map;

public class Document extends Block {
    private final Map<String, LinkReference> linkReferences = new HashMap<>();

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    /**
     * Add a link reference definition.
     *
     * @param linkReference
     *            link reference
     */
    public void addLinkReference(LinkReference linkReference) {
        String key = normalizeLabel(linkReference.getLabel());
        if (!linkReferences.containsKey(key)) {
            linkReferences.put(key, linkReference);
        }
    }

    public LinkReference getLinkReference(String label) {
        return linkReferences.get(normalizeLabel(label));
    }

    private String normalizeLabel(String label) {
        return label.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    public Map<String, LinkReference> getLinkReferences() {
        return linkReferences;
    }
}
