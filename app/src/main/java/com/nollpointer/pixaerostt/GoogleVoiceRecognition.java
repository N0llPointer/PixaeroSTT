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

public class GoogleVoiceRecognition extends AppCompatActivity {

    public static final String TAG = "GVR";

    Button recognizeButton;
    TextView recognizedText;

    SpeechRecognizer recognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_voice_recognition);
        recognizeButton = findViewById(R.id.recognize_button);
        recognizedText = findViewById(R.id.recognized_text);
        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new SpeechListener());
        recognizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,"com.nollpointer.pixaerostt");
                intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
                recognizer.startListening(intent);
                Log.wtf("111111","11111111");
            }
        });

        recognizedText.setText("NUASNDASD:JASD");
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

}
