package es.goofyahead.mykeys.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.braintreepayments.api.dropin.BraintreePaymentActivity;
import com.braintreepayments.api.dropin.Customization;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.inject.Inject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import es.goofyahead.mykeys.R;
import es.goofyahead.mykeys.custom.NetWorkImageViewCircle;
import es.goofyahead.mykeys.interfaces.ChallengeListener;
import es.goofyahead.mykeys.interfaces.ResponseListener;
import es.goofyahead.mykeys.models.Device;
import es.goofyahead.mykeys.volley.Api;
import es.goofyahead.utils.CustomSSL;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

public class DeviceActivity extends RoboActivity implements ResponseListener, ChallengeListener {

    private static final String TAG = DeviceActivity.class.getName();
    @Inject
    Api api;
    private KeyPair kp;
    Device currentDevice;
    private static final String SERVER_BASE = "http://www.madgeeklabs.com:3000";
    private static final int REQUEST_CODE = Menu.FIRST;
    private AsyncHttpClient client = new AsyncHttpClient();
    private String clientToken;
    @InjectView(R.id.register)
    Button requestAccess;
    @InjectView(R.id.item_description)
    TextView description;
    @InjectView(R.id.item_price) TextView price;
    private boolean onUse;
    @InjectView(R.id.btn_start) Button paymentOrOff;
    private String decodedChallenge;
    @InjectView(R.id.detail_item_image)
    NetworkImageView image;
    @Inject
    ImageLoader loader;
    @InjectView(R.id.user_avatar)
    NetWorkImageViewCircle avatarImg;
    @InjectView(R.id.ratingBar)
    RatingBar rating;

    private static final String USER = "goofyahead";
    private static final String AVATAR = "http://gravatar.com/avatar/cd351ae83b3a49c828bc6b4b5320844e";

    Key publicKey = null;
    Key privateKey = null;
    private String keyToSend;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        currentDevice = (Device) getIntent().getExtras().getSerializable("DEVICE");

        description.setText(currentDevice.getName());
        price.setText("$" + currentDevice.getCost() + ".00 per use");
        image.setImageUrl(currentDevice.getImageUrl(), loader);
        avatarImg.setImageUrl(currentDevice.getGravatar(), loader);
        rating.setRating(3.5f);
        CustomSSL.nuke();

        getToken();

        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024);
            kp = kpg.genKeyPair();
            publicKey = kp.getPublic();
            privateKey = kp.getPrivate();
        } catch (Exception e) {
            Log.e(TAG, "RSA key pair error");
        }

        String key = Base64.encodeToString(publicKey.getEncoded(), Base64.NO_WRAP);
        Iterable<String> listkeys = Splitter.fixedLength(64).split(key);

        Joiner joiner = Joiner.on("\n").skipNulls();
        keyToSend = joiner.join(listkeys);

        Log.d(TAG, "public key: " + keyToSend);

        requestAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Generate key pair for 1024-bit RSA encryption and decryption
                api.sendKey(keyToSend, USER, 4000, DeviceActivity.this);
            }
        });
    }


    private void getToken() {
        client.get(SERVER_BASE + "/token", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String content) {
                clientToken = content;
                findViewById(R.id.btn_start).setEnabled(true);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                Log.d("error", String.valueOf(statusCode));
                Log.d("error", String.valueOf(errorResponse));
            }
        });
    }

    private String decode(byte[] response) {
        try {
            Cipher c = Cipher.getInstance("RSA/NONE/PKCS1Padding");
            c.init(Cipher.DECRYPT_MODE, kp.getPrivate());
            byte[] decodedBytes = c.doFinal(response);
            // byte[] decodedBytes = c.doFinal(Base64.decode(response,
            // Base64.DEFAULT));
            Log.d(TAG, "decoded from challenge: " + new String(decodedBytes));
            return new String(decodedBytes);
        } catch (IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.device, menu);
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
    public void onChallenge(String challenge) {
        byte[] challengeArray = Base64.decode(challenge, Base64.DEFAULT);
        decodedChallenge = decode(challengeArray);
        api.useDevice(decodedChallenge, USER);
        Log.d(TAG, "challenge that came from server is " + decodedChallenge);
    }

    @Override
    public void onSuccess(String response) {

    }

    @Override
    public void onError(VolleyError error) {

    }

    public void onStartClick(View view) {
        if (onUse) {
            paymentOrOff.setText("Pay for use");
            api.offDevice(decodedChallenge, USER);
        }else {
            Customization customization = new Customization.CustomizationBuilder()
                    .primaryDescription("Awesome payment")
                    .secondaryDescription("Using the Client SDK")
                    .amount("$" + currentDevice.getCost() + ".00")
                    .submitButtonText("Pay")
                    .build();

            Intent intent = new Intent(this, BraintreePaymentActivity.class);
            intent.putExtra(BraintreePaymentActivity.EXTRA_CUSTOMIZATION, customization);
            intent.putExtra(BraintreePaymentActivity.EXTRA_CLIENT_TOKEN, clientToken);

            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == BraintreePaymentActivity.RESULT_OK) {
            String paymentMethodNonce = data.getStringExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE);

            api.getChallenge(USER, this);
            api.sendTransaction(currentDevice.getCost());
            onUse = true;

            paymentOrOff.setText("Turn OFF");

            RequestParams requestParams = new RequestParams();
            requestParams.put("payment_method_nonce", paymentMethodNonce);
            requestParams.put("amount", "10.00");

            client.post(SERVER_BASE + "/payment", requestParams, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String content) {
                    Log.d("successs", String.valueOf(content));
                    Toast.makeText(DeviceActivity.this, content, Toast.LENGTH_LONG).show();
                }

            });
        }
    }
}
