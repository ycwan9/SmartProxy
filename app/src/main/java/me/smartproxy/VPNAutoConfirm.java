package me.smartproxy;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class VPNAutoConfirm implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.vpndialogs")) {
            return;
        }
        XposedBridge.log("VPN Auto confirm Loaded");

        final XC_MethodHook autoConfirmHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    Object mService = XposedHelpers.getObjectField(param.thisObject, "mService");
                    String mPackage = ((Activity) param.thisObject).getCallingPackage();

                    Class<?>[] prepareVPNSignature = {String.class, String.class};
                    if((Boolean) XposedHelpers.callMethod(mService, "prepareVpn", prepareVPNSignature, mPackage, null)) {
                        return;
                    }

                    XposedHelpers.callMethod(mService,"prepareVpn", prepareVPNSignature, null, mPackage);
                    ((Activity) param.thisObject).setResult(Activity.RESULT_OK);
                    Toast.makeText((Context) param.thisObject, "Auto allowed VpnService app: " + mPackage, Toast.LENGTH_LONG).show();
                    ((Activity) param.thisObject).finish();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        findAndHookMethod("com.android.vpndialogs.ConfirmDialog", lpparam.classLoader,
                "onResume", autoConfirmHook);
    }
}
