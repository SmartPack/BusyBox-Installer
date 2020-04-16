package com.smartpack.busyboxinstaller.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.ads.MobileAds;
import com.smartpack.busyboxinstaller.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 11, 2020
 * Based on the original implementation on Kernel Adiutor by
 * Willi Ye <williye97@gmail.com>
 */

public class Utils {

    public static final String version = "1.31.0";
    private static boolean superUser = false;

    public static void initializeAppTheme(Context context) {
        if (getBoolean("dark_theme", true, context)) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static void initializeGoogleAds(Context context) {
        MobileAds.initialize(context, "ca-app-pub-2781194772510522~1549441426");
    }

    private static void create(String text, String path) {
        RootUtils.runCommand("echo '" + text + "' > " + path);
    }

    private static void delete(String path) {
        if (Utils.existFile(path)) {
            RootUtils.runCommand("rm -r " + path);
        }
    }

    private static void move(String source, String dest) {
        RootUtils.runCommand("mv " + source + " " + dest);
    }

    static void chmod(String permission, String path) {
        RootUtils.runCommand("chmod " + permission + " " + path);
    }

    public static void toast(String message, Context context) {
        toast(message, context, Toast.LENGTH_SHORT);
    }

    public static void toast(@StringRes int id, Context context) {
        toast(context.getString(id), context);
    }

    private static void toast(String message, Context context, int duration) {
        Toast.makeText(context, message, duration).show();
    }

    public static void launchUrl(String url, Context context) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            context.startActivity(i);
        } catch (ActivityNotFoundException ignored) {
        }
    }

    private static void mountSystem(String command) {
        RootUtils.runCommand("mount -o remount," + command + " /system");
    }

    private static void mountRootFS(String command) {
        RootUtils.runCommand("mount -o remount," + command + " /");
    }

    public static void sleep(int s) {
        RootUtils.runCommand("sleep " + s);
    }

    public static String readFile(String file) {
        return readFile(file, true);
    }

    private static String readFile(String file, boolean root) {
        return readFile(file, root ? RootUtils.getSU() : null);
    }

    private static String readFile(String file, RootUtils.SU su) {
        if (su != null) {
            return new RootFile(file, su).readFile();
        }

        BufferedReader buf = null;
        try {
            buf = new BufferedReader(new FileReader(file));

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = buf.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            return stringBuilder.toString().trim();
        } catch (IOException ignored) {
        } finally {
            try {
                if (buf != null) buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static boolean existFile(String file) {
        return existFile(file, true);
    }

    private static boolean existFile(String file, boolean root) {
        return existFile(file, root ? RootUtils.getSU() : null);
    }

    private static boolean existFile(String file, RootUtils.SU su) {
        return su == null ? new File(file).exists() : new RootFile(file, su).exists();
    }

    /*
     * Taken and used almost as such from the following stackoverflow discussion
     * Ref: https://stackoverflow.com/questions/7203668/how-permission-can-be-checked-at-runtime-without-throwing-securityexception
     */
    public static boolean checkWriteStoragePermission(Context context) {
        String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = context.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    public static boolean getBoolean(String name, boolean defaults, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(name, defaults);
    }

    public static void saveBoolean(String name, boolean value, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(name, value).apply();
    }

    public static String getArch() {
        return RootUtils.runCommand("uname -m");
    }

    private static void copyBinary(Context context) {
        AssetManager assetManager = context.getAssets();
        InputStream in;
        OutputStream out;
        try {
            in = assetManager.open(getArch());
            File outFile = new File(Environment.getExternalStorageDirectory().getPath() + "/busybox_" + version);
            out = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int read;
            while((read = in.read(buffer)) != -1){
                out.write(buffer, 0, read);
            }
        } catch (Exception ignored) {
        }
    }

    //TODO: Don't delegate this to the Utils class as it will hold a reference to the activity
    // here workarounded using a WeakReference
    public static void installBusyBox(WeakReference<Activity> activityRef) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(activityRef.get());
                mProgressDialog.setMessage(activityRef.get().getString(R.string.installing,version) + " ...");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
            @Override
            protected Void doInBackground(Void... voids) {
                mountRootFS("rw");
                mountSystem("rw");
                sleep(1);
                copyBinary(activityRef.get());
                move(Environment.getExternalStorageDirectory().getPath() + "/busybox_" + version, "/system/xbin/");
                // Detect 'su' binary
                if (existFile("/system/xbin/su")) superUser = true;
                if (existFile("/system/xbin/busybox_" + version)) {
                    chmod("755", "/system/xbin/busybox_" + version);
                    RootUtils.runCommand("cd /system/xbin/");
                    RootUtils.runCommand("busybox_" + version + " --install .");
                    delete("busybox_" + version);
                    if (!superUser) {
                        // Remove 'su' binary to avoid SafetyNet failure
                        delete("/system/xbin/su");
                    }
                    create(version, "/system/xbin/bb_version");
                    RootUtils.runCommand("sync");
                    sleep(1);
                }
                mountSystem("ro");
                mountRootFS("ro");
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Activity activity = activityRef.get();
                if (activity.isFinishing() || activity.isDestroyed()) return;

                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                AlertDialog.Builder reboot = new AlertDialog.Builder(activity);
                reboot.setIcon(R.mipmap.ic_launcher_round);
                if (existFile("/system/xbin/bb_version") && readFile("/system/xbin/bb_version").equals(version)) {
                    reboot.setMessage(R.string.install_busybox_success);
                    reboot.setNegativeButton(R.string.cancel, (dialog, which) -> {
                    });
                    reboot.setPositiveButton(R.string.reboot, (dialog, which) -> {
                        RootUtils.runCommand("svc power reboot");
                    });
                } else {
                    reboot.setMessage(R.string.install_busybox_failed);
                    reboot.setPositiveButton(R.string.cancel, (dialog, which) -> {
                    });
                }
                reboot.show();
            }
        }.execute();
    }

}