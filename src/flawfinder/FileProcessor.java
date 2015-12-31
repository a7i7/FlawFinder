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
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author afif
 */
public class FileProcessor {
    
    private Arguments arguments;
    Map<String,Set<Integer> > patchInfo;
    String file;
    private RuleSet ruleSet;
    
    private int lineNumber;
    private int ignoreLine;
    private int sumLines;
    private int sloc;
    
    private static Pattern pWhitespace = Pattern.compile("[ \\t\\v\\f]+");
    private static Pattern pInclude = Pattern.compile("#\\s*include\\s+(<.*?>|\".*?\")");
    private static Pattern pDigits = Pattern.compile("[0-9]");
    private static Pattern pAlphaAunder = Pattern.compile("[A-Za-z_]");
    private static Pattern pCword = Pattern.compile("[A-Za-z_][A-Za-z_0-9$]*");
    private static Pattern pDirective = Pattern.compile("(?i)\\s*(ITS4|Flawfinder|RATS):\\s*([^\\*]*)");
    private static final int MAX_LOOKAHEAD = 500;
    
    public FileProcessor(Arguments arguments,String file,  Map<String,Set<Integer> > patchInfo,RuleSet ruleSet)
    {
        this.arguments = arguments;
        this.patchInfo = patchInfo;
        this.file = file;
        this.ruleSet = ruleSet;
        
        this.lineNumber = 0;
        this.ignoreLine = -1;
        this.sumLines = 0;
    }
    
    public void processCFile() throws IOException
    {
        int inComment,inString,lineBegin,codeInLine;
        BufferedReader input = null;
        lineNumber = 1;
        ignoreLine = -1;
        
        inComment = 0;
        inString = 0;
        lineBegin = 1;
        codeInLine = 0;
        
        sloc = 0;
        
        if(patchInfo!=null && !patchInfo.containsKey(file))
        {
            if(!arguments.isQuiet())
            {
                if(arguments.isOutputFormat())
                    System.out.println("Skipping unpatched file "+HelperFunctions.htmlize(file)+" <br>");
                else
                    System.out.println("Skipping unpatched file "+file);
                System.out.flush();
            }
        }
        
        if(file.equals("-"))
        {
            input = new BufferedReader(new InputStreamReader(System.in));
        }
        else
        {
            try
            {
                //add symlink check
                input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            }
            catch (FileNotFoundException ex)
            {
                System.out.println("Error: failed to open "+HelperFunctions.h(file, arguments.isOutputFormat()));
                System.exit(1);
            }
        }
        
        if(!arguments.isQuiet())
        {
            if(arguments.isOutputFormat())
                System.out.println("Examining "+HelperFunctions.htmlize(file)+"<br>");
            else
                System.out.println("Examining "+file);
        }
        
        String text = "";
        String line;
        while((line=input.readLine())!=null)
        {
            text = text+line+"\n";
        }
        System.out.println(text.length());
        int i = 0;
        while(i<text.length())
        {
//            System.out.println(i);
            Matcher m = pWhitespace.matcher(text.substring(i));
            char c,nextc;
            if(m.lookingAt())
            {
                i += m.end(0);
            }
            
            if(i>=text.length())
                c = '\n';
            else
                c = text.charAt(i);
            
            if(lineBegin!=0)
            {
                lineBegin = 0;
                if(c=='#')
                    codeInLine = 1;
                m = pInclude.matcher(text.substring(i));
                if(m.lookingAt())
                {
                    i += m.end(0);
                    continue;
                }
            }
//            System.out.println("here "+i);
            if(c=='\n')
            {
                lineNumber = lineNumber+1;
                sumLines = sumLines + 1;
                lineBegin = 1;
                if(codeInLine!=0)
                    ++sloc;
                codeInLine = 0;
                ++i;
                continue;
            }
            
            ++i;
            
            if(i<text.length())
                nextc = text.charAt(i);
            else
                nextc = 0;
            
            if(inComment!=0)
            {
                if(c=='*' && nextc=='/')
                {
                    ++i;
                    inComment = 0;
                }
            }
            else if(inString!=0)
            {
                if(c=='\\' && nextc!='\n')
                    ++i;
                else if(c=='"' && inString==1)
                    inString = 0;
                else if(c=='\'' && inString==2)
                    inString = 0;
            }//line 1417
            else
            {
//                System.out.println("here "+i);
                if(c=='/' && nextc=='*')
                {
                    m = pDirective.matcher(text.substring(i+1));
                    if(m.lookingAt())
                        processDirective();
                    ++i;
                    inComment = 1;
                }
                else if(c=='/' && nextc=='/')
                {
                    m = pDirective.matcher(text.substring(i+1));
                    if(m.lookingAt())
                        processDirective();
                    while(i<text.length() && text.charAt(i)!='\n')
                        ++i;
                }
                else if(c=='"')
                {
                    inString = 1;
                    codeInLine = 1;
                }
                else if(c=='\'')
                {
                    inString = 2;
                    codeInLine = 1;
                }
                else
                {
                    codeInLine = 1;
                    m = pCword.matcher(text.substring(i-1));
                    if(m.lookingAt())
                    {
                        int startPos = i-1;
                        int endPos = m.end(0);  //length actually
                        i+=endPos-1;
                        String word = "";
                        word = text.substring(startPos,startPos+endPos);
                        System.out.println(word);
//                        System.out.println("ERROR "+startPos+" "+endPos);
                        if(ruleSet.hasKey(word) && cValidMatch(text,startPos+endPos))
                        {
                            if(patchInfo==null || (patchInfo.get(file)!=null && patchInfo.get(file).contains(lineNumber)))
                            {
                                //hit stuff here
                            }
                        }
                    }
                    else if(c>='0' && c<='9')
                    {
                          while(i<text.length() && text.charAt(i) >='0' && text.charAt(i)<='9')
                              ++i;
                    }            
                }
            }   
            
        }
        if(codeInLine!=0)
            ++sloc;
        if(inComment!=0)
            System.out.println("ERROR: File ended while in comment");
        if(inString!=0)
            System.out.println("ERROR: File ended while in string");
                 
        
    }

    private void processDirective() {
//        throw new UnsupportedOperationException("Not yet implemented");
        return;
    }

    private boolean cValidMatch(String text, int position) {
        char c;
        String whitespaces = "\f\t\n\r ";
        for(int i = position;position<text.length();i++)
        {
            c = text.charAt(i);
            if(c=='(')
                return true;
            else if(((int)c>=9 && (int)c<=13) || ((int)c==32))
                ++i;
            else
            {
                if(arguments.isFalsePositive())
                    return false;
                if(c=='=' || c=='+' || c=='-')
                    return false;
                return true;
            }
            
        }
        return false;
    }
    
    
}
