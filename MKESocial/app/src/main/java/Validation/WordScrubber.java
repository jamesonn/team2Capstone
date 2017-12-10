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
        if(offensiveWords == null) return false;
        if(Collections.binarySearch(offensiveWords, input, compare) >= 0)
            return true;
        return false;
    }
    /**
     * Stars some bad words from given string
     * Swear -> S****
     * @param sentence
     * @return
     */
    public String filterOffensiveWords(String sentence)
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
        return returnString.trim();
    }



}
