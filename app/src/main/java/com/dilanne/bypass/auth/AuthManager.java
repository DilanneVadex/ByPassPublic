package com.dilanne.bypass.auth;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthManager {
    private final FirebaseAuth mAuth;
    private final GoogleSignInClient mGoogleSignInClient;

    public AuthManager(Activity activity) {
        mAuth = FirebaseAuth.getInstance();
        
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(com.dilanne.bypass.R.string.default_web_client_id))
                .requestEmail()
                .build();
        
        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
    }

    public com.google.android.gms.tasks.Task<com.google.firebase.auth.AuthResult> signInWithGoogle(String idToken) {
        com.google.firebase.auth.AuthCredential credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null);
        return mAuth.signInWithCredential(credential);
    }

    public com.google.android.gms.tasks.Task<com.google.firebase.auth.AuthResult> signInWithGitHub(Activity activity) {
        com.google.firebase.auth.OAuthProvider.Builder provider = com.google.firebase.auth.OAuthProvider.newBuilder("github.com");
        return mAuth.startActivityForSignInWithProvider(activity, provider.build());
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public Intent getGoogleSignInIntent() {
        return mGoogleSignInClient.getSignInIntent();
    }

    public com.google.android.gms.tasks.Task<com.google.firebase.auth.AuthResult> registerWithEmail(String email, String password) {
        return mAuth.createUserWithEmailAndPassword(email, password);
    }

    public com.google.android.gms.tasks.Task<com.google.firebase.auth.AuthResult> loginWithEmail(String email, String password) {
        return mAuth.signInWithEmailAndPassword(email, password);
    }

    public void logout() {
        mAuth.signOut();
        mGoogleSignInClient.signOut();
        com.dilanne.bypass.data.local.AppDatabase.destroyInstance();
    }
}
