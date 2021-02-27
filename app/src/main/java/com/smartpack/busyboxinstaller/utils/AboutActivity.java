package com.smartpack.busyboxinstaller.utils;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.android.material.textview.MaterialTextView;
import com.smartpack.busyboxinstaller.BuildConfig;
import com.smartpack.busyboxinstaller.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 17, 2020
 */

public class AboutActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageView mDeveloper = findViewById(R.id.developer);
        MaterialTextView mAppName = findViewById(R.id.app_title);
        MaterialTextView mCredits = findViewById(R.id.credits);
        MaterialTextView mChangeLogs = findViewById(R.id.change_logs);
        MaterialTextView mCancel = findViewById(R.id.cancel_button);
        mAppName.setText(getString(R.string.app_name) + (Utils.isSupporter(this) ? " Pro v" : " v") + BuildConfig.VERSION_NAME);
        mCredits.setText(getString(R.string.credits_summary));
        String change_log = null;
        try {
            change_log = new JSONObject(Objects.requireNonNull(Utils.readAssetFile(
                    this, "changelogs.json"))).getString("releaseNotes");
        } catch (JSONException ignored) {
        }
        mChangeLogs.setText(change_log);
        mDeveloper.setOnClickListener(v -> {
            Utils.launchUrl("https://github.com/sunilpaulmathew", this);
        });
        mBack.setOnClickListener(v -> {
            onBackPressed();
        });
        mCancel.setOnClickListener(v -> {
            onBackPressed();
        });
    }

}