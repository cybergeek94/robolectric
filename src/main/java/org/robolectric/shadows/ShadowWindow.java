package org.robolectric.shadows;

import android.app.Activity;
import android.view.Window;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;
import org.robolectric.tester.android.view.RoboWindow;

import static org.robolectric.Robolectric.directlyOn;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = Window.class)
public class ShadowWindow {
    @RealObject private Window realWindow;

    private int flags;
//    private Context context;

    public static Window create(Activity activity) {
        return new RoboWindow(activity.getBaseContext());
    }

//    public void __constructor__(android.content.Context context) {
//        this.context = context;
//    }
//
//    @Implementation
//    public Context getContext() {
//        return context;
//    }
//
//    @Implementation
//    public WindowManager.LayoutParams getAttributes() {
//        return new WindowManager.LayoutParams();
//    }

    @Implementation
    public void setFlags(int flags, int mask) {
        this.flags = (this.flags & ~mask) | (flags & mask);
        directlyOn(realWindow, Window.class).setFlags(flags, mask);
    }

    public boolean getFlag(int flag) {
        return (flags & flag) == flag;
    }

    public void performLayout() {
        ((RoboWindow) realWindow).performLayout();
    }
}
