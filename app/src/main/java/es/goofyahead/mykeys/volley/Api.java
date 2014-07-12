package es.goofyahead.mykeys.volley;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.inject.Inject;

import es.goofyahead.mykeys.interfaces.ChallengeListener;
import es.goofyahead.mykeys.interfaces.ResponseListener;

public class Api {

    @Inject
    RequestQueue queue;

    private String provisionalUlr = "https://192.168.0.111/keys";
    private String provisionalChallenge = "https://192.168.0.111/challenge/";

    public void sendKey(final String key, final String name, final long ttl, final ResponseListener listener) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, provisionalUlr, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // response
                Log.d("Response", response);
                listener.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                Log.d("Error.Response", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("key", key);
                params.put("ttl", String.valueOf(ttl));
                params.put("name", name);
                return params;
            }
        };
        queue.add(postRequest);
    }

    public void getChallenge(final String user, final ChallengeListener listener) {
        StringRequest postRequest = new StringRequest(Request.Method.GET, provisionalChallenge + user, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // response
                Log.d("Response", response);
                listener.onChallenge(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                Log.d("Error.Response", error.getMessage());
            }
        });
        queue.add(postRequest);
    }
}
