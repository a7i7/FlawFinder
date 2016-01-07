/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package flawfinder;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 *Each instance of Hit is a warning of some kind in a source code file.
  See the rulesets, which define the conditions for triggering a hit.
  Hit is initialized with a tuple containing the following:
    hook: function to call when function name found.
    level: (default) warning level, 0-5. 0=no problem, 5=very risky.
    warning: warning (text saying what's the problem)
    suggestion: suggestion (text suggesting what to do instead)
    category: One of "buffer" (buffer overflow), "race" (race condition),
              "tmpfile" (temporary file creation), "format" (format string).
              Use "" if you don't have a better category.
    url: URL fragment reference.
    other:  A dictionary with other settings.

  Other settings usually set:

    name: function name
    parameter: the function parameters (0th parameter null)
    input: set to 1 if the function inputs from external sources.
    start: start position (index) of the function name (in text)
    end:  end position of the function name (in text)
    filename: name of file
    line: line number in file
    column: column in line in file
    context_text: text surrounding hit
 * @author afif
 */
public class Hit implements Serializable{
    
    private RuleValue ruleValue;
    private String name;
    private int start;
    private int end;
    private int line;
    private int column;
    private String filename;
    private String contextText;
    private List<String> parameters;
    private String lookahead;
    private Arguments arguments;
    private String note;
    
    //extra attributes
    private int input;
    private int formatPosition;
    private int sourcePosition;
    private boolean extractLookahead;
    private int checkForNull;
    
    private static final String FORMAT_POSITION_KEY = "'format_position'";
    private static final String CHECK_FOR_NULL_KEY = "'check_for_null'";
    private static final String INPUT_KEY = "'input'";
    private static final String EXTRACT_LOOKAHEAD_KEY = "'extract_lookahead'";
    private static final String SOURCE_POSITION_KEY = "'source_position'"; //not used
    
    public Hit(RuleValue ruleValue, String name, 
            int start, int end, 
            int line, int column, 
            String filename, String contextText,
            List<String> parameters,  String lookahead, 
            Arguments arguments, String note) {
        this.input = 0;
        this.formatPosition = 1;
        this.sourcePosition = 2;
        this.extractLookahead = false;
        this.note = "";
        this.filename = "";
        
        this.ruleValue = new RuleValue(ruleValue);
        this.name = name;
        this.start = start;
        this.end = end;
        this.line = line;
        this.column = column;
        this.filename = filename;
        this.contextText = contextText;
        this.parameters = parameters;
        this.lookahead = lookahead;
        this.arguments = arguments;
        this.note = note;
        
        Map<String,Integer> other = ruleValue.getOther();
        if(other.containsKey(FORMAT_POSITION_KEY))
            formatPosition = other.get(FORMAT_POSITION_KEY);
        if(other.containsKey(CHECK_FOR_NULL_KEY))
            checkForNull = other.get(CHECK_FOR_NULL_KEY);
        if(other.containsKey(INPUT_KEY))
            input = other.get(INPUT_KEY);
        if(other.containsKey(EXTRACT_LOOKAHEAD_KEY))
            extractLookahead = other.get(EXTRACT_LOOKAHEAD_KEY)==1;
        if(other.containsKey(SOURCE_POSITION_KEY))
            sourcePosition = other.get(SOURCE_POSITION_KEY);
        
    }
    
    public int compareTo(Hit h)
    {
        int c;
        Integer thisLevel = new Integer(this.getRuleValue().getLevel());
        Integer otherLevel = new Integer(h.getRuleValue().getLevel());
        c = otherLevel.compareTo(thisLevel);
        if(c!=0)
            return c;
        
        String thisFilename = this.getFilename();
        String otherFilename = h.getFilename();
        c = thisFilename.compareTo(otherFilename);
        if(c!=0)
            return c;
        
        Integer thisLine = this.getLine();
        Integer otherLine = h.getLine();
        c = thisLine.compareTo(otherLine);
        if(c!=0)
            return c;
        
        Integer thisColumn = this.getColumn();
        Integer otherColumn = h.getColumn();
        c = thisColumn.compareTo(otherColumn);
        if(c!=0)
            return c;
        
        String thisName = this.getName();
        String otherName = h.getName();
        c = thisName.compareTo(otherName);
        return c;
    }
    
    public static Comparator<Hit> hitComparator  =new Comparator<Hit>()
    {
        @Override
        public int compare(Hit g,Hit h)
        {
            int c;
            Integer thisLevel = new Integer(g.getRuleValue().getLevel());
            Integer otherLevel = new Integer(h.getRuleValue().getLevel());
            c = otherLevel.compareTo(thisLevel);
            if(c!=0)
                return c;

            String thisFilename = g.getFilename();
            String otherFilename = h.getFilename();
            c = thisFilename.compareTo(otherFilename);
            if(c!=0)
                return c;

            Integer thisLine = g.getLine();
            Integer otherLine = h.getLine();
            c = thisLine.compareTo(otherLine);
            if(c!=0)
                return c;

            Integer thisColumn = g.getColumn();
            Integer otherColumn = h.getColumn();
            c = thisColumn.compareTo(otherColumn);
            if(c!=0)
                return c;

            String thisName = g.getName();
            String otherName = h.getName();
            c = thisName.compareTo(otherName);
            return c;
        }
    };
    
    @Override
    public String toString()
    {
        String res = "";
        
//        if(true)
//            return getRuleValue().getWarning();
        
        if(getArguments().isOutputFormat())
            res+="<li>";
        
        res+=HelperFunctions.h( getFilename(), getArguments().isOutputFormat());
        
        if(getArguments().isShowColumns())
            res+=":"+getLine()+":"+getColumn()+":";
        else
            res+=":"+getLine()+":";
        
        if(getArguments().isOutputFormat())
            res+="<b>";
        res+=" ["+getRuleValue().getLevel()+"]";
        if(getArguments().isOutputFormat())
            res+="</b>";
        
        res+="("+getRuleValue().getCategory()+")";
        
        if(getArguments().isOutputFormat())
            res+="<i>";
        res+=""+getName()+":";
        String mainText = HelperFunctions.h(getRuleValue().getWarning()+". ",getArguments().isOutputFormat());
        
        
        if(getArguments().isOutputFormat())
            mainText = "DO SOME REGEX THING";
        
        
        
        if(getArguments().isSingleLine())
        {
            res+=mainText;
            if(getRuleValue().getSuggestion()!=null)
                res+=HelperFunctions.h(getRuleValue().getSuggestion(), getArguments().isOutputFormat())+".";
            res+=HelperFunctions.h( getNote(), getArguments().isOutputFormat());            
        }
        else
        {
            if(getRuleValue().getSuggestion()!=null)
                mainText+=HelperFunctions.h(getRuleValue().getSuggestion(), getArguments().isOutputFormat())+". ";
            mainText+=HelperFunctions.h( getNote(), getArguments().isOutputFormat());
            res+="\n";
            res+=HelperFunctions.printMultiLineText(mainText);
        }
        
        
        if(getArguments().isOutputFormat())
            res+="</i>";
        res+="\n";
        if(getArguments().isShowContext())
        {
            System.exit(0);
            if(getArguments().isOutputFormat())
                res+="<pre>";
            res+=HelperFunctions.h( getContextText(), getArguments().isOutputFormat());            
            if(getArguments().isOutputFormat())
                res+="</pre>";
        }
        return res;
    }

    /**
     * @return the ruleValue
     */
    public RuleValue getRuleValue() {
        return ruleValue;
    }

    /**
     * @param ruleValue the ruleValue to set
     */
    public void setRuleValue(RuleValue ruleValue) {
        this.ruleValue = ruleValue;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the start
     */
    public int getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public int getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(int end) {
        this.end = end;
    }

    /**
     * @return the line
     */
    public int getLine() {
        return line;
    }

    /**
     * @param line the line to set
     */
    public void setLine(int line) {
        this.line = line;
    }

    /**
     * @return the column
     */
    public int getColumn() {
        return column;
    }

    /**
     * @param column the column to set
     */
    public void setColumn(int column) {
        this.column = column;
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * @return the contextText
     */
    public String getContextText() {
        return contextText;
    }

    /**
     * @param contextText the contextText to set
     */
    public void setContextText(String contextText) {
        this.contextText = contextText;
    }

    /**
     * @return the parameters
     */
    public List<String> getParameters() {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(List<String> parameters) {
        this.setParameters(parameters);
    }

    /**
     * @return the extractLookahead
     */
    public boolean isExtractLookahead() {
        return extractLookahead;
    }

    /**
     * @param extractLookahead the extractLookahead to set
     */
    public void setExtractLookahead(boolean extractLookahead) {
        this.extractLookahead = extractLookahead;
    }

    /**
     * @return the lookahead
     */
    public String getLookahead() {
        return lookahead;
    }

    /**
     * @param lookahead the lookahead to set
     */
    public void setLookahead(String lookahead) {
        this.lookahead = lookahead;
    }

    /**
     * @return the arguments
     */
    public Arguments getArguments() {
        return arguments;
    }

    /**
     * @param arguments the arguments to set
     */
    public void setArguments(Arguments arguments) {
        this.arguments = arguments;
    }

    /**
     * @return the note
     */
    public String getNote() {
        return note;
    }

    /**
     * @param note the note to set
     */
    public void setNote(String note) {
        this.note = note;
    }


    /**
     * @return the input
     */
    public int getInput() {
        return input;
    }

    /**
     * @param input the input to set
     */
    public void setInput(int input) {
        this.input = input;
    }

    /**
     * @return the formatPosition
     */
    public int getFormatPosition() {
        return formatPosition;
    }

    /**
     * @param formatPosition the formatPosition to set
     */
    public void setFormatPosition(int formatPosition) {
        this.formatPosition = formatPosition;
    }

    /**
     * @return the sourcePosition
     */
    public int getSourcePosition() {
        return sourcePosition;
    }

    /**
     * @param sourcePosition the sourcePosition to set
     */
    public void setSourcePosition(int sourcePosition) {
        this.sourcePosition = sourcePosition;
    }

    /**
     * @return the checkForNull
     */
    public int getCheckForNull() {
        return checkForNull;
    }

    /**
     * @param checkForNull the checkForNull to set
     */
    public void setCheckForNull(int checkForNull) {
        this.checkForNull = checkForNull;
    }

//    @Override
//    public int compare(Hit arg0, Hit arg1) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }

}
