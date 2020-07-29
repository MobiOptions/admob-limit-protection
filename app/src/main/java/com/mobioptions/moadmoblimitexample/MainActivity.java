package com.mobioptions.moadmoblimitexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.Toast;

import com.mobioptions.limitprotection.LimitAds;
import com.mobioptions.limitprotection.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//
        final LimitAds ads = LimitAds.getInstance();
        ads.init("skYwsnhYZnfjck4ziSG29Fx2d0dEx6", this, new LimitAds.InitListener() {
            @Override
            public void onInit() {
                Log.D("success");
                ads.loadAd("interstitial_1");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ads.showInterstitial("interstitial_1");

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                                Toast.makeText(getApplicationContext(),"hiding", Toast.LENGTH_SHORT).show();
                            }
                        },2000);
                    }
                },3000);
            }

            @Override
            public void onError(String error) {
                Log.D(error);
            }
        });

    }

}
