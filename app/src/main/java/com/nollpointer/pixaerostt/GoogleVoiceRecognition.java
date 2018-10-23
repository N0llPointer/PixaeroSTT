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
import android.media.AudioManager;
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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.nollpointer.pixaerostt.utils.TextToGrammar;

import java.util.ArrayList;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class GoogleVoiceRecognition extends AppCompatActivity {

    public static final String TAG = "GVR";

    private Intent recognizerIntent;

    private List<String> languages;

    Toolbar toolbar;
    FloatingActionButton recognizeButton;
    FloatingActionButton languageButton;
    FloatingActionButton textButton;
    //TextView recognizedText;
    TextView contentText;
    ScrollView scrollView;
    ScrollViewController controller;

    SpeechRecognizer recognizer;

    int textSize = 30;

    String partialResultsString = "null";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //setContentView(R.layout.activity_main);

        Fabric.with(this, new Crashlytics());


        setContentView(R.layout.activity_google_voice_recognition);
        recognizeButton = findViewById(R.id.recognize_button);
        languageButton = findViewById(R.id.language_button);
        textButton = findViewById(R.id.text_button);
        //recognizedText = findViewById(R.id.recognized_text);
        contentText = findViewById(R.id.original_text);
        toolbar = findViewById(R.id.toolbar);
        scrollView = findViewById(R.id.scrollView);

        initializeToolbar();

        searchForLanguages();

        int permissionRecord = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionRecord != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            return;
        }

        recognizerIntent = createIntentForRecognizer(null);

        initializeRecognizer();

        contentText.setText(demo);
        contentText.setTextSize(textSize);

        controller = new ScrollViewController(scrollView, contentText);

        //recognizedText.setText("Нажмите Recognize и начните говорить");
    }

    @Override
    protected void onStart() {
        super.onStart();

        AudioManager audio = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));
        audio.setStreamMute(AudioManager.STREAM_MUSIC,true);

    }

    @Override
    protected void onStop() {
        super.onStop();


        AudioManager audio = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));
        audio.setStreamMute(AudioManager.STREAM_MUSIC,false);
    }

    private void initializeToolbar() {
        toolbar.inflateMenu(R.menu.recognizer_toolbar_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.font_up:
                        textSize += 2;
                        break;
                    case R.id.font_down:
                        textSize -= 2;
                        break;
                    case R.id.more_info:
                        InfoDialog.getInstance(partialResultsString).show(getSupportFragmentManager(), TAG);
                        break;
                }

                contentText.setTextSize(textSize);

                return true;
            }
        });
    }

    private void initializeRecognizer() {
        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new SpeechListener());
        recognizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = createIntentForRecognizer(null);
                recognizer.startListening(recognizerIntent);
                toolbar.setTitle("Listening");
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
                        Log.wtf(TAG, languages.get(which));
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });

        textButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                partialResultsString = "ну вот теперь я смогу записывать обращения на камеру максимально оперативно и удобно";
                Log.e(TAG, "onClick: textButton");
                controller.processDataV2(partialResultsString);
            }
        });
    }

    public Intent createIntentForRecognizer(String language) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.nollpointer.pixaerostt");
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        // Выставление минимального времени для распознователя
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 25 * 1000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 25 * 1000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 25 * 1000);
        //intent.putExtra(RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT,true);

        //intent.putExtra("android.speech.extra.DICTATION_MODE", true);

        if (Build.VERSION.SDK_INT >= 23)
            intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);

        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        if (language != null)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);

        return intent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String d = data.getExtras().getString(RecognizerIntent.EXTRA_RESULTS_PENDINGINTENT);
        Log.e(TAG, "onActivityResult: " + d);
    }

    public void searchForLanguages() {
        Intent details = RecognizerIntent.getVoiceDetailsIntent(this);
        sendOrderedBroadcast(details, null, new LanguageDetailsChecker(), null, Activity.RESULT_OK, null, null);
    }

    public boolean showInstallOfflineVoiceFiles() {

        String PACKAGE_NAME_GOOGLE_NOW = "com.google.android.googlequicksearchbox";
        String ACTIVITY_INSTALL_OFFLINE_FILES = "com.google.android.voicesearch.greco3.languagepack.InstallActivity";


        final Intent intent = new Intent();
        intent.setComponent(new ComponentName(PACKAGE_NAME_GOOGLE_NOW, ACTIVITY_INSTALL_OFFLINE_FILES));

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        try {
            startActivity(intent);
            return true;
        } catch (final Exception e) {
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                initializeRecognizer();
            else
                finish();
        }
    }

    class LanguageDetailsChecker extends BroadcastReceiver {
        List<String> supportedLanguages;

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle results = getResultExtras(true);
            if (results.containsKey(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)) {
                supportedLanguages = results.getStringArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES);
                for (String s : supportedLanguages)
                    Log.wtf(TAG, s);
                languages = supportedLanguages;
            }
            if (results.containsKey(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE)) {
                String prefs = results.getString(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE);
                Log.wtf("Preferences: ", prefs);
            }
        }
    }

    class SpeechListener implements RecognitionListener {
        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {
            toolbar.setTitle("Start");
        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {
            toolbar.setTitle("End");
//            recognizer.stopListening();
//            recognizer.startListening(recognizerIntent);

        }

        @Override
        public void onError(int error) {

        }

        @Override
        public void onResults(Bundle results) {
            String text = "null";
            if (results.containsKey(SpeechRecognizer.RESULTS_RECOGNITION)) {
                ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                text = data.get(0);
            }
            Log.e(TAG, text);
            //toolbar.setTitle("Pixaero");
            //recognizer.startListening(recognizerIntent);


            recognizer.startListening(recognizerIntent);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            if (partialResults.containsKey(SpeechRecognizer.RESULTS_RECOGNITION)) {
                ArrayList<String> data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                partialResultsString = data.get(0);
            }
            controller.processDataV2(partialResultsString);

            Log.e(TAG, "onPartialResults: " + partialResultsString );
            //recognizedText.setText(text);
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
