/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package flawfinder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
    
    private static Pattern pWhitespace = Pattern.compile("[ \\t\\v\\f]+");
    private static Pattern pInclude = Pattern.compile("#\\s*include\\s+(<.*?>|\".*?\")");
    private static Pattern pDigits = Pattern.compile("[0-9]");
    private static Pattern pAlphaAunder = Pattern.compile("[A-Za-z_]");
    private static Pattern pCword = Pattern.compile("[A-Za-z_][A-Za-z_0-9$]*");
    private static Pattern pDirective = Pattern.compile("(?i)\\s*(ITS4|Flawfinder|RATS):\\s*([^\\*]*)");
    private static final int MAX_LOOKAHEAD = 500;
    
    public static List<Hit> hitList = new ArrayList<Hit>();
    
    public FileProcessor(Arguments arguments,String file,  Map<String,Set<Integer> > patchInfo,RuleSet ruleSet)
    {
        this.arguments = arguments;
        this.patchInfo = patchInfo;
        this.file = file;
        this.ruleSet = ruleSet;
        
        this.lineNumber = 0;
        this.ignoreLine = -1;
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
        int i = 0;
        while(i<text.length())
        {
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
            if(c=='\n')
            {
                lineNumber = lineNumber+1;
                ++Statistics.SUM_LINES;
                lineBegin = 1;
                if(codeInLine!=0)
                    ++Statistics.SLOC;
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
            }
            else
            {
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

                        if(ruleSet.hasKey(word) && cValidMatch(text,startPos+endPos))
                        {
                            
                            if(patchInfo==null || (patchInfo.get(file)!=null && patchInfo.get(file).contains(lineNumber)))
                            {
                                Hit hit = new Hit(
                                        ruleSet.getRule(word),
                                        word,
                                        startPos,
                                        endPos,
                                        lineNumber,
                                        HelperFunctions.findColumn(text,startPos),
                                        file,
                                        HelperFunctions.getContext(text,startPos),
                                        HelperFunctions.extractCParameters(text,startPos+endPos,arguments.isOutputFormat()),
                                        null,
                                        arguments,""
                                        ); //no notes
                                if(hit.isExtractLookahead())
                                {
                                    if((startPos+MAX_LOOKAHEAD)>text.length())
                                        hit.setLookahead(text.substring(startPos));
                                    else
                                        hit.setLookahead(text.substring(startPos,startPos+MAX_LOOKAHEAD));
                                        
                                }
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
            ++Statistics.SLOC;
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
                ++Statistics.NUM_IGNORED_HITS;
                it.remove();
            }
        }
        
        if(!hitFound)
            ignoreLine = lineNumber + 1;
        
        return;
    }

    protected boolean cValidMatch(String text, int position) {
        char c;
        int i;
        Integer whitespaces[] = {9,10,11,12,13,32};
        for(i = position;i<text.length();)
        {
            c = text.charAt(i);
            if(c=='(')
                return true;
            else if(Arrays.asList(whitespaces).contains((int)c))
            {
                ++i;
            }
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
        
        String hook = hit.getRuleValue().getHook();
        final String C_MULTI_BYTE_TO_WIDE_CHAR = "c_multi_byte_to_wide_char";
        final String C_STATIC_ARRAY = "c_static_array";
        final String C_HIT_IF_NULL = "c_hit_if_null";
        final String NORMAL = "normal";
        final String C_PRINTF = "c_printf";
        final String C_SCANF = "c_scanf";
        final String C_STRNCAT = "c_strncat";
        final String C_BUFFER = "c_buffer";
        final String C_SPRINTF = "c_sprintf";
        
        if(hook.equals(C_MULTI_BYTE_TO_WIDE_CHAR))
            cMultiByteToWideChar(hit);
        else if(hook.equals(C_STATIC_ARRAY))
            cStaticArray(hit);
        else if(hook.equals(C_HIT_IF_NULL))
            cHitIfNull(hit);
        else if(hook.equals(NORMAL))
            normal(hit);
        else if(hook.equals(C_PRINTF))
            cPrintf(hit);
        else if(hook.equals(C_SCANF))
            cScanf(hit);
        else if(hook.equals(C_STRNCAT))
            cStrncat(hit);
        else if(hook.equals(C_BUFFER))
            cBuffer(hit);
        else if(hook.equals(C_SPRINTF))
            cSprintf(hit);
        else
        {
            System.out.println("NO EQUIVALENT HOOK FOUND!!! "+hook);
        }
        return;
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
        addWarning(hit);
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
                return;
            }
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
        Matcher m;
        m = pStaticArray.matcher(hit.getLookahead());
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
    
    private void cScanf(Hit hit)
    {
        int formatPosition = hit.getFormatPosition();
        List<String> parameters = hit.getParameters();
        Pattern pDangerousScanfFormat = Pattern.compile("%s");
        Pattern pLowRiskScanfFormat = Pattern.compile("%[0-9]+s");
        if(formatPosition<=parameters.size()-1)
        {
            String source = strip_i18n(parameters.get(formatPosition));
            if(cConstantString(source))
            {
                Matcher m1 = pDangerousScanfFormat.matcher(source);
                Matcher m2 = pLowRiskScanfFormat.matcher(source);
                if(m1.find())
                    ;//pass
                else if(m2.find())
                {
                    hit.getRuleValue().setLevel(1);
                    hit.getRuleValue().setWarning("It's unclear if the %s limit in the format string is small enough (CWE-120)");
                    hit.getRuleValue().setSuggestion("Check that the limit is sufficiently small, or use a different input function");
                }
                else
                {
                    hit.getRuleValue().setLevel(0);
                    hit.setNote("No risky scanf format detected");
                }
            }
            else
                hit.setNote("If the scanf format is influenceable by an attacker, it's exploitable.");
        }
        addWarning(hit);
    }
    
    private void cSprintf(Hit hit)
    {
        int sourcePosition = hit.getSourcePosition();
        List<String> parameters = hit.getParameters();
        Pattern pDangerousSprintfFormat = Pattern.compile("%-?([0-9]+|\\*)?s");
        if(parameters==null)
        {
            hit.getRuleValue().setWarning("format string parameter problem");
            hit.getRuleValue().setSuggestion("Check if required parameters present and quotes close.");
            hit.getRuleValue().setLevel(4);
            hit.getRuleValue().setCategory("format");
            hit.getRuleValue().setUrl("");
        }
        else if(sourcePosition<=parameters.size()-1)
        {
            String source = parameters.get(sourcePosition);
            if(cSingletonString(source))
            {
                hit.getRuleValue().setLevel(1);
                hit.setNote("Risk is low because the source is a constant character.");
            }
            else
            {

                source = strip_i18n(source);
                if(cConstantString(source))
                {
                    Matcher m = pDangerousSprintfFormat.matcher(source);
                    if(!m.find())
                    {
                        int level = hit.getRuleValue().getLevel();
                        level = Math.max(level-2, 1);
                        hit.getRuleValue().setLevel(level);
                        hit.setNote("Risk is low because the source has a constant maximum length.");
                    }
                }
                else
                {
                        hit.getRuleValue().setWarning("Potential format string problem (CWE-134)");
                        hit.getRuleValue().setSuggestion("Make format string constant");
                        hit.getRuleValue().setLevel(4);
                        hit.getRuleValue().setCategory("format");
                        hit.getRuleValue().setUrl("");
                }
            }
        }

        addWarning(hit);
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
                ++Statistics.NUM_IGNORED_HITS;
            else
            {
                hitList.add(hit);
                if(arguments.isShowImmediately())
                    System.out.println(hit);
            }
        }
    }
    
}
