package com.nollpointer.pixaerostt;

import android.content.Intent;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button voiceRecognizerButton;
    private TextView partialResults;
    private TextView fullResults;

    private SpeechRecognizer recognizer;

    private boolean isRecording = false;
    private Intent recognizerIntent;

    public static String TAG = "STT";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        voiceRecognizerButton = findViewById(R.id.button_voice_recognizer_android);
        partialResults = findViewById(R.id.text_view_partial);
        fullResults = findViewById(R.id.text_view_full);

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognizerListener());

        recognizerIntent = createRecognizerIntent();

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
    }

    private Intent createRecognizerIntent(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,"ru-RU");
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,3000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,3000);


        return intent;
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
}
