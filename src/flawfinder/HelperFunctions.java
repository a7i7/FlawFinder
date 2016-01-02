/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package flawfinder;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 *
 * @author afif
 */
public class HelperFunctions {
    
    private static String version = "1.31";
    
    public static void displayHeader(boolean outputFormat)
    {
        if(outputFormat)
        {
            System.out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" " +
            "\"http://www.w3.org/TR/html4/loose.dtd\">");
            System.out.println("<html>");
            System.out.println("<head>");
            System.out.println("<meta http-equiv=\"Content-type\" content=\"text/html; charset=utf8\">");
            //write other stuff
        }
        else
            System.out.println("Flawfinder version "+version+", (C) 2001-2014 David A. Wheeler.");
    }
    
    public static String htmlize(String s)
    {
        String s1 = s.replace("&","&amp;");
        s1 = s1.replace("<","&lt;");
        s1 = s1.replace(">","&gt;");
        return s1;
    }
    
    public static String printMultiLineText(String text)
    {
        String res = "";
        final int WIDTH = 78;
        final String PREFIX = " ";
        int startingPosition = PREFIX.length()+1;
        res+=PREFIX;
        int position = startingPosition;
        StringTokenizer st = new StringTokenizer(text);
        while(st.hasMoreTokens())
        {
            String w = st.nextToken();
            if((w.length()+position)>=WIDTH)
            {
                res+="\n";
                res+=PREFIX;
                position = startingPosition;
            }
            res+=w;
            position+=w.length()+1;
        }
        return res;
    }
    
    public static int findColumn(String text,int position)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    public static String getContext(String text,int position)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    public static List<String> extractCParameters(String text)
    {
        return extractCParameters(text,0);
    }
    
    public static List<String> extractCParameters(String text,int position)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static String h(String patchFileName,boolean outputFormat) {
        if(outputFormat)
          return htmlize(patchFileName);
        else
            return patchFileName;
//        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    private static Map<String,Integer> parseToDictionary(String dictionaryString)
    {
        Map<String,Integer> map = new HashMap<String,Integer>();
        dictionaryString = dictionaryString.replace("{", "");
        dictionaryString = dictionaryString.replace("}", "");
        if(dictionaryString.length()==0)
            return map;
        String ruleValuePairs [] = dictionaryString.split(",");
        for(String s:ruleValuePairs)
        {
            String[] keyValueSeperated = s.split(":");
            String key = keyValueSeperated[0].trim();
            Integer value = Integer.parseInt(keyValueSeperated[1].trim());
            map.put(key, value);
        }
        return map;
    }
    
    public static RuleSet readRuleSet(String filename) throws FileNotFoundException, IOException
    {
        final String START_RULE = "START_RULE";
        final String END_RULE = "END_RULE";
        final int numAttributes = 8;
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
        String line;
        
        String functionName;
        String hook;
        int level;
        String temp;
        String warning;
        String suggestion;
        String category;
        String unknown;
        Map<String,Integer> other;
        
        RuleSet r = new RuleSet();
        
        while((line = br.readLine())!=null)
        {
            if(line.equals(START_RULE))
            {
                functionName = br.readLine();
                hook = br.readLine();
                level = Integer.parseInt(br.readLine());
//                temp = br.readLine();
                warning = br.readLine();
                suggestion = br.readLine();
                category = br.readLine();
                unknown = br.readLine();
                other = parseToDictionary(br.readLine());
//                temp = br.readLine();
//                System.out.println(hook);
//                System.out.println(level);
//                System.out.println(warning);
//                System.out.println(suggestion);
//                System.out.println(category);
//                System.out.println(unknown);
//                System.out.println(other);
                r.addRule(functionName, new RuleValue(hook,level,warning,suggestion,category,unknown,other));
            }
            if(!(br.readLine().equals("END_RULE")))
            {
                System.out.println("UNEXPECTED");
                return null;
            }
        }
        return r;
    }
    
}
