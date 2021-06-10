package com.smartpack.busyboxinstaller.utils;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.smartpack.busyboxinstaller.BuildConfig;
import com.smartpack.busyboxinstaller.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 17, 2020
 */

public class AboutActivity extends AppCompatActivity {

    private final ArrayList <RecycleViewItem> mData = new ArrayList<>();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        AppCompatImageButton mBack = findViewById(R.id.back);
        MaterialCardView mCreditsCard = findViewById(R.id.credits_card);
        MaterialCardView mChangeLogsCard = findViewById(R.id.change_log_card);
        MaterialTextView mChangeLogs = findViewById(R.id.change_logs);
        MaterialTextView mCancel = findViewById(R.id.cancel_button);
        MaterialTextView mVersion = findViewById(R.id.version);
        MaterialTextView mCopyright = findViewById(R.id.copyright);
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);

        mVersion.setText(getString(R.string.version) + (Utils.isSupporter(this) ? ": Pro " : ": ") + BuildConfig.VERSION_NAME);
        mCopyright.setText(getString(R.string.copyright, "2021-2022, sunilpaulmathew"));

        if (!Utils.isDarkTheme(this)) {
            mCreditsCard.setBackgroundColor(Color.LTGRAY);
            mChangeLogsCard.setBackgroundColor(Color.LTGRAY);
        }

        mData.add(new RecycleViewItem("Willi Ye", "Code Contributions", "https://github.com/Grarak"));
        mData.add(new RecycleViewItem("topjohnwu", "libsu", "https://github.com/topjohnwu/libsu"));
        mData.add(new RecycleViewItem("topjohnwu", "BusyBox Binaries", "https://github.com/topjohnwu/ndk-box-kitchen"));
        mData.add(new RecycleViewItem("https://busybox.net/", "BusyBox Binaries (old versions)", "https://busybox.net/"));
        mData.add(new RecycleViewItem("linsui", "BusyBox Binaries (building)", "https://gitlab.com/linsui"));
        mData.add(new RecycleViewItem("Lennoard Silva", "Code Contributions & Portuguese (Brazilian) Translations", "https://github.com/Lennoard"));
        mData.add(new RecycleViewItem("sajid_islam", "App Icon", "https://t.me/sajid_islam"));
        mData.add(new RecycleViewItem("FiestaLake", "Korean Translations", "https://github.com/FiestaLake"));
        mData.add(new RecycleViewItem("Mikesew1320", "Amharic & Russian Translations", "https://github.com/Mikesew1320"));
        mData.add(new RecycleViewItem("tsiflimagas", "Greek Translations", "https://github.com/tsiflimagas"));
        mData.add(new RecycleViewItem("Hafitz Setya", "Indonesian Translations", "https://github.com/breakdowns"));
        mData.add(new RecycleViewItem("Jonas. Ned", "Czech Translations", null));
        mData.add(new RecycleViewItem("Cold", "Spanish Translations", null));
        mData.add(new RecycleViewItem("omerakgoz34", "Turkish Translations", null));
        mData.add(new RecycleViewItem("Waiyan", "Burmese Translations", null));
        mData.add(new RecycleViewItem("Khalid1717", "Arabic Translations", null));
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        RecycleViewAdapter mRecycleViewAdapter = new RecycleViewAdapter(mData);
        mRecyclerView.setAdapter(mRecycleViewAdapter);

        String change_log = null;
        try {
            change_log = new JSONObject(Objects.requireNonNull(Utils.readAssetFile(
                    this, "changelogs.json"))).getString("releaseNotes");
        } catch (JSONException ignored) {
        }
        mChangeLogs.setText(change_log);
        mCopyright.setOnClickListener(v -> {
            Utils.launchUrl("https://github.com/sunilpaulmathew", this);
        });
        mBack.setOnClickListener(v -> {
            onBackPressed();
        });
        mCancel.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    private static class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {

        private static ArrayList<RecycleViewItem> data;

        public RecycleViewAdapter(ArrayList<RecycleViewItem> data) {
            RecycleViewAdapter.data = data;
        }

        @NonNull
        @Override
        public RecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_about, parent, false);
            return new RecycleViewAdapter.ViewHolder(rowItem);
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onBindViewHolder(@NonNull RecycleViewAdapter.ViewHolder holder, int position) {
            holder.Title.setText(data.get(position).getTitle());
            holder.Description.setText(data.get(position).getDescription());
            holder.Description.setPaintFlags(holder.Description.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            holder.Description.setOnClickListener(v -> {
                if (data.get(position).getURL() != null) {
                    Utils.launchUrl(data.get(position).getURL(), holder.Description.getContext());
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            private MaterialTextView Title, Description;

            public ViewHolder(View view) {
                super(view);
                this.Title = view.findViewById(R.id.title);
                this.Description = view.findViewById(R.id.description);
            }
        }
    }

    private static class RecycleViewItem implements Serializable {
        private String mTitle;
        private String mDescription;
        private String mURL;

        public RecycleViewItem(String title, String description, String url) {
            this.mTitle = title;
            this.mDescription = description;
            this.mURL = url;
        }

        public String getTitle() {
            return mTitle;
        }

        public String getDescription() {
            return mDescription;
        }

        public String getURL() {
            return mURL;
        }

    }

}