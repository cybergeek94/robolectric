package org.robolectric.shadows;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.widget.Button;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ThemeTest {
    @Test public void testWhatever() throws Exception {
        putApplicationInRightState();
        TestActivity activity = new TestActivity();
        shadowOf(activity).callAttachBaseContext(Robolectric.application);
        activity.setTheme(R.style.Theme_Robolectric);
        shadowOf(activity).callOnCreate(null);
        Button theButton = (Button) activity.findViewById(R.id.button);
        assertThat(theButton.getBackground()).isEqualTo(new ColorDrawable(0xffff0000));
    }

    private void putApplicationInRightState() {
        ContextThemeWrapper context = new ContextThemeWrapper() {
            @Override public Resources.Theme getTheme() {
                return Robolectric.getShadowApplication().getResources().newTheme();
            }
        };
        shadowOf(Robolectric.application).callAttachBaseContext(context);
    }

    public static class TestActivity extends Activity {
        @Override protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.styles_button_layout);
        }
    }
}
