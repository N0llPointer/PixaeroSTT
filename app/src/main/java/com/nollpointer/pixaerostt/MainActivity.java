package com.nollpointer.pixaerostt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class MainActivity extends AppCompatActivity {

    private Button voiceRecognizerButton;
    private Button voicePocketRecognizer;

    private TextView partialResults;
    private TextView fullResults;

    private SpeechRecognizer recognizer;

    private boolean isRecording = false;
    private Intent recognizerIntent;

    public static String TAG = "STT";


    private static final String KWS_SEARCH = "wakeup";
    private static final String MENU_SEARCH = "menu";

    private static final String KEYPHRASE = "well hello";

    private edu.cmu.pocketsphinx.SpeechRecognizer pocketRecognizer;

    private boolean isPocketOnGoing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        voiceRecognizerButton = findViewById(R.id.button_voice_recognizer_android);
        voicePocketRecognizer = findViewById(R.id.button_voice_recognizer_sphynx);
        partialResults = findViewById(R.id.text_view_partial);
        fullResults = findViewById(R.id.text_view_full);

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognizerListener());

        recognizerIntent = createRecognizerIntent();

        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1);
            return;
        }

        voiceRecognizerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRecording){
                    isRecording = false;
                    recognizer.stopListening();
                }else{
                    isRecording = true;
                    recognizer.startListening(recognizerIntent);
                }
            }
        });

        voicePocketRecognizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPocketOnGoing)
                    pocketRecognizer.stop();
                else
                    pocketRecognizer.startListening(MENU_SEARCH);


                isPocketOnGoing = !isPocketOnGoing;
            }
        });

        runRecognizerSetup();

        String init = TextToGrammer.convertTextToJSGF(text);
        partialResults.setText(init);

        fullResults.setText(text);
    }

    private void runRecognizerSetup(){
        new AsyncTask<Void,Void,Exception>(){
            @Override
            protected void onPostExecute(Exception e) {
               if(e != null) {
                   Log.wtf(TAG,e);
                   Snackbar.make(MainActivity.this.findViewById(R.id.container), "Exception with Voice", Snackbar.LENGTH_SHORT).show();
               }
            }

            @Override
            protected Exception doInBackground(Void... voids) {
                try{
                    Assets assets = new Assets(MainActivity.this);
                    File assetsDir = assets.syncAssets();
                    setupRecognizer(assetsDir);
                }catch (Exception e){
                    return e;
                }

                return null;
            }
        }.execute();
    }

    private void setupRecognizer(File dir) {
        try {
            pocketRecognizer = SpeechRecognizerSetup.defaultSetup()
                    .setAcousticModel(new File(dir, "ru-ptm"))
                    .setDictionary(new File(dir, "ru.dic"))
                    .setKeywordThreshold(1e-22f)
                    .getRecognizer();
        }catch (Exception e){
            Log.wtf(TAG,e);
        }

        pocketRecognizer.addListener(new PocketRecognizerListener());

        //pocketRecognizer.addKeyphraseSearch(KWS_SEARCH,KEYPHRASE);



        //File menuGrammar = new File(dir,"mymenu.gram");

        String menuGrammar = TextToGrammer.convertTextToJSGF(text);
        File file = TextToGrammer.saveJSFGToFile("test",menuGrammar,dir);

        pocketRecognizer.addGrammarSearch(MENU_SEARCH,file);

        Snackbar.make(findViewById(R.id.container),"Setup complete",Snackbar.LENGTH_SHORT).show();

        //pocketRecognizer.startListening()
    }

    private void switchSearch(String str){
        pocketRecognizer.stop();

        if(str.equals(KWS_SEARCH))
            pocketRecognizer.startListening(str);
        else
            pocketRecognizer.startListening(str, 10000);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(pocketRecognizer != null){
            pocketRecognizer.cancel();
            pocketRecognizer.shutdown();
        }
    }

    private Intent createRecognizerIntent(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,"ru-RU");
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,3000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,3000);


        return intent;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(pocketRecognizer != null)
            pocketRecognizer.stop();
        isPocketOnGoing = false;
    }

    class PocketRecognizerListener implements edu.cmu.pocketsphinx.RecognitionListener{
        @Override
        public void onBeginningOfSpeech() {
            Log.wtf(TAG,"Start of the Speech");
        }

        @Override
        public void onEndOfSpeech() {
            Log.wtf(TAG,"End of the Speech");
        //if(!pocketRecognizer.getSearchName().equals(KWS_SEARCH))
            //switchSearch(KWS_SEARCH);
        }

        @Override
        public void onPartialResult(Hypothesis hypothesis) {
            if (hypothesis == null)
                return;
            Log.wtf(TAG,"Partial: " + hypothesis.getHypstr());

            String text = hypothesis.getHypstr();
            fullResults.setText(text);
//            if (text.equals(KEYPHRASE))
//                switchSearch(MENU_SEARCH);
//            else {
//                Log.wtf(TAG,hypothesis.getHypstr());
//            }
        }

        @Override
        public void onResult(Hypothesis hypothesis) {
            if(hypothesis != null)
                Log.wtf(TAG,"End result: " + hypothesis.getHypstr());
        }

        @Override
        public void onError(Exception e) {
            Log.wtf(TAG,e);
        }

        @Override
        public void onTimeout() {
            switchSearch(KWS_SEARCH);
        }
    }


    class RecognizerListener implements RecognitionListener{
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            Log.wtf(TAG,"Start of Speech");
        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float v) {

        }

        @Override
        public void onBufferReceived(byte[] bytes) {

        }

        @Override
        public void onEndOfSpeech() {
            Log.wtf(TAG,"End of Speech");

        }

        @Override
        public void onError(int i) {

        }

        @Override
        public void onResults(Bundle bundle) {
            ArrayList<String> strings = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            if(strings.size() > 0) {

                String words = "";
                for (String s : strings)
                    words += s + "\n";

                fullResults.setText(words);
            }else
                fullResults.setText("Nothing was Recognized");
        }

        @Override
        public void onPartialResults(Bundle bundle) {
            ArrayList<String> strings = bundle.getStringArrayList(RecognizerIntent.EXTRA_PARTIAL_RESULTS);

            if(strings.size() > 0) {

                String words = "";
                for (String s : strings)
                    words += s + "\n";

                partialResults.setText(words);

            }else
                partialResults.setText("Nothing was Recognized");
        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    }


    private static final String text = "Когда я был молод, игры только появились и вся наша компания только и жила ими!";


    private static final String demoText = "Ну вот! Теперь я смогу записывать обращения на камеру максимально оперативно и удобно.\\n\\nЯ очень буду ждать обновление, в котором появится личный кабинет. Там я смогу писать тексты с компьютера и синхронизировать с приложением. Программисты уже работают, чтобы добавить распознавание голоса. В этом случае скорость прокрутки текста автоматически подстроится под мою речь. А если я начну импровизировать," +
            " текст остановится и будет ждать пока я вернусь к чтению.\\n\\nА еще, я теперь знаю, что инженеры PIXAERO разработали мобильный телесуфлёр, который весит менее двухсот грамм, пристегивается к объективу камеры и сделан в России. Они постарались сделать его не только очень качественным и надежным, но и одним из самых доступных телесуфлёров в мире! Больше информации о суфлёре PIXAERO MOBUS я всегда могу найти на сайте pixaero.pro.\\n\\n" +
            "Если у меня возникнут идеи как сделать приложение или суфлёр еще более удобным, я напишу ребятам из PIXAERO и они постараются воплотить это в жизнь!";
}
