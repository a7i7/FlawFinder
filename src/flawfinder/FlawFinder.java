/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package flawfinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author afif
 */
public class FlawFinder {

    public static boolean shouldProcessFiles(Arguments arguments,RuleSet ruleSet) throws FileNotFoundException, IOException, ClassNotFoundException
    {
        if(arguments.getLoadHitList()!=null)
        {
            ObjectInputStream oin = new ObjectInputStream(new FileInputStream(arguments.getLoadHitList()));
            List<Hit> hitList = (List<Hit>) oin.readObject();
            return false;
        }
        else
        {
            Map<String,Set<Integer> > patchInformation = null;
            if(arguments.getPatchFile()!=null)
            {
                PatchInformationLoader patchInfoLoader = new PatchInformationLoader(arguments.getPatchFile());
                patchInformation = patchInfoLoader.startLoadingPatchInfo();
            }
            String [] fileList = arguments.getFileList();
            if(fileList==null || fileList.length==0)
            {
                System.out.println("*** No input files");
                return false;
            }
            FileListProcessor fileListProcessor = new FileListProcessor(arguments,fileList,patchInformation,ruleSet);
            fileListProcessor.processFileArgs();
            return true;
        }
        
    }
    
    public static void showFinalResults(Arguments arguments) throws FileNotFoundException, IOException, ClassNotFoundException
    {
        List<Hit> hitList = FileProcessor.hitList;
        int count = 0;
        Map<Integer,Integer> countPerLevel = new HashMap<Integer,Integer>();
        Map<Integer,Integer> countPerLevelAndUp = new HashMap<Integer,Integer>();
        
        for(int i=0;i<6;i++)
            countPerLevel.put(i, 0);
        
        for(int i=0;i<6;i++)
            countPerLevelAndUp.put(i, 0);
        
        if(arguments.isShowImmediately() || !arguments.isQuiet())
        {
            System.out.println("");
            if(arguments.isShowHeading())
            {
                if(arguments.isOutputFormat())
                    System.out.println("<h2>Final Results</h2>");
                else
                    System.out.println("FINAL RESULTS\n");
            }
        }
        Collections.sort(hitList,Hit.hitComparator);
        if(arguments.getDiffHitList()!=null)
        {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(arguments.getDiffHitList()));
            List<Hit> diffHitList = (List<Hit>) ois.readObject();
            if(arguments.isOutputFormat())
                System.out.println("<ul>");
            for(Hit h:hitList)
            {
                if(!diffHitList.contains(h))
                {
                    System.out.println(h);
                    int level = h.getRuleValue().getLevel();
                    int tmp = countPerLevel.get(level);
                    countPerLevel.put(level,tmp+1);
                    ++count;
                }
            }
            if(arguments.isOutputFormat())
                System.out.println("</ul>");
            ois.close();
        }
        else
        {
            if(arguments.isOutputFormat())
                System.out.println("<ul>");
            for(Hit h:hitList)
            {
                System.out.println(h);
                int level = h.getRuleValue().getLevel();
                int tmp = countPerLevel.get(level);
                countPerLevel.put(level,tmp+1);
            }
            if(arguments.isOutputFormat())
                System.out.println("</ul>");
            count = hitList.size();
        }
        
        if(arguments.isShowHeading())
        {
            if(arguments.isOutputFormat())
                System.out.println("<h2>Analysis Summary<h2>");
            else
            {
                System.out.println("\nANALYSIS SUMMARY");
            }
            
            if(arguments.isOutputFormat())
                System.out.println("<p>");
            else
                System.out.println("");
            
            if(count>0)
                System.out.println("Hits = "+count);
            else
                System.out.println("No hits found");
            
            if(arguments.isOutputFormat())
                System.out.println("<br>");
            long timeAnalyzing = new Date().getTime()-Statistics.startTime.getTime();
            
            if(arguments.getRequiredRegex()!=null)
                System.out.println("Hits limited to regular expression "+arguments.getRequiredRegex());
            System.out.println("Lines Analyzed "+Statistics.SUM_LINES);
            if(timeAnalyzing>0 && !arguments.isOmitTime())
            {
                float linesPerSecond = (float)Statistics.SUM_LINES/timeAnalyzing;
                System.out.println("In approximately "+timeAnalyzing+" ms "+linesPerSecond+" lines/second");
            }
            else
                System.out.println("");
            
            if(arguments.isOutputFormat())
                System.out.println("<br>");
            System.out.println("Physical Source Lines Of Code (SLOC)"+Statistics.SLOC);
            if(arguments.isOutputFormat())
                System.out.println("<br>");
            
            System.out.print("Hits@level =");
            
            for(int i=0;i<6;i++)
                System.out.printf("[%d] %3d",i,countPerLevel.get(i));
            
            if(arguments.isOutputFormat())
                System.out.println("<br>");
            else
                System.out.println("");
            
            System.out.print("Hits@level+ =");
            for(int i=0;i<6;i++)
                for(int j=i;j<6;j++)
                {
                    int x = countPerLevelAndUp.get(i);
                    int y = countPerLevel.get(j);
                    countPerLevelAndUp.put(i,x+y);
                }
           for(int i=0;i<6;i++)
               System.out.printf("[%d+] %3d",i,countPerLevelAndUp.get(i));
           if(arguments.isOutputFormat())
               System.out.println("<br>");
           else
               System.out.println("");
           
           if(Statistics.SLOC>0)
           {
               System.out.print("Hits/KSLOC@level+ =");
               for(int i=0;i<6;i++)
                   System.out.printf("[%d+] %3g",i,countPerLevelAndUp.get(i)*1000.0f/Statistics.SLOC);
           }
           
           if(arguments.isOutputFormat())
               System.out.println("<br>");
           else
               System.out.println("");
           
           if(Statistics.NUM_LINKS_SKIPPED>0)
           {
               System.out.println("Symlinks skipped ="+Statistics.NUM_LINKS_SKIPPED+" (--allowlink overrides but see doc for security purposes");
               if(arguments.isOutputFormat())
                   System.out.println("<br>");
           }
           
           if(Statistics.NUM_DOTDIRS_SKIPPED>0)
           {
               System.out.println("Dot directories skipped ="+Statistics.NUM_DOTDIRS_SKIPPED+" (--followdotdir overrides)");
               if(arguments.isOutputFormat())
                   System.out.println("<br>");
           }
           
           if(Statistics.NUM_IGNORED_HITS>0)
           {
               System.out.println("Suppressed hits ="+Statistics.NUM_IGNORED_HITS+" (use --neverignore to show them)");
               if(arguments.isOutputFormat())
                   System.out.println("<br>");
           }
           
           System.out.println("Minimum risk level "+arguments.getMinimumLevel());
           
           if(arguments.isOutputFormat())
               System.out.println("");
           
           if(count>0)
           {
               System.out.println("Not every hit is necessarily a security vulnerability.");
               if(arguments.isOutputFormat())
                   System.out.println("<br>");
           }
           
           System.out.println("There may be other security vulnerabilities; review your code!");
           
           if(arguments.isOutputFormat())
           {
               System.out.println("<br>");
               System.out.println("See '<a href=\"http://www.dwheeler.com/secure-programs\">Secure Programming for Linux and Unix HOWTO</a>'");
               System.out.println("(<a href=\"http://www.dwheeler.com/secure-programs\">http://www.dwheeler.com/secure-programs</a>) for more information.");
           }
           else
           {
               System.out.println("See 'Secure Programming for Linux and Unix HOWTO'");
               System.out.println("(http://www.dwheeler.com/secure-programs) for more information.");
           }
           
           if(arguments.isOutputFormat())
           {
               System.out.println("</body>");
               System.out.println("</html>");
           }
           
           
        }
         
    }
    
    public static void saveIfDesired(Arguments arguments) throws FileNotFoundException, IOException
    {
        String saveHitList = arguments.getSaveHitList();
        if(saveHitList!=null)
        {
            System.out.println("Saving Hitlist to "+saveHitList);
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveHitList));
            oos.writeObject(FileProcessor.hitList);
            oos.close();
        }
    }
    
    public static Arguments commandLineParser(String args [])
    {
        Arguments arguments = new Arguments(args);
        return arguments;
    }
    
    public static void main(String[] args) throws IOException, URISyntaxException, FileNotFoundException, ClassNotFoundException {
        
        
        Arguments arguments = commandLineParser(args);
        arguments.displayHeader();
        RuleSet r = HelperFunctions.readRuleSet("rules.txt");
        r.expandRuleSet();
        Statistics.reset();
        if(shouldProcessFiles(arguments,r))
        {
            showFinalResults(arguments);
            saveIfDesired(arguments);
        }
        return;
    }   
         
    
}
