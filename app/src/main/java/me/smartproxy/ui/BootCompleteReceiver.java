package me.smartproxy.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import me.smartproxy.R;
import me.smartproxy.core.LocalVpnService;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Utils.readAutoStartConfig(context)) {
            Intent vpnIntent = LocalVpnService.prepare(context);
            if (vpnIntent == null) {
                startVPNService(context);
            } else {
                // do nothing since we don't have permission to run service
//                startActivityForResult(intent, START_VPN_SERVICE_REQUEST_CODE);
            }
        } else {
            Toast.makeText(context, "boot complete but wont start service", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void startVPNService(Context context) {
        String configUrl = Utils.readConfigUrl(context, Utils.readCurrentProfile(context));
        if (!Utils.isValidUrl(configUrl)) {
            Toast.makeText(context, R.string.err_invalid_url, Toast.LENGTH_SHORT).show();
            return;
        }

        LocalVpnService.ConfigUrl = configUrl;
        context.startService(new Intent(context, LocalVpnService.class));
    }
}
