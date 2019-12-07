package com.example.keyboard;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

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
            timeRef.setValue(score*100);

            //float modifiedScore = score*magnitude*100;



            getData(score*100);


        }


    }



    private void getData(float score)
    {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dataRef = database.getReference(currentUser.getUid());

        SharedPreferences sharedPreferences = this.getSharedPreferences("DATA", Context.MODE_PRIVATE);
        System.out.println("DrDisrespect " + sharedPreferences.getString("STRENGTH",null));
        String [] genreList;
        //{"Chill", "hip-hop", "pop", "rock"}

        String action=null;
        score = -70;
        score = (int) score;
        //System.out.println("SCORE "+score);
        if(score<0 && score>=-20)
        {
            action = "joke";
        }
        else if(score<-20 && score>=-40)
        {
            action = "meme";
        }
        else if(score<-40 && score>=-60)
        {
            action = "song";
        }
        else if(score<-60 && score>=-80)
        {
            action = "familyPic";
        }
        else if(score<-80 && score>=-100)
        {
            action = "text";
        }


        final String final_action = action;

        if(final_action=="text")
        {
            sendSMS();
        }
        else if(final_action=="familyPic")
        {
            //get random URL
            callCorrospondingNotificationMethod(final_action, "/storage/emulated/0/Pictures/Telepathy/image26.jpg");
        }
        else if(action!=null)
        {
            dataRef.getParent().child("Notification").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Random rand = new Random();

                    //System.out.println("start "+final_action);
                    HashMap<String, Object> td = (HashMap<String,Object>) dataSnapshot.getValue();
                    HashMap<String, Object> data = (HashMap<String,Object>) td.get(final_action);
                    //System.out.println("SAS " + td.toString());

                    if(final_action=="song")
                    {

                        ArrayList<String> al = new ArrayList<String>();

                        if(sharedPreferences.getString("Rock",null)=="1")
                        {
                            al.add("rock");
                        }

                        if(sharedPreferences.getString("Hip_hop",null)=="1")
                        {
                            al.add("hip-hop");
                        }

                        if(sharedPreferences.getString("Pop",null)=="1")
                        {
                            al.add("pop");
                        }

                        if(sharedPreferences.getString("Chill",null)=="1")
                        {
                            al.add("Chill");
                        }


                        //data = (HashMap<String,Object>) data.get(genreList[rand.nextInt(genreList.length)]);

                        data = (HashMap<String,Object>) data.get(al.get(rand.nextInt(al.size())));

                        //System.out.println("KEKW "+ data.toString());
                    }



                    int idx = rand.nextInt(data.size())+1;


                    //System.out.println("KEKW"+ idx);


                    callCorrospondingNotificationMethod(final_action, data.get("k"+ idx).toString());
                    //callMessageNotification(songs.get("song1").toString());
                    //songname = songs.get("song1");
                    //System.out.println("end");
                    //   List<Object> values = td.values();

                    //notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    protected void sendSMS() {
        Log.i("Send SMS", "");
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);

        smsIntent.setData(Uri.parse("smsto:"));
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("address"  , new String ("4803108822"));
        smsIntent.putExtra("sms_body"  , "Hi, I am feeling bit low, can we catchup?");

        try {
            smsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(smsIntent);
            //finish();
            Log.i("Finished sending SMS...", "");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this,
                    "SMS faild, please try again later.", Toast.LENGTH_SHORT).show();
        }
    }


    private void callCorrospondingNotificationMethod(String final_action, String value)
    {
        //boolean isImageNotification=true;


        if(final_action=="familyPic")
        {
            value = "file://"+value;
        }

        final String newVal = value;

        if(final_action=="meme" || final_action=="familyPic")
        {
            new AsyncTask<Void, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Void... urls) {

                    try {

                        System.out.println("SHERLOCK HERE HERE");
//                        if(final_action=="familyPic")
//                        {
//                            value+="file://"+
//                        }
                        Bitmap picture = BitmapFactory.decodeStream(new java.net.URL(newVal).openStream());
                        return picture;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                protected void onPostExecute(Bitmap picture) {

//                    loadingpic = picture;
                    //System.out.println("SHERLOCKED");
                    callImageNotification(picture, newVal);


                }
            }.execute();
        }
        else if(final_action=="song"){
            String displayData = value.split("-")[0]+ "By" +value.split("-")[1];
            String link = value.split("-")[2];

            //System.out.println("SONG "+ displayData+ " -- "+ link);
            callMessageNotification(displayData,link);
        }else
        {
            callMessageNotification(value, null);
        }
    }

    private void callImageNotification(Bitmap picture, String link)
    {
        //System.out.println("PogU");
        NewImageNotification naman = new NewImageNotification();
        naman.notify(this, link, 5, "1", picture);
    }


    private void callMessageNotification(String displayData, String link)
    {
        //System.out.println("PogU");

        NewMessageNotification naman = new NewMessageNotification();
        naman.notify(this, displayData, link, 5, "1");
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
