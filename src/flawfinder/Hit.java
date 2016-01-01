/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package flawfinder;

import java.util.List;

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
public class Hit {
    
    private RuleValue ruleValue;
    private String name;
    private int start;
    private int end;
    private int line;
    private int column;
    private String filename;
    private String contextText;
    private List<String> parameters;
    private boolean extractLookahead;
    private String lookahead;
    private Arguments arguments;
    private String note;
    
    public static final boolean INPUT = false;
    
    public Hit(RuleValue ruleValue, String name, 
            int start, int end, 
            int line, int column, 
            String filename, String contextText,
            List<String> parameters,  String lookahead, 
            Arguments arguments, String note) {
        this.ruleValue = ruleValue;
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
    }
    
    
    
    public String toString()
    {
        String res = "";
        
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
        this.parameters = parameters;
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
}
