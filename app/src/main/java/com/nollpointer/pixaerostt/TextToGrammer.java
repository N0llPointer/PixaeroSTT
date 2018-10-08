package com.nollpointer.pixaerostt;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class TextToGrammer {

    public static String convertTextToJSGF(String text){
        StringBuilder builder = new StringBuilder();
        builder.append("#JSGF V1.0;\ngrammar commands;\n");
        builder.append("public <command> = <commands>+;\n");
        builder.append("<commands> =");

        //String[] words = text.split("[,.?;:!\\-\\s]+");
        String[] words = text.split("[\\p{Punct}\\s]+");

        builder.append(" " + words[0].toLowerCase() + " |");

        for(String w: words){

            String word = w.toLowerCase();
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
