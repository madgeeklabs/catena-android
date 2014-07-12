package es.goofyahead.mykeys.activities;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import roboguice.activity.RoboActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import es.goofyahead.mykeys.R;
import es.goofyahead.mykeys.interfaces.ChallengeListener;
import es.goofyahead.mykeys.interfaces.ResponseListener;
import es.goofyahead.mykeys.volley.Api;
import es.goofyahead.utils.CustomSSL;

public class MainActivity extends RoboActivity implements ResponseListener, ChallengeListener {

    private static final String TAG = MainActivity.class.getName();
    private static final String SERVER_BASE = "http://www.madgeeklabs.com:3000";
    private static final int REQUEST_CODE = Menu.FIRST;
    private AsyncHttpClient client = new AsyncHttpClient();
    private String clientToken;
    @Inject
    Api api;

    private KeyPair kp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CustomSSL.nuke();

        getToken();
        // Original text
        String theTestText = "hola";

        // Generate key pair for 1024-bit RSA encryption and decryption
        Key publicKey = null;
        Key privateKey = null;
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
        String keyToSend = joiner.join(listkeys);

        Log.d(TAG, "public key: " + keyToSend);

        // Encode the original data with RSA private key
        byte[] encodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("RSA/NONE/PKCS1Padding");
            c.init(Cipher.ENCRYPT_MODE, publicKey);
            encodedBytes = c.doFinal(theTestText.getBytes());
        } catch (Exception e) {
            Log.e(TAG, "RSA encryption error " + e.getMessage());
        }

        Log.d(TAG, "ENCODED");
        Log.d(TAG, Base64.encodeToString(encodedBytes, Base64.DEFAULT));

        // Decode the encoded data with RSA public key
        byte[] decodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("RSA/NONE/PKCS1Padding");
            c.init(Cipher.DECRYPT_MODE, privateKey);
            // decodedBytes = c.doFinal(Base64.decode(encodedBytes,
            // Base64.DEFAULT));
            decodedBytes = c.doFinal(Base64.decode(Base64.encodeToString(encodedBytes, Base64.DEFAULT).getBytes(), Base64.DEFAULT));
        } catch (Exception e) {
            Log.e(TAG, "RSA decryption error " + e.getMessage());
        }
        Log.d(TAG, "decoded: " + new String(decodedBytes));

        api.sendKey(keyToSend, "goofyahead", 4000, this);

        api.getChallenge("goofyahead", this);
    }

    public void onStartClick(View view) {
        Customization customization = new Customization.CustomizationBuilder()
                .primaryDescription("Awesome payment")
                .secondaryDescription("Using the Client SDK")
                .amount("$10.00")
                .submitButtonText("Pay")
                .build();

        Intent intent = new Intent(this, BraintreePaymentActivity.class);
        intent.putExtra(BraintreePaymentActivity.EXTRA_CUSTOMIZATION, customization);
        intent.putExtra(BraintreePaymentActivity.EXTRA_CLIENT_TOKEN, clientToken);

        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == BraintreePaymentActivity.RESULT_OK) {
            String paymentMethodNonce = data.getStringExtra(BraintreePaymentActivity.EXTRA_PAYMENT_METHOD_NONCE);

            RequestParams requestParams = new RequestParams();
            requestParams.put("payment_method_nonce", paymentMethodNonce);
            requestParams.put("amount", "10.00");

            client.post(SERVER_BASE + "/payment", requestParams, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String content) {
                    Log.d("successs", String.valueOf(content));
                    Toast.makeText(MainActivity.this, content, Toast.LENGTH_LONG).show();
                }

            });
        }
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
        Log.d(TAG, response);
    }

    @Override
    public void onError(VolleyError error) {
        // TODO Auto-generated method stub

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
    public void onChallenge(String challenge) {
        byte[] challengeArray = Base64.decode(challenge, Base64.DEFAULT);
        Log.d(TAG, "challenge that came from server is " + decode(challengeArray));
    }
}
