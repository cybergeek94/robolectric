package org.robolectric.shadows;

import android.text.SpannableStringBuilder;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

@Implements(SpannableStringBuilder.class)
public class ShadowSpannableStringBuilder {
    @RealObject SpannableStringBuilder realSpannableStringBuilder;

    // this sucks because while ssb.equals(equivalentString) is true, equivalentString.equals(ssb) is not! sorry. [xw]
    @Override public boolean equals(Object obj) {
        // todo: we should check that the spans match too...
        return realSpannableStringBuilder.toString().equals(obj.toString());
    }

    @Override public int hashCode() {
        return realSpannableStringBuilder.toString().hashCode();
    }
}
