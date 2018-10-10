package com.nollpointer.pixaerostt;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextToGrammer {

    private static final String REGEX = "[\\p{Punct}\\s]+ | \\n+ | !+";

    public static String convertTextToJSGF(String text){
        StringBuilder builder = new StringBuilder();
        builder.append("#JSGF V1.0;\ngrammar commands;\n");
        builder.append("public <command> = <commands>+;\n");
        builder.append("<commands> =");

        //String[] words = text.split("[,.?;:!\\-\\s]+");
        String[] words = text.split(REGEX);

        builder.append(" " + words[0].toLowerCase() + " |");

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

    public static List<String> getUniqueWordsList(String t){
        ArrayList<String> list = new ArrayList<>();
        String text = t.toLowerCase();
        String[] array = text.split(REGEX);
        for(int i=0;i<array.length;i++) {
            String str = array[i].trim();
            if(!list.contains(str))
                list.add(str);
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
