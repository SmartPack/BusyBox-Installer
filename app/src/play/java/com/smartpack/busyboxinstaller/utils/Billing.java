package com.smartpack.busyboxinstaller.utils;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 29, 2021
 */
public class Billing {

    public static void launchDonationMenu(Context context) {
        Intent donations = new Intent(context, BillingActivity.class);
        context.startActivity(donations);
    }

}