package org.robolectric.res;

import android.view.View;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import java.io.InputStream;

public interface ResourceLoader {
    String ANDROID_NS = "http://schemas.android.com/apk/res/android";

    String getNameForId(int id);

    TypedResource getValue(@NotNull ResName resName, String qualifiers);

    Plural getPlural(ResName resName, int quantity, String qualifiers);

    Document getXml(ResName resName, String qualifiers);

    DrawableNode getDrawableNode(ResName resName, String qualifiers);

    InputStream getRawValue(ResName resName);

    PreferenceNode getPreferenceNode(ResName resName, String qualifiers);

    ResourceIndex getResourceIndex();

    ViewNode getLayoutViewNode(ResName resName, String qualifiers);

    MenuNode getMenuNode(ResName resName, String qualifiers);

    boolean hasAttributeFor(Class<? extends View> viewClass, String namespace, String attribute);

    String convertValueToEnum(Class<? extends View> viewClass, String namespace, String attribute, String part);

    boolean providesFor(String namespace);
}
