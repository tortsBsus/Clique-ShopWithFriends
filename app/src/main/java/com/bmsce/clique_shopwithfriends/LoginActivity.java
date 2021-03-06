package com.bmsce.clique_shopwithfriends;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 120;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private GoogleSignInClient googleSignInClient;
    Button login;

    FirebaseDatabase rootNode;
    DatabaseReference DBreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Firebase Auth Instance
        mAuth = FirebaseAuth.getInstance();

        login =  findViewById(R.id.button);
        login.setOnClickListener(v -> signIn());

        ImageView iv = findViewById(R.id.imageViewAnim);
        Animation animUpDown;
        animUpDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.up_down);
        animUpDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.up_down);
                anim.setAnimationListener(this);
                iv.startAnimation(anim);
            }
            @Override
            public void onAnimationRepeat(Animation arg0) { }
            @Override
            public void onAnimationStart(Animation arg0) { }
        });
        iv.startAnimation(animUpDown);
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            Exception exception = task.getException();

            if(task.isSuccessful()) {
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account.getIdToken());

                    // Toast.makeText(getApplicationContext(), "firebaseAuthWithGoogle:" + account.getId(), Toast.LENGTH_SHORT).show();
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    Toast.makeText(getApplicationContext(), "Google sign in failed: " + e, Toast.LENGTH_SHORT).show();
                }
            } else {
                if(exception != null) {
                    Toast.makeText(getApplicationContext(), "Exception: " + exception, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(LoginActivity.this, "Successful Login!", Toast.LENGTH_SHORT).show();

                        rootNode = FirebaseDatabase.getInstance();
                        DBreference = rootNode.getReference("users");

                        currentUser = mAuth.getCurrentUser();
                        UserDefinition user = new UserDefinition(currentUser.getDisplayName().toString(), currentUser.getEmail().toString());

                        DBreference.child(user.getName()).setValue(user);

                        Intent home_activity = new Intent(LoginActivity.this, HomeScreen.class);
                        startActivity(home_activity);
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(LoginActivity.this, "Login Unsuccessful!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}