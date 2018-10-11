package com.nollpointer.pixaerostt;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.nollpointer.pixaerostt.views.CountDownView;
import com.nollpointer.pixaerostt.views.PartialResultsView;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    private Button recognizerButton;


    private ScrollView scrollView;
    private TextView contentText;
    private CountDownView countDownView;
    private FrameLayout container;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private PartialResultsView partialResultsView;

    private ScrollViewController controller;

    public static String TAG = "STT";
    public static String DUMP = "DUMP";

    private static final String KWS_SEARCH = "wakeup";
    private static final String MENU_SEARCH = "menu";

    private static final String KEYPHRASE = "well hello";

    public static final String AUDIO_FOLDER = "Pixaero/Audio";

    private edu.cmu.pocketsphinx.SpeechRecognizer recognizer;

    private boolean isPocketOnGoing = false;

    private MediaPlayer voiceRecognitionSoundEffect;

    private ArrayList<String> recognizedWords = new ArrayList<>();

    private ArrayList<String> uniqueWords = new ArrayList<>();


    private int textSize = 40;

    private int indexOfNewSequence = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fabric.with(this, new Crashlytics());

        recognizerButton = findViewById(R.id.button_voice_recognizer_sphynx);

        contentText = findViewById(R.id.contentText);
        scrollView = findViewById(R.id.scrollView);
        container = findViewById(R.id.container);
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressbar);
        
        progressBar.setProgress(0);

        countDownView = new CountDownView(this);
        container.addView(countDownView);

        partialResultsView = new PartialResultsView(this);
        container.addView(partialResultsView);


        toolbar.inflateMenu(R.menu.main_menu);
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
                    toolbar.setTitle(R.string.app_name);
                }else {
                    recognizer.startListening(MENU_SEARCH);
                    toolbar.setTitle(R.string.listening);
                    countDownView.setVisibility(View.VISIBLE);
                    countDownView.startCountDown(3);

                }
                voiceRecognitionSoundEffect.start();
                isPocketOnGoing = !isPocketOnGoing;



                if(controller == null){
                    controller = new ScrollViewController(scrollView,contentText,MainActivity.this);

                    Log.wtf(TAG,controller.getCurrentShowingSubString());

                    uniqueWords.addAll(TextToGrammer.getUniqueWordsList(controller.getCurrentShowingReadngSubString()));
                }
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.font_up:
                        textSize += 2;
                        break;
                    case R.id.font_down:
                        textSize -=2;
                        break;
                    case R.id.font_refresh:
                        askForAudioSave();
                        textSize = 40;
                        break;
                    case R.id.more_info:
                        //showMoreInfo();
                        dumpToLogEverything();
                        break;
                }
                if(textSize > 0)
                    contentText.setTextSize(textSize);
                else
                    contentText.setTextSize(1);
                return true;
            }
        });


        runRecognizerSetup();

        voiceRecognitionSoundEffect = MediaPlayer.create(this,R.raw.stairs);

        contentText.setText(demoText);

    }

    private void dumpToLogEverything(){
        String recognized = "";
        String unique = "";
        for(String s:recognizedWords){
            recognized += s + " | ";
        }

        for(String s: uniqueWords){
            unique += s + " | ";
        }

        Log.wtf(DUMP,"Recognized: " + recognized);
        Log.wtf(DUMP,"Unique: " + unique);
    }

    private void askForAudioSave(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean isCreated = createFolderForRawAudio();
                Snackbar.make(container,"Создано: " + isCreated,Snackbar.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setTitle("Создание папки");
        builder.setMessage("Вы хотите сохранять аудиозаписи суфлера?");
        builder.create().show();
    }

    private boolean createFolderForRawAudio(){
        String folder_main = "Pixaero/Audio";

        File file = new File(Environment.getExternalStorageDirectory(), folder_main);
        return file.mkdirs();
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
        new RecognizerSetup(this).execute();
    }

    private void setupRecognizer(File dir) {
        try {
            recognizer = SpeechRecognizerSetup.defaultSetup()
                    .setAcousticModel(new File(dir, "ru-ptm"))
                    .setDictionary(new File(dir, "ru.dic"))
                    .setKeywordThreshold(1e-7f)
                    .setRawLogDir(new File(Environment.getExternalStorageDirectory(),AUDIO_FOLDER))
                    .getRecognizer();
        }catch (Exception e){
            Log.wtf(TAG,e);
        }

        recognizer.addListener(new recognizerListener());

        //recognizer.addKeyphraseSearch(KWS_SEARCH,KEYPHRASE);

        //File menuGrammar = new File(dir,"mymenu.gram");

        String menuGrammar = TextToGrammer.convertTextToJSGF(demoText);
        File file = TextToGrammer.saveJSFGToFile("test",menuGrammar,dir);

        recognizer.addGrammarSearch(MENU_SEARCH,file);
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
        toolbar.setTitle(R.string.app_name);
    }

    private void showMoreInfo(){

        PopupMenu menu = new PopupMenu(this,findViewById(R.id.more_info));
        menu.inflate(R.menu.popup_menu);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()){
                    case R.id.recognized_words:
                        showRecognizedWords();
                        break;
                    case R.id.for_recognize_words:
                        showWordsForRecognize();
                        break;
                }

                return true;
            }
        });
        menu.show();
    }

    private void showRecognizedWords(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String text = "";
        for(String s: recognizedWords){
            text += s + "\t";
        }
        builder.setMessage(text);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void showWordsForRecognize(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String text = "";
        for(String s: uniqueWords){
            text += s + "\t";
        }
        builder.setMessage(text);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public boolean testRecognizedWords(){

        int size = uniqueWords.size();
        int recognized = 0;
        for(String s:recognizedWords){
            if(uniqueWords.contains(s))
                recognized++;
        }
        double percent = recognized;
        percent /= size;
        Log.wtf(TAG,recognized + " " + size + " " + percent);
        if(percent > 0.5){
            controller.swipeUp();
            uniqueWords.clear();
            uniqueWords.addAll(TextToGrammer.getUniqueWordsList(controller.getCurrentShowingReadngSubString()));
        }

        Log.wtf(TAG,Double.toString(Math.ceil(percent * 100)));
        progressBar.setProgress(((int) Math.ceil(percent * 100)));

        return percent > 0.5;
    }

    class recognizerListener implements edu.cmu.pocketsphinx.RecognitionListener{
        @Override
        public void onBeginningOfSpeech() {
            Log.wtf(TAG,"Start of the Speech");
            partialResultsView.show();
        }

        @Override
        public void onEndOfSpeech() {
            Log.wtf(TAG,"End of the Speech");
        }

        @Override
        public void onPartialResult(Hypothesis hypothesis) {
            if (hypothesis == null)
                return;
            String text = hypothesis.getHypstr();
            Log.wtf(TAG,"Partial: " + text);
            List<String> list = TextToGrammer.getUniqueWordsWithoutPunctuation(text.substring(indexOfNewSequence));
            recognizedWords.clear();
            recognizedWords.addAll(list);

            String recd = "";
            for(String s: list)
                recd += s + " | ";

            partialResultsView.setText(text.substring(indexOfNewSequence) + "\n---\n" + recd);

            if(testRecognizedWords())
                indexOfNewSequence = text.length() - 1;

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

    class RecognizerSetup extends AsyncTask<Void,Void,Exception>{

        AppCompatActivity context;

        RecognizerSetup(AppCompatActivity context) {
            this.context = context;
        }

        @Override
        protected void onPostExecute(Exception e) {
            if(e != null) {
                Log.wtf(TAG,e);
                Snackbar.make(context.findViewById(R.id.container), "Exception with Voice", Snackbar.LENGTH_SHORT).show();
            }else {
                toolbar.setTitle(R.string.app_name);
                recognizerButton.setEnabled(true);
            }
        }

        @Override
        protected Exception doInBackground(Void... voids) {
            try{
                Assets assets = new Assets(context);
                File assetsDir = assets.syncAssets();
                setupRecognizer(assetsDir);
            }catch (Exception e){
                return e;
            }

            return null;
        }
    }

    private static final String text = "Когда я был молод, игры только появились и вся наша компания только и жила ими!";


    private static final String demoText = "Ну вот! Теперь я смогу записывать обращения на камеру максимально оперативно и удобно. Я очень буду ждать обновление, в котором появится личный кабинет. Там я смогу писать тексты с компьютера и синхронизировать с приложением. Программисты уже работают, чтобы добавить распознавание голоса. В этом случае скорость прокрутки текста автоматически подстроиться под мою речь. А если я начну импровизировать," +
            " текст остановится и будет ждать пока я вернусь к чтению"; //.\\n\\n А еще, я теперь знаю, что инженеры разработали мобильный телесуфлёр, который весит менее двухсот грамм, пристегивается к объективу камеры и сделан в России. Они постарались сделать его не только очень качественным и надежным, но и одним из самых доступных телесуфлёров в мире! Больше информации о суфлёр я всегда могу найти на сайте \\n\\n " +
//            "Если у меня возникнут идеи как сделать приложение или суфлёр еще более удобным, я напишу ребятам из и они постараются воплотить это в ЖИЗНЬ. ";



}
