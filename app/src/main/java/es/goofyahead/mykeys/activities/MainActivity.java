package es.goofyahead.mykeys.activities;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import es.goofyahead.mykeys.adapters.DevicesAdapter;
import es.goofyahead.mykeys.models.Device;
import roboguice.activity.RoboActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.braintreepayments.api.dropin.BraintreePaymentActivity;
import com.braintreepayments.api.dropin.Customization;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.inject.Inject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import es.goofyahead.mykeys.R;
import es.goofyahead.mykeys.interfaces.ChallengeListener;
import es.goofyahead.mykeys.interfaces.ResponseListener;
import es.goofyahead.mykeys.volley.Api;
import es.goofyahead.utils.CustomSSL;
import roboguice.inject.InjectView;

public class MainActivity extends RoboActivity implements ResponseListener {

    private static final String TAG = MainActivity.class.getName();
    private static final String SERVER_BASE = "http://www.madgeeklabs.com:3000";
    private static final int REQUEST_CODE = Menu.FIRST;
    private AsyncHttpClient client = new AsyncHttpClient();
    private String clientToken;
    @Inject
    Api api;
    @InjectView (R.id.listView)
    ListView devicesListView;

    private KeyPair kp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CustomSSL.nuke();

        api.getDevices(this);

        devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Device device = (Device) adapterView.getItemAtPosition(i);

                Intent deviceActivity = new Intent(MainActivity.this, DeviceActivity.class );
                deviceActivity.putExtra("DEVICE", device);
                startActivity(deviceActivity);

            }
        });
//        getToken();
        // Original text

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSuccess(String response) {
        ArrayList<Device> devicesList = new ArrayList<Device>();
        try {
            JSONArray devices = new JSONArray(response);

            for (int x = 0; x < devices.length(); x++) {
                JSONObject device = devices.getJSONObject(x);

                Device current = new Device(device.getString("name"), device.getInt("price"));
                devicesList.add(current);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        devicesListView.setAdapter(new DevicesAdapter(this, devicesList));

        Log.d(TAG, response);
    }

    @Override
    public void onError(VolleyError error) {
        // TODO Auto-generated method stub

    }



}
