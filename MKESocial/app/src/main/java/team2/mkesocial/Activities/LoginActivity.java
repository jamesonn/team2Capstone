package team2.mkesocial.Activities;

import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GithubAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import Firebase.User;
import team2.mkesocial.Fragments.PhoneLoginDialogFragment;
import team2.mkesocial.R;

public class LoginActivity extends FragmentActivity implements PhoneLoginDialogFragment.PhoneLoginDialogListener {
    private TextView signUp;
    private Button login, githubLogin;
    private EditText userName, password;

    private AuthCredential credential;

    private static final String PROTECTED_RESOURCE_URL = "https://github.com/login/oauth/authorize";
    private String mkeSocialCallback = "https://mkesocial-3f65e.firebaseapp.com/__/auth/handler";

    private String mVerificationId;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private static FirebaseAuth mAuth;
    private CallbackManager mCallbackManager;
    public static GoogleSignInClient mGoogleSignInClient;

    private void signUp(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            user.sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(LoginActivity.this, "Verification Email Sent", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            // If sign in fails, display a message to the user.

                            Toast.makeText(LoginActivity.this, "Sign-Up failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            new User(user).add();
                            Toast.makeText(LoginActivity.this, "Sign In Successful", Toast.LENGTH_SHORT).show();
                            logMeRightIn();
                        } else {
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signIn(GoogleSignInClient client){
        Intent signInIntent = client.getSignInIntent();
        startActivityForResult(signInIntent, 1);
    }

    private void signIn(String pn){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                pn,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks);

        DialogFragment dialog = new PhoneLoginDialogFragment();
        dialog.show(getFragmentManager(), "PhoneLoginDialogFragment");
        dialog.onAttach(LoginActivity.this);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            new User(user).add();
                            Toast.makeText(LoginActivity.this, "Sign In Successful", Toast.LENGTH_SHORT).show();
                            logMeRightIn();
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                            }
                        }
                    }
                });
    }

    private void signIn(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            if(user.isEmailVerified()) {
                                Toast.makeText(LoginActivity.this, "Sign In Successful",
                                        Toast.LENGTH_SHORT).show();
                                new User(mAuth.getCurrentUser()).add();
                                logMeRightIn();
                            } else {
                                Toast.makeText(LoginActivity.this, "Please verify your email",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Sign In failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void verifyGithubToken(AuthCredential token){
        mAuth.signInWithCredential(token)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            logMeRightIn();
                        }
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void logMeRightIn() {
        Intent goToFeed = new Intent(LoginActivity.this, FeedActivity.class);
        startActivity(goToFeed);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        final GoogleSignInAccount account;
        try {
            account = completedTask.getResult(ApiException.class);
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                new User(account).add();
                                logMeRightIn();
                            } else {
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (ApiException e) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        signUp = (TextView) findViewById(R.id.signUp);
        login = (Button) findViewById(R.id.sign_in_button);
        githubLogin = (Button) findViewById(R.id.github_sign_in_button);
        userName = (EditText) findViewById(R.id.userName);
        password = (EditText) findViewById(R.id.password);
        mAuth = FirebaseAuth.getInstance();

        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.facebook_login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {

                } else if (e instanceof FirebaseTooManyRequestsException) {

                }
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null){
            logMeRightIn();
        }

        SignInButton signInButton = (SignInButton) findViewById(R.id.google_sign_in);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(mGoogleSignInClient);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userName.getText().toString() != null) {
                    if (PhoneNumberUtils.isGlobalPhoneNumber(userName.getText().toString())) {
                        signIn(userName.getText().toString());
                    } else {
                        if(password.getText().toString() != null) {
                            signIn(userName.getText().toString(), password.getText().toString());
                        }
                    }
                }
            }
        });

        githubLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gitHubIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(PROTECTED_RESOURCE_URL + "?client_id=" + "29986eb3c7d405c3036c" + "&scope=repo&redirect_url=" + mkeSocialCallback));
                startActivity(gitHubIntent);
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userName.getText().toString() != null && password.getText().toString() != null) {
                    signUp(userName.getText().toString(), password.getText().toString());
                }
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            logMeRightIn();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Uri uri = getIntent().getData();
        if(uri != null && uri.toString().startsWith(mkeSocialCallback)){
            new AsyncTokenRetrieval().execute(uri.getQueryParameter("code"));
        }
    }

    @Override
    protected void onPause() { super.onPause(); }

    @Override
    public void onDialogPositiveClick(String inputCode) {
        verifyPhoneNumberWithCode( mVerificationId, inputCode);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

        if(requestCode == 64206){
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    public static GoogleSignInClient getGoogleSignIn(){
        return mGoogleSignInClient;
    }

    public static FirebaseAuth getAuth(){
        return mAuth;
    }

    private class AsyncTokenRetrieval extends AsyncTask<String, Void, AuthCredential> {
        @Override
        protected AuthCredential doInBackground(String... params) {
            OAuth2AccessToken accessToken = null;
            try {
                final String secretState = "secret" + new Random().nextInt(999_999);
                OAuth20Service service = new ServiceBuilder()
                        .apiKey("29986eb3c7d405c3036c")
                        .apiSecret("4053dd92819fb7d067685c741a72fa9a0cf40502")
                        .state(secretState)
                        .callback(mkeSocialCallback)
                        .build(GitHubApi.instance());

                accessToken = service.getAccessToken(params[0]);

            } catch (Exception e) {
                Log.d("EXCEPTION", e.toString());
            }
            if(accessToken.toString() != null) {
                AuthCredential credential = GithubAuthProvider.getCredential(accessToken.getAccessToken());
                return credential;
            }else{
                return null;
            }
        }

        protected void onPostExecute(AuthCredential result) {
            if (result != null) {
                verifyGithubToken(result);
            }
        }
    }
}





