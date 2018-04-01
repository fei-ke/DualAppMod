package fei_ke.com.dualappmod;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.getAdditionalStaticField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalStaticField;

public class XposedMod implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("com.samsung.android.da.daagent")) {
            findAndHookMethod("com.samsung.android.da.daagent.fwwrapper.PmWrapper", lpparam.classLoader
                    , "getPossibleDualAppPackages", Context.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Context context = (Context) param.args[0];
                            ArrayList<String> list = new ArrayList<>();
                            PackageManager pm = context.getPackageManager();
                            List<PackageInfo> installedPackages = pm.getInstalledPackages(PackageManager.GET_GIDS);
                            for (PackageInfo info : installedPackages) {
                                if ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0
                                        && (info.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0)

                                    list.add(info.packageName);
                            }
                            param.setResult(list);
                        }
                    });

        }
    }

}