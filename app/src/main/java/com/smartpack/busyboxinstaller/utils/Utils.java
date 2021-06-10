package com.smartpack.busyboxinstaller.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Locale;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 11, 2020
 * Based on the original implementation on Kernel Adiutor by
 * Willi Ye <williye97@gmail.com>
 */

public class Utils {

    public static final String version = "1.32.1";
    public static StringBuilder mOutput = null;
    public static boolean superUser = false;
    public static boolean SAR = false;
    public static boolean mountable = true;

    public static boolean isNotDonated(Context context) {
        try {
            context.getPackageManager().getApplicationInfo("com.smartpack.donate", 0);
            return false;
        } catch (PackageManager.NameNotFoundException ignored) {
            return true;
        }
    }

    public static boolean isSupporter(Context context) {
        return !isNotDonated(context) || getBoolean("support_received", false, context);
    }

    public static void initializeAppTheme(Context context) {
        if (isDarkTheme(context)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static boolean isDarkTheme(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void create(String text, String path) {
        RootUtils.runCommand("echo '" + text + "' > " + path);
    }

    public static void delete(String path) {
        if (Utils.existFile(path)) {
            RootUtils.runCommand("rm -r " + path);
        }
    }

    public static void move(String source, String dest) {
        RootUtils.runCommand("mv " + source + " " + dest);
    }

    public static String chmod(String permission, String path) {
        return RootUtils.runAndGetOutput("chmod " + permission + " " + path);
    }

    public static void snackbar(View view, String message) {
        Snackbar snackbar;
        snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public static void launchUrl(String url, Context context) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            context.startActivity(i);
        } catch (ActivityNotFoundException ignored) {
        }
    }

    public static String mountSystem(String command) {
        return RootUtils.runAndGetError("mount -o remount," + command + " /system");
    }

    public static String mountRootFS(String command) {
        return RootUtils.runAndGetError("mount -o remount," + command + " /");
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

    public static boolean getBoolean(String name, boolean defaults, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(name, defaults);
    }

    public static void saveBoolean(String name, boolean value, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(name, value).apply();
    }

    public static String getString(String name, String defaults, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(name, defaults);
    }

    public static void saveString(String name, String value, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(name, value).apply();
    }

    public static String readAssetFile(Context context, String file) {
        InputStream input = null;
        BufferedReader buf = null;
        try {
            StringBuilder s = new StringBuilder();
            input = context.getAssets().open(file);
            buf = new BufferedReader(new InputStreamReader(input));

            String str;
            while ((str = buf.readLine()) != null) {
                s.append(str).append("\n");
            }
            return s.toString().trim();
        } catch (IOException ignored) {
        } finally {
            try {
                if (input != null) input.close();
                if (buf != null) buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getArch() {
        String arch = RootUtils.runAndGetOutput("uname -m");
        if (arch.matches("aarch64|armv8l")) {
            return "aarch64";
        } else if (arch.equals("armv7l")) {
            return "armv7l";
        } else {
            return "i686";
        }
    }

    public static boolean isWritableSystem() {
        return !mountSystem("rw").equals("mount: '/system' not in /proc/mounts");
    }

    public static boolean isWritableRoot() {
        return !mountRootFS("rw").contains("' is read-only");
    }

    public static void copyBinary(Context context) {
        AssetManager assetManager = context.getAssets();
        InputStream in;
        OutputStream out;
        try {
            in = assetManager.open(getArch());
            File outFile = new File(context.getExternalFilesDir("") + "/busybox_" + version);
            out = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int read;
            while((read = in.read(buffer)) != -1){
                out.write(buffer, 0, read);
            }
        } catch (Exception ignored) {
        }
    }

    public static String getBusyBoxVersion() {
        try {
            for (String line : RootUtils.runAndGetOutput("/system/xbin/busybox_" + version).split("\\r?\\n")) {
                if (line.startsWith("BusyBox v")) {
                    return line.replace("BusyBox v", "");
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static String getAppletsList() {
        return RootUtils.runAndGetOutput("/system/xbin/busybox_" + version + " --list").replace("su\n", "");
    }

    public static String getLanguage(Context context) {
        return getString("appLanguage", java.util.Locale.getDefault().getLanguage(), context);
    }

    public static void setLanguage(Context context) {
        Locale myLocale = new Locale(getString("appLanguage", java.util.Locale.getDefault()
                .getLanguage(), context));
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

}