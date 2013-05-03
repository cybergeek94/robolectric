package org.robolectric.shadows;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
    @Test public void whenExplicitlySetOnActivity_activityGetsThemeFromActivityInManifest() throws Exception {
        TestActivity activity = new TestActivity();
        activity.setTheme(R.style.Theme_Robolectric);
        shadowOf(activity).callOnCreate(null);
        System.out.println("theme: " + shadowOf(activity).callGetThemeResId());
        Button theButton = (Button) activity.findViewById(R.id.button);
        assertThat(theButton.getBackground()).isEqualTo(new ColorDrawable(0xff00ff00));
    }

    @Test public void whenSetOnActivityInManifest_activityGetsThemeFromActivityInManifest() throws Exception {
        TestActivity activity = Robolectric.buildActivity(TestActivity.class).create().get();
        System.out.println("theme: " + shadowOf(activity).callGetThemeResId());
        Button theButton = (Button) activity.findViewById(R.id.button);
        assertThat(theButton.getBackground()).isEqualTo(new ColorDrawable(0xff00ff00));
    }

    @Test public void whenNotSetOnActivityInManifest_activityGetsThemeFromApplicationInManifest() throws Exception {
        TestActivity activity = Robolectric.buildActivity(TestActivity.class).create().get();
        System.out.println("theme: " + shadowOf(activity).callGetThemeResId());
        Button theButton = (Button) activity.findViewById(R.id.button);
        assertThat(theButton.getBackground()).isEqualTo(new ColorDrawable(0xffff0000));
    }

    public static class TestActivity extends Activity {
        @Override protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.styles_button_layout);
        }
    }
}
