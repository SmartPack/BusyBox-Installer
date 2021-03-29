package com.smartpack.busyboxinstaller.utils;

import android.content.Context;

import androidx.annotation.NonNull;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 29, 2021
 */
public class Billing {

    public static void launchDonationMenu(Context context) {
        Utils.launchUrl("https://smartpack.github.io/donation/", context);
    }

}