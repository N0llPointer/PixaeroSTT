package com.nollpointer.pixaerostt;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class GoogleVoiceRecognition extends AppCompatActivity {

    public static final String TAG = "GVR";

    private Intent recognizerIntent;

    private List<String> languages;

    FloatingActionButton recognizeButton;
    FloatingActionButton languageButton;
    TextView recognizedText;
    TextView originalText;

    SpeechRecognizer recognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_voice_recognition);
        recognizeButton = findViewById(R.id.recognize_button);
        languageButton = findViewById(R.id.language_button);
        recognizedText = findViewById(R.id.recognized_text);
        originalText = findViewById(R.id.original_text);

        searchForLanguages();

        int permissionRecord = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionRecord != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            return;
        }

        recognizerIntent = createIntentForRecognizer(null);

        initializeRecognizer();

        originalText.setText(demo);
        recognizedText.setText("Нажмите Recognize и начните говорить");
    }

    private void initializeRecognizer(){
        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new SpeechListener());
        recognizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = createIntentForRecognizer(null);
                recognizer.startListening(recognizerIntent);
            }
        });

        languageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showInstallOfflineVoiceFiles();

                AlertDialog.Builder builder = new AlertDialog.Builder(GoogleVoiceRecognition.this);
                builder.setTitle("Pick Language");
                builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                String[] array = new String[languages.size()];
                array = languages.toArray(array);
                builder.setAdapter(new ArrayAdapter<String>(GoogleVoiceRecognition.this, android.R.layout.simple_list_item_1, array), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        recognizer.stopListening();
                        recognizerIntent = createIntentForRecognizer(languages.get(which));
                        Log.wtf(TAG,languages.get(which));
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
    }

    public Intent createIntentForRecognizer(String language){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"com.nollpointer.pixaerostt");
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        // Выставление минимального времени для распознователя
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,15 * 1000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,15 * 1000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,15 * 1000);

        if(Build.VERSION.SDK_INT >= 23)
            intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true);

        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);

        if(language != null)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,language);

        return intent;
    }

    public void searchForLanguages(){
        Intent details = RecognizerIntent.getVoiceDetailsIntent(this);
        sendOrderedBroadcast(details,null,new LanguageDetailsChecker(),null, Activity.RESULT_OK,null,null);
    }

    public static final String PACKAGE_NAME_GOOGLE_NOW = "com.google.android.googlequicksearchbox";
    public static final String ACTIVITY_INSTALL_OFFLINE_FILES = "com.google.android.voicesearch.greco3.languagepack.InstallActivity";

    public boolean showInstallOfflineVoiceFiles() {

        String PACKAGE_NAME_GOOGLE_NOW = "com.google.android.googlequicksearchbox";
        String ACTIVITY_INSTALL_OFFLINE_FILES = "com.google.android.voicesearch.greco3.languagepack.InstallActivity";


        final Intent intent = new Intent();
        intent.setComponent(new ComponentName(PACKAGE_NAME_GOOGLE_NOW, ACTIVITY_INSTALL_OFFLINE_FILES));

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        try {
            startActivity(intent);
            return true;
        } catch (final Exception e) {}

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                initializeRecognizer();
            else
                finish();
        }
    }

    class LanguageDetailsChecker extends BroadcastReceiver{
        List<String> supportedLanguages;

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle results = getResultExtras(true);
            if(results.containsKey(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)){
                supportedLanguages = results.getStringArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES);
                for(String s: supportedLanguages)
                    Log.wtf(TAG,s);
                languages = supportedLanguages;
            }
            if(results.containsKey(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE)) {
                String prefs = results.getString(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE);
                Log.wtf("Preferences: ",prefs);
            }
        }
    }

    class SpeechListener implements RecognitionListener{
        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {

        }

        @Override
        public void onResults(Bundle results) {
            String text = "null";
            if(results.containsKey(SpeechRecognizer.RESULTS_RECOGNITION)){
                ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                text = data.get(0);
            }
            Log.e(TAG ,text);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            String text = "null";
            if(partialResults.containsKey(SpeechRecognizer.RESULTS_RECOGNITION)){
                ArrayList<String> data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                text = data.get(0);
            }
            recognizedText.setText(text);
        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    }

    private static final String demo = "Ну вот! Теперь я смогу записывать обращения на камеру максимально оперативно и удобно.\n" +
            "\n" +
            "Я очень буду ждать обновление, в котором появится личный кабинет. Там я смогу писать тексты с компьютера и синхронизировать с приложением. Программисты уже работают, чтобы добавить распознавание голоса. В этом случае скорость прокрутки текста автоматически подстроится под мою речь. А если я начну импровизировать, текст остановится и будет ждать пока я вернусь к чтению.\n" +
            "\n" +
            "А еще, я теперь знаю, что инженеры PIXAERO разработали мобильный телесуфлер, который весит менее двухсот грамм, пристегивается к объективу камеры и сделан в России. Они постарались сделать его не только очень качественным и надежным, но и одним из самых доступных телесуфлеров в мире! Больше информации о суфлере PIXAERO MOBUS я всегда могу найти на сайте pixaero.pro.\n" +
            "\n" +
            "Если у меня возникнут идеи как сделать приложение или суфлер еще более удобным, я напишу ребятам из PIXAERO и они постараются воплотить это в жизнь!";


}
