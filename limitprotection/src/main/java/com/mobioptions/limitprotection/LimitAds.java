package com.mobioptions.limitprotection;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.KeyEvent;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import androidx.annotation.RequiresApi;

import static android.content.Context.MODE_PRIVATE;

public class LimitAds {

    private static LimitAds limitAdsSingleton;
    private Context context;

    private LimitAds() {
    }

    public static LimitAds getInstance() {
        if (limitAdsSingleton == null) { //if there is no instance available... create new one
            limitAdsSingleton = new LimitAds();
        }
        return limitAdsSingleton;
    }

    public void init(String appID, final Context context, final InitListener initListener) {
        this.context = context;
        final RequestQueue mRequestQueue;
        String url = "https://api.mobioptions.com/api/limitproject/get/" + appID;
        mRequestQueue = Volley.newRequestQueue(context);
        final StringRequest mStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject object = new JSONObject(response);
                    if (!object.getBoolean("status")) {
                        if (object.getInt("code") == 404) {
                            initListener.onError("Token Exception: Please specify a valid token");
                        } else {
                            initListener.onError("An Error Happened, Please Contact Admin");
                        }
                    } else {
                        Instance.projectId = object.getJSONObject("limitProject").getInt("id");
                        Instance.adsEnabled = object.getJSONObject("limitProject").getInt("ads_activated") == 1;
                        Instance.CLICK_LIMIT = object.getJSONObject("limitProject").getInt("clicks");
                        Instance.BAN_DURATION = object.getJSONObject("limitProject").getInt("ban_hours") * 3600000;
                        JSONArray ads = object.getJSONObject("limitProject").getJSONArray("ads");
                        Instance.InterstitialInstances = new HashMap<>();
                        Log.D(ads.toString());
                        for (int i = 0; i < ads.length(); i++) {
                            JSONObject ad = ads.getJSONObject(i);
                            if (ad.getString("type").equals("interstitial")) {
                                //admob stuff
                                InterstitialAd interstitialAd = new InterstitialAd(context);
                                interstitialAd.setAdUnitId(ad.getString("admob_id"));
                                Instance.InterstitialInstances.put(ad.getString("name"), new InterstitialInstance(interstitialAd));
                            }
                        }
                        initListener.onInit();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.D(error.getMessage());
            }
        });
        mRequestQueue.add(mStringRequest);
    }


    public void loadAd(String name) {
        if (Instance.adsEnabled && shouldShowAds()) {
            if (Instance.InterstitialInstances.containsKey(name)) {
                Instance.InterstitialInstances.get(name).getAdmob().loadAd(new AdRequest.Builder().build());
            } else
                Log.D("the string provided '" + name + "' doesn't exist in your project");
        }
        else
            Log.D("Ads not enabled");
    }

    public void showInterstitial(final String name) {
        if (Instance.adsEnabled && shouldShowAds()) {
            Instance.InterstitialInstances.get(name).getAdmob().show();
            Instance.InterstitialInstances.get(name).getAdmob().setAdListener(new AdListener() {
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    Log.D("ad clicked");
                    Instance.Click_Count++;
                    if(Instance.Click_Count >= Instance.CLICK_LIMIT)
                        banUser();

                }
            });
        } else
            Log.D("the string provided '" + name + "' doesn't exist in your project");
    }


    public boolean shouldShowAds() {
        SharedPreferences prefs = context.getSharedPreferences("LIMIT_ADS", MODE_PRIVATE);
        long banedFor = prefs.getLong("bannedTill", 0);

        if(banedFor > System.currentTimeMillis()){
            Log.D("banned for "+(banedFor-System.currentTimeMillis())/60000+" min");
            return false;
        }
        return true;
    }

    public void banUser() {
        Log.D("banning user");
        SharedPreferences.Editor editor = context.getSharedPreferences("LIMIT_ADS", MODE_PRIVATE).edit();
        editor.putLong("bannedTill", System.currentTimeMillis() + Instance.BAN_DURATION);
        editor.apply();
    }

    public interface InitListener {
        void onInit();
        void onError(String error);
    }
}
