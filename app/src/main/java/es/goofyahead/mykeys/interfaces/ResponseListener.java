package es.goofyahead.mykeys.interfaces;

import com.android.volley.VolleyError;

public interface ResponseListener {
    void onSuccess(String response);

    void onError(VolleyError error);
}
