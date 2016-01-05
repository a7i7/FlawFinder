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
import java.util.List;
import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.Option;
import jargs.gnu.CmdLineParser.Option.BooleanOption;
import jargs.gnu.CmdLineParser.Option.StringOption;

/**
 *
 * @author afif
 */
public class FlawFinder {

    /**
     * @param args the command line arguments
     */
    
    public static RuleSet ruletest()
    {
     return null;
    }
    
    public static void addRulesAndInitialize()
    {
        //add rules;
        return;
    }
    
    public static void loadPatchInfoTest() throws IOException
    {
        PatchInformationLoader pr = new PatchInformationLoader("/home/afif/NetBeansProjects/FlawFinder/src/flawfinder/advancedpatch.txt");
        pr.startLoadingPatchInfo();
    }
    
    public static void fileProcessorTest() throws IOException
    {
        Arguments arguments = new Arguments();
        String fileName = "/home/afif/flawfinder/flawfinder-1.31/sloctest.c";
        RuleSet r = HelperFunctions.readRuleSet("/home/afif/flawfinder/flawfinder-1.31/rules.txt");
        r.expandRuleSet();
        BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        String text="",line;
        while((line=input.readLine())!=null)
        {
            text = text+line+"\n";
        }
        FileProcessor fileProcessor = new FileProcessor(arguments,fileName,null,r);
        fileProcessor.processCFile();
    }
    
    public static void cExtractParametersTest() throws FileNotFoundException, IOException
    {   //works perfectly when tested agains python code
        String fileName = "/home/afif/flawfinder/flawfinder-1.31/test.c";
        String text = "";
        String line;
        BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        while((line=input.readLine())!=null)
        {
            text = text+line+"\n";
        }
        System.out.println(text.length());
        int endpos = 0;
        List<String> param = HelperFunctions.extractCParameters(text, endpos,false);
        for(String x:param)
            System.out.println(x.length());
        return;
    }
    
    public static Arguments commandLineParser(String args [])
    {
        Arguments arguments = new Arguments(args);
        System.exit(0);
        return arguments;
    }
    public static void main(String[] args) throws IOException {
//            cExtractParametersTest();
//        fileProcessorTest();
           commandLineParser(args);
//         CmdLineParser parser = new CmdLineParser();
//         BooleanOption debug = (BooleanOption) parser.addBooleanOption('d', "debug");
//         try
//         {
//             parser.parse(args);
//         }
//         catch(CmdLineParser.OptionException e)
//         {
//             System.out.println("Mayhem!");
//             System.exit(0);
//         }
//         
//         System.out.println(parser.getOptionValue(debug));
         
         
    }
}
