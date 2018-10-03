package com.nollpointer.pixaerostt;

public class TextToGrammer {

    public static String convertTextToJSGF(String text){
        StringBuilder builder = new StringBuilder();
        builder.append("#JSGF V1.0;\ngrammar commands;\n");
        builder.append("public <command> = <commands>+;\n");
        builder.append("<commands> =");

        String[] words = text.split("[,.?;:!\\-\\s\n]+");

        builder.append(" " + words[0].toLowerCase() + " |");

        for(String word: words){

            if(builder.toString().contains(" " + word.toLowerCase() + " "))
                continue;

            builder.append(" ");
            builder.append(word.toLowerCase());
            builder.append(" |");
        }

        builder.setCharAt(builder.length()-1,';');

        return builder.toString();
    }

    public static String saveJSFGToFile(String text){



        return "";
    }
}
