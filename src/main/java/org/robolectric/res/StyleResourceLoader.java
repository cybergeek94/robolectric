package org.robolectric.res;

import javax.xml.xpath.XPathExpressionException;

public class StyleResourceLoader extends XpathResourceXmlLoader {
    private final ResBunch data;

    public StyleResourceLoader(ResBunch data) {
        super("/resources/style");
        this.data = data;
    }

    @Override
    protected void processNode(String name, XmlNode xmlNode, XmlContext xmlContext) throws XPathExpressionException {
        String styleName = underscorize(xmlNode.getAttrValue("name"));
        String styleParent = underscorize(xmlNode.getAttrValue("parent"));

        StyleData styleData = new StyleData(styleName, styleParent);

        for (XmlNode item : xmlNode.selectElements("item")) {
            String attrName = item.getAttrValue("name");
            String value = item.getTextContent();

            styleData.add(attrName, value);
        }

        data.put("style", styleName, new TypedResource<StyleData>(styleData, ResType.STYLE), xmlContext);
    }

    private String underscorize(String s) {
        return s == null ? null : s.replace('.', '_');
    }
}
