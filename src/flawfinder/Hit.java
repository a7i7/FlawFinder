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
    
    public String toString()
    {
        String res = "";
        
        if(arguments.isOutputFormat())
            res+="<li>";
        
        res+=HelperFunctions.h(filename, arguments.isOutputFormat());
        
        if(arguments.isShowColumns())
            res+=":"+line+":"+column+":";
        else
            res+=":"+line+":";
        
        if(arguments.isOutputFormat())
            res+="<b>";
        res+=" ["+ruleValue.getLevel()+"]";
        if(arguments.isOutputFormat())
            res+="</b>";
        
        res+="("+ruleValue.getCategory()+")";
        
        if(arguments.isOutputFormat())
            res+="<i>";
        
        String mainText = HelperFunctions.h(ruleValue.getWarning()+". ",arguments.isOutputFormat());
        
        if(arguments.isOutputFormat())
            mainText = "DO SOME REGEX THING";
        
        
        
        if(arguments.isSingleLine())
        {
            res+=mainText;
            if(ruleValue.getSuggestion()!=null)
                res+=HelperFunctions.h(ruleValue.getSuggestion(), arguments.isOutputFormat())+".";
            res+=HelperFunctions.h(note, arguments.isOutputFormat());            
        }
        else
        {
            if(ruleValue.getSuggestion()!=null)
                mainText+=HelperFunctions.h(ruleValue.getSuggestion(), arguments.isOutputFormat())+". ";
            mainText+=HelperFunctions.h(note, arguments.isOutputFormat());
            res+="\n";
            res+=HelperFunctions.printMultiLineText(mainText);          
        }
        
        if(arguments.isOutputFormat())
            res+="</i>";
        res+="\n";
        if(arguments.isShowContext())
        {
            if(arguments.isOutputFormat())
                res+="<pre>";
            res+=HelperFunctions.h(contextText, arguments.isOutputFormat());            
            if(arguments.isOutputFormat())
                res+="</pre>";
        }
        return res;
    }
}
