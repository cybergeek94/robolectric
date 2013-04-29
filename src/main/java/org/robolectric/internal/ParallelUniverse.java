package org.robolectric.internal;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import org.robolectric.AndroidManifest;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.TestLifecycle;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.builder.RobolectricPackageManager;
import org.robolectric.shadows.ShadowContextImpl;
import org.robolectric.shadows.ShadowResources;
import org.robolectric.util.DatabaseConfig;

import java.lang.reflect.Method;

import static org.fest.reflect.core.Reflection.*;
import static org.robolectric.Robolectric.shadowOf;

public class ParallelUniverse implements ParallelUniverseInterface {
    private Class<?> contextImplClass;

    public void resetStaticState() {
        Robolectric.reset();
    }

    @Override public void setDatabaseMap(DatabaseConfig.DatabaseMap databaseMap) {
        DatabaseConfig.setDatabaseMap(databaseMap);
    }

    @Override public void setUpApplicationState(Method method, TestLifecycle testLifecycle, boolean strictI18n, ResourceLoader systemResourceLoader, AndroidManifest appManifest) {
        Robolectric.application = null;
        Robolectric.packageManager = new RobolectricPackageManager();
        if (appManifest != null) {
            Robolectric.packageManager.addManifest(appManifest);
        }

        ShadowResources.setSystemResources(systemResourceLoader);
        String qualifiers = RobolectricTestRunner.determineResourceQualifiers(method);
        Resources systemResources = Resources.getSystem();
        Configuration configuration = systemResources.getConfiguration();
        shadowOf(configuration).overrideQualifiers(qualifiers);
        systemResources.updateConfiguration(configuration, systemResources.getDisplayMetrics());

        contextImplClass = type(ShadowContextImpl.CLASS_NAME)
                .withClassLoader(getClass().getClassLoader())
                .load();

        Class<?> activityThreadClass = type("android.app.ActivityThread")
                .withClassLoader(getClass().getClassLoader())
                .load();

        Object activityThread = constructor()
                .in(activityThreadClass)
                .newInstance();

        ResourceLoader resourceLoader = null;
        if (appManifest != null) {
            resourceLoader = RobolectricTestRunner.getAppResourceLoader(systemResourceLoader, appManifest);
        }

        Context systemContextImpl = (Context) method("createSystemContext")
                .withReturnType(contextImplClass)
                .withParameterTypes(activityThreadClass)
                .in(contextImplClass)
                .invoke(activityThread);

        final Application application = (Application) testLifecycle.createApplication(method, appManifest);
        if (application != null) {
            ApplicationInfo applicationInfo;
            if (appManifest == null) {
                applicationInfo = new ApplicationInfo();
                applicationInfo.packageName = "some.package.name";
            } else {
                try {
                    applicationInfo = Robolectric.packageManager.getApplicationInfo(appManifest.getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            Class<?> compatibilityInfoClass = type("android.content.res.CompatibilityInfo").load();
            Object loadedApk = method("getPackageInfo")
                    .withParameterTypes(ApplicationInfo.class, compatibilityInfoClass, ClassLoader.class, boolean.class, boolean.class)
                    .in(activityThread)
                    .invoke(applicationInfo, null, getClass().getClassLoader(), false, true);
            System.out.println("loadedApk = " + loadedApk);

            Context contextImpl = method("createPackageContext")
                    .withReturnType(Context.class)
                    .withParameterTypes(String.class, int.class) // packageName, flags
                    .in(systemContextImpl)
                    .invoke(applicationInfo.packageName, Context.CONTEXT_INCLUDE_CODE);

            method("attach")
                    .withParameterTypes(Context.class)
                    .in(application)
                    .invoke(contextImpl);

            shadowOf(application).bind(appManifest, resourceLoader);
            Resources resources = application.getResources();
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            shadowOf(application).setStrictI18n(strictI18n);

            Robolectric.application = application;
            application.onCreate();
        }
    }

    @Override public void tearDownApplication() {
        if (Robolectric.application != null) {
            Robolectric.application.onTerminate();
        }
    }

    @Override public Object getCurrentApplication() {
        return Robolectric.application;
    }
}
