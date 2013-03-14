package org.robolectric.shadows;

import android.widget.CheckedTextView;
import org.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = CheckedTextView.class)
public class ShadowCheckedTextView extends ShadowTextView {
//    @RealObject CheckedTextView realCheckedTextView;
//    private boolean checked;
//
//    @Implementation
//    public void setChecked(boolean checked) {
//        this.checked = checked;
//    }
//
//    @Implementation
//    public boolean isChecked() {
//        return checked;
//    }
//
//    @Implementation
//    public boolean performClick() {
//        realCheckedTextView.toggle();
//        return realView.performClick();
//    }
//
//    @Implementation
//    public void toggle() {
//        realCheckedTextView.setChecked(!realCheckedTextView.isChecked());
//    }
}
