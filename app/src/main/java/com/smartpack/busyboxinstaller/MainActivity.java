/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of BusyBox Installer: A one-click BusyBox installation utility for Android.
 *
 */

package com.smartpack.busyboxinstaller;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CheckBox;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.smartpack.busyboxinstaller.utils.RootUtils;
import com.smartpack.busyboxinstaller.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 11, 2020
 */

public class MainActivity extends AppCompatActivity {

    private boolean mExit;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize App Theme & Google Ads
        Utils.initializeAppTheme(this);
        Utils.initializeGoogleAds(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCompatTextView installText = findViewById(R.id.install_text);
        if (Utils.existFile("/system/xbin/bb_version")) {
            if (Utils.readFile("/system/xbin/bb_version").equals(Utils.version)) {
                installText.setText(R.string.updated_message);
            } else {
                installText.setText(R.string.update_busybox);
            }
        } else {
            installText.setText(R.string.install_busybox);
        }

        // Initialize Banner Ads
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public void installDialog(View view) {
        if (!RootUtils.rootAccess()) {
            Utils.toast(R.string.no_root_message, this);
            return;
        }
        if (!Utils.checkWriteStoragePermission(this)) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            Utils.toast(R.string.no_permission_message, this);
            return;
        }
        AlertDialog.Builder install = new AlertDialog.Builder(this);
        install.setIcon(R.mipmap.ic_launcher);
        if (Utils.getArch().equals("aarch64") || Utils.getArch().equals("armv7l") || Utils.getArch().equals("i686")) {
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
                        Utils.installBusyBox(this);
                    });
                }
            } else {
                install.setTitle(R.string.install_busybox);
                install.setMessage((getString(R.string.install_busybox_message, Utils.version)));
                install.setNegativeButton(R.string.cancel, (dialog, which) -> {
                });
                install.setPositiveButton(R.string.install, (dialog, which) -> {
                    Utils.installBusyBox(this);
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

    public void donateToMe(View view) {
        Utils.launchUrl("https://www.paypal.me/menacherry", this);
    }

    public void switchTheme(View view) {
        if (Utils.getBoolean("dark_theme", true, this)) {
            Utils.saveBoolean("dark_theme", false, this);
            Utils.toast(getString(R.string.switch_theme, getString(R.string.light)), this);
        } else {
            Utils.toast(getString(R.string.switch_theme, getString(R.string.dark)), this);
            Utils.saveBoolean("dark_theme", true, this);
        }
        Utils.sleep(1);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void shareApp(View view) {
        Intent shareapp = new Intent();
        shareapp.setAction(Intent.ACTION_SEND);
        shareapp.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        shareapp.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app, "v" + BuildConfig.VERSION_NAME));
        shareapp.setType("text/plain");
        Intent shareIntent = Intent.createChooser(shareapp, null);
        startActivity(shareIntent);
    }

    public void aboutDialog(View view) {
        new AlertDialog.Builder(this)
        .setIcon(R.mipmap.ic_launcher_round)
                .setCancelable(false)
        .setTitle(getString(R.string.app_name) + "\nv" + BuildConfig.VERSION_NAME)
                .setMessage(R.string.about)
                .setNeutralButton(R.string.cancel, (dialog, which) -> {
                })
                .setNegativeButton(R.string.support_group, (dialog, which) -> {
                    Utils.launchUrl("https://t.me/smartpack_kmanager", this);
                })
                .setPositiveButton(R.string.source_code, (dialog, which) -> {
                    Utils.launchUrl("https://github.com/SmartPack/BusyBox-Installer", this);
                })
                .show();
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
                CheckBox checkBox = checkBoxView.findViewById(R.id.checkbox);
                checkBox.setText(getString(R.string.hide));
                checkBox.setChecked(false);
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        Utils.saveBoolean("update_dialogue", false, this);
                    }
                });

                AlertDialog.Builder update = new AlertDialog.Builder(this);
                update.setIcon(R.mipmap.ic_launcher);
                update.setTitle(getString(R.string.update_busybox));
                update.setMessage(getString(R.string.install_busybox_update, Utils.version));
                update.setCancelable(false);
                update.setView(checkBoxView);
                update.setNegativeButton(R.string.cancel, (dialog, which) -> {
                });
                update.setPositiveButton(R.string.update, (dialog, which) -> {
                    Utils.installBusyBox(this);
                });
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
            Utils.toast(R.string.press_back, this);
            mExit = true;
            mHandler.postDelayed(() -> mExit = false, 2000);
        }
    }

}