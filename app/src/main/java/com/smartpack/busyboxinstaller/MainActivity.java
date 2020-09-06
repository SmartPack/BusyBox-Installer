/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of BusyBox Installer: A one-click BusyBox installation utility for Android.
 *
 */

package com.smartpack.busyboxinstaller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.smartpack.busyboxinstaller.utils.RootUtils;
import com.smartpack.busyboxinstaller.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 11, 2020
 */

public class MainActivity extends AppCompatActivity {

    private AppCompatImageButton mBack;
    private AppCompatImageView mAppIcon;
    private AppCompatTextView mCardTitle;
    private AppCompatTextView mAppName;
    private AppCompatTextView mAboutApp;
    private AppCompatTextView mDevelopedBy;
    private AppCompatTextView mCreditsTitle;
    private AppCompatTextView mCredits;
    private AppCompatTextView mForegroundText;
    private AppCompatTextView mCancel;
    private AppCompatImageView mDeveloper;
    private boolean mExit;
    private boolean mForegroundActive = false;
    private CardView mForegroundCard;
    private Handler mHandler = new Handler();
    private LinearLayout mInstall;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize App Theme & Google Ads
        Utils.initializeAppTheme(this);
        Utils.initializeGoogleAds(this);
        super.onCreate(savedInstanceState);
        // Set App Language
        Utils.setLanguage(this);
        setContentView(R.layout.activity_main);
        Utils.mInstallText = findViewById(R.id.install_text);
        Utils.refreshTitles();

        mInstall = findViewById(R.id.install);
        mInstall.setVisibility(View.VISIBLE);
        mInstall.setOnClickListener(v -> {
            installDialog();
        });
        AppCompatImageButton settings = findViewById(R.id.settings_menu);
        mForegroundCard = findViewById(R.id.foreground_card);
        mBack = findViewById(R.id.back);
        mAppIcon = findViewById(R.id.app_image);
        mCardTitle = findViewById(R.id.card_title);
        mAppName = findViewById(R.id.app_title);
        mAboutApp = findViewById(R.id.about_app);
        mDevelopedBy = findViewById(R.id.developed_by);
        mDeveloper = findViewById(R.id.developer);
        mCreditsTitle = findViewById(R.id.credits_title);
        mCredits = findViewById(R.id.credits);
        mForegroundText = findViewById(R.id.foreground_text);
        mDeveloper.setOnClickListener(v -> {
            Utils.launchUrl("https://github.com/sunilpaulmathew", this);
        });
        mCancel = findViewById(R.id.cancel_button);
        mBack.setOnClickListener(v -> {
            closeForeground();
        });
        mCancel.setOnClickListener(v -> {
            closeForeground();
        });
        settings.setOnClickListener(v -> {
            if (mForegroundActive) return;
            PopupMenu popupMenu = new PopupMenu(this, settings);
            Menu menu = popupMenu.getMenu();
            if (Utils.existFile("/system/xbin/bb_version")) {
                menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.remove));
            }
            menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.dark_theme)).setCheckable(true)
                    .setChecked(Utils.getBoolean("dark_theme", true, this));
            SubMenu language = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.language, Utils.getLanguage(this)));
            language.add(Menu.NONE, 11, Menu.NONE, getString(R.string.language_default)).setCheckable(true)
                    .setChecked(Utils.languageDefault(this));
            language.add(Menu.NONE, 12, Menu.NONE, getString(R.string.language_en)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_en", false, this));
            language.add(Menu.NONE, 13, Menu.NONE, getString(R.string.language_ko)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_ko", false, this));
            language.add(Menu.NONE, 14, Menu.NONE, getString(R.string.language_am)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_am", false, this));
            language.add(Menu.NONE, 15, Menu.NONE, getString(R.string.language_el)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_el", false, this));
            language.add(Menu.NONE, 16, Menu.NONE, getString(R.string.language_pt)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_pt", false, this));
            language.add(Menu.NONE, 17, Menu.NONE, getString(R.string.language_ru)).setCheckable(true)
                    .setChecked(Utils.getBoolean("use_ru", false, this));
            if (Utils.existFile("/system/xbin/busybox_" + Utils.version)) {
                menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.list_applets));
                menu.add(Menu.NONE, 4, Menu.NONE, getString(R.string.version));
            }
            SubMenu about = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.about));
            about.add(Menu.NONE, 5, Menu.NONE, getString(R.string.share));
            about.add(Menu.NONE, 6, Menu.NONE, getString(R.string.source_code));
            about.add(Menu.NONE, 7, Menu.NONE, getString(R.string.support_group));
            about.add(Menu.NONE, 10, Menu.NONE, getString(R.string.change_log));
            if (Utils.isNotDonated(this)) {
                about.add(Menu.NONE, 8, Menu.NONE, getString(R.string.donations));
            }
            about.add(Menu.NONE, 9, Menu.NONE, getString(R.string.about));
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        break;
                    case 1:
                        removeBusyBox();
                        break;
                    case 2:
                        switchTheme();
                        break;
                    case 3:
                        new AlertDialog.Builder(this)
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(R.string.list_applets)
                                .setMessage(getString(R.string.list_applets_summary, Utils.getAppletsList().replace("\n", "\n - ")))
                                .setPositiveButton(R.string.cancel, (dialog, which) -> {
                                })
                                .show();
                        break;
                    case 4:
                        new AlertDialog.Builder(this)
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(R.string.busybox_version)
                                .setMessage(Utils.getBusyBoxVersion())
                                .setPositiveButton(R.string.cancel, (dialog, which) -> {
                                })
                                .show();
                        break;
                    case 5:
                        shareApp();
                        break;
                    case 6:
                        Utils.launchUrl("https://github.com/SmartPack/BusyBox-Installer", this);
                        break;
                    case 7:
                        Utils.launchUrl("https://t.me/smartpack_kmanager", this);
                        break;
                    case 8:
                        donateToMe();
                        break;
                    case 9:
                        aboutDialog();
                        break;
                    case 10:
                        String change_log = null;
                        try {
                            change_log = new JSONObject(Objects.requireNonNull(Utils.readAssetFile(
                                    this, "changelogs.json"))).getString("releaseNotes");
                        } catch (JSONException ignored) {
                        }
                        mCardTitle.setText(R.string.change_log);
                        mAppName.setText(getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME);
                        mForegroundText.setText(change_log);
                        mAppIcon.setVisibility(View.VISIBLE);
                        mAppName.setVisibility(View.VISIBLE);
                        mBack.setVisibility(View.VISIBLE);
                        mCancel.setVisibility(View.VISIBLE);
                        mCardTitle.setVisibility(View.VISIBLE);
                        mForegroundText.setVisibility(View.VISIBLE);
                        mForegroundActive = true;
                        mForegroundCard.setVisibility(View.VISIBLE);
                        break;
                    case 11:
                        if (!Utils.languageDefault(this)) {
                            Utils.setDefaultLanguage(this);
                            restartApp();
                        }
                        break;
                    case 12:
                        if (!Utils.getBoolean("use_en", false, this)) {
                            Utils.setDefaultLanguage(this);
                            Utils.saveBoolean("use_en", true, this);
                            restartApp();
                        }
                        break;
                    case 13:
                        if (!Utils.getBoolean("use_ko", false, this)) {
                            Utils.setDefaultLanguage(this);
                            Utils.saveBoolean("use_ko", true, this);
                            restartApp();
                        }
                        break;
                    case 14:
                        if (!Utils.getBoolean("use_am", false, this)) {
                            Utils.setDefaultLanguage(this);
                            Utils.saveBoolean("use_am", true, this);
                            restartApp();
                        }
                        break;
                    case 15:
                        if (!Utils.getBoolean("use_el", false, this)) {
                            Utils.setDefaultLanguage(this);
                            Utils.saveBoolean("use_el", true, this);
                            restartApp();
                        }
                        break;
                    case 16:
                        if (!Utils.getBoolean("use_pt", false, this)) {
                            Utils.setDefaultLanguage(this);
                            Utils.saveBoolean("use_pt", true, this);
                            restartApp();
                        }
                        break;
                    case 17:
                        if (!Utils.getBoolean("use_ru", false, this)) {
                            Utils.setDefaultLanguage(this);
                            Utils.saveBoolean("use_ru", true, this);
                            restartApp();
                        }
                        break;
                }
                return false;
            });
            popupMenu.show();
        });

        // Initialize Banner Ads
        if (Utils.isNotDonated(this)) {
            LinearLayout mAdLayout = findViewById(R.id.adLayout);
            AdView mAdView = findViewById(R.id.adView);
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    mAdLayout.setVisibility(View.VISIBLE);
                }
            });
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            mAdView.loadAd(adRequest);
        }
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
                        Utils.installBusyBox(new WeakReference<>(this));
                    });
                }
            } else {
                install.setTitle(R.string.install_busybox);
                install.setMessage((getString(R.string.install_busybox_message, Utils.version)));
                install.setNegativeButton(R.string.cancel, (dialog, which) -> {
                });
                install.setPositiveButton(R.string.install, (dialog, which) -> {
                    Utils.installBusyBox(new WeakReference<>(this));
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
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.remove_busybox_message, Utils.version))
                    .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    })
                    .setPositiveButton(R.string.remove, (dialog, which) -> {
                        Utils.removeBusyBox(new WeakReference<>(this));
                    })
                    .show();
    }

    private void donateToMe() {
        new AlertDialog.Builder(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(getString(R.string.support_developer))
                .setMessage(getString(R.string.support_developer_message))
                .setNeutralButton(getString(R.string.cancel), (dialog1, id1) -> {
                })
                .setPositiveButton(getString(R.string.donation_app), (dialogInterface, i) -> {
                    Utils.launchUrl("https://play.google.com/store/apps/details?id=com.smartpack.donate", this);
                })
                .show();
    }

    private void switchTheme() {
        if (Utils.getBoolean("dark_theme", true, this)) {
            Utils.saveBoolean("dark_theme", false, this);
            Utils.snackbar(mInstall, getString(R.string.switch_theme, getString(R.string.light)));
        } else {
            Utils.snackbar(mInstall, getString(R.string.switch_theme, getString(R.string.dark)));
            Utils.saveBoolean("dark_theme", true, this);
        }
        Utils.sleep(1);
        restartApp();
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

    @SuppressLint("SetTextI18n")
    private void aboutDialog() {
        mCardTitle.setText(R.string.about);
        mAppName.setText(getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME);
        mCredits.setText(getString(R.string.credits_summary));
        mCardTitle.setVisibility(View.VISIBLE);
        mBack.setVisibility(View.VISIBLE);
        mAppIcon.setVisibility(View.VISIBLE);
        mAppName.setVisibility(View.VISIBLE);
        mAboutApp.setVisibility(View.VISIBLE);
        mDevelopedBy.setVisibility(View.VISIBLE);
        mDeveloper.setVisibility(View.VISIBLE);
        mCreditsTitle.setVisibility(View.VISIBLE);
        mCredits.setVisibility(View.VISIBLE);
        mCancel.setVisibility(View.VISIBLE);
        mForegroundActive = true;
        mForegroundCard.setVisibility(View.VISIBLE);
    }

    private void closeForeground() {
        mCardTitle.setVisibility(View.GONE);
        mBack.setVisibility(View.GONE);
        mAppIcon.setVisibility(View.GONE);
        mAppName.setVisibility(View.GONE);
        mAboutApp.setVisibility(View.GONE);
        mDevelopedBy.setVisibility(View.GONE);
        mDeveloper.setVisibility(View.GONE);
        mCreditsTitle.setVisibility(View.GONE);
        mCredits.setVisibility(View.GONE);
        mForegroundText.setVisibility(View.GONE);
        mCancel.setVisibility(View.GONE);
        mForegroundCard.setVisibility(View.GONE);
        mForegroundActive = false;
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
                update.setPositiveButton(R.string.update, (dialog, which) -> Utils.installBusyBox(new WeakReference<>(this)));
                update.show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mForegroundActive) {
            closeForeground();
        } else if (mExit) {
            mExit = false;
            super.onBackPressed();
        } else {
            Utils.snackbar(mInstall, getString(R.string.press_back));
            mExit = true;
            mHandler.postDelayed(() -> mExit = false, 2000);
        }
    }

}