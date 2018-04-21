package fei_ke.com.dualappmod;

import android.content.ContentProvider;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;

public class XposedMod implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("com.samsung.android.da.daagent")) {
            final Class classDualAppProvider = findClass("com.samsung.android.da.daagent.provider.DualAppProvider", lpparam.classLoader);
            findAndHookMethod(classDualAppProvider, "query", Uri.class, String[].class, String.class, String[].class, String.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Uri uri = (Uri) param.args[0];
                            UriMatcher sUriMatcher = (UriMatcher) getStaticObjectField(classDualAppProvider, "sUriMatcher");
                            int match = sUriMatcher.match(uri);
                            if (match == 3 || (match != 2 && match != 3 && match != 4 && match != 5)) {
                                MatrixCursor cursor = (MatrixCursor) param.getResult();
                                ContentProvider thisObject = (ContentProvider) param.thisObject;
                                ArrayList<String> pkgs = getAllInstalledPkg(thisObject.getContext());
                                for (String pkg : pkgs) {
                                    cursor.addRow(new String[]{pkg});
                                }
                                param.setResult(cursor);
                            }
                        }
                    });


            final Class classPmWrapper = findClass("com.samsung.android.da.daagent.fwwrapper.PmWrapper", lpparam.classLoader);
            findAndHookMethod(classPmWrapper, "getPossibleDualAppPackages", Context.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Context context = (Context) param.args[0];
                            ArrayList<String> list = getAllInstalledPkg(context);
                            param.setResult(list);
                        }
                    });


            final Class<?> classDAUtility = findClass("com.samsung.android.da.daagent.utils.DAUtility", lpparam.classLoader);
            final Class classDaWrapper = findClass("com.samsung.android.da.daagent.fwwrapper.DaWrapper", lpparam.classLoader);
            findAndHookMethod(classDAUtility, "updateWhitelistAppsInSystemServer", Context.class,
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            Context context = (Context) param.args[0];
                            Bundle bundle = new Bundle();
                            HashMap<String, Integer> pkgMap = new HashMap<>();
                            ArrayList<String> pkgs = getAllInstalledPkg(context);
                            for (String pkgName : pkgs) {
                                pkgMap.put(pkgName, 0);
                            }
                            bundle.putString("command", "updateWhitelistPkgs");
                            bundle.putSerializable("packageList", pkgMap);

                            callStaticMethod(classDaWrapper, "updateDualAppData", context, bundle);
                            return null;
                        }
                    });

        }
    }

    private static ArrayList<String> getAllInstalledPkg(Context context) {
        ArrayList<String> list = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> installedPackages = pm.getInstalledPackages(PackageManager.GET_GIDS);

        for (PackageInfo info : installedPackages) {
            if ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0
                    && (info.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0)
                list.add(info.packageName);
        }

        return list;
    }

}