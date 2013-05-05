package org.robolectric.util;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowActivityThread;

import static org.fest.reflect.core.Reflection.*;
import static org.robolectric.Robolectric.shadowOf_;

public class ActivityController<T> {
    private final T activity;
    private final ShadowActivity shadowActivity;

    private Application application;
    private Context baseContext;
    private Intent intent;

    private boolean attached;

    public ActivityController(Class<T> activityClass) {
        this.activity = constructor().in(activityClass).newInstance();
        shadowActivity = shadowOf_(activity);
    }

    public ActivityController(T activity) {
        this.activity = activity;
        shadowActivity = shadowOf_(activity);
        attached = true;
    }

    public T get() {
        return activity;
    }

    public ActivityController<T> withApplication(Application application) {
        this.application = application;
        return this;
    }

    public ActivityController<T> withBaseContext(Context baseContext) {
        this.baseContext = baseContext;
        return this;
    }

    public ActivityController<T> withIntent(Intent intent) {
        this.intent = intent;
        return this;
    }

    public ActivityController<T> attach() {
        Application application = this.application == null ? Robolectric.application : this.application;
        Context baseContext = this.baseContext == null ? application : this.baseContext;
        Intent intent = this.intent == null ? createIntent() : this.intent;
        ActivityInfo activityInfo = new ActivityInfo();

        ClassLoader cl = baseContext.getClassLoader();
        Class<?> activityThreadClass = type(ShadowActivityThread.CLASS_NAME).withClassLoader(cl).load();
        Class<?> nonConfigurationInstancesClass = type("android.app.Activity$NonConfigurationInstances")
                .withClassLoader(cl).load();

        method("attach").withParameterTypes(
                Context.class /* context */, activityThreadClass /* aThread */,
                Instrumentation.class /* instr */, IBinder.class /* token */, int.class /* ident */,
                Application.class /* application */, Intent.class /* intent */, ActivityInfo.class /* info */,
                CharSequence.class /* title */, Activity.class /* parent */, String.class /* id */,
                nonConfigurationInstancesClass /* lastNonConfigurationInstances */,
                Configuration.class /* config */
        ).in(activity).invoke(baseContext, null /* aThread */,
                null /* instr */, null /* token */, 0 /* ident */,
                application, intent /* intent */, activityInfo,
                "title", null /* parent */, "id",
                null /* lastNonConfigurationInstances */,
                application.getResources().getConfiguration());

        attached = true;

        return this;
    }

    private Intent createIntent() {
        return Robolectric.packageManager.getLaunchIntentForPackage(Robolectric.application.getPackageName());
    }

    public ActivityController<T> create(Bundle bundle) {
        if (!attached) attach();

        method("performCreate").withParameterTypes(Bundle.class).in(activity).invoke(bundle);
        return this;
    }

    public ActivityController<T> create() {
        return create(null);
    }

    public ActivityController<T> restoreInstanceState(Bundle bundle) {
        method("performRestoreInstanceState").withParameterTypes(Bundle.class).in(activity).invoke(bundle);
        return this;
    }

    public ActivityController<T> postCreate(Bundle bundle) {
        shadowActivity.callOnPostCreate(bundle);
        return this;
    }

    public ActivityController<T> start() {
        method("performStart").in(activity).invoke();
        return this;
    }

    public ActivityController<T> restart() {
        method("performRestart").in(activity).invoke();
        return this;
    }

    public ActivityController<T> resume() {
        method("performResume").in(activity).invoke();
        return this;
    }

    public ActivityController<T> postResume() {
        shadowActivity.callOnPostResume();
        return this;
    }

    public ActivityController<T> newIntent(android.content.Intent intent) {
        shadowActivity.callOnNewIntent(intent);
        return this;
    }

    public ActivityController<T> saveInstanceState(android.os.Bundle outState) {
        method("performSaveInstanceState").withParameterTypes(Bundle.class).in(activity).invoke(outState);
        return this;
    }

    public ActivityController<T> pause() {
        method("performPause").in(activity).invoke();
        return this;
    }

    public ActivityController<T> userLeaving() {
        method("performUserLeaving").in(activity).invoke();
        return this;
    }

    public ActivityController<T> stop() {
        method("performStop").in(activity).invoke();
        return this;
    }

    public ActivityController<T> destroy() {
        method("performDestroy").in(activity).invoke();
        return this;
    }
}
