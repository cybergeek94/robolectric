package org.robolectric.shadows;

import android.widget.Gallery;
import org.robolectric.internal.Implements;

@Implements(value = Gallery.class)
public class ShadowGallery extends ShadowAbsSpinner {

//    @RealObject Gallery gallery;
//
//    @Implementation
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_DPAD_RIGHT:
//            case KeyEvent.KEYCODE_DPAD_LEFT:
//                if (onKeyListener != null) {
//                    onKeyListener.onKey(gallery, keyCode, event);
//                }
//                return true;
//        }
//        return false;
//    }

}
