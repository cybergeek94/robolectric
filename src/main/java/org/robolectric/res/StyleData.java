package org.robolectric.res;

import java.util.LinkedHashMap;
import java.util.Map;

public class StyleData implements Style {
    private final String name;
    private final String parent;
    private final Map<ResName, String> items = new LinkedHashMap<ResName, String>();

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

    public void add(ResName attrName, String attrValue) {
        attrName.mustBe("attr");
        items.put(attrName, attrValue);
    }

    @Override public String getAttrValue(ResName name) {
        name.mustBe("attr");
        return items.get(name);
    }

  @Override public String toString() {
    return "StyleData{" +
        "name='" + name + '\'' +
        ", parent='" + parent + '\'' +
        '}';
  }
}
