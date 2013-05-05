package org.robolectric.res;

import android.view.View;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.InputStream;

abstract class XResourceLoader implements ResourceLoader {
    final AttrResourceLoader attrResourceLoader = new AttrResourceLoader();
    final ResBunch data = new ResBunch();
    final ResBundle<PluralResourceLoader.PluralRules> pluralsData = new ResBundle<PluralResourceLoader.PluralRules>();
    final ResBundle<String> stringData = new ResBundle<String>();
    final ResBundle<ViewNode> layoutData = new ResBundle<ViewNode>();
    final ResBundle<MenuNode> menuData = new ResBundle<MenuNode>();
    final ResBundle<DrawableNode> drawableData = new ResBundle<DrawableNode>();
    final ResBundle<PreferenceNode> preferenceData = new ResBundle<PreferenceNode>();
    final ResBundle<Document> xmlDocuments = new ResBundle<Document>();
    final ResBundle<FsFile> rawResources = new ResBundle<FsFile>();
    final ResBundle<StyleData> styleData = new ResBundle<StyleData>();
    private final ResourceIndex resourceIndex;
    boolean isInitialized = false;

    protected XResourceLoader(ResourceIndex resourceIndex) {
        this.resourceIndex = resourceIndex;
    }

    abstract void doInitialize();

    void initialize() {
        if (isInitialized) return;
        doInitialize();
        isInitialized = true;

        makeImmutable();
    }

    protected void makeImmutable() {
        data.makeImmutable();

        pluralsData.makeImmutable();
        stringData.makeImmutable();
        layoutData.makeImmutable();
        menuData.makeImmutable();
        drawableData.makeImmutable();
        preferenceData.makeImmutable();
        xmlDocuments.makeImmutable();
        rawResources.makeImmutable();
        styleData.makeImmutable();
    }

    @Override
    public String getNameForId(int id) {
        return resourceIndex.getResourceName(id);
    }

    public TypedResource getValue(ResName resName, String qualifiers) {
        initialize();
        ResBunch.Value value = data.getValue(resName, qualifiers);
        return value == null ? null : value.getTypedResource();
    }

    @Override
    public Plural getPlural(ResName resName, int quantity, String qualifiers) {
        initialize();
        PluralResourceLoader.PluralRules pluralRules = pluralsData.get(resName, qualifiers);
        if (pluralRules == null) return null;

        return pluralRules.find(quantity);
    }

    @Override
    public Document getXml(ResName resName, String qualifiers) {
        initialize();
        return xmlDocuments.get(resName, qualifiers);
    }

    @Override
    public DrawableNode getDrawableNode(ResName resName, String qualifiers) {
        return drawableData.get(resName, qualifiers);
    }

    @Override
    public InputStream getRawValue(ResName resName) {
        initialize();

        FsFile file = rawResources.get(resName, "");
        try {
            return file == null ? null : file.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public Style getStyle(ResName resName, String qualifiers) {
        final StyleData styleData = this.styleData.get(resName, qualifiers);
        return new StyleResolver(styleData);
    }

    @Override
    public PreferenceNode getPreferenceNode(ResName resName, String qualifiers) {
        initialize();

        return preferenceData.get(resName, qualifiers);
    }

    @Override
    public ViewNode getLayoutViewNode(ResName resName, String qualifiers) {
        initialize();
        if (resName == null) return null;
        return layoutData.get(resName, qualifiers);
    }

    @Override
    public MenuNode getMenuNode(ResName resName, String qualifiers) {
        initialize();
        if (resName == null) return null;
        return menuData.get(resName, qualifiers);
    }

    @Override
    public ResourceIndex getResourceIndex() {
        return resourceIndex;
    }

    @Override
    public boolean hasAttributeFor(Class<? extends View> viewClass, String namespace, String attribute) {
        initialize();
        return attrResourceLoader.hasAttributeFor(viewClass, namespace, attribute);
    }

    @Override
    public String convertValueToEnum(Class<? extends View> viewClass, String namespace, String attribute, String part) {
        initialize();
        return attrResourceLoader.convertValueToEnum(viewClass, namespace, attribute, part);
    }

    abstract static class Resolver<T> {
        private final ResBundle<String> resBundle;

        protected Resolver(ResBundle<String> resBundle) {
            this.resBundle = resBundle;
        }

        public T resolve(ResName resName, String qualifiers) {
            ResBundle.Value<String> value = resBundle.getValue(resName, qualifiers);
            if (value == null) return null;
            return resolveValue(qualifiers, value.value, value.xmlContext.packageName);
        }

        T resolveValue(String qualifiers, String value, String packageName) {
            if (value == null) return null;
            if (value.startsWith("@")) {
                ResName resName = new ResName(ResName.qualifyResourceName(value.substring(1), packageName, null));
                return resolve(resName, qualifiers);
            } else {
                return convert(value);
            }
        }

        abstract T convert(String rawValue);
    }

    static class BasicResolver extends Resolver<String> {
        BasicResolver(ResBundle<String> resBundle) {
            super(resBundle);
        }

        @Override
        String convert(String rawValue) {
            return rawValue;
        }
    }

    private class StyleResolver implements Style {
        private final StyleData leafStyle;

        public StyleResolver(StyleData styleData) {
            this.leafStyle = styleData;
        }

        @Override public String getAttrValue(String name) {
            StyleData currentStyle = leafStyle;
            String value = null;
            while (currentStyle != null && (value = currentStyle.getAttrValue(name)) == null) {
                String parent = currentStyle.getParent();
//                currentStyle = parent == null ? null : styleData.get(new ResName(parent));
            }
            throw new UnsupportedOperationException("todo!");
        }
    }
}
