package com.smartpack.busyboxinstaller.utils;

import androidx.annotation.NonNull;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 11, 2020
 * Based on the original implementation on Kernel Adiutor by
 * Willi Ye <williye97@gmail.com>
 */

public class RootFile {

    private final String mFile;

    RootFile(String file) {
        mFile = file;
    }

    boolean exists() {
        String output = RootUtils.runCommand("[ -e " + mFile + " ] && echo true");
        return !output.isEmpty() && output.equals("true");
    }

    String readFile() {
        return RootUtils.runCommand("cat '" + mFile + "'");
    }

    @Override
    @NonNull
    public String toString() {
        return mFile;
    }

}