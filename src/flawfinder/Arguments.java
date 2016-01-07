/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package flawfinder;

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.Option.BooleanOption;
import jargs.gnu.CmdLineParser.Option.StringOption;
import jargs.gnu.CmdLineParser.UnknownOptionException;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author afif
 */
public class Arguments implements Serializable{
    
    private boolean showContext;
    private boolean showInputs;
    private boolean allowLink;
    private boolean skipDotDir;
    private boolean omitTime;
    
    private boolean outputFormat;
    private int minimumLevel;
    private boolean showImmediately;
    private boolean singleLine;
    
    private boolean falsePositive;
    
    private Pattern requiredRegex;
    private Matcher requiredRegexCompiled;
    
    private boolean showColumns;
    
    private boolean neverIgnore;
    private boolean quiet;
    private boolean showHeading;
    private boolean listRules;
    
    private String loadHitList; //probably String
    private String saveHitList; //probably ,,
    private String diffHitList; //probably ,,
    private String patchFile; //probably ,,
    private String[] fileList;
    
    private boolean displayedHeader = false;
    private static final boolean  EXTRACT_LOOKAHEAD = false;
    private static final int MAX_LOOKAHEAD = 500;
    
    Arguments(String[] args) {
        this();
        CmdLineParser parser = new CmdLineParser();
        
        BooleanOption context = (BooleanOption) parser.addBooleanOption('c',"context");
        BooleanOption columns = (BooleanOption) parser.addBooleanOption('C',"columns");
        BooleanOption quiet = (BooleanOption) parser.addBooleanOption('Q',"quiet");
        BooleanOption dataonly = (BooleanOption) parser.addBooleanOption('D',"dataonly");
        BooleanOption input = (BooleanOption) parser.addBooleanOption('I',"input");
        BooleanOption inputs = (BooleanOption) parser.addBooleanOption("inputs");
        BooleanOption falsePositive = (BooleanOption) parser.addBooleanOption('F',"falsepositive");
        BooleanOption falsePositives = (BooleanOption) parser.addBooleanOption("falsepositives");
        BooleanOption noLink = (BooleanOption) parser.addBooleanOption("nolink");
        BooleanOption omitTime = (BooleanOption) parser.addBooleanOption("omittime");
        BooleanOption allowLink = (BooleanOption) parser.addBooleanOption("allowlink");
        BooleanOption followDotDir = (BooleanOption) parser.addBooleanOption("followdotdir");
        BooleanOption listRules = (BooleanOption) parser.addBooleanOption("listrules");
        BooleanOption html = (BooleanOption) parser.addBooleanOption('H',"html");
        StringOption minLevel = (StringOption) parser.addStringOption('m',"minlevel");
        BooleanOption singleLine = (BooleanOption) parser.addBooleanOption('S',"singleline");
        BooleanOption immediate = (BooleanOption) parser.addBooleanOption('i',"immediate");
        BooleanOption neverIgnore = (BooleanOption) parser.addBooleanOption('n',"neverignore");
        StringOption regex = (StringOption) parser.addStringOption('e',"regex");
        StringOption patch = (StringOption) parser.addStringOption('P', "patch");
        StringOption loadHitList = (StringOption) parser.addStringOption("loadhitlist");
        StringOption saveHitList = (StringOption) parser.addStringOption("savehitlist");
        StringOption diffHitList = (StringOption) parser.addStringOption("diffhitlist");
        BooleanOption version = (BooleanOption) parser.addBooleanOption("version");
        BooleanOption help = (BooleanOption) parser.addBooleanOption('h',"help");
        try
        {
            parser.parse(args);
        }
        catch(IllegalOptionValueException e)
        {
            System.out.println("ILLEGAL");
//            System.out.println(HelperFunctions.usage());
            System.exit(0);
        }
        catch(UnknownOptionException e)
        {
            System.out.println(e);
            System.exit(0);
        }
        
        String [] otherArgs = parser.getRemainingArgs();
        
        this.showContext = parser.getOptionValue(context)!=null;
        this.showColumns = parser.getOptionValue(columns)!=null;
        this.quiet = parser.getOptionValue(quiet)!=null;
        this.showHeading = parser.getOptionValue(dataonly)==null;
        if(parser.getOptionValue(input)!=null || parser.getOptionValue(inputs)!=null)
        {
            this.showInputs = true;
            this.minimumLevel = 0;
        }
        this.falsePositive = parser.getOptionValue(falsePositive)!=null;
        this.falsePositive= this.falsePositive || parser.getOptionValue(falsePositives)!=null;
        this.allowLink = parser.getOptionValue(noLink)==null;
        this.omitTime = parser.getOptionValue(omitTime)!=null;
        this.allowLink = parser.getOptionValue(allowLink)!=null;
        this.skipDotDir = parser.getOptionValue(followDotDir)==null;
        this.listRules = parser.getOptionValue(listRules)!=null;
        
        if(parser.getOptionValue(html)!=null)
        {
            this.outputFormat = true;
            this.singleLine = false;
        }
        
        Object data = parser.getOptionValue(minLevel);
        if(data!=null)
        {
            this.minimumLevel = Integer.parseInt(data.toString());
            System.out.println(minimumLevel);
        }
        
        this.singleLine = parser.getOptionValue(singleLine)!=null;
        this.showImmediately = parser.getOptionValue(immediate)!=null;
        this.neverIgnore = parser.getOptionValue(neverIgnore)!=null;
        
        String regexData = (String) parser.getOptionValue(regex);
        if(regexData!=null)
        {
            this.requiredRegex = Pattern.compile(regexData);
            System.out.println(regexData);
        }

        String patchFile = (String) parser.getOptionValue(patch);
        if(patchFile!=null)
        {
            this.patchFile = patchFile;
            this.neverIgnore = true;
        }
        
        String loadHitListFile = (String) parser.getOptionValue(loadHitList);
        if(loadHitListFile!=null)
        {
            this.loadHitList = loadHitListFile;
            displayHeader();
            if(this.showHeading)
                System.out.println("Loading hits from "+this.loadHitList);
        }
        
        String saveHitListFile = (String) parser.getOptionValue(saveHitList);
        if(saveHitListFile!=null)
        {
            this.saveHitList = saveHitListFile;
            displayHeader();
            if(this.showHeading)
                System.out.println("Saving hitlist to "+this.saveHitList);
        }
        
        String diffHitListFile = (String) parser.getOptionValue(diffHitList);
        if(diffHitListFile!=null)
        {
            this.diffHitList = diffHitListFile;
            displayHeader();
            if(this.showHeading)
                System.out.println("Showing hits not in "+this.diffHitList);
        }
        
        if(parser.getOptionValue(version)!=null)
        {
            System.out.println(HelperFunctions.version);
            System.exit(0);
        }
        
        if(parser.getOptionValue(help)!=null)
        {
            usage();
            System.exit(0);
        }
        this.fileList = parser.getRemainingArgs();

            
    }
    
    public Arguments()
    {
        this.showContext = false;
        this.minimumLevel = 1;
        this.showImmediately = false;
        this.showInputs = false;
        this.falsePositive = false;
        this.allowLink = false;
        this.skipDotDir = true;
        
        this.showColumns = false;
        this.neverIgnore = false;
        this.listRules = false;
        this.patchFile = null;
        this.loadHitList = null;
        this.saveHitList = null;
        this.diffHitList = null;
        this.quiet = false;
        this.showHeading = true;
        this.outputFormat = false;
        this.singleLine = false;
        this.omitTime = false;
        this.requiredRegex = null;
        this.requiredRegexCompiled  = null;
        
        
    }

    
    /**
     * 
     * @return  MAX_LOOKAHEAD
     */
    public int getMaxLookahead()
    {
        return MAX_LOOKAHEAD;
    }
    
    /**
     * Be sure to delete this
     * @return the EXTRACT_LOOKAHEAD
     */
    private boolean isExtractLookaheadEnabled()
    {
        return EXTRACT_LOOKAHEAD;
    }
    /**
     * @return the showContext
     */
    public boolean isShowContext() {
        return showContext;
    }

    /**
     * @param showContext the showContext to set
     */
    public void setShowContext(boolean showContext) {
        this.showContext = showContext;
    }

    /**
     * @return the showInputs
     */
    public boolean isShowInputs() {
        return showInputs;
    }

    /**
     * @param showInputs the showInputs to set
     */
    public void setShowInputs(boolean showInputs) {
        this.showInputs = showInputs;
    }

    /**
     * @return the allowLink
     */
    public boolean isAllowLink() {
        return allowLink;
    }

    /**
     * @param allowLink the allowLink to set
     */
    public void setAllowLink(boolean allowLink) {
        this.allowLink = allowLink;
    }

    /**
     * @return the skipDotDir
     */
    public boolean isSkipDotDir() {
        return skipDotDir;
    }

    /**
     * @param skipDotDir the skipDotDir to set
     */
    public void setSkipDotDir(boolean skipDotDir) {
        this.skipDotDir = skipDotDir;
    }

    /**
     * @return the omitTime
     */
    public boolean isOmitTime() {
        return omitTime;
    }

    /**
     * @param omitTime the omitTime to set
     */
    public void setOmitTime(boolean omitTime) {
        this.omitTime = omitTime;
    }

    /**
     * @return the outputFormat
     */
    public boolean isOutputFormat() {
        return outputFormat;
    }

    /**
     * @param outputFormat the outputFormat to set
     */
    public void setOutputFormat(boolean outputFormat) {
        this.outputFormat = outputFormat;
    }

    /**
     * @return the minimumLevel
     */
    public int getMinimumLevel() {
        return minimumLevel;
    }

    /**
     * @param minimumLevel the minimumLevel to set
     */
    public void setMinimumLevel(int minimumLevel) {
        this.minimumLevel = minimumLevel;
    }

    /**
     * @return the showImmediately
     */
    public boolean isShowImmediately() {
        return showImmediately;
    }

    /**
     * @param showImmediately the showImmediately to set
     */
    public void setShowImmediately(boolean showImmediately) {
        this.showImmediately = showImmediately;
    }

    /**
     * @return the singleLine
     */
    public boolean isSingleLine() {
        return singleLine;
    }

    /**
     * @param singleLine the singleLine to set
     */
    public void setSingleLine(boolean singleLine) {
        this.singleLine = singleLine;
    }


    /**
     * @param requiredRegex the requiredRegex to set
     */
    public void setRequiredRegex(boolean requiredRegex) {
        this.setRequiredRegex(requiredRegex);
    }

    /**
     * @return the falsePositive
     */
    public boolean isFalsePositive() {
        return falsePositive;
    }

    /**
     * @param falsePositive the falsePositive to set
     */
    public void setFalsePositive(boolean falsePositive) {
        this.falsePositive = falsePositive;
    }

    /**
     * @return the showColumns
     */
    public boolean isShowColumns() {
        return showColumns;
    }

    /**
     * @param showColumns the showColumns to set
     */
    public void setShowColumns(boolean showColumns) {
        this.showColumns = showColumns;
    }

    /**
     * @return the neverIgnore
     */
    public boolean isNeverIgnore() {
        return neverIgnore;
    }

    /**
     * @param neverIgnore the neverIgnore to set
     */
    public void setNeverIgnore(boolean neverIgnore) {
        this.neverIgnore = neverIgnore;
    }

    /**
     * @return the quiet
     */
    public boolean isQuiet() {
        return quiet;
    }

    /**
     * @param quiet the quiet to set
     */
    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    /**
     * @return the showHeading
     */
    public boolean isShowHeading() {
        return showHeading;
    }

    /**
     * @param showHeading the showHeading to set
     */
    public void setShowHeading(boolean showHeading) {
        this.showHeading = showHeading;
    }

    /**
     * @return the listRules
     */
    public boolean isListRules() {
        return listRules;
    }

    /**
     * @param listRules the listRules to set
     */
    public void setListRules(boolean listRules) {
        this.listRules = listRules;
    }

    /**
     * @return the loadHitList
     */
    public String getLoadHitList() {
        return loadHitList;
    }

    /**
     * @param loadHitList the loadHitList to set
     */
    public void setLoadHitList(String loadHitList) {
        this.loadHitList = loadHitList;
    }

    /**
     * @return the saveHitList
     */
    public String getSaveHitList() {
        return saveHitList;
    }

    /**
     * @param saveHitList the saveHitList to set
     */
    public void setSaveHitList(String saveHitList) {
        this.saveHitList = saveHitList;
    }

    /**
     * @return the diffHitList
     */
    public String getDiffHitList() {
        return diffHitList;
    }

    /**
     * @param diffHitList the diffHitList to set
     */
    public void setDiffHitList(String diffHitList) {
        this.diffHitList = diffHitList;
    }

    /**
     * @return the patchFile
     */
    public String getPatchFile() {
        return patchFile;
    }

    /**
     * @param patchFile the patchFile to set
     */
    public void setPatchFile(String patchFile) {
        this.patchFile = patchFile;
    }

    /**
     * @return the requiredRegex
     */
    public Pattern getRequiredRegex() {
        return requiredRegex;
    }

    /**
     * @param requiredRegex the requiredRegex to set
     */
    public void setRequiredRegex(Pattern requiredRegex) {
        this.requiredRegex = requiredRegex;
    }

    /**
     * @return the requiredRegexCompiled
     */
    public Matcher getRequiredRegexCompiled() {
        return requiredRegexCompiled;
    }

    /**
     * @param requiredRegexCompiled the requiredRegexCompiled to set
     */
    public void setRequiredRegexCompiled(Matcher requiredRegexCompiled) {
        this.requiredRegexCompiled = requiredRegexCompiled;
    }

    public void displayHeader() {
        if(!this.showHeading)
            return;
        if(!this.displayedHeader)
        {
            if(this.isOutputFormat())
            {
                System.out.println ("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" " +
            "\"http://www.w3.org/TR/html4/loose.dtd\">");
              System.out.println("<html>");
              System.out.println("<head>");
              System.out.println("<meta http-equiv=\"Content-type\" content=\"text/html; charset=utf8\">");
              System.out.println("<title>Flawfinder Results</title>");
              System.out.println("<meta name=\"author\" content=\"David A. Wheeler\">");
              System.out.println("<meta name=\"keywords\" lang=\"en\" content=\"flawfinder results, security scan\">");
              System.out.println("</head>");
              System.out.println("<body>");
              System.out.println("<h1>Flawfinder Results</h1>");
              System.out.println("Here are the security scan results from");
              System.out.println("<a href=\"http://www.dwheeler.com/flawfinder\">Flawfinder version %s</a>,"+HelperFunctions.version);
              System.out.println("(C) 2001-2014 <a href=\"http://www.dwheeler.com\">David A. Wheeler</a>.");
            }
            else
                System.out.println("Flawfinder version %s, (C) 2001-2014 David A. Wheeler." +HelperFunctions.version);
        }
        this.displayedHeader = true;
            
    }

    private void usage() {
        final String USAGE_STRING = "flawfinder [--help | -h] [--version] [--listrules]\n" +
  "[--allowlink] [--followdotdir] [--nolink]\n" +
           "[--patch filename | -P filename]\n"+
  "[--inputs | -I] [--minlevel X | -m X]\n"+
           "[--falsepositive | -F] [--neverignore | -n]\n"+
  "[--context | -c] [--columns | -C] [--dataonly | -D]\n"+
           "[--html | -H] [--immediate | -i] [--singleline | -S]\n"+
           "[--omittime] [--quiet | -Q]\n"+
  "[--loadhitlist F] [--savehitlist F] [--diffhitlist F]\n"+
  "[--] [source code file or source root directory]"+ "\n"+

  "The options cover various aspects of flawfinder as follows."+"\n"+

  "Documentation:"+"\n"+
  "--help | -h Show this usage help."+"\n"+
  "--version   Show version number."+"\n"+
  "--listrules List the rules in the ruleset (rule database)."+"\n"+

  "Selecting Input Data:"+"\n"+
  "--allowlink Allow symbolic links."+"\n"+
  "--followdotdir"+"\n"+
              "Follow directories whose names begin with \".\".\n"+
              "Normally they are ignored.\n"+
  "--nolink    Skip symbolic links (ignored).\n"+
  "--patch F | -P F"+"\n"+
              "Display information related to the patch F"+"\n"+
              "(patch must be already applied)."+"\n"+

  "Selecting Hits to Display:"+"\n"+
  "--inputs | -I"+"\n"+
              "Show only functions that obtain data from outside the program;"+"\n"+
              "this also sets minlevel to 0."+"\n"+
  "-m X | --minlevel=X"+"\n"+
              "Set minimum risk level to X for inclusion in hitlist.  This"+"\n"+
              "can be from 0 (``no risk'')  to  5  (``maximum  risk'');  the"+"\n"+
              "default is 1."+"\n"+
  "--falsepositive | -F"+"\n"+
              "Do not include hits that are likely to be false  positives."+"\n"+
              "Currently,  this  means  that function names are ignored if"+"\n"+
              "they're not followed by \"(\", and that declarations of char-"+"\n"+
              "acter  arrays  aren't noted.  Thus, if you have use a vari-"+"\n"+
              "able named \"access\" everywhere, this will eliminate  refer-"+"\n"+
              "ences  to  this ordinary variable.  This isn't the default,"+"\n"+
              "because this  also  increases  the  likelihood  of  missing"+"\n"+
              "important  hits;  in  particular, function names in #define"+"\n"+
              "clauses and calls through function pointers will be missed."+"\n"+
  "--neverignore | -n"+"\n"+
              "Never ignore security issues, even if they have an ``ignore''"+"\n"+
              "directive in a comment."+"\n"+
  "--regex PATTERN | -e PATTERN"+"\n"+
              "Only report hits that match the regular expression PATTERN."+"\n"+
  "Selecting Output Format:"+"\n"+
  "--columns | -C"+"\n"+
              "Show  the  column  number  (as well as the file name and"+"\n"+
              "line number) of each hit; this is shown after the line number"+"\n"+
              "by adding a colon and the column number in the line (the first"+"\n"+
              "character in a line is column number 1)."+"\n"+
  "--context | -c"+"\n"+
              "Show context (the line having the \"hit\"/potential flaw)"+"\n"+
  "--dataonly | -D"+"\n"+
              "Don't display the headers and footers of the analysis;"+"\n"+
              "use this along with --quiet to get just the results."+"\n"+
  "--html | -H"+"\n"+
              "Display as HTML output."+"\n"+
  "--immediate | -i"+"\n"+
              "Immediately display hits (don't just wait until the end)."+"\n"+
  "--singleline | -S"+"\n"+
              "Single-line output."+"\n"+
  "--omittime  Omit time to run."+"\n"+
  "--quiet | -Q"+"\n"+
              "Don't display status information (i.e., which files are being"+"\n"+
              "examined) while the analysis is going on."+"\n"+

  "Hitlist Management:"+"\n"+
  "--savehitlist=F"+"\n"+
              "Save all hits (the \"hitlist\") to F."+"\n"+
  "--loadhitlist=F"+"\n"+
             " Load hits from F instead of analyzing source programs."+"\n"+
  "--diffhitlist=F"+"\n"+
              "Show only hits (loaded or analyzed) not in F."+"\n"+


  "For more information, please consult the manpage or available"+"\n"+
  "documentation.";
        System.out.println(USAGE_STRING);
        return;
    }

    /**
     * @return the fileList
     */
    public String[] getFileList() {
        return fileList;
    }

    /**
     * @param fileList the fileList to set
     */
    public void setFileList(String[] fileList) {
        this.fileList = fileList;
    }

    
    
}
