package com.mobioptions.limitprotection;

import com.google.android.gms.ads.InterstitialAd;

public class InterstitialInstance {
    InterstitialAd admob;

    public InterstitialInstance(InterstitialAd admob) {
        this.admob = admob;
    }

    public InterstitialAd getAdmob() {
        return admob;
    }

    public void setAdmob(InterstitialAd admob) {
        this.admob = admob;
    }

}
