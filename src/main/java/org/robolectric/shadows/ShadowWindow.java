package org.robolectric.shadows;

import android.content.Context;
import android.view.Window;
import android.view.WindowManager;
import org.robolectric.Robolectric;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;
import org.robolectric.tester.android.view.RoboWindow;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = Window.class, callThroughByDefault = false)
public class ShadowWindow {
    @RealObject private Window window;

    private int flags;
    private Context context;

    public static Window create() {
        return new RoboWindow(Robolectric.application);
    }

    public void __constructor__(android.content.Context context) {
        this.context = context;
    }

    @Implementation
    public Context getContext() {
        return context;
    }

    @Implementation
    public WindowManager.LayoutParams getAttributes() {
        return new WindowManager.LayoutParams();
    }

    @Implementation
    public void setFlags(int flags, int mask) {
        this.flags = (this.flags & ~mask) | (flags & mask);
    }

    public boolean getFlag(int flag) {
        return (flags & flag) == flag;
    }

    public void performLayout() {
        ((RoboWindow) window).performLayout();
    }
}
