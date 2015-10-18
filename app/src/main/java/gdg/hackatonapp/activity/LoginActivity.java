package gdg.hackatonapp.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.json.JSONObject;

import java.util.Arrays;

import gdg.hackatonapp.R;
import gdg.hackatonapp.entity.User;
import gdg.hackatonapp.services.Service;
import gdg.hackatonapp.util.Utils;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 0;
    private ProgressDialog progressDialog;
    private GoogleApiClient plusClient;
    private CallbackManager callbackManager;
    private boolean mIntentInProgress;
    private LoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_login);

        plusClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();


        if (plusClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(plusClient);
            plusClient.disconnect();
            plusClient.connect();
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Conectando...");

        loginButton = (LoginButton) findViewById(R.id.login_button);

        loginButton.setReadPermissions("public_profile, email, user_birthday, user_friends");
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                            String mPhoneNumber = tMgr.getLine1Number();

                            String id = object.getString("id");
                            String personName = object.getString("name");

                            User user = new User();
                            user.setId(0L);
                            user.setGcm_id(id);
                            user.setNroTelefono(mPhoneNumber);
                            user.setNombres(personName);
                            user.setId_facebook(id);

                            new Utils(LoginActivity.this).writeUser(user);

//                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
//                            finish();
                            new SendTask().execute(user);

                        } catch (Exception e) {
                            LoginManager.getInstance().logOut();
                            new Utils(LoginActivity.this).writeUser(null);
                            e.printStackTrace();
                        }
                    }
                }).executeAsync();
            }

            @Override
            public void onCancel() {
                Log.i("Facebook", "Login cancelado");
            }

            @Override
            public void onError(FacebookException exception) {
                exception.printStackTrace();
            }
        });

        findViewById(R.id.btnLoginFacebook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile, email, user_birthday, user_friends"));
            }
        });
        findViewById(R.id.btnLoginGoogle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plusClient.connect();
                progressDialog.show();
            }
        });


        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plusClient.connect();
                progressDialog.show();
            }
        });
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        progressDialog.dismiss();
        if (Plus.PeopleApi.getCurrentPerson(plusClient) != null) {
            TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String mPhoneNumber = tMgr.getLine1Number();

            Person currentPerson = Plus.PeopleApi.getCurrentPerson(plusClient);
            String personName = currentPerson.getDisplayName();
            String personPhoto = currentPerson.getImage().getUrl();
            String personId = currentPerson.getId();
            String personEmail = currentPerson.getUrl();

            User user = new User();
            user.setId(0L);
            user.setGcm_id(personId);
            user.setEmail(personEmail);
            user.setNroTelefono(mPhoneNumber);
            user.setNombres(personName);
            user.setId_google(personPhoto);

            new Utils(this).writeUser(user);
//            startActivity(new Intent(LoginActivity.this, MainActivity.class));
//            finish();
            new SendTask().execute(user);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        try {
            progressDialog.dismiss();
            if (!mIntentInProgress && result.hasResolution()) {
                try {
                    mIntentInProgress = true;
                    startIntentSenderForResult(result.getResolution().getIntentSender(),
                            RC_SIGN_IN, null, 0, 0, 0);
                } catch (IntentSender.SendIntentException e) {
                    mIntentInProgress = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            mIntentInProgress = false;
        }
        if (callbackManager != null)
            callbackManager.onActivityResult(requestCode, responseCode, intent);
    }

    public class SendTask extends AsyncTask<User, Void, Void> {

        long idResponse = 0;

        @Override
        protected Void doInBackground(User... users) {
            try {
                idResponse = new Service(LoginActivity.this).login(users[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (idResponse != 0) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "ERROR AL ENVIAR DATOS", Toast.LENGTH_LONG).show();

                if(plusClient.isConnected()){
                    Plus.AccountApi.clearDefaultAccount(plusClient);
                    plusClient.disconnect();
                }else{
                    LoginManager.getInstance().logOut();
                }

                new Utils(LoginActivity.this).writeUser(null);
            }
        }
    }
}
