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

    public static final String version = "1.31.0";
    public static final String appletsList = "[ [[ acpid add-shell addgroup adduser adjtimex arch arp arping ash awk base64 basename bc beep blkdiscard blkid blockdev bootchartd brctl bunzip2 bzcat bzip2 cal cat chat chattr chgrp chmod chown chpasswd chpst chroot chrt chvt cksum clear cmp comm conspy cp cpio crond crontab cryptpw cttyhack cut date dc dd deallocvt delgroup deluser depmod devmem df dhcprelay diff dirname dmesg dnsd dnsdomainname dos2unix dpkg dpkg-deb du dumpkmap dumpleases echo ed egrep eject env envdir envuidgid ether-wake expand expr factor fakeidentd fallocate false fatattr fbset fbsplash fdflush fdformat fdisk fgconsole fgrep find findfs flock fold free freeramdisk fsck fsck.minix fsfreeze fstrim fsync ftpd ftpget ftpput fuser getopt getty grep groups gunzip gzip halt hd hdparm head hexdump hexedit hostid hostname httpd hush hwclock i2cdetect i2cdump i2cget i2cset i2ctransfer id ifconfig ifdown ifenslave ifplugd ifup inetd init insmod install ionice iostat ip ipaddr ipcalc ipcrm ipcs iplink ipneigh iproute iprule iptunnel kbd_mode kill killall killall5 klogd last less link linux32 linux64 linuxrc ln loadfont loadkmap logger login logname logread losetup lpd lpq lpr ls lsattr lsmod lsof lspci lsscsi lsusb lzcat lzma lzop makedevs makemime man md5sum mdev mesg microcom mkdir mkdosfs mke2fs mkfifo mkfs.ext2 mkfs.minix mkfs.vfat mknod mkpasswd mkswap mktemp modinfo modprobe more mount mountpoint mpstat mt mv nameif nanddump nandwrite nbd-client nc netstat nice nl nmeter nohup nologin nproc nsenter nslookup ntpd nuke od openvt partprobe passwd paste patch pgrep pidof ping ping6 pipe_progress pivot_root pkill pmap popmaildir poweroff powertop printenv printf ps pscan pstree pwd pwdx raidautorun rdate rdev readahead readlink readprofile realpath reboot reformime remove-shell renice reset resize resume rev rm rmdir rmmod route rpm rpm2cpio rtcwake run-init run-parts runlevel runsv runsvdir rx script scriptreplay sed sendmail seq setarch setconsole setfattr setfont setkeycodes setlogcons setpriv setserial setsid setuidgid sh sha1sum sha256sum sha3sum sha512sum showkey shred shuf slattach sleep smemcap softlimit sort split ssl_client start-stop-daemon stat strings stty sulogin sum sv svc svlogd svok swapoff swapon switch_root sync sysctl syslogd tac tail tar taskset tc tcpsvd tee telnet telnetd test tftp tftpd time timeout top touch tr traceroute traceroute6 true truncate ts tty ttysize tunctl ubiattach ubidetach ubimkvol ubirename ubirmvol ubirsvol ubiupdatevol udhcpc udhcpc6 udhcpd udpsvd uevent umount uname unexpand uniq unix2dos unlink unlzma unshare unxz unzip uptime users usleep uudecode uuencode vconfig vi vlock volname w wall watch watchdog wc wget which who whoami whois xargs xxd xz xzcat yes zcat zcip";
    private static boolean superUser = false;

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
                mProgressDialog.setMessage(activityRef.get().getString(R.string.installing, version) + " ...");
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
            }
            @Override
            protected Void doInBackground(Void... voids) {
                mountRootFS("rw");
                mountSystem("rw");
                sleep(1);
                RootUtils.runCommand("cd /system/xbin/");
                RootUtils.runCommand("rm -r " + appletsList);
                delete("/system/xbin/bb_version");
                RootUtils.runCommand("sync");
                sleep(1);
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
                AlertDialog.Builder result = new AlertDialog.Builder(activity);
                result.setIcon(R.mipmap.ic_launcher_round);
                if (existFile("/system/xbin/bb_version")) {
                    result.setMessage(activityRef.get().getString(R.string.remove_busybox_failed, version));
                    result.setPositiveButton(R.string.cancel, (dialog, which) -> {
                    });
                } else {
                    result.setMessage(activityRef.get().getString(R.string.remove_busybox_completed, version));
                    result.setNegativeButton(R.string.cancel, (dialog, which) -> {
                    });
                    result.setPositiveButton(R.string.reboot, (dialog, which) -> {
                        RootUtils.runCommand("svc power reboot");
                    });
                }
                result.show();
            }
        }.execute();
    }

}