package com.nollpointer.pixaerostt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    private Button recognizerButton;


    private ScrollView scrollView;
    private TextView contentText;

    public static String TAG = "STT";


    private static final String KWS_SEARCH = "wakeup";
    private static final String MENU_SEARCH = "menu";

    private static final String KEYPHRASE = "well hello";

    private edu.cmu.pocketsphinx.SpeechRecognizer recognizer;

    private boolean isPocketOnGoing = false;

    private MediaPlayer voiceRecognitionSoundEffect;


    private int textSize = 40;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fabric.with(this, new Crashlytics());



        recognizerButton = findViewById(R.id.button_voice_recognizer_sphynx);
//        partialResults = findViewById(R.id.text_view_partial);
//        fullResults = findViewById(R.id.text_view_full);

        contentText = findViewById(R.id.contentText);
        scrollView = findViewById(R.id.scrollView);

        contentText.setText(text);

        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1);
            return;
        }

        recognizerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPocketOnGoing) {
                    recognizer.stop();
                }else {
//                    voiceRecognitionSoundEffect.reset();
//                    voiceRecognitionSoundEffect.start();
                    recognizer.startListening(MENU_SEARCH);
                }
                voiceRecognitionSoundEffect.start();
                //voiceRecognitionSoundEffect.reset();
                //voiceRecognitionSoundEffect.
                isPocketOnGoing = !isPocketOnGoing;

                //throw new RuntimeException("LOOOOL");
            }
        });

        runRecognizerSetup();

        voiceRecognitionSoundEffect = MediaPlayer.create(this,R.raw.stairs);

        String init = TextToGrammer.convertTextToJSGF(demoText);
        //partialResults.setText(init);
        contentText.setText(init);
        //fullResults.setText(text);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.font_up:
                textSize += 2;
                break;
            case R.id.font_down:
                textSize -=2;
                break;
        }
        if(textSize > 0)
            contentText.setTextSize(textSize);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                runRecognizerSetup();
            else
                finish();
        }
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
            recognizer = SpeechRecognizerSetup.defaultSetup()
                    .setAcousticModel(new File(dir, "ru-ptm"))
                    .setDictionary(new File(dir, "ru.dic"))
                    .setKeywordThreshold(1e-22f)
                    .getRecognizer();
        }catch (Exception e){
            Log.wtf(TAG,e);
        }

        recognizer.addListener(new recognizerListener());

        //recognizer.addKeyphraseSearch(KWS_SEARCH,KEYPHRASE);



        //File menuGrammar = new File(dir,"mymenu.gram");

        String menuGrammar = TextToGrammer.convertTextToJSGF(text);
        File file = TextToGrammer.saveJSFGToFile("test",menuGrammar,dir);

        recognizer.addGrammarSearch(MENU_SEARCH,file);

        Snackbar.make(findViewById(R.id.container),"Setup complete",Snackbar.LENGTH_SHORT).show();

        //recognizer.startListening()
    }

    private void switchSearch(String str){
        recognizer.stop();

        if(str.equals(KWS_SEARCH))
            recognizer.startListening(str);
        else
            recognizer.startListening(str, 10000);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(recognizer != null){
            recognizer.cancel();
            recognizer.shutdown();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if(recognizer != null)
            recognizer.stop();
        isPocketOnGoing = false;
    }

    class recognizerListener implements edu.cmu.pocketsphinx.RecognitionListener{
        @Override
        public void onBeginningOfSpeech() {
            Log.wtf(TAG,"Start of the Speech");
        }

        @Override
        public void onEndOfSpeech() {
            Log.wtf(TAG,"End of the Speech");
        //if(!recognizer.getSearchName().equals(KWS_SEARCH))
            //switchSearch(KWS_SEARCH);
        }

        @Override
        public void onPartialResult(Hypothesis hypothesis) {
            if (hypothesis == null)
                return;
            Log.wtf(TAG,"Partial: " + hypothesis.getHypstr());

            String text = hypothesis.getHypstr();
            //fullResults.setText(text);
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


    private static final String text = "Когда я был молод, игры только появились и вся наша компания только и жила ими!";


    private static final String demoText = "Ну вот! Теперь я смогу записывать обращения на камеру максимально оперативно и удобно.\\n\\nЯ очень буду ждать обновление, в котором появится личный кабинет. Там я смогу писать тексты с компьютера и синхронизировать с приложением. Программисты уже работают, чтобы добавить распознавание голоса. В этом случае скорость прокрутки текста автоматически подстроится под мою речь. А если я начну импровизировать," +
            " текст остановится и будет ждать пока я вернусь к чтению.\\n\\nА еще, я теперь знаю, что инженеры PIXAERO разработали мобильный телесуфлёр, который весит менее двухсот грамм, пристегивается к объективу камеры и сделан в России. Они постарались сделать его не только очень качественным и надежным, но и одним из самых доступных телесуфлёров в мире! Больше информации о суфлёре PIXAERO MOBUS я всегда могу найти на сайте pixaero.pro.\\n\\n" +
            "Если у меня возникнут идеи как сделать приложение или суфлёр еще более удобным, я напишу ребятам из PIXAERO и они постараются воплотить это в жизнь!";
}
