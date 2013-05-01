package org.robolectric.res;

import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.List;

public class NewAttrResourceLoader extends XpathResourceXmlLoader {
    private final ResBunch resBunch;

    public NewAttrResourceLoader(ResBunch resBunch) {
        super("/resources//attr");
        this.resBunch = resBunch;
    }

    @Override
    protected void processNode(String name, XmlNode xmlNode, XmlContext xmlContext)
            throws XPathExpressionException {
        String format = xmlNode.getAttrValue("format");
        List<AttrData.Pair> pairs = null;

        if (format == null) {
            pairs = new ArrayList<AttrData.Pair>();

            for (XmlNode enumNode : xmlNode.selectElements("enum")) {
                format = "enum";
                pairs.add(new AttrData.Pair(enumNode.getAttrValue("name"), enumNode.getAttrValue("value")));
            }

            for (XmlNode flagNode : xmlNode.selectElements("flag")) {
                if ("enum".equals(format)) {
                    throw new IllegalStateException(
                            "you can't have a mix of enums and flags for \"" + name + "\" in " + xmlContext);
                }
                format = "flag";
                pairs.add(new AttrData.Pair(flagNode.getAttrValue("name"), flagNode.getAttrValue("value")));
            }
        }
        if (format == null) {
            return;
//            throw new IllegalStateException(
//                    "you need a format, enums, or flags for \"" + name + "\" in " + xmlContext);
        }
        AttrData attrData = new AttrData(name, format, pairs);
        resBunch.put("attr", name, new TypedResource<AttrData>(attrData, ResType.ATTR_DATA),
                xmlContext);
    }
}
