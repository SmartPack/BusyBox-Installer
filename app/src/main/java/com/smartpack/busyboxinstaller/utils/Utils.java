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
import androidx.appcompat.widget.AppCompatTextView;

import com.facebook.ads.AudienceNetworkAds;
import com.smartpack.busyboxinstaller.BuildConfig;
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

    public static AppCompatTextView mInstallText = null;
    public static final String version = "1.31.0";
    private static StringBuilder mOutput = null;
    private static boolean superUser = false;
    private static boolean SAR = false;
    private static boolean mountable = true;

    public static boolean isNotDonated(Context context) {
        if (BuildConfig.DEBUG) return false;
        try {
            context.getPackageManager().getApplicationInfo("com.smartpack.donate", 0);
            return false;
        } catch (PackageManager.NameNotFoundException ignored) {
            return true;
        }
    }

    public static void initializeAppTheme(Context context) {
        if (getBoolean("dark_theme", true, context)) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static void initializeFaceBookAds(Context context) {
        AudienceNetworkAds.initialize(context);
    }

    private static void create(String text, String path) {
        RootUtils.runCommand("echo '" + text + "' > " + path);
    }

    private static String delete(String path) {
        if (Utils.existFile(path)) {
            return RootUtils.runCommand("rm -r " + path);
        }
        return null;
    }

    private static String move(String source, String dest) {
        return RootUtils.runCommand("mv " + source + " " + dest);
    }

    static String chmod(String permission, String path) {
        return RootUtils.runCommand("chmod " + permission + " " + path);
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

    private static String mountSystem(String command) {
        return RootUtils.runCommand("mount -o remount," + command + " /system");
    }

    private static String mountRootFS(String command) {
        return RootUtils.runCommand("mount -o remount," + command + " /");
    }

    public static void sleep(int s) {
        RootUtils.runCommand("sleep " + s);
    }

    public static String readFile(String file) {
        return readFile(file, true);
    }


    private static String readFile(String file, boolean root) {
        if (root) {
            return new RootFile(file).readFile();
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
        return !root ? new File(file).exists() : new RootFile(file).exists();
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

    private static boolean isWritableSystem() {
        return !mountSystem("rw").equals("mount: '/system' not in /proc/mounts");
    }

    private static boolean isWritableRoot() {
        return !mountRootFS("rw").contains("' is read-only");
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
                mProgressDialog.setMessage(activityRef.get().getString(R.string.installing, version) + " ...");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                if (mOutput == null) {
                    mOutput = new StringBuilder();
                } else {
                    mOutput.setLength(0);
                }
            }
            @Override
            protected Void doInBackground(Void... voids) {
                mOutput.append("** Preparing to install BusyBox v" + version + "...\n\n");
                mOutput.append("** Checking device partitions...\n");
                if (isWritableSystem()) {
                    mOutput.append("** Mounting '/system' partition: Done *\n\n");
                } else if (isWritableRoot()) {
                    SAR = true;
                    mOutput.append("** Mounting 'root' partition: Done *\n\n");
                } else {
                    mountable = false;
                    mOutput.append("** Both 'root' & '/system' partitions on your device are not writable! *\n\n");
                    mOutput.append(activityRef.get().getString(R.string.install_busybox_failed));
                }
                if (mountable) {
                    sleep(1);
                    mOutput.append("** Copying BusyBox v" + version + " binary into '").append(Environment.getExternalStorageDirectory().getPath()).append("': Done *\n\n");
                    copyBinary(activityRef.get());
                    if (!existFile("/system/xbin/")) {
                        RootUtils.runCommand("mkdir /system/xbin/");
                        mOutput.append("** Creating '/system/xbin/': Done*\n\n");
                    }
                    mOutput.append("** Moving BusyBox binary into '/system/xbin/': ");
                    move(Environment.getExternalStorageDirectory().getPath() + "/busybox_" + version, "/system/xbin/\n");
                    mOutput.append(existFile("/system/xbin/busybox_" + version) ? "Done *\n\n" : "Failed *\n\n");
                    if (existFile("/system/xbin/busybox_" + version)) {
                        mOutput.append("** Detecting 'su' binary: ");
                        if (existFile("/system/xbin/su")) superUser = true;
                        mOutput.append(superUser ? "Detected *\n\n" : "Not Detected *\n\n");
                        mOutput.append("** Setting permissions: Done *\n");
                        mOutput.append(chmod("755", "/system/xbin/busybox_" + version)).append("\n");
                        mOutput.append("** Installing applets: ");
                        RootUtils.runCommand("cd /system/xbin/");
                        RootUtils.runCommand("busybox_" + version + " --install .");
                        mOutput.append("Done *\n\n");
                        if (!superUser) {
                            mOutput.append("** Removing 'su' binary to avoid SafetyNet failure: ");
                            delete("/system/xbin/su");
                            mOutput.append("Done *\n\n");
                        }
                        create(version, "/system/xbin/bb_version");
                        mOutput.append("** Syncing file systems: ");
                        RootUtils.runCommand("sync");
                        mOutput.append("Done *\n\n");
                        sleep(1);
                    }
                    if (SAR) {
                        mOutput.append(mountRootFS("ro"));
                        mOutput.append("** Making 'root' file system read-only: Done*\n\n");
                    } else {
                        mOutput.append(mountSystem("ro"));
                        mOutput.append("** Making 'system' partition read-only: Done*\n\n");
                    }
                    mOutput.append(activityRef.get().getString(R.string.install_busybox_success));
                }
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
                refreshTitles();
                AlertDialog.Builder status = new AlertDialog.Builder(activity);
                status.setIcon(R.mipmap.ic_launcher_round);
                status.setTitle(R.string.app_name);
                status.setMessage(mOutput.toString());
                if (existFile("/system/xbin/bb_version") && readFile("/system/xbin/bb_version").equals(version)) {
                    status.setNeutralButton(R.string.save_log, (dialog, which) -> {
                        create("## BusyBox Installation log created by BusyBox Installer v" + BuildConfig.VERSION_NAME + "\n\n" +
                                        mOutput.toString(),Environment.getExternalStorageDirectory().getPath() + "/bb_log");
                        toast(activityRef.get().getString(R.string.save_log_summary, Environment.getExternalStorageDirectory().getPath() + "/bb_log"), activity);
                    });
                    status.setNegativeButton(R.string.cancel, (dialog, which) -> {
                    });
                    status.setPositiveButton(R.string.reboot, (dialog, which) -> {
                        RootUtils.runCommand("svc power reboot");
                    });
                } else {
                    status.setPositiveButton(R.string.cancel, (dialog, which) -> {
                    });
                }
                status.show();
            }
        }.execute();
    }

    public static void removeBusyBox(WeakReference<Activity> activityRef) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mProgressDialog;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressDialog = new ProgressDialog(activityRef.get());
                mProgressDialog.setMessage(activityRef.get().getString(R.string.removing_busybox, version) + " ...");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                if (mOutput == null) {
                    mOutput = new StringBuilder();
                } else {
                    mOutput.setLength(0);
                }
                mOutput.append("** Preparing to remove BusyBox v" + version + "...\n\n");
            }
            @Override
            protected Void doInBackground(Void... voids) {
                if (isWritableSystem()) {
                    mOutput.append("** Mounting '/system' partition: Done *\n\n");
                } else if (isWritableRoot()) {
                    SAR = true;
                    mOutput.append("** System-As-Root device detected\n");
                    mOutput.append("** Mounting 'root' partition: Done *\n\n");
                }
                sleep(1);
                mOutput.append("** Removing BusyBox v" + version + "  applets: ");
                RootUtils.runCommand("cd /system/xbin/");
                RootUtils.runCommand("rm -r " + getAppletsList().replace("\n", " "));
                delete("/system/xbin/bb_version");
                delete("/system/xbin/busybox_" + version);
                mOutput.append("Done *\n\n");
                mOutput.append("** Syncing file systems: ");
                RootUtils.runCommand("sync");
                mOutput.append("Done *\n\n");
                sleep(1);
                if (SAR) {
                    mOutput.append(mountRootFS("ro"));
                    mOutput.append("** Making 'root' file system read-only: Done*\n\n");
                } else {
                    mOutput.append(mountSystem("ro"));
                    mOutput.append("** Making 'system' partition read-only: Done*\n\n");
                }
                mOutput.append(activityRef.get().getString(R.string.remove_busybox_completed, version));
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
                refreshTitles();
                AlertDialog.Builder status = new AlertDialog.Builder(activity);
                status.setIcon(R.mipmap.ic_launcher_round);
                status.setTitle(R.string.app_name);
                status.setMessage(mOutput.toString());
                status.setNeutralButton(R.string.cancel, (dialog, which) -> {
                });
                status.setPositiveButton(R.string.reboot, (dialog, which) -> {
                    RootUtils.runCommand("svc power reboot");
                });
                status.show();
            }
        }.execute();
    }

    public static String getBusyBoxVersion() {
        try {
            for (String line : RootUtils.runCommand("/system/xbin/busybox_" + version).split("\\r?\\n")) {
                if (line.startsWith("BusyBox v")) {
                    return line.replace("BusyBox v", "");
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static String getAppletsList() {
        return RootUtils.runCommand("/system/xbin/busybox_" + version + " --list").replace("su\n", "");
    }

    public static void refreshTitles() {
        if (Utils.existFile("/system/xbin/bb_version")) {
            if (Utils.readFile("/system/xbin/bb_version").equals(Utils.version)) {
                mInstallText.setText(R.string.updated_message);
            } else {
                mInstallText.setText(R.string.update_busybox);
            }
        } else {
            mInstallText.setText(R.string.install_busybox);
        }
    }

}