package org.robolectric.shadows;

import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.LongSparseArray;
import android.util.TypedValue;
import android.view.Display;
import org.jetbrains.annotations.NotNull;
import org.robolectric.Robolectric;
import org.robolectric.internal.HiddenApi;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;
import org.robolectric.res.Attribute;
import org.robolectric.res.DrawableNode;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.res.Plural;
import org.robolectric.res.ResName;
import org.robolectric.res.ResType;
import org.robolectric.res.ResourceIndex;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.Style;
import org.robolectric.res.TypedResource;
import org.robolectric.res.XmlFileLoader;
import org.robolectric.res.builder.DrawableBuilder;
import org.robolectric.res.builder.XmlFileBuilder;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.fest.reflect.core.Reflection.field;
import static org.robolectric.Robolectric.directlyOn;
import static org.robolectric.Robolectric.shadowOf;

/**
 * Shadow of {@code Resources} that simulates the loading of resources
 *
 * @see org.robolectric.RobolectricTestRunner#RobolectricTestRunner(Class)
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Resources.class)
public class ShadowResources {
    private static Resources system = null;

    private float density = 1.0f;
    private DisplayMetrics displayMetrics;
    private Display display;
    @RealObject Resources realResources;
    private ResourceLoader resourceLoader;

    public static void reset() {
        for (Field field : Resources.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType().equals(LongSparseArray.class)) {
                try {
                    field.setAccessible(true);
                    LongSparseArray longSparseArray = (LongSparseArray) field.get(null);
                    if (longSparseArray != null) {
                        longSparseArray.clear();
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void setSystemResources(ResourceLoader systemResourceLoader) {
        AssetManager assetManager = Robolectric.newInstanceOf(AssetManager.class);
        ShadowAssetManager.bind(assetManager, null, systemResourceLoader);
        DisplayMetrics metrics = new DisplayMetrics();
        Configuration config = new Configuration();
        system = ShadowResources.bind(new Resources(assetManager, metrics, config), systemResourceLoader);
    }

    static Resources bind(Resources resources, ResourceLoader resourceLoader) {
        ShadowResources shadowResources = shadowOf(resources);
        if (shadowResources.resourceLoader != null) throw new RuntimeException("ResourceLoader already set!");
        shadowResources.resourceLoader = resourceLoader;
        return resources;
    }

    @Implementation
    public static Resources getSystem() {
        return system;
    }

    private TypedArray attrsToTypedArray(AttributeSet set, int[] attrs, int defStyleAttr, int themeResourceId) {
            /*
             * When determining the final value of a particular attribute, there are four inputs that come into play:
             *
             * 1. Any attribute values in the given AttributeSet.
             * 2. The style resource specified in the AttributeSet (named "style").
             * 3. The default style specified by defStyleAttr and defStyleRes
             * 4. The base values in this theme.
             */
        ResourceLoader resourceLoader = getResourceLoader();
        ShadowAssetManager shadowAssetManager = shadowOf(realResources.getAssets());
        String qualifiers = shadowAssetManager.getQualifiers();

        if (set == null) {
            set = new RoboAttributeSet(new ArrayList<Attribute>(), realResources, null);
        }
        Style defStyle = null;
        Style theme = null;

        if (defStyleAttr == 0) {
            defStyle = null;
        } else if (themeResourceId != -1) {
            // Load the style for the theme we represent. E.g. "@style/Theme.Robolectric"
            ResName themeStyleName = tryResName(themeResourceId);
            System.out.println("themeStyleName = " + themeStyleName);

            theme = ShadowAssetManager.resolveStyle(resourceLoader, themeStyleName, shadowAssetManager.getQualifiers());

            // Load the theme attribute for the default style attributes. E.g., attr/buttonStyle
            ResName defStyleName = tryResName(defStyleAttr);

            // Load the style for the default style attribute. E.g. "@style/Widget.Robolectric.Button";
            Attribute defStyleAttribute = theme.getAttrValue(defStyleName);
            while (defStyleAttribute.isStyleReference()) {
                defStyleAttribute = theme.getAttrValue(defStyleAttribute.getStyleReference());
                // todo
                System.out.println("TODO: Not handling " + defStyleAttribute + " yet in ShadowResouces!");
            }

            if (defStyleAttribute.isResourceReference()) {
                ResName defStyleResName = defStyleAttribute.getResourceReference();
                defStyle = ShadowAssetManager.resolveStyle(resourceLoader, defStyleResName, shadowAssetManager.getQualifiers());
            }
        }

        List<Attribute> attributes = new ArrayList<Attribute>();
        for (int attr : attrs) {
            ResName attrName = tryResName(attr); // todo probably getResName instead here?
//                System.out.println("index " + i + " has " + attrName);
            if (attrName == null) continue;

            Attribute attribute = findAttributeValue(attrName, set, defStyle, theme);
            while (attribute != null && attribute.isStyleReference()) {
                ResName otherAttrName = attribute.getStyleReference();
                if (theme == null) throw new RuntimeException("no theme, but trying to look up " + otherAttrName);
                attribute = theme.getAttrValue(otherAttrName);
                if (attribute != null) {
                    attribute = new Attribute(attrName, attribute.value, attribute.contextPackageName);
                }
            }

            if (attribute != null) {
                Attribute.put(attributes, attribute);
            }
        }

        return ShadowTypedArray.create(realResources, attributes, attrs);
    }

    private Attribute findAttributeValue(ResName attrName, AttributeSet attributeSet, Style defStyle, Style theme) {
        String attrValue = attributeSet.getAttributeValue(attrName.getNamespaceUri(), attrName.name);
        if (attrValue != null) {
            System.out.println("Got " + attrName + " from attr: " + attrValue);
            return new Attribute(attrName, attrValue, "fixme!!!");
        }

        // todo: check for style attribute...

        // else if attr in defStyle, use its value
        if (defStyle != null) {
            Attribute attribute = defStyle.getAttrValue(attrName);
            if (attribute != null) {
                System.out.println("Got " + attrName + " from defStyle: " + attribute);
                return attribute;
            }
        }

        // else if attr in theme, use its value
        if (theme != null) {
            Attribute attribute = theme.getAttrValue(attrName);
            if (attribute != null) {
                System.out.println("Got " + attrName + " from theme: " + attrValue);
                return attribute;
            }
        }

        return null;

        // if attr in attribute set, use its value
        // TODO look for attrvalue in attriburte set

        // else if attr in defStyle, use its value
        //                if (defStyle != null) {
        //                    attrValue = defStyle.getAttrValue(attrName);
        //                }
    }

    @Implementation
    public TypedArray obtainAttributes(AttributeSet set, int[] attrs) {
        return attrsToTypedArray(set, attrs, -1, -1);
    }

    @Implementation
    public void updateConfiguration(Configuration config, DisplayMetrics metrics) {
        shadowOf(realResources.getAssets()).setQualifiers(shadowOf(config).getQualifiers());
        directlyOn(realResources, Resources.class).updateConfiguration(config, metrics);
    }

//    todo: should implement this:
//    @Implementation
//    public void updateConfiguration(Configuration config, DisplayMetrics metrics, CompatibilityInfo compat) {
//    }

    @Implementation
    public int getIdentifier(String name, String defType, String defPackage) {
        ResourceIndex resourceIndex = resourceLoader.getResourceIndex();
        ResName resName = ResName.qualifyResName(name, defPackage, defType);
        Integer resourceId = resourceIndex.getResourceId(resName);
        if (resourceId == null) return 0;
        return resourceId;
    }

    @Implementation
    public String getResourceName(int resId) throws Resources.NotFoundException {
        return getResName(resId).getFullyQualifiedName();
    }

    @Implementation
    public String getResourcePackageName(int resId) throws Resources.NotFoundException {
        return getResName(resId).namespace;
    }

    @Implementation
    public String getResourceTypeName(int resId) throws Resources.NotFoundException {
        return getResName(resId).type;
    }

    @Implementation
    public String getResourceEntryName(int resId) throws Resources.NotFoundException {
        return getResName(resId).name;
    }

    private boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    private @NotNull ResName getResName(int id) {
        ResName resName = resourceLoader.getResourceIndex().getResName(id);
        if (resName == null) {
            throw new Resources.NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(id));
        }
        return resName;
    }

    private ResName tryResName(int id) {
        return resourceLoader.getResourceIndex().getResName(id);
    }

    private String getQualifiers() {
        return shadowOf(realResources.getConfiguration()).getQualifiers();
    }

//    @Implementation
//    public ColorStateList getColorStateList(int id) {
//        String colorValue = resourceLoader.getColorValue(tryResName(id), getQualifiers());
//        if (colorValue != null) {
//            return ColorStateList.valueOf(Color.parseColor(colorValue));
//        } else {
//            return new ColorStateList(new int[0][0], new int[0]);
//        }
//    }

    @Implementation
    public String getQuantityString(int id, int quantity, Object... formatArgs) throws Resources.NotFoundException {
        String raw = getQuantityString(id, quantity);
        return String.format(Locale.ENGLISH, raw, formatArgs);
    }

    @Implementation
    public String getQuantityString(int id, int quantity) throws Resources.NotFoundException {
        ResName resName = getResName(id);
        Plural plural = resourceLoader.getPlural(resName, quantity, getQualifiers());
        String string = plural.getString();
        ShadowAssetManager shadowAssetManager = shadowOf(realResources.getAssets());
        TypedResource typedResource = shadowAssetManager.resolve(new TypedResource(string, ResType.CHAR_SEQUENCE), getQualifiers(),
                new ResName(resName.namespace, "string", resName.name));
        return typedResource == null ? null : typedResource.asString();
    }

    @Implementation
    public InputStream openRawResource(int id) throws Resources.NotFoundException {
        return resourceLoader.getRawValue(getResName(id));
    }

    public void setDensity(float density) {
        this.density = density;
        if (displayMetrics != null) {
            displayMetrics.density = density;
        }
    }

    public void setDisplay(Display display) {
        this.display = display;
        displayMetrics = null;
    }

    @Implementation
    public DisplayMetrics getDisplayMetrics() {
        if (displayMetrics == null) {
            if (display == null) {
                display = Robolectric.newInstanceOf(Display.class);
            }

            displayMetrics = new DisplayMetrics();
            display.getMetrics(displayMetrics);
        }
        displayMetrics.density = this.density;
        return displayMetrics;
    }

    @Implementation
    public Drawable getDrawable(int drawableResourceId) throws Resources.NotFoundException {
        ResName resName = getResName(drawableResourceId);
        String qualifiers = getQualifiers();
        DrawableNode drawableNode = resourceLoader.getDrawableNode(resName, qualifiers);
        final DrawableBuilder drawableBuilder = new DrawableBuilder(getResourceLoader().getResourceIndex());
        return drawableBuilder.getDrawable(resName, realResources, drawableNode);
    }

    private static final String[] UNITS = {"dp", "dip", "pt", "px", "sp"};

    Float temporaryDimenConverter(String rawValue) {
        int end = rawValue.length();
        for (String unit : UNITS) {
            int index = rawValue.indexOf(unit);
            if (index >= 0 && end > index) {
                end = index;
            }
        }

        return Float.parseFloat(rawValue.substring(0, end));
    }

    @Implementation
    public XmlResourceParser getXml(int id) throws Resources.NotFoundException {
        ResName resName = getResName(id);
        Document document = resourceLoader.getXml(resName, getQualifiers());
        if (document == null) {
            throw new Resources.NotFoundException();
        }
        return new XmlFileBuilder().getXml(document, resName.getFullyQualifiedName(), resName.namespace, realResources);
    }

    @HiddenApi @Implementation
    public XmlResourceParser loadXmlResourceParser(String file, int id, int assetCookie, String type) throws Resources.NotFoundException {
        FsFile fsFile = Fs.fileFromPath(file);
        Document document = new XmlFileLoader(null).parse(fsFile);
        if (document == null) {
            throw new Resources.NotFoundException(notFound(id));
        }
        String packageName = getResName(id).namespace;
        return new XmlFileBuilder().getXml(document, fsFile.getPath(), packageName, realResources);
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    private String notFound(int id) {
        return "couldn't find resource " + getResName(id).getFullyQualifiedName();
    }

    @Implements(Resources.Theme.class)
    public static class ShadowTheme {
        @RealObject Resources.Theme realTheme;
        protected Resources resources;
        private int styleResourceId;

        @Implementation
        public void applyStyle(int resid, boolean force) {
            this.styleResourceId = resid;
        }

        @Implementation
        public void setTo(Resources.Theme other) {
            this.styleResourceId = shadowOf(other).styleResourceId;
        }

        public int getStyleResourceId() {
            return styleResourceId;
        }

        @Implementation
        public TypedArray obtainStyledAttributes(int[] attrs) {
            return obtainStyledAttributes(0, attrs);
        }

        @Implementation
        public TypedArray obtainStyledAttributes(int resid, int[] attrs) throws android.content.res.Resources.NotFoundException {
            return obtainStyledAttributes(null, attrs, 0, 0);
        }

        @Implementation
        public TypedArray obtainStyledAttributes(AttributeSet set, int[] attrs, int defStyleAttr, int defStyleRes) {
            return shadowOf(getResources()).attrsToTypedArray(set, attrs, defStyleAttr, this.styleResourceId);
        }

        Resources getResources() {
            // ugh
            return field("this$0").ofType(Resources.class).in(realTheme).get();
        }
    }

    @Implementation
    public final Resources.Theme newTheme() {
        Resources.Theme theme = directlyOn(realResources, Resources.class).newTheme();
        int themeId = field("mTheme").ofType(int.class).in(theme).get();
        shadowOf(realResources.getAssets()).setTheme(themeId, theme);
        return theme;
    }

    @Implementation
    public Drawable loadDrawable(TypedValue value, int id) {
        Drawable drawable = (Drawable) directlyOn(realResources, Resources.class, "loadDrawable", TypedValue.class, int.class).invoke(value, id);
        // todo: this kinda sucks, find some better way...
        if (drawable != null) {
            shadowOf(drawable).setLoadedFromResourceId(id);
            if (drawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                if (bitmap != null && shadowOf(bitmap).createdFromResId == -1) {
                    shadowOf(bitmap).createdFromResId = id;
                }
            }
        }
        return drawable;
    }

    public static <T> T inject(Resources resources, T instance) {
        Object shadow = Robolectric.shadowOf_(instance);
        if (shadow instanceof UsesResources) {
            ((UsesResources) shadow).injectResources(resources);
        }
        return instance;
    }

    @Implements(Resources.NotFoundException.class)
    public static class ShadowNotFoundException {
        @RealObject Resources.NotFoundException realObject;

        private String message;

        public void __constructor__() {
        }

        public void __constructor__(String name) {
            this.message = name;
        }

        @Implementation
        public String toString() {
            return realObject.getClass().getName() + ": " + message;
        }
    }
}
