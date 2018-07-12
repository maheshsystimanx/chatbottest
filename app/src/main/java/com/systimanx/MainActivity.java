package com.systimanx.chatbottest;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class MainActivity extends AppCompatActivity implements AIListener, TextToSpeech.OnInitListener {

    private static final String TAG = MainActivity.class.getName();
    ImageView send;
    EditText editmessage;
    TextView reply_msg;
    private TextToSpeech text_ToSpeech;
    DatabaseReference ref;
    RecyclerView recylerview;
    AIConfiguration config;
    AIRequest aiRequest;
    ArrayList<String> user_array = new ArrayList<String>();
    ArrayList<String> bot_array = new ArrayList<String>();
    ArrayList<ChatMessage> view_array = new ArrayList<ChatMessage>();
    AIDataService aiDataService;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        editmessage = (EditText) findViewById(R.id.editmessage);
        reply_msg = (TextView) findViewById(R.id.replymsg);
        recylerview = (RecyclerView) findViewById(R.id.recylerview);
        recylerview.setHasFixedSize(true);

        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        recylerview.setLayoutManager(layoutManager);

        ref = FirebaseDatabase.getInstance().getReference();
        ref.keepSynced(true);
        send = (ImageView) findViewById(R.id.send);


        config = new AIConfiguration("8b6f77c05f734fbe90328f90b38cc28e",    //Define Client key in api.ai
                AIConfiguration.SupportedLanguages.English,                 // Ai config support language
                AIConfiguration.RecognitionEngine.System);


        aiDataService = new AIDataService(this, config);

        aiRequest = new AIRequest();

        text_ToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    text_ToSpeech.setLanguage(Locale.US);
                }
            }
        });

        // readdata();
        readdata1();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (editmessage.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Enter Message", Toast.LENGTH_SHORT).show();
                } else {
                    user_array.clear();
                    bot_array.clear();
                    // testarray.clear();
                    final String message = editmessage.getText().toString();
                    aiRequest.setQuery(message);
                    getPackNameByAppName(message);
                    final ChatMessage chatmodel = new ChatMessage(message, "msguser");
                    view_array.add(new ChatMessage(message, "msguser"));
                    ref.child("chat").push().setValue(chatmodel);
                    editmessage.setText("");
                    final RecyclerView.Adapter adapter = new Chatadpter(view_array, new Chatadpter.customerlistadapterListner() {
                        @Override
                        public void robotext(int position) {


                        }
                    });
                    recylerview.setAdapter(adapter);


                    new AsyncTask<AIRequest, Void, AIResponse>() {
                        @Override
                        protected AIResponse doInBackground(AIRequest... requests) {
                            final AIRequest request = requests[0];
                            try {
                                final AIResponse response = aiDataService.request(aiRequest);
                                return response;
                            } catch (AIServiceException e) {
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(AIResponse aiResponse) {

                            if (aiResponse != null) {

                                if (aiResponse != null) {
                                    Result result = aiResponse.getResult();
                                    String reply = result.getFulfillment().getSpeech();
                                    System.out.println("reply" + reply);
                                    reply_msg.setText(reply);


                                    if (reply.equals("systimanx"))
                                    {
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.systimanx.com"));
                                        startActivity(browserIntent);
                                    }else if (reply.equals("open Google"))
                                    {
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
                                        startActivity(browserIntent);


                                    }else  if (reply.equals("")) {
                                        reply = "Sorry, can you say that again?";
                                    }else if (reply.equals("gmail"))
                                    {
                                        openApp("com.google.android.gm","0");
                                        // Intent intent = getPackageManager().getLaunchIntentForPackage("com.google.android.gm");
                                        //startActivity(intent);
                                    }else if (reply.equals("facebook"))
                                    {
                                        openApp("com.facebook.katana","0");


                                    }else if (reply.equals("open aquadrop"))
                                    {
                                        openApp("com.systimanx.aqua","0");
                                    }else if (reply.equals("whatsapp"))
                                    {
                                        openApp("com.whatsapp","0");
                                    }else if (reply.equals("WhatsApp"))
                                    {
                                        openApp("com.whatsapp","0");
                                    }else if (reply.equals("doubt via whatsapp"))
                                    {
                                        openApp("com.whatsapp","1");
                                    }else if (reply.equals("doubt via gmail"))
                                    {
                                        openApp("com.google.android.gm","1");
                                    }





                                    HashMap<String, String> names = new HashMap<String, String>();
                                    names.put("msguser", message);
                                    names.put("bot", reply);


                                    final ChatMessage chatmodel = new ChatMessage(reply, "bot");
                                    ref.child("chat").push().setValue(chatmodel);

                                    readdata1();


                                }
                            }
                        }
                    }.execute(aiRequest);

                }

            }
        });


    }

    @Override
    public void onResult(AIResponse result) {


        Result result1 = result.getResult();

        String message = result1.getResolvedQuery();
        System.out.println("message" + message);


        String reply = result1.getFulfillment().getSpeech();
        System.out.println("reply" + reply);


        System.out.println("result" + "result");

    }

    @Override
    public void onError(AIError error) {
        System.out.println("result" + error);
        reply_msg.setText(error.getMessage());

    }

    @Override
    public void onAudioLevel(float level) {
        System.out.println("result" + "level");


    }

    @Override
    public void onListeningStarted() {
        System.out.println("result" + "start");


    }

    @Override
    public void onListeningCanceled() {
        System.out.println("result" + "cancel");

    }

    @Override
    public void onListeningFinished() {
        System.out.println("result" + "finish");


    }


    public void readdata1() {
        view_array.clear();
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference myRef = database.child("chat");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ChatMessage chatmodel = dataSnapshot.getValue(ChatMessage.class);

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {


                    view_array.add(new ChatMessage(singleSnapshot.child("msgText").getValue().toString(), singleSnapshot.child("msgUser").getValue().toString()));


                }
                final RecyclerView.Adapter adapter = new Chatadpter(view_array, new Chatadpter.customerlistadapterListner() {
                    @Override
                    public void robotext(int position) {


                    }
                });
                recylerview.setAdapter(adapter);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //view_array.clear();

        //user_array.clear();


        Toast.makeText(getApplicationContext(), "Now onStart() calls", Toast.LENGTH_LONG).show(); //onStart Called
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    user_array.clear();
                    bot_array.clear();

                    final String message = result.get(0);
                    aiRequest.setQuery(message);
                    getPackNameByAppName(message);
                    final ChatMessage chatmodel = new ChatMessage(message, "msguser");
                    view_array.add(new ChatMessage(message, "msguser"));
                    ref.child("chat").push().setValue(chatmodel);

                    final RecyclerView.Adapter adapter = new Chatadpter(view_array, new Chatadpter.customerlistadapterListner() {
                        @Override
                        public void robotext(int position) {


                        }
                    });
                    recylerview.setAdapter(adapter);


                    new AsyncTask<AIRequest, Void, AIResponse>() {
                        @Override
                        protected AIResponse doInBackground(AIRequest... requests) {
                            final AIRequest request = requests[0];
                            try {
                                final AIResponse response = aiDataService.request(aiRequest);
                                return response;
                            } catch (AIServiceException e) {
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(AIResponse aiResponse) {
                            if (aiResponse != null) {

                                if (aiResponse != null) {

                                    Result result = aiResponse.getResult();
                                    String reply = result.getFulfillment().getSpeech();
                                    System.out.println("reply" + reply);
                                    reply_msg.setText(reply);
                                    text_ToSpeech.speak(reply, TextToSpeech.QUEUE_ADD, null);

                                    if (reply.equals("systimanx"))
                                    {
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.systimanx.com"));
                                        startActivity(browserIntent);
                                    }else if (reply.equals("open Google"))
                                    {
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
                                        startActivity(browserIntent);


                                    }else  if (reply.equals("")) {
                                        reply = "Sorry, can you say that again?";
                                    }else if (reply.equals("gmail"))
                                    {
                                        openApp("com.google.android.gm","0");
                                        // Intent intent = getPackageManager().getLaunchIntentForPackage("com.google.android.gm");
                                        //startActivity(intent);
                                    }else if (reply.equals("facebook"))
                                    {
                                        openApp("com.facebook.katana","0");


                                    }else if (reply.equals("open aquadrop"))
                                    {
                                        openApp("com.systimanx.aqua","0");
                                    }else if (reply.equals("whatsapp"))
                                    {
                                        openApp("com.whatsapp","0");
                                    }else if (reply.equals("WhatsApp"))
                                    {
                                        openApp("com.whatsapp","0");
                                    }else if (reply.equals("doubt via whatsapp"))
                                    {
                                        openApp("com.whatsapp","1");
                                    }else if (reply.equals("doubt via gmail"))
                                    {
                                        openApp("com.google.android.gm","1");
                                    }



                                    HashMap<String, String> names = new HashMap<String, String>();
                                    names.put("msguser", message);
                                    names.put("bot", reply);
                                    ;


                                    final ChatMessage chatmodel = new ChatMessage(reply, "bot");
                                    ref.child("chat").push().setValue(chatmodel);

                                    readdata1();


                                }
                            }
                        }
                    }.execute(aiRequest);


                }
                break;
            }

        }
    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = text_ToSpeech.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {

            } else {

            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }
// send message via social networks
    private void openApp(String packagename,String type) {
        String packageName = packagename;
        if (isAppInstalled(MainActivity.this, packageName)) {
            if (packageName.equals("com.whatsapp")) {
                if (type.equals("1")) {

                    PackageManager packageManager =this.getPackageManager();

                    Intent i = new Intent(Intent.ACTION_VIEW);
                    try {
                        String url = "https://api.whatsapp.com/send?phone=" +"phonenumber"+ "&text=" +"testmessage";
                        i.setPackage("com.whatsapp");
                        i.setData(Uri.parse(url));
                        if (i.resolveActivity(packageManager) != null) {
                            startActivity(i);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }




                } else {

                    startActivity(getPackageManager().getLaunchIntentForPackage(packageName));
                }
            }else if (packageName.equals("com.google.android.gm"))
            {
                Intent intent = new Intent (Intent.ACTION_VIEW , Uri.parse("mailto:" + "your@gmail.com"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "your_subject");
                intent.putExtra(Intent.EXTRA_TEXT, "your_text");
                startActivity(intent);
            }else
            {
                startActivity(getPackageManager().getLaunchIntentForPackage(packageName));
            }
        }
        else{
            if (packageName.equals("com.facebook.katana"))
            {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.facebook.com"));
                startActivity(browserIntent);


            }else
            {
                Toast.makeText(MainActivity.this, "App not installed", Toast.LENGTH_SHORT).show();

                Intent i = new Intent(android.content.Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://play.google.com/store/apps/details?id="+packageName));
                startActivity(i);

            }

        }


    }

    public static boolean isAppInstalled(Activity activity, String packageName) {
        PackageManager pm = activity.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return false;
    }
    @Override
    public void onResume(){
        super.onResume();
        config = new AIConfiguration("8b6f77c05f734fbe90328f90b38cc28e",    //Define Client key in api.ai
                AIConfiguration.SupportedLanguages.English,                 // Ai config support language
                AIConfiguration.RecognitionEngine.System);


        aiDataService = new AIDataService(this, config);

        aiRequest = new AIRequest();
        // put your code here...

    }

    public String getPackNameByAppName(String name) {
        PackageManager pm =this.getPackageManager();
        List<ApplicationInfo> l = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        String packName = name;
        for (ApplicationInfo ai : l) {
            String n = (String)pm.getApplicationLabel(ai);
            if (n.contains(name) || name.contains(n)){
                packName = ai.packageName;
                System.out.println("appname"+packName.toString());
                openApp(packName,"1");
            }
        }

        return packName;
    }



}
