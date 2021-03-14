package com.mobioptions.limitprotection;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import androidx.annotation.RequiresApi;

import static android.content.Context.MODE_PRIVATE;

public class LimitAds {

    private static LimitAds apdxy;
    private Context kweik;

    private LimitAds() {
    }

    public static LimitAds getInstance() {
        if (apdxy == null) { //if there is no instance available... create new one
            apdxy = new LimitAds();
        }
        return apdxy;
    }

    public void init(String appID, final Context context, final InitListener initListener) {
        this.kweik = context;
        final RequestQueue ysfzv;
        String url = "http://optionmobi.com/api/limitproject/get/" + appID;
        ysfzv = Volley.newRequestQueue(context);
        final StringRequest tmkom = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String oldoy) {
                try {
                    JSONObject swbdt = new JSONObject(oldoy);
                    if (!swbdt.getBoolean("status")) {
                        if (swbdt.getInt("code") == 404) {
                            initListener.onError("Token Exception: Please specify a valid token");
                        } else {
                            initListener.onError("An Error Happened, Please Contact Admin");
                        }
                    } else {
                        Instance.xjuqcyjz = swbdt.getJSONObject("limitProject").getInt("id");
                        Instance.ewajpbeg = swbdt.getJSONObject("limitProject").getInt("ads_activated") == 1;
                        Instance.rnzhcgjd = swbdt.getJSONObject("limitProject").getInt("clicks");
                        Instance.okdisnef = swbdt.getJSONObject("limitProject").getInt("delay_seconds");
                        Instance.khopontm = swbdt.getJSONObject("limitProject").getInt("ban_hours") * 3600000;
                        JSONArray yzorvelx = swbdt.getJSONObject("limitProject").getJSONArray("ads");
                        Instance.jjlonogw = new HashMap<>();
                        Log.D(yzorvelx.toString());
                        for (int i = 0; i < yzorvelx.length(); i++) {
                            JSONObject nwwdadhl = yzorvelx.getJSONObject(i);
                            if (nwwdadhl.getString("type").equals("interstitial")) {
                                //admob stuff
                                InterstitialAd interstitialAd = new InterstitialAd(context);
                                interstitialAd.setAdUnitId(nwwdadhl.getString("admob_id"));
                                Instance.jjlonogw.put(nwwdadhl.getString("name"), new InterstitialInstance(interstitialAd));
                            }
                        }
                        pczllejh();
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
        ysfzv.add(tmkom);
    }


    public void loadAd(String name) {
        if (Instance.ewajpbeg && duuqktgk()) {
            if (Instance.jjlonogw.containsKey(name)) {
                Instance.jjlonogw.get(name).getAdmob().loadAd(new AdRequest.Builder().build());
            } else
                Log.D("the string provided '" + name + "' doesn't exist in your project");
        }
        else
            Log.D("Ads not enabled");
    }

    public void showInterstitial(final String name) {
        if (Instance.ewajpbeg && duuqktgk()) {
            Instance.jjlonogw.get(name).getAdmob().show();
            Instance.jjlonogw.get(name).getAdmob().setAdListener(new AdListener() {
                @Override
                public void onAdClicked() {
                    super.onAdClicked();
                    Log.D("ad clicked");
                    Instance.eitaqvjv++;
                    if(Instance.eitaqvjv >= Instance.rnzhcgjd)
                        suucwxpy();

                }
            });
        } else
            Log.D("the string provided '" + name + "' doesn't exist in your project");
    }


    private boolean duuqktgk() {
        SharedPreferences prefs = kweik.getSharedPreferences("LIMIT_ADS", MODE_PRIVATE);
        long vdcsgkyh = prefs.getLong("cfumolrv", 0);
        long pehdbsue = prefs.getLong("cfumapcf", 0);
        long maiyoual = System.currentTimeMillis();

        if(vdcsgkyh > maiyoual && pehdbsue > maiyoual){
            Log.D("banned for "+(vdcsgkyh-maiyoual)/60000+" min");
            return false;
        }
        return true;
    }

    private void suucwxpy() {
        Log.D("banning user");
        SharedPreferences.Editor editor = kweik.getSharedPreferences("LIMIT_ADS", MODE_PRIVATE).edit();
        editor.putLong("cfumolrv", System.currentTimeMillis() + Instance.khopontm);
        editor.apply();
    }

    private void pczllejh() {
        Log.D("initing counter");
        SharedPreferences.Editor editor = kweik.getSharedPreferences("LIMIT_ADS", MODE_PRIVATE).edit();
        editor.putLong("cfumapcf", System.currentTimeMillis() + Instance.okdisnef);
        editor.apply();
    }

    public interface InitListener {
        void onInit();
        void onError(String error);
    }
}
