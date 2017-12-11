package Validation;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.load.engine.Resource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by cfoxj2 on 12/9/2017.
 */

public class WordScrubber {
    private static List<String> offensiveWords;
    private Comparator<String> compare;

    public WordScrubber(Context c)
    {
        offensiveWords = new ArrayList<String>();
        try {

            InputStream inStream = c.getAssets().open("BadWords.txt");//this.getClass().getClassLoader().getResourceAsStream("assets/BadWords.txt");

            BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                offensiveWords.add(line);
            }

        }
        catch (Exception e)
        {
            Log.w("","Unable to read file");
        }
        compare = new Comparator<String>()
        {
            public int compare(String s1, String s2)
            {
                return s1.compareTo(s2);
            }
        };
    }

    public boolean isBadWord(String input)
    {
        for(int i=0; i<offensiveWords.size();++i){
            if(StringUtils.containsIgnoreCase(input, offensiveWords.get(i))) {return true;}
        }
        if(offensiveWords == null) {return false;}
        //if(Collections.binarySearch(offensiveWords, input, compare) >= 0) return true;
        return false;
    }
    /**
     * Stars some bad words from given string
     * Swear -> S****
     * @param sentence
     * @return
     */
   /* public String filterOffensiveWords(String sentence)
    {
        String returnString = "";
        String[] words = sentence.split("[\\p{Punct}\\s]+");
        for(String w: words)
        {
            if(Collections.binarySearch(offensiveWords, w, compare) >= 0)
                returnString += w.charAt(0)+ w.substring(1).replaceAll(".", "*")+ " ";
            else
                returnString += w + " ";
        }

        return filterHiddenBadWords(returnString.trim());
        //return returnString.trim();
    }
 */
    public String filterHiddenBadWords(String sentence){
        String returnString="";
        for(int i=0; i<offensiveWords.size(); ++i){ //doesn't capture case; finds buried swear words & handles copy, paste
            if(StringUtils.containsIgnoreCase(sentence, offensiveWords.get(i))){

                Boolean contain = StringUtils.containsIgnoreCase(sentence, offensiveWords.get(i));
                System.out.println(contain);

                String replacer ="";
                for(int x=0;x<offensiveWords.get(i).length();++x){//figure length of swear word
                    if(x==0){replacer+=offensiveWords.get(i).charAt(x);}
                    else {replacer+="*";}
                }
                //returnString = sentence.replace(offensiveWords.get(i),replacer);
                returnString = StringUtils.replaceIgnoreCase(sentence, offensiveWords.get(i), replacer);
                break;
            }
        }
        return returnString;
    }

}
