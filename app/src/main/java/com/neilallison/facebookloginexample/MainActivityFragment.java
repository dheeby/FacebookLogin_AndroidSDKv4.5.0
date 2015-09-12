package com.neilallison.facebookloginexample;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

/**
 * Fragment for the MainActivity to handle login authentication through Facebook.
 *
 * @author Neil Allison
 * @version 09.11.2015
 */
public class MainActivityFragment extends Fragment {

    private TextView welcomeDisplayMessage;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;
    private CallbackManager callbackManager;
    private AccessToken accessToken;

    /**
     * No-argument constructor for this fragment which must be provided for all subclasses of
     * Fragment so that this class can be re-instantiated if the state is being restored.
     */
    public MainActivityFragment() {}

    /**
     * Initializes the FacebookSdk for use with the login button. Initializes the callback manager
     * which will be used when registering the callback for the login button. Initializes the
     * tracker for the user's access token to handle when the user logs out or lost internet
     * connection so that the user may stay logged in and the tracker for the user's profile so any
     * changes made to the user's profile such as profile image, name, etc. will be tracked in this
     * application.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     *                           this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        // Handling change in access token on logout, lost connection, etc.
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldToken, AccessToken newToken) {
                accessToken = newToken;
                if (accessToken != null) {
                    Log.i("AccessToken Changed", accessToken.toString());
                } else {
                    Log.i("AccessToken Changed", "User logged out from Facebook");
                    setWelcomeMessage(null);
                    Toast.makeText(getActivity(), "Logout success", Toast.LENGTH_SHORT).show();
                }
            }
        };
        accessTokenTracker.startTracking();

        // Handle change in the logged in user's profile
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile newProfile) {
                setWelcomeMessage(newProfile);
                if (newProfile != null) {
                    Log.i("Profile Changed", newProfile.toString());
                }
            }
        };
        profileTracker.startTracking();
    }

    /**
     * Inflates the view associated with the fragment which contains the Facebook LoginButton to be
     * used for authenticating the user through the Facebook login. Associates this fragment with
     * the Facebook login button and sets the permissions for which profile data this app is
     * allowed to access. Registers the callback for the login button with the provided callback
     * manager and FacebookCallback handler. The FacebookCallback responds to the login button when
     * login is successful, cancelled, or has an error.
     *
     * @param inflater The LayoutInflater that can be used to inflate any views in this fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be
     *                  attached to. The fragment should not add the view itself, but this can be
     *                  used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous
     *                           saved state as given here.
     * @return The View for the fragment's UI, or null.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        LoginButton fbLoginButton = (LoginButton) view.findViewById(R.id.fb_login_button);

        // Used to display name on successful login
        welcomeDisplayMessage = (TextView) view.findViewById(R.id.login_message);

        // Set permissions for which profile data app can access
        fbLoginButton.setReadPermissions("user_friends");

        // Must set the fragment on the FB login button
        fbLoginButton.setFragment(this);

        // Callback registration
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                accessToken = loginResult.getAccessToken();
                Profile profile = Profile.getCurrentProfile();
                setWelcomeMessage(profile);
            }

            @Override
            public void onCancel() {
                // Handler when user cancels login
                Log.i("Login Cancelled", "User cancelled login");
            }

            @Override
            public void onError(FacebookException e) {
                // Handler if there is an error with login
                Log.d("Login Error", "Login caused exception: " + e.getMessage());
            }
        });

        return view;
    }

    /**
     * Display the welcome message with the logged in user's name when this Fragment is active.
     */
    @Override
    public void onResume() {
        super.onResume();
        Profile profile = Profile.getCurrentProfile();
        setWelcomeMessage(profile);
    }

    /**
     * Makes sure that the logged in user's access token and profile for this current login session
     * are no longer tracked when the fragment is no longer visible.
     */
    @Override
    public void onStop() {
        super.onStop();
        accessTokenTracker.stopTracking();
        profileTracker.stopTracking();
    }

    /**
     * Manages the callbacks into the FacebookSdk through the CallbackManager.
     *
     * @param requestCode The request code received by this Fragment.
     * @param resultCode The result code received by this Fragment.
     * @param data The result data received by this Fragment.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Changes the text on the login page to verify that correct user was logged in and their name
     * could be accessed through their profile, or that the profile passed was null.
     *
     * @param profile The Facebook profile for the current logged in user
     */
    private void setWelcomeMessage(Profile profile) {
        if (profile != null) {
            welcomeDisplayMessage.setText("Welcome, " + profile.getName());
            Toast.makeText(getActivity(), "Facebook login success!", Toast.LENGTH_SHORT).show();
        } else {
            welcomeDisplayMessage.setText("Have a Facebook account?");
        }
    }
}
