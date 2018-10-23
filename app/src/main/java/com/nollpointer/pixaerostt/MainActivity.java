package com.nollpointer.pixaerostt;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.nollpointer.pixaerostt.utils.TextToGrammar;
import com.nollpointer.pixaerostt.views.CountDownView;
import com.nollpointer.pixaerostt.views.MenuSeekBar;
import com.nollpointer.pixaerostt.views.PartialResultsView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton recognizerButton;

    //private Dialog progressDialog;


    private String currentText = demka;
    private boolean isRussian = true;


    private ScrollView scrollView;
    private TextView contentText;
    private CountDownView countDownView;
    private FrameLayout container;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private PartialResultsView partialResultsView;
    private MenuSeekBar seekbar;

    private ScrollViewController controller;

    public static String TAG = "STT";
    public static String DUMP = "DUMP";

    private static final String KWS_SEARCH = "wakeup";
    private static final String MENU_SEARCH = "menu";

    //private static final String KEYPHRASE = "well hello";

    public static final String AUDIO_FOLDER = "Pixaero/Audio";

    private edu.cmu.pocketsphinx.SpeechRecognizer recognizer;

    private boolean isPocketOnGoing = false;

    private MediaPlayer voiceRecognitionSoundEffect;

    private ArrayList<String> recognizedWords = new ArrayList<>();

    private ArrayList<String> uniqueWords = new ArrayList<>();

    private int indexOfNewSequence = 0;

    private int threshold = 7;
    private StringBuilder recognized;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

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

        seekbar = new MenuSeekBar(this);
        container.addView(seekbar);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                toolbar.setSubtitle("Threshold = " + progress);
                threshold = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar.setVisibility(View.GONE);
                toolbar.setSubtitle(null);
                resetRecognizer(currentText,isRussian);
            }
        });

        recognizerButton.hide();

        toolbar.inflateMenu(R.menu.main_menu);

        int permissionWrite = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionRecord = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionRecord != PackageManager.PERMISSION_GRANTED && permissionWrite != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1);
            return;
        }

        recognizerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPocketOnGoing) {
                    recognizer.stop();
                    toolbar.setTitle(R.string.app_name);
                    recognizerButton.setImageResource(R.drawable.ic_play);
                }else {
                    controller = new ScrollViewController(scrollView,contentText,MainActivity.this);
                    indexOfNewSequence = 0;
                    voiceRecognitionSoundEffect.start();
                    toolbar.setTitle(R.string.listening);
                    countDownView.setVisibility(View.VISIBLE);
                    countDownView.startCountDown(3);
                    recognizerButton.setImageResource(R.drawable.ic_pause);
                    recognizer.startListening(MENU_SEARCH);
                }
                isPocketOnGoing = !isPocketOnGoing;
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.threshold:
                        seekbar.setProgress(threshold);
                        seekbar.show(container);
                        break;
//                    case R.id.scroll_speed:
//                        pickText();
//                        //askForAudioSave();
//                        //controller.increaseScrollDelta(2);
//                        break;
                    case R.id.more_info:
                        dumpToLogEverything();
                        break;
                }
                return true;
            }
        });

        runRecognizerSetup(demka,true);

        voiceRecognitionSoundEffect = MediaPlayer.create(this,R.raw.stairs);

        contentText.setText(demka);

    }

    private void pickText(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                resetRecognizer(currentText,isRussian);
            }
        });
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        int which = 0;

        switch (currentText){
            case demka:
                which = 0;
                break;
            case englishText:
                which = 1;
                break;
            case testText:
                which = 2;
                break;
        }

        builder.setSingleChoiceItems(R.array.text_to_pick, which, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        currentText = demka;
                        isRussian = true;
                        break;
                    case 1:
                        currentText = englishText;
                        isRussian = false;
                        break;
                    case 2:
                        currentText = testText;
                        isRussian = true;
                        break;
                }

            }
        });
        builder.create().show();
    }


    private void dumpToLogEverything(){
        StringBuilder recognized = new StringBuilder();

        StringBuilder unique = new StringBuilder();
        for(String s:recognizedWords){
            recognized.append(s);
            recognized.append(" | ");
        }

        for(String s: uniqueWords){
            unique.append(s);
            unique.append(" | ");
        }

        Log.wtf(DUMP,"Recognized: " + recognized.toString());
        Log.wtf(DUMP,"Unique: " + unique.toString());
    }

    private void resetRecognizer(String text, boolean isRussian){
        recognizer.stop();
        recognizer.shutdown();
        contentText.setText(text);
        controller = new ScrollViewController(scrollView,contentText,this);
        runRecognizerSetup(text,isRussian);
        recognizerButton.hide();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                runRecognizerSetup(currentText,isRussian);
            else
                finish();
        }
    }

    private void runRecognizerSetup(String text, boolean isRussian){
        new RecognizerSetup(this,text,isRussian).execute();
    }

    private File createAudioSessionFolder(){
        long time = System.currentTimeMillis();
        String sessionFolderName = Long.toHexString(time);

        File file = new File(Environment.getExternalStorageDirectory(),AUDIO_FOLDER + "/" + sessionFolderName);
        boolean isCreated = file.mkdirs();

        if(isCreated)
            return file;
        else
            return null;
    }

    private void setupRecognizer(File dir, String text, boolean isRussian) {
        File audioSessionFolder = createAudioSessionFolder();
        try {
            SpeechRecognizerSetup setup = SpeechRecognizerSetup.defaultSetup();

            if(isRussian) {
                setup.setAcousticModel(new File(dir, "ru-ptm"))
                        .setDictionary(new File(dir, "ru.dic"));
            }else {
                setup.setAcousticModel(new File(dir, "en-us-ptm"))
                        .setDictionary(new File(dir, "cmudict-en-us.dict"));
            }

                    setup.setKeywordThreshold((float) Math.pow(1,-threshold));
            if(audioSessionFolder != null)
                        setup.setRawLogDir(audioSessionFolder);

                    //.setBoolean("-remove_noise", false)

                    recognizer = setup.getRecognizer();
        }catch (Exception e){
            Log.wtf(TAG,e);
        }

        recognizer.addListener(new recognizerListener());

        TextToGrammar.checkIfWordsAreInDictionary(recognizer.getDecoder(),text);

        String menuGrammar = TextToGrammar.convertTextToJSGF(text,recognizer.getDecoder());
        File file = TextToGrammar.saveJSFGToFile("test",menuGrammar,dir);

        recognizer.addGrammarSearch(MENU_SEARCH,file);

        //progressDialog.dismiss();
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
            //Log.wtf(TAG,"Partial: " + text);
            List<String> list = TextToGrammar.getUniqueWordsWithoutPunctuation(text.substring(indexOfNewSequence));
            recognizedWords.clear();
            recognizedWords.addAll(list);


            String str = controller.processData(recognizedWords);
            if(str != null)
                indexOfNewSequence = text.lastIndexOf(str);

//            String recd = "";
//            for(String s: list)
//                recd += s + " | ";
            partialResultsView.setText(text.substring(indexOfNewSequence));
            //testRecognizedWords();

            //if(testRecognizedWords())
            //    indexOfNewSequence = text.length() - 1;

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
            //switchSearch(KWS_SEARCH);
        }
    }

    class RecognizerSetup extends AsyncTask<Void,Void,Exception>{

        AppCompatActivity context;
        String text;
        boolean isRussian;

        RecognizerSetup(AppCompatActivity context, String text, boolean isRussian) {
            this.context = context;
            this.text = text;
            this.isRussian = isRussian;
        }

        @Override
        protected void onPostExecute(Exception e) {
            if(e != null) {
                Log.wtf(TAG,e);
                Snackbar.make(context.findViewById(R.id.container), "Exception with Voice", Snackbar.LENGTH_SHORT).show();
            }else {
                toolbar.setTitle(R.string.app_name);
                recognizerButton.show();
                Toast.makeText(context,"Threshold = " + threshold,Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Exception doInBackground(Void... voids) {
            try{
                Assets assets = new Assets(context);
                File assetsDir = assets.syncAssets();
                setupRecognizer(assetsDir,text,isRussian);
            }catch (Exception e){
                return e;
            }

            return null;
        }
    }

    private static final String demka = "Ну вот! Теперь я смогу записывать обращения на камеру максимально оперативно и удобно.\n" +
            "\n" +
            "Я очень буду ждать обновление, в котором появится личный кабинет. Там я смогу писать тексты с компьютера и синхронизировать с приложением. Программисты уже работают, чтобы добавить распознавание голоса. В этом случае скорость прокрутки текста автоматически подстроится под мою речь. А если я начну импровизировать, текст остановится и будет ждать пока я вернусь к чтению.\n" +
            "\n" +
            "А еще, я теперь знаю, что инженеры PIXAERO разработали мобильный телесуфлер, который весит менее двухсот грамм, пристегивается к объективу камеры и сделан в России. Они постарались сделать его не только очень качественным и надежным, но и одним из самых доступных телесуфлеров в мире! Больше информации о суфлере PIXAERO MOBUS я всегда могу найти на сайте pixaero.pro.\n" +
            "\n" +
            "Если у меня возникнут идеи как сделать приложение или суфлер еще более удобным, я напишу ребятам из PIXAERO и они постараются воплотить это в жизнь!";

    private static final String testText = "А сейчас парень и в каком формате вам не надо было дома остаться без НДС с другой компанией и все что нужно для начала работы и в конце концов это не важно что бы вы прислать фото не очень хорошо что у вас он есть у вас есть возможность и стоимость по договору подряда и все это время в пути в том же духе в не знаю как это развивать и все же я могу ему не хватает в универ " +
            "новая и не было бы хорошо с вами по Скайпу и не было оповещения и не было оповещения и трансляции на 50 не оч хорошо в ближайшее к нам на склад не знаю почему не работает и в конце недели или на группу подписался в том же месте не было бы здорово встретиться в четверг в том же году в универ новая в том числе на работе не было оповещения от меня и у него огромная просьба к вам не нужно для того настроя не оч " +
            "охота за информацию о вашем заказе в универ новая и новейшая технология и не было в наличии есть все необходимые данные не знаю что делать будешь уже в кровати в том же месте решим как раз в месяц в США от плиты взять в аренду в том же месте и в этом году мы не сможем получить от вас было получено письмо не является обязательным является одним или двумя руками не оч удобно и не только от меня пойдем к сожалению" +
            " в связи в случае необходимости готов работать по договору займа в универ новая в том числе и по проектам и в конце недели и до конца дня пришлю в ближайшее будущее России по республике Казахстан Алматы в универ и в конце есть в наличие и цену не знаю почему так что я уже хочу полировать в универ новая и не только" +
            " в том числе в том же месте не было оповещения о нем не было бы хорошо чтобы за что я уже забыл про меня есть план в приложении во вкладке не так давно я тоже так что не надо будет в понедельник после 3 курс по дифурам экзамен на индивидуальный предприниматель в том числе в не очень охота и предоставление информации в соответствии со строками в универ и не понял о чём речь в данном этапе это сообщение";

    private static final String englishText = "Students compile a collection of their texts in a variety of genres over time and choose two pieces to present for summative assessment. In the majority of cases, the work in the student’s collection will arise from normal classwork, as the examples below illustrate. \n" +
            " \n" +
            "The annotations capture insights by the student’s teacher, using the features of quality, with a view to establishing the level of achievement the text reflects. The purpose of the annotations is to make the teacher's thinking visible. The annotations were confirmed by the Quality Assurance group, consisting of practicing English teachers and representatives of the Inspectorate, the SEC and JCT. \n" +
            " \n" +
            "The purpose of these examples is to support teachers' professional development. They are not to be used for any other purpose. More examples will be added over time.";



//    public boolean testRecognizedWords(){
//
//        int size = uniqueWords.size();
//        int recognized = 0;
//        for(String s:recognizedWords){
//            if(uniqueWords.contains(s))
//                recognized++;
//        }
//        double percent = recognized;
//        percent /= size;
//        Log.wtf(TAG,recognized + " " + size + " " + percent);
//        Log.wtf(TAG,Double.toString(Math.ceil(percent * 100)));
//        progressBar.setProgress(((int) Math.ceil(percent * 100)));
//
//        return percent > 0.5;
//    }


//    private void askForAudioSave(){
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                boolean isCreated = createFolderForRawAudio();
//                Snackbar.make(container,"Создано: " + isCreated,Snackbar.LENGTH_SHORT).show();
//                dialog.dismiss();
//            }
//        });
//        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        builder.setTitle("Создание папки");
//        builder.setMessage("Вы хотите сохранять аудиозаписи суфлера?");
//        builder.create().show();
//    }


//    private void askForAudioSave(){
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                boolean isCreated = createFolderForRawAudio();
//                Snackbar.make(container,"Создано: " + isCreated,Snackbar.LENGTH_SHORT).show();
//                dialog.dismiss();
//            }
//        });
//        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        builder.setTitle("Создание папки");
//        builder.setMessage("Вы хотите сохранять аудиозаписи суфлера?");
//        builder.create().show();
//    }

//    private boolean createFolderForRawAudio(){
//        String folder_main = "Pixaero/Audio";
//
//        File file = new File(Environment.getExternalStorageDirectory(), folder_main);
//        return file.mkdirs();
//    }

}