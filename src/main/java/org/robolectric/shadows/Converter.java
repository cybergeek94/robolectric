package org.robolectric.shadows;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;
import org.robolectric.res.AttrData;
import org.robolectric.res.Attribute;
import org.robolectric.res.ResName;
import org.robolectric.res.ResType;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.TypedResource;
import org.robolectric.util.Util;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.robolectric.Robolectric.shadowOf;

public class Converter<T> {
    private static final Map<String, ResType> ATTR_TYPE_MAP = new LinkedHashMap<String, ResType>();

    static {
        ATTR_TYPE_MAP.put("boolean", ResType.BOOLEAN);
        ATTR_TYPE_MAP.put("color", ResType.COLOR);
        ATTR_TYPE_MAP.put("dimension", ResType.DIMEN);
        ATTR_TYPE_MAP.put("float", ResType.FLOAT);
        ATTR_TYPE_MAP.put("integer", ResType.INTEGER);
        ATTR_TYPE_MAP.put("string", ResType.CHAR_SEQUENCE);
    }

    public static void convertAndFill(Attribute attribute, Resources resources, TypedValue outValue) {
        if (attribute == null || attribute.isNull()) {
            outValue.type = TypedValue.TYPE_NULL;
            return;
        }

        ResourceLoader resourceLoader = shadowOf(resources).getResourceLoader();
        String qualifiers = shadowOf(resources.getAssets()).getQualifiers();
        TypedResource attrTypeData = resourceLoader.getValue(attribute.resName, qualifiers);
        if (attrTypeData != null) {
            convertAndFill(attribute.value, attribute.contextPackageName, resourceLoader, outValue, (AttrData) attrTypeData.getData(), qualifiers);
        }
    }

    public static void convertAndFill(String value, String contextPackageName, ResourceLoader resourceLoader, TypedValue outValue, AttrData attrData, String qualifiers) {
        String format = attrData.getFormat();
        String[] types = format.split("\\|");
        for (String type : types) {
            if (value.startsWith("@") && "reference".equals(type)) {
                ResName resName = ResName.qualifyResName(value.substring(1).replace("+", ""), contextPackageName, null);
                Integer resourceId = resourceLoader.getResourceIndex().getResourceId(resName);
                if (resourceId == null) {
                    throw new Resources.NotFoundException("unknown resource " + resName);
                }

                // wtf. color references reference are all kinds of stupid.
                TypedResource dereferencedRef = resourceLoader.getValue(resName, qualifiers);
                if (dereferencedRef != null && dereferencedRef.getResType().equals(ResType.COLOR_STATE_LIST)) {
                    outValue.string = dereferencedRef.asString();
                    outValue.type = TypedValue.TYPE_STRING;
                } else if (dereferencedRef != null && dereferencedRef.getResType().equals(ResType.COLOR)) {
                    outValue.data = Color.parseColor(dereferencedRef.asString());
                    outValue.type = TypedValue.TYPE_FIRST_COLOR_INT;
                } else {
                    outValue.string = resName.getFullyQualifiedName();
                    outValue.type = TypedValue.TYPE_REFERENCE;
                    outValue.resourceId = resourceId;
                }
                return;
            }

            Converter converter = ATTR_TYPE_MAP.containsKey(type)
                    ? getConverter(ATTR_TYPE_MAP.get(type))
                    : null;

            if (converter == null) {
                if (format.equals("enum")) {
                    converter = new EnumConverter(attrData);
                } else if (format.equals("flag")) {
                    converter = new FlagConverter(attrData);
                }
            }

            if (converter != null) {
              try {
                  converter.fillTypedValue(value, outValue);
              } catch (Exception e) {
                throw new RuntimeException("error converting " + value + " using " + converter.getClass().getSimpleName(), e);
              }
              return;
            }
        }
    }

    public static Converter getConverter(ResType resType) {
        switch (resType) {
            case ATTR_DATA:
                return new FromAttrData();
            case BOOLEAN:
                return new FromBoolean();
            case CHAR_SEQUENCE:
                return new FromCharSequence();
            case COLOR:
                return new FromColor();
            case COLOR_STATE_LIST:
                return new FromColorStateList();
            case DIMEN:
                return new FromDimen();
            case INTEGER:
                return new FromNumeric();
            case CHAR_SEQUENCE_ARRAY:
            case INTEGER_ARRAY:
                return new FromArray();
            default:
                throw new UnsupportedOperationException(resType.name());
        }
    }

    public CharSequence asCharSequence(TypedResource typedResource) {
        throw cantDo("asCharSequence");
    }

    public int asInt(TypedResource typedResource) {
        throw cantDo("asInt");
    }

    public TypedResource[] getItems(TypedResource typedResource) {
        throw cantDo("getItems");
    }

    public void fillTypedValue(T data, TypedValue typedValue) {
        throw cantDo("fillTypedValue");
    }

    private UnsupportedOperationException cantDo(String operation) {
        return new UnsupportedOperationException(getClass().getName() + " doesn't support " + operation);
    }

    public static class FromAttrData extends Converter<AttrData> {
        @Override public CharSequence asCharSequence(TypedResource typedResource) {
            return typedResource.asString();
        }

        @Override public void fillTypedValue(AttrData data, TypedValue typedValue) {
            typedValue.type = TypedValue.TYPE_STRING;
        }
    }

    public static class FromCharSequence extends Converter<String> {
        @Override public CharSequence asCharSequence(TypedResource typedResource) {
            return typedResource.asString();
        }

        @Override public int asInt(TypedResource typedResource) {
            String rawValue = typedResource.asString();
            return convertInt(rawValue);
          }

        @Override public void fillTypedValue(String data, TypedValue typedValue) {
            typedValue.type = TypedValue.TYPE_STRING;
            typedValue.string = data;
        }
    }

    public static class FromColor extends Converter<String> {
        @Override public void fillTypedValue(String data, TypedValue typedValue) {
            typedValue.type = TypedValue.TYPE_INT_COLOR_ARGB8;
            typedValue.data = Color.parseColor(data);
        }
    }

    private static class FromColorStateList extends Converter<String> {
        @Override public void fillTypedValue(String data, TypedValue typedValue) {
            typedValue.type = TypedValue.TYPE_STRING;
            typedValue.string = data;
        }
    }

    public static class FromArray extends Converter {
        @Override public TypedResource[] getItems(TypedResource typedResource) {
            return (TypedResource[]) typedResource.getData();
        }
    }

    private static class FromNumeric extends Converter<String> {
        @Override public void fillTypedValue(String data, TypedValue typedValue) {
            typedValue.type = TypedValue.TYPE_INT_HEX;
            typedValue.data = convertInt(data);
        }

        @Override public int asInt(TypedResource typedResource) {
            String rawValue = typedResource.asString();
            return convertInt(rawValue);
        }
    }

    private static class FromBoolean extends Converter<String> {
        @Override public void fillTypedValue(String data, TypedValue typedValue) {
            typedValue.type = TypedValue.TYPE_INT_BOOLEAN;
            typedValue.data = convertBool(data) ? 1 : 0;
        }

    }

    private static class FromDimen extends Converter<String> {
        @Override public void fillTypedValue(String data, TypedValue typedValue) {
            ResourceHelper.parseFloatAttribute(null, data, typedValue, false);
        }
    }

    ///////////////////////

    private static int convertInt(String rawValue) {
        try {
            // Decode into long, because there are some large hex values in the android resource files
            // (e.g. config_notificationsBatteryLowARGB = 0xFFFF0000 in sdk 14).
            // Integer.decode() does not support large, i.e. negative values in hex numbers.
            // try parsing decimal number
            return (int) Long.parseLong(rawValue);
        } catch (NumberFormatException nfe) {
            // try parsing hex number
            try {
                return Long.decode(rawValue).intValue();
            } catch (NumberFormatException e) {
                throw new RuntimeException(rawValue + " is not an integer.", nfe);
            }
        }
    }

    private static boolean convertBool(String rawValue) {
        if ("true".equalsIgnoreCase(rawValue)) {
            return true;
        } else if ("false".equalsIgnoreCase(rawValue)) {
            return false;
        }

        int intValue = Integer.parseInt(rawValue);
        if (intValue == 0) {
            return false;
        }
        return true;
    }

    private static class EnumConverter extends EnumOrFlagConverter {
        public EnumConverter(AttrData attrData) {
            super(attrData);
        }

        @Override public void fillTypedValue(String data, TypedValue typedValue) {
            typedValue.type = TypedValue.TYPE_INT_HEX;
            typedValue.data = findValueFor(data);
        }
    }

    private static class FlagConverter extends EnumOrFlagConverter {
        public FlagConverter(AttrData attrData) {
            super(attrData);
        }

        @Override public void fillTypedValue(String data, TypedValue typedValue) {
            int flags = 0;
            for (String key : data.split("\\|")) {
                flags |= findValueFor(key);
            }

            typedValue.type = TypedValue.TYPE_INT_HEX;
            typedValue.data = flags;
        }
    }

    private static class EnumOrFlagConverter extends Converter<String> {
        private final AttrData attrData;

        public EnumOrFlagConverter(AttrData attrData) {
            this.attrData = attrData;
        }

        protected int findValueFor(String key) {
            String valueFor = attrData.getValueFor(key);
            if (valueFor == null) {
                throw new RuntimeException("no value found for " + key);
            }
            return Util.parseInt(valueFor);
        }
    }
}
