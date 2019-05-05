package com.dragons.aurora;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;

import com.aurora.services.IPrivilegedCallback;
import com.aurora.services.IPrivilegedService;
import com.dragons.aurora.model.App;

import timber.log.Timber;

import static com.dragons.aurora.Aurora.PRIVILEGED_EXTENSION_PACKAGE_NAME;
import static com.dragons.aurora.Aurora.PRIVILEGED_EXTENSION_SERVICE_INTENT;
import static com.dragons.aurora.Util.isExtensionAvailable;

public class InstallerAurora extends InstallerAbstract {
    public static final int ACTION_INSTALL_REPLACE_EXISTING = 2;

    public InstallerAurora(Context context) {
        super(context);
    }

    @Override
    public boolean verify(App app) {
        if (!super.verify(app)) {
            return false;
        }
        return isExtensionAvailable(context);
    }

    @Override
    protected void install(final App app) {
        InstallationState.setInstalling(app.getPackageName());
        ServiceConnection mServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                IPrivilegedService service = IPrivilegedService.Stub.asInterface(binder);
                IPrivilegedCallback callback = new IPrivilegedCallback.Stub() {
                    @Override
                    public void handleResult(String packageName, int returnCode) throws RemoteException {
                        Timber.i("Installation of " + packageName + " complete with code " + returnCode);
                        sendBroadcast(packageName, returnCode > 0);
                    }
                };
                try {
                    if (!service.hasPrivilegedPermissions()) {
                        Timber.e("service.hasPrivilegedPermissions() is false");
                        sendBroadcast(app.getPackageName(), false);
                        return;
                    }
                    service.installPackage(
                            Uri.fromFile(Paths.getApkPath(context, app.getPackageName(), app.getVersionCode())),
                            ACTION_INSTALL_REPLACE_EXISTING,
                            BuildConfig.APPLICATION_ID,
                            callback
                    );
                } catch (RemoteException e) {
                    Timber.e("Connecting to privileged service failed");
                    sendBroadcast(app.getPackageName(), false);
                }
            }

            public void onServiceDisconnected(ComponentName name) {
            }
        };

        Intent serviceIntent = new Intent(PRIVILEGED_EXTENSION_SERVICE_INTENT);
        serviceIntent.setPackage(PRIVILEGED_EXTENSION_PACKAGE_NAME);
        context.getApplicationContext().bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }
}
