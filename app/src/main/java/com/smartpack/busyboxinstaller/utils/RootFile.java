package com.smartpack.busyboxinstaller.utils;

import androidx.annotation.NonNull;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 11, 2020
 * Based on the original implementation on Kernel Adiutor by
 * Willi Ye <williye97@gmail.com>
 */

public class RootFile {

    private final String mFile;
    private RootUtils.SU mSU;

    public RootFile(String file) {
        mFile = file;
        mSU = RootUtils.getSU();
    }

    RootFile(String file, RootUtils.SU su) {
        mFile = file;
        mSU = su;
    }

    boolean exists() {
        String output = mSU.runCommand("[ -e " + mFile + " ] && echo true");
        return output != null && output.equals("true");
    }

    String readFile() {
        return mSU.runCommand("cat '" + mFile + "'");
    }

    @Override
    @NonNull
    public String toString() {
        return mFile;
    }

}