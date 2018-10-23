package com.nollpointer.pixaerostt.utils;

import android.util.Log;

import com.nollpointer.pixaerostt.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.pocketsphinx.Decoder;

import static com.nollpointer.pixaerostt.MainActivity.DUMP;

public class TextToGrammar {

    private static final String REGEX = "[\\p{Punct}\\s]+ | \\n+ | !+";
    //private static final String DEBUG_REGEX = "[,.?;:!\\\\-\\\\s]+";

    public static String convertTextToJSGF(String text){
        StringBuilder builder = new StringBuilder();
        builder.append("#JSGF V1.0;\ngrammar commands;\n");
        builder.append("public <command> = <commands>+;\n");
        builder.append("<commands> =");

        //String[] words = text.split("[,.?;:!\\-\\s]+");
        String[] words = text.split(REGEX);

        builder.append(" ");
        builder.append(words[0].toLowerCase());
        builder.append(" |");


        for(String w: words){

            String word = w.toLowerCase().trim();
            if(word.contains("\n")) {
                continue;
            }



            if(builder.toString().contains(" " + word + " "))
                continue;

            builder.append(" ");
            builder.append(word);
            builder.append(" |");
        }

        builder.setCharAt(builder.length()-1,';');

        return builder.toString();
    }

    public static String convertTextToJSGF(String text,Decoder decoder){
        StringBuilder builder = new StringBuilder();
        builder.append("#JSGF V1.0;\ngrammar commands;\n");
        builder.append("public <command> = <commands>+;\n");
        builder.append("<commands> =");

        //String[] words = text.split("[,.?;:!\\-\\s]+");
        String[] words = deletePunctuationSigns(text.toLowerCase()).split("\\s+");

        builder.append("\t");
        builder.append(words[0].toLowerCase());
        builder.append("\t|");


        for(String word: words){

            //String word = w.toLowerCase().trim();
            if(decoder.lookupWord(word) == null) {
                continue;
            }


            if(builder.toString().contains("\t" + word + "\t"))
                continue;

            //int threshold = getOptimalThreshold(word.length());


            builder.append("\n\t");
            builder.append(word);
            //builder.append(" /1e-" + threshold + "/");


            builder.append("\t|");
        }

        builder.setCharAt(builder.length()-1,';');

        return builder.toString();
    }



    private static int getOptimalThreshold(int wordSize){
        if(wordSize == 1)
            return 1;
        else if(wordSize == 2)
            return 3;
        else if(wordSize < 5)
            return 10;
        else if(wordSize < 10)
            return 20;
        else
            return 30;
    }

    public static void checkIfWordsAreInDictionary(Decoder decoder,String text){
        String[] words = deletePunctuationSigns(text.toLowerCase()).split("\\s+");
        int size = words.length;
        int errors = 0;
        for(String word: words) {
            if (decoder.lookupWord(word) == null) {
                errors++;
                Log.e("Check", "Unknown word: " + word);
            }
        }

        Log.e("Check", "total percent of errors: " + ((int) Math.ceil(((double) errors / size)*100)) + " %");
    }

    public static List<String> getUniqueWordsList(String t){
        ArrayList<String> list = new ArrayList<>();

        String text = deletePunctuationSigns(t.toLowerCase());
        String[] words = text.split("\\s+");
        for(String s:words) {
            if (list.contains(s))
                continue;
            else
                list.add(s);
        }
        Log.wtf(DUMP,words.length + " " + list.size());
        return list;
    }

    public static String deletePunctuationSigns(String text){
        StringBuilder builder = new StringBuilder(text);
        for(int i=0;i<builder.length();i++){
            char symbol = builder.charAt(i);
            if(Character.isLetterOrDigit(symbol))
                continue;
            else
                builder.setCharAt(i,' ');
        }

        for(int i=0;i<builder.length();i++){
            char symbol = builder.charAt(i);
            if(Character.isLetterOrDigit(symbol))
                continue;
            else
                builder.setCharAt(i,' ');
        }

        return builder.toString();
    }

    public static List<String> getUniqueWordsWithoutPunctuation(String t){
        String text = t.toLowerCase();
        ArrayList<String> list = new ArrayList<>();

        String[] array = text.split(" ");
        for(String s:array) {
            if (list.contains(s))
                continue;
            else
                list.add(s);
        }


        return list;
    }



    public static File saveJSFGToFile(String id,String text, File directory){
        File file = new File(directory,id + ".gram");
        try{
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
            writer.write(text);
            writer.flush();
            writer.close();
        }catch (Exception e){
            Log.wtf(MainActivity.TAG,e);
        }

        return file;
    }
}
