package org.robolectric.res;

import java.util.LinkedHashMap;
import java.util.Map;

public class StyleData implements Style {
    private final String name;
    private final String parent;
    private final Map<String, String> items = new LinkedHashMap<String, String>();

    public StyleData(String name, String parent) {
        this.name = name;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public String getParent() {
        return parent;
    }

    public void add(String attrName, String attrValue) {
        items.put(attrName, attrValue);
    }

    @Override public String getAttrValue(String name) {
        return items.get(name);
    }
}
