package elyervesson.login_google;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import java.util.List;


public class SplashScreenActivity extends Activity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int SIGN_IN_CODE = 56465;

    private GoogleApiClient googleApiClient;
    private ConnectionResult connectionResult;

    private boolean isConsentScreenOpened;
    private boolean isSignInButtonClicked;

    public static final String PREFERENCE_NAME = "USER_PREFERENCE";

    public static final String USER_ID = "USER_ID";
    public static final String USER_NOME = "USER_NOME";
    public static final String USER_EMAIL = "USER_EMAIL";
    public static final String USER_URL_PHOTO = "USER_URL_PHOTO";
    public static final String USER_STATUS = "USER_STATUS";

    private static final int DURATION_MILLIS = 3300;

    // VIEWS
    private ProgressBar pbContainer;
    private SignInButton btSignInDefault;
    private LinearLayout layoutGooglePlus;

    private boolean conectado;
    private boolean jatentouabrirmain = false;
    private boolean jatentoulogar = false;
    private boolean closed = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        conectado = sharedPreferences.getBoolean(USER_STATUS, false);

        getSettingsPreferences();

        accessViews();

        googleApiClient = new GoogleApiClient.Builder(SplashScreenActivity.this)
                .addConnectionCallbacks(SplashScreenActivity.this)
                .addOnConnectionFailedListener(SplashScreenActivity.this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(DURATION_MILLIS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (!jatentouabrirmain) {
                        jatentouabrirmain = true;
                        if (conectado) {
                            chamarTelaMain();
                        }
                    }

                    if (googleApiClient != null) {
                        googleApiClient.connect();
                    }
                }
            }
        };
        timer.start();
    }

    //USUARIO JA TENTOU LOGAR
    public void getSettingsPreferences() {
        if (conectado) {
            chamarTelaMain();
        }
    }

    private void chamarTelaMain() {
        if (!isActivityRunning(MainActivity.class) && !closed) {
            startActivity(new Intent(this, MainActivity.class));
        }
        closed = true;
        finish();
    }
    @Override
    public void onStart() {
        super.onStart();
        if(googleApiClient != null){
            googleApiClient.connect();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closed = true;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SIGN_IN_CODE) {
            isConsentScreenOpened = false;

            if (resultCode != RESULT_OK) {
                isSignInButtonClicked = false;
            }if (!googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        }
    }

    //MANTER
    private void chamarTelaPrincipal() {
        if (!isActivityRunning(MainActivity.class) && !closed) {
            startActivity(new Intent(this, MainActivity.class));
        }
        closed = true;
        finish();
    }
    //MANTER
    protected Boolean isActivityRunning(Class activityClass) {
        ActivityManager activityManager = (ActivityManager) getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (activityClass.getCanonicalName().equalsIgnoreCase(task.baseActivity.getClassName())) {
                return true;
            }
        }

        return false;
    }

    // UTIL
    private void accessViews() {
        layoutGooglePlus = (LinearLayout) findViewById(R.id.layoutGooglePlus);
        pbContainer = (ProgressBar) findViewById(R.id.pbContainer);

        btSignInDefault = (SignInButton) findViewById(R.id.btSignInDefault);
        btSignInDefault.setOnClickListener(SplashScreenActivity.this);
        btSignInDefault.setVisibility(SignInButton.GONE);
    }

    //MANTER
    private void showUi(boolean statusProgressBar) {
        if (!statusProgressBar) {
            layoutGooglePlus.setVisibility(View.VISIBLE);
            pbContainer.setVisibility(View.GONE);
        } else {
            layoutGooglePlus.setVisibility(View.GONE);
            pbContainer.setVisibility(View.VISIBLE);
        }
    }

    //MANTER
    private void resolveSignIn() {
        if (connectionResult != null && connectionResult.hasResolution()) {
            try {
                isConsentScreenOpened = true;
                connectionResult.startResolutionForResult(SplashScreenActivity.this, SIGN_IN_CODE);
            } catch (IntentSender.SendIntentException e) {
                isConsentScreenOpened = false;
                googleApiClient.connect();
            }
        }
    }

    // MANTER
    private void getDataProfile() {
        Person pessoa = Plus.PeopleApi.getCurrentPerson(googleApiClient);

        if (pessoa != null) {
            SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            String pessoaId = pessoa.getId();
            String name = pessoa.getDisplayName();
            String email = Plus.AccountApi.getAccountName(googleApiClient);
            String urlImage = pessoa.getImage().getUrl();

            editor.putString(USER_ID, pessoaId);
            editor.putString(USER_NOME, name);
            editor.putString(USER_EMAIL, email);
            editor.putString(USER_URL_PHOTO, urlImage);

            editor.apply();
            chamarTelaPrincipal();
        } else {
            Toast.makeText(SplashScreenActivity.this, getString(R.string.login_error), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btSignInDefault) {
            if (!googleApiClient.isConnecting()) {
                isSignInButtonClicked = true;
                showUi(true);
                resolveSignIn();
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        isSignInButtonClicked = false;
        showUi(false);
        getDataProfile();
    }


    @Override
    public void onConnectionSuspended(int cause) {
        googleApiClient.connect();
        showUi(false);
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!conectado) {
            if (!result.hasResolution()) {
                GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), SplashScreenActivity.this, 0).show();
                return;
            }

            if (!isConsentScreenOpened) {
                connectionResult = result;

                if (jatentoulogar) {
                    btSignInDefault.setVisibility(SignInButton.VISIBLE);
                    showUi(false);
                }

                if (isSignInButtonClicked || !jatentoulogar) {
                    resolveSignIn();
                    jatentoulogar = true;
                }
            }
        } else {
            showUi(false);
        }
    }
}