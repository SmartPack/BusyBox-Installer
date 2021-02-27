/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of BusyBox Installer: A one-click BusyBox installation utility for Android.
 *
 */

package com.smartpack.busyboxinstaller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.busyboxinstaller.utils.AboutActivity;
import com.smartpack.busyboxinstaller.utils.BillingActivity;
import com.smartpack.busyboxinstaller.utils.RootUtils;
import com.smartpack.busyboxinstaller.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 11, 2020
 */

public class MainActivity extends AppCompatActivity {

    private boolean mExit;
    private Handler mHandler = new Handler();
    private LinearLayout mInstall;
    private LinearLayout mProgress;
    private MaterialTextView mInstallText;
    private MaterialTextView mProgressText;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize App Theme
        Utils.initializeAppTheme(this);
        super.onCreate(savedInstanceState);
        // Set App Language
        Utils.setLanguage(this);
        setContentView(R.layout.activity_main);

        mProgress = findViewById(R.id.progress_layout);
        mProgressText = findViewById(R.id.progress_text);
        mInstallText = findViewById(R.id.install_text);
        mInstall = findViewById(R.id.install);
        AppCompatImageButton settings = findViewById(R.id.settings_menu);

        refreshTitles();

        mInstall.setVisibility(View.VISIBLE);
        mInstall.setOnClickListener(v -> {
            installDialog();
        });
        settings.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, settings);
            Menu menu = popupMenu.getMenu();
            if (Utils.existFile("/system/xbin/bb_version")) {
                menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.remove));
            }
            SubMenu language = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.language, Utils.getLanguage(this)));
            language.add(Menu.NONE, 9, Menu.NONE, getString(R.string.language_default)).setCheckable(true)
                    .setChecked(Utils.languageDefault(this));
            language.add(Menu.NONE, 10, Menu.NONE, getString(R.string.language_en)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_en", false, this));
            language.add(Menu.NONE, 11, Menu.NONE, getString(R.string.language_ko)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_ko", false, this));
            language.add(Menu.NONE, 12, Menu.NONE, getString(R.string.language_am)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_am", false, this));
            language.add(Menu.NONE, 13, Menu.NONE, getString(R.string.language_el)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_el", false, this));
            language.add(Menu.NONE, 14, Menu.NONE, getString(R.string.language_pt)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_pt", false, this));
            language.add(Menu.NONE, 15, Menu.NONE, getString(R.string.language_ru)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_ru", false, this));
            language.add(Menu.NONE, 16, Menu.NONE, getString(R.string.language_in)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_in", false, this));
            if (Utils.existFile("/system/xbin/busybox_" + Utils.version)) {
                menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.list_applets));
                menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.version));
            }
            SubMenu about = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.about));
            about.add(Menu.NONE, 4, Menu.NONE, getString(R.string.share));
            about.add(Menu.NONE, 5, Menu.NONE, getString(R.string.source_code));
            about.add(Menu.NONE, 6, Menu.NONE, getString(R.string.support_group));
            about.add(Menu.NONE, 17, Menu.NONE, getString(R.string.translations));
            about.add(Menu.NONE, 7, Menu.NONE, getString(R.string.donations));
            about.add(Menu.NONE, 8, Menu.NONE, getString(R.string.about));
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        break;
                    case 1:
                        removeBusyBox();
                        break;
                    case 2:
                        new MaterialAlertDialogBuilder(this)
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(R.string.list_applets)
                                .setMessage(getString(R.string.list_applets_summary, Utils.getAppletsList().replace("\n", "\n - ")))
                                .setPositiveButton(R.string.cancel, (dialog, which) -> {
                                })
                                .show();
                        break;
                    case 3:
                        new MaterialAlertDialogBuilder(this)
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(R.string.busybox_version)
                                .setMessage(Utils.getBusyBoxVersion())
                                .setPositiveButton(R.string.cancel, (dialog, which) -> {
                                })
                                .show();
                        break;
                    case 4:
                        shareApp();
                        break;
                    case 5:
                        Utils.launchUrl("https://github.com/SmartPack/BusyBox-Installer", this);
                        break;
                    case 6:
                        Utils.launchUrl("https://t.me/smartpack_kmanager", this);
                        break;
                    case 7:
                        Intent donations = new Intent(this, BillingActivity.class);
                        startActivity(donations);
                        break;
                    case 8:
                        Intent aboutDialog = new Intent(this, AboutActivity.class);
                        startActivity(aboutDialog);
                        break;
                    case 9:
                        if (!Utils.languageDefault(this)) {
                            Utils.setDefaultLanguage(this);
                            restartApp();
                        }
                        break;
                    case 10:
                        if (!Utils.getBoolean("use_en", false, this)) {
                            Utils.setDefaultLanguage(this);
                            Utils.saveBoolean("use_en", true, this);
                            restartApp();
                        }
                        break;
                    case 11:
                        if (!Utils.getBoolean("use_ko", false, this)) {
                            Utils.setDefaultLanguage(this);
                            Utils.saveBoolean("use_ko", true, this);
                            restartApp();
                        }
                        break;
                    case 12:
                        if (!Utils.getBoolean("use_am", false, this)) {
                            Utils.setDefaultLanguage(this);
                            Utils.saveBoolean("use_am", true, this);
                            restartApp();
                        }
                        break;
                    case 13:
                        if (!Utils.getBoolean("use_el", false, this)) {
                            Utils.setDefaultLanguage(this);
                            Utils.saveBoolean("use_el", true, this);
                            restartApp();
                        }
                        break;
                    case 14:
                        if (!Utils.getBoolean("use_pt", false, this)) {
                            Utils.setDefaultLanguage(this);
                            Utils.saveBoolean("use_pt", true, this);
                            restartApp();
                        }
                        break;
                    case 15:
                        if (!Utils.getBoolean("use_ru", false, this)) {
                            Utils.setDefaultLanguage(this);
                            Utils.saveBoolean("use_ru", true, this);
                            restartApp();
                        }
                        break;
                    case 16:
                        if (!Utils.getBoolean("use_in", false, this)) {
                            Utils.setDefaultLanguage(this);
                            Utils.saveBoolean("use_in", true, this);
                            restartApp();
                        }
                        break;
                    case 17:
                        Utils.launchUrl("https://poeditor.com/join/project?hash=JsnaHsMpUk", this);
                }
                return false;
            });
            popupMenu.show();
        });
    }

    private void installDialog() {
        if (!RootUtils.rootAccess()) {
            Utils.snackbar(mInstall, getString(R.string.no_root_message));
            return;
        }
        if (!Utils.checkWriteStoragePermission(this)) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            Utils.snackbar(mInstall, getString(R.string.no_permission_message));
            return;
        }
        MaterialAlertDialogBuilder install = new MaterialAlertDialogBuilder(this);
        install.setIcon(R.mipmap.ic_launcher);
        if (Utils.getArch().equals("aarch64") || Utils.getArch().equals("armv7l") || Utils.getArch().equals("armv8l")
                || Utils.getArch().equals("i686")) {
            if (Utils.existFile("/system/xbin/bb_version")) {
                if (Utils.readFile("/system/xbin/bb_version").equals(Utils.version)) {
                    install.setTitle(R.string.updated_message);
                    install.setMessage(getString(R.string.install_busybox_latest, Utils.version));
                    install.setPositiveButton(R.string.cancel, (dialog, which) -> {
                    });
                } else {
                    install.setTitle(R.string.update_busybox);
                    install.setMessage(getString(R.string.install_busybox_update, Utils.version));
                    install.setNegativeButton(R.string.cancel, (dialog, which) -> {
                    });
                    install.setPositiveButton(R.string.update, (dialog, which) -> {
                        installBusyBox(mInstall, this);
                    });
                }
            } else {
                install.setTitle(R.string.install_busybox);
                install.setMessage((getString(R.string.install_busybox_message, Utils.version)));
                install.setNegativeButton(R.string.cancel, (dialog, which) -> {
                });
                install.setPositiveButton(R.string.install, (dialog, which) -> {
                    installBusyBox(mInstall, this);
                });
            }
        } else {
            install.setTitle(R.string.upsupported);
            install.setMessage(getString(R.string.install_busybox_unavailable, Utils.getArch()));
            install.setPositiveButton(R.string.cancel, (dialog, which) -> {
            });
        }
        install.show();
    }

    private void removeBusyBox() {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(getString(R.string.remove_busybox_message, Utils.version))
                    .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    })
                    .setPositiveButton(R.string.remove, (dialog, which) -> {
                        removeBusyBox(this);
                    })
                    .show();
    }

    private void restartApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void shareApp() {
        Intent shareapp = new Intent();
        shareapp.setAction(Intent.ACTION_SEND);
        shareapp.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        shareapp.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app, "v" + BuildConfig.VERSION_NAME));
        shareapp.setType("text/plain");
        Intent shareIntent = Intent.createChooser(shareapp, null);
        startActivity(shareIntent);
    }

    @SuppressLint("StaticFieldLeak")
    public void installBusyBox(View view, Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressText.setText(activity.getString(R.string.installing, Utils.version) + " ...");
                mProgress.setVisibility(View.VISIBLE);
                mInstall.setVisibility(View.GONE);
                if (Utils.mOutput == null) {
                    Utils.mOutput = new StringBuilder();
                } else {
                    Utils.mOutput.setLength(0);
                }
            }
            @Override
            protected Void doInBackground(Void... voids) {
                Utils.mOutput.append("** Preparing to install BusyBox v" + Utils.version + "...\n\n");
                Utils.mOutput.append("** Checking device partitions...\n");
                if (Utils.isWritableSystem()) {
                    Utils.mOutput.append("** Mounting '/system' partition: Done *\n\n");
                } else if (Utils.isWritableRoot()) {
                    Utils.SAR = true;
                    Utils.mOutput.append("** Mounting 'root' partition: Done *\n\n");
                } else {
                    Utils.mountable = false;
                    Utils.mOutput.append("** Both 'root' & '/system' partitions on your device are not writable! *\n\n");
                    Utils.mOutput.append(activity.getString(R.string.install_busybox_failed));
                }
                if (Utils.mountable) {
                    Utils.sleep(1);
                    Utils.mOutput.append("** Copying BusyBox v" + Utils.version + " binary into '").append(getExternalFilesDir("")).append("': Done *\n\n");
                    Utils.copyBinary(activity);
                    if (!Utils.existFile("/system/xbin/")) {
                        RootUtils.runCommand("mkdir /system/xbin/");
                        Utils.mOutput.append("** Creating '/system/xbin/': Done*\n\n");
                    }
                    Utils.mOutput.append("** Moving BusyBox binary into '/system/xbin/': ");
                    Utils.move(getExternalFilesDir("") + "/busybox_" + Utils.version, "/system/xbin/\n");
                    Utils.mOutput.append(Utils.existFile("/system/xbin/busybox_" + Utils.version) ? "Done *\n\n" : "Failed *\n\n");
                    if (Utils.existFile("/system/xbin/busybox_" + Utils.version)) {
                        Utils.mOutput.append("** Detecting 'su' binary: ");
                        if (Utils.existFile("/system/xbin/su")) Utils.superUser = true;
                        Utils.mOutput.append(Utils.superUser ? "Detected *\n\n" : "Not Detected *\n\n");
                        Utils.mOutput.append("** Setting permissions: Done *\n");
                        Utils.mOutput.append(Utils.chmod("755", "/system/xbin/busybox_" + Utils.version)).append("\n");
                        Utils.mOutput.append("** Installing applets: ");
                        RootUtils.runCommand("cd /system/xbin/");
                        Utils.mOutput.append(RootUtils.runAndGetError("busybox_" + Utils.version + " --install ."));
                        Utils.mOutput.append("Done *\n\n");
                        if (!Utils.superUser) {
                            Utils.mOutput.append("** Removing 'su' binary to avoid SafetyNet failure: ");
                            Utils.delete("/system/xbin/su");
                            Utils.mOutput.append("Done *\n\n");
                        }
                        Utils.create(Utils.version, "/system/xbin/bb_version");
                        Utils.mOutput.append("** Syncing file systems: ");
                        RootUtils.runCommand("sync");
                        Utils.mOutput.append("Done *\n\n");
                        Utils.sleep(1);
                    }
                    if (Utils.SAR) {
                        Utils.mOutput.append(Utils.mountRootFS("ro"));
                        Utils.mOutput.append("** Making 'root' file system read-only: Done*\n\n");
                    } else {
                        Utils.mOutput.append(Utils.mountSystem("ro"));
                        Utils.mOutput.append("** Making 'system' partition read-only: Done*\n\n");
                    }
                    Utils.mOutput.append(activity.getString(R.string.install_busybox_success));
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (activity.isFinishing() || activity.isDestroyed()) return;
                mProgress.setVisibility(View.GONE);
                mInstall.setVisibility(View.VISIBLE);
                refreshTitles();
                MaterialAlertDialogBuilder status = new MaterialAlertDialogBuilder(activity);
                status.setIcon(R.mipmap.ic_launcher_round);
                status.setTitle(R.string.app_name);
                status.setMessage(Utils.mOutput.toString());
                if (Utils.existFile("/system/xbin/bb_version") && Utils.readFile("/system/xbin/bb_version").equals(Utils.version)) {
                    status.setNeutralButton(R.string.save_log, (dialog, which) -> {
                        Utils.create("## BusyBox Installation log created by BusyBox Installer v" + BuildConfig.VERSION_NAME + "\n\n" +
                                Utils.mOutput.toString(),getExternalFilesDir("") + "/bb_log");
                        Utils.snackbar(view, activity.getString(R.string.save_log_summary, getExternalFilesDir("") + "/bb_log"));
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

    @SuppressLint("StaticFieldLeak")
    public void removeBusyBox(Activity activity) {
        new AsyncTask<Void, Void, Void>() {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mProgressText.setText(activity.getString(R.string.removing_busybox, Utils.version) + " ...");
                mProgress.setVisibility(View.VISIBLE);
                mInstall.setVisibility(View.GONE);
                if (Utils.mOutput == null) {
                    Utils.mOutput = new StringBuilder();
                } else {
                    Utils.mOutput.setLength(0);
                }
                Utils.mOutput.append("** Preparing to remove BusyBox v" + Utils.version + "...\n\n");
            }
            @Override
            protected Void doInBackground(Void... voids) {
                if (Utils.isWritableSystem()) {
                    Utils.mOutput.append("** Mounting '/system' partition: Done *\n\n");
                } else if (Utils.isWritableRoot()) {
                    Utils.SAR = true;
                    Utils.mOutput.append("** System-As-Root device detected\n");
                    Utils.mOutput.append("** Mounting 'root' partition: Done *\n\n");
                }
                Utils.sleep(1);
                Utils.mOutput.append("** Removing BusyBox v" + Utils.version + "  applets: ");
                RootUtils.runCommand("cd /system/xbin/");
                RootUtils.runCommand("rm -r " + Utils.getAppletsList().replace("\n", " "));
                Utils.delete("/system/xbin/bb_version");
                Utils.delete("/system/xbin/busybox_" + Utils.version);
                Utils.mOutput.append("Done *\n\n");
                Utils.mOutput.append("** Syncing file systems: ");
                RootUtils.runCommand("sync");
                Utils.mOutput.append("Done *\n\n");
                Utils.sleep(1);
                if (Utils.SAR) {
                    Utils.mOutput.append(Utils.mountRootFS("ro"));
                    Utils.mOutput.append("** Making 'root' file system read-only: Done*\n\n");
                } else {
                    Utils.mOutput.append(Utils.mountSystem("ro"));
                    Utils.mOutput.append("** Making 'system' partition read-only: Done*\n\n");
                }
                Utils.mOutput.append(activity.getString(R.string.remove_busybox_completed, Utils.version));
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (activity.isFinishing() || activity.isDestroyed()) return;
                mProgress.setVisibility(View.GONE);
                mInstall.setVisibility(View.VISIBLE);
                refreshTitles();
                MaterialAlertDialogBuilder status = new MaterialAlertDialogBuilder(activity);
                status.setIcon(R.mipmap.ic_launcher_round);
                status.setTitle(R.string.app_name);
                status.setMessage(Utils.mOutput.toString());
                status.setNeutralButton(R.string.cancel, (dialog, which) -> {
                });
                status.setPositiveButton(R.string.reboot, (dialog, which) -> {
                    RootUtils.runCommand("svc power reboot");
                });
                status.show();
            }
        }.execute();
    }

    private void refreshTitles() {
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

    @Override
    public void onStart() {
        super.onStart();

        if (!RootUtils.rootAccess() || !Utils.checkWriteStoragePermission(this)
                || !Utils.getBoolean("update_dialogue", true, this)) {
            return;
        }

        if (Utils.getArch().equals("aarch64") || Utils.getArch().equals("armv7l") || Utils.getArch().equals("i686")) {
            if (Utils.existFile("/system/xbin/bb_version") && !Utils.readFile("/system/xbin/bb_version").equals(Utils.version)) {
                View checkBoxView = View.inflate(this, R.layout.rv_checkbox, null);
                MaterialCheckBox checkBox = checkBoxView.findViewById(R.id.checkbox);
                checkBox.setText(getString(R.string.hide));
                checkBox.setChecked(false);
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        Utils.saveBoolean("update_dialogue", false, this);
                    }
                });

                MaterialAlertDialogBuilder update = new MaterialAlertDialogBuilder(this);
                update.setIcon(R.mipmap.ic_launcher);
                update.setTitle(getString(R.string.update_busybox));
                update.setMessage(getString(R.string.install_busybox_update, Utils.version));
                update.setCancelable(false);
                update.setView(checkBoxView);
                update.setNegativeButton(R.string.cancel, (dialog, which) -> {
                });
                update.setPositiveButton(R.string.update, (dialog, which) -> installBusyBox(mInstall, this));
                update.show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mExit) {
            mExit = false;
            super.onBackPressed();
        } else {
            Utils.snackbar(mInstall, getString(R.string.press_back));
            mExit = true;
            mHandler.postDelayed(() -> mExit = false, 2000);
        }
    }

}