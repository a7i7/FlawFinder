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
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
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
    
    private static int numIgnoredHits = 0;
    
    private static List<Hit> hitList = new ArrayList<Hit>();
    
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
                                Hit hit = new Hit(
                                        ruleSet.getRule(word),
                                        word,
                                        startPos,
                                        endPos,
                                        lineNumber,
                                        HelperFunctions.findColumn(text,startPos),
                                        file,
                                        HelperFunctions.getContext(text,startPos),
                                        HelperFunctions.extractCParameters(text,endPos),
                                        arguments.isExtractLookaheadEnabled()?text.substring(startPos,startPos+arguments.getMaxLookahead()):null,
                                        arguments,""
                                        ); //no notes
                                 startHooking(hit);
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
        
        if(arguments.isNeverIgnore())
            return;
        boolean hitFound = false;
        ListIterator<Hit> it = hitList.listIterator(hitList.size());
        while(it.hasPrevious())
        {
            Hit h = it.previous();
            if(h.getFilename().equals(file) && h.getLine()==lineNumber)
            {
                hitFound = true;
                ++numIgnoredHits;
                it.remove();
            }
        }
        
        if(!hitFound)
            ignoreLine = lineNumber + 1;
        
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

    private void startHooking(Hit hit) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    private void cBuffer(Hit hit)
    {
        String source;
        int sourcePosition = hit.getSourcePosition();
        List<String> parameters = hit.getParameters();
        if(sourcePosition<=parameters.size()-1)
        {
            source = parameters.get(sourcePosition);
            if(cSingletonString(source))
            {
                hit.getRuleValue().setLevel(1);
                hit.setNote("Risk is low because the source is a constant character.");
            }
            else if(cConstantString(strip_i18n(source)))
            {
                int level = hit.getRuleValue().getLevel();
                level = Math.max(1, level-2);
                hit.getRuleValue().setLevel(level);
                hit.setNote("Risk is low because the source is a constant string.");
            }
        }
        
    }

    private boolean cSingletonString(String text) {
        
        Pattern pCSingletonString = Pattern.compile("^\\s*L?\"([^\\\\]|\\\\[^0-6]|\\\\[0-6]+)?\"\\s*$");
        Matcher m = pCSingletonString.matcher(text);
        return m.find();
    }
    
    private String strip_i18n(String text)
    {
        Pattern getTextPattern = Pattern.compile("(?s)^\\s*gettext\\s*\\((.*)\\)\\s*$");
        Pattern underscorePattern = Pattern.compile("(?s)^\\s*_(T(EXT)?)?\\s*\\((.*)\\)\\s*$");
        Matcher m;
        m = getTextPattern.matcher(text);
        if(m.find())
            return m.group(1).trim();
        m = underscorePattern.matcher(text);
        if(m.find())
            return m.group(3).trim();
        return text;
    }

    private boolean cConstantString(String text) {
        
        Pattern pCConstantString = Pattern.compile("^\\s*L?\"([^\\\\]|\\\\[^0-6]|\\\\[0-6]+)*\"$");
        Matcher m = pCConstantString.matcher(text);
        return m.find();
    }
    
    private void cStrncat(Hit hit)
    {
        Pattern pDangerousStrncat = Pattern.compile("^\\s*sizeof\\s*(\\(\\s*)?[A-Za-z_$0-9]+\\s*(\\)\\s*)?(-\\s*1\\s*)?$");
        Pattern pLooksLikeConstant = Pattern.compile("^\\s*[A-Z][A-Z_$0-9]+\\s*(-\\s*1\\s*)?$");
        Matcher m1,m2 ;
        if(hit.getParameters().size()>3)
        {
            String lengthText = hit.getParameters().get(3);
            m1 = pDangerousStrncat.matcher(lengthText);
            m2 = pLooksLikeConstant.matcher(lengthText);
            if(m1.find() || m2.find())
            {
                hit.getRuleValue().setLevel(5);
                hit.setNote("Risk is high; the length parameter appears to be a constant, " +
                 "instead of computing the number of characters left.");
                addWarning(hit);
            }
            return;
        }
        cBuffer(hit);
    }
    
    private void normal(Hit hit)
    {
        addWarning(hit);
    }
    
    private void cStaticArray(Hit hit)
    {
        Pattern pStaticArray = Pattern.compile("^[A-Za-z_]+\\s+[A-Za-z0-9_$,\\s\\*()]+\\[[^]]");
        Matcher m = pStaticArray.matcher(hit.getLookahead());
        if(m.find())
            addWarning(hit);
    }
    
    private void cMultiByteToWideChar(Hit hit)
    {
        Pattern pDangerousMultiByte = Pattern.compile("^\\s*sizeof\\s*(\\(\\s*)?[A-Za-z_$0-9]+"  +
                                    "\\s*(\\)\\s*)?(-\\s*1\\s*)?$");
        Pattern pSafeMultiByte = Pattern.compile("^\\s*sizeof\\s*(\\(\\s*)?[A-Za-z_$0-9]+\\s*(\\)\\s*)?" +
                                     "/\\s*sizeof\\s*\\(\\s*?[A-Za-z_$0-9]+\\s*" +
                                     "\\[\\s*0\\s*\\]\\)\\s*(-\\s*1\\s*)?$");
        
        List<String> parameters = hit.getParameters();
        if((parameters.size()-1)>=6)
        {
            String numCharsToCopy = parameters.get(6);
            Matcher m1,m2;
            m1 = pDangerousMultiByte.matcher(numCharsToCopy);
            m2 = pSafeMultiByte.matcher(numCharsToCopy);
            if(m1.find())
            {
                hit.getRuleValue().setLevel(5);
                hit.setNote("Risk is high, it appears that the size is given as bytes, but the " +
                 "function requires size as characters.");
            }
            else if(m2.find())
            {
                hit.getRuleValue().setLevel(1);
                hit.setNote("Risk is very low, the length appears to be in characters not bytes.");
            }
        }
        addWarning(hit);
    }
    
    private void cHitIfNull(Hit hit)
    {
        Pattern pNullText = Pattern.compile("^ *(NULL|0|0x0) *$");
        int nullPosition = hit.getCheckForNull();
        if(nullPosition<=(hit.getParameters().size()-1))
        {
            String nullText = hit.getParameters().get(nullPosition);
            Matcher m = pNullText.matcher(nullText);
            if(m.find())
                addWarning(hit);
            else
                return;
        }
        addWarning(hit);    //doubtful about double call
    }
    
    private void cPrintf(Hit hit)
    {
        int formatPosition = hit.getFormatPosition();
        List<String> parameters = hit.getParameters();
        if(formatPosition<=(parameters.size()-1))
        {
            String source = strip_i18n(parameters.get(formatPosition));
            if(cConstantString(source))
            {
                if(hit.getName().equals("snprintf") || hit.getName().equals("vsnprintf"))
                {
                    hit.getRuleValue().setLevel(1);
                    hit.getRuleValue().setWarning("On some very old systems, snprintf is incorrectly implemented " + "and permits buffer overflows; there are also incompatible "
                            + "standard definitions of it");
                    hit.getRuleValue().setSuggestion("Check it during installation, or use something else");
                    hit.getRuleValue().setCategory("port");
                }
                else
                {
                    hit.getRuleValue().setLevel(0);
                    hit.setNote("Constant format string, so not considered very risky (there's some residual risk, especially in a loop).");
                }
            }
        }
        addWarning(hit);
    }
    
    private void addCScanf(Hit hit)
    {
        
    }
    private void addWarning(Hit hit)
    {
        if(arguments.isShowInputs() && hit.getInput()==0)
            return;
        Pattern requiredRegex = arguments.getRequiredRegex();
        if(requiredRegex!=null)
        {
            Matcher m = requiredRegex.matcher(hit.getRuleValue().getWarning());
            if(!m.find())
                return;
        }
        if(hit.getRuleValue().getLevel()>=arguments.getMinimumLevel())
        {
            if(lineNumber==ignoreLine)
                ++numIgnoredHits;
            else
            {
                hitList.add(hit);
                if(arguments.isShowImmediately())
                    System.out.println(hit);
            }
        }
    }
    
}
