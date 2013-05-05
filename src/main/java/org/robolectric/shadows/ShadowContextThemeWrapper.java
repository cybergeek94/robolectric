package org.robolectric.shadows;

import android.view.ContextThemeWrapper;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

import static org.fest.reflect.core.Reflection.method;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = ContextThemeWrapper.class)
public class ShadowContextThemeWrapper extends ShadowContextWrapper {
    @RealObject private ContextThemeWrapper realContextThemeWrapper;

//    @Implementation
//    public void setTheme(int resid) {
//        directlyOn(realContextThemeWrapper, ContextThemeWrapper.class).setTheme(resid);
//        realContextThemeWrapper.getTheme()
//    }

    public Integer callGetThemeResId() {
        return method("getThemeResId").withReturnType(int.class).in(realContextThemeWrapper).invoke();
    }
}