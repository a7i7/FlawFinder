/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package flawfinder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author afif
 */
public class PatchInformationLoader {
    
    private String patchFileName;
    
    private Pattern diffIndexFileName = Pattern.compile("^Index:\\s+(?<filename>.*)");
    private Pattern diffGitFileName = Pattern.compile("^diff --git a/.* b/(?<filename>.*)$");
    private Pattern diffNewFile = Pattern.compile("^\\+\\+\\+\\s(?<filename>.*)$");
    private Pattern diffHunk = Pattern.compile("^@@ -\\d+(,\\d+)?\\s+\\+(?<linenumber>\\d+)[, ].*@@$");
    private Pattern diffLineAdded = Pattern.compile("^\\+[^+].*");
    private Pattern diffLineDel = Pattern.compile("^-[^-].*");
    private Pattern diffFindJunk = Pattern.compile("^(?<filename>.*)((\\s\\d\\d\\d\\d+-\\d\\d-\\d\\d\\s+\\d\\d:\\d[0-9:.]+Z?(\\s+[\\-\\+0-9A-Z]+)?)|(\\s[A-Za-z][a-z]+\\s[A-za-z][a-z]+\\s\\d+\\s\\d+:\\d[0-9:.]+Z?(\\s[\\-\\+0-9]*)?\\s\\d\\d\\d\\d+)|(\\s\\(.*\\)))\\s*$");
    
    private Map<String,Set<Integer> > patch;
    
    private static String gitSplitter = "b/";
    private static final String SVN_PATCH = "SVN_PATCH";
    private static final String GIT_PATCH = "GIT_PATCH";
    private static final String GNU_PATCH = "GNU_PATCH";
    
    private boolean isSvnDiff(String line)
    {
        return line.indexOf("Index:")!=-1;
    }
    
    private boolean isGnuDiff(String line)
    {
        return line.startsWith("--- ");
    }
    
    private boolean isGitDiff(String line)
    {
        return line.startsWith("diff --git a");
    }
    
    private Matcher svnDiffGetFileName(String line)
    {
        return diffIndexFileName.matcher(line);
    }
    
    private Matcher gnuDiffGetFileName(String line)
    {
        Matcher newFileMatch = diffNewFile.matcher(line);
        if(newFileMatch.lookingAt())
        {
            String patchedFileName = newFileMatch.group("filename");
            patchedFileName = patchedFileName.trim();
            return diffFindJunk.matcher(patchedFileName);
        }
        return null;
    }
    
    private Matcher gitDiffGetFileName(String line)
    {
        return diffGitFileName.matcher(line);
    }
    public PatchInformationLoader(String patchFileName)
    {
        this.patchFileName = patchFileName;
        patch = new HashMap<String,Set<Integer> >();
    }
    
    public Map<String,Set<Integer> > startLoadingPatchInfo() throws IOException
    {
        int lineCounter = 0;
        int initialNumber = 0;
        String newPatchedFileName;
        String line ; 
        String patchFormat = "";
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(patchFileName));
        } catch (FileNotFoundException ex) {
            System.out.println("Error: failed to open "+HelperFunctions.h(patchFileName,false));
            System.exit(1);
        }
        
        newPatchedFileName = "";
        line = br.readLine();
        
        if(isSvnDiff(line))
            patchFormat = SVN_PATCH;
        else if(isGitDiff(line))
            patchFormat = GIT_PATCH;
        else if(isGnuDiff(line))
            patchFormat = GNU_PATCH;
        else
        {
            System.out.println("Unrecognized patch format");
            System.exit(1);
        }
        
        
        while(true)
        {
            line = line+((char)10);
            Matcher fileNameMatch = null;
            if(patchFormat.equals(SVN_PATCH))
                fileNameMatch = svnDiffGetFileName(line);
            else if(patchFormat.equals(GIT_PATCH))
                fileNameMatch = gitDiffGetFileName(line);
            else if(patchFormat.equals(GNU_PATCH))
                fileNameMatch = gnuDiffGetFileName(line);
            else
            {
                System.out.println("Should not happen");
                System.exit(1);
            }
            
            if(fileNameMatch.lookingAt())
            {
                newPatchedFileName = fileNameMatch.group("filename").trim();
                if(patch.containsKey(newPatchedFileName))
                {
                    System.out.println("filename occurs more than once in the patch: "+newPatchedFileName);
                    System.exit(1);
                }
                else
                    patch.put(newPatchedFileName,new HashSet<Integer>());
            }
            else
            {
                Matcher hunkMatch = diffHunk.matcher(line);
                if(hunkMatch.lookingAt())
                {
                    if(newPatchedFileName.equals(""))
                    {
                        System.out.println("wrong type of patch file : we have a line number without having seen a filename");
                        System.exit(1);
                    }
                    initialNumber = Integer.parseInt(hunkMatch.group("linenumber"));
                    lineCounter = 0;
                }
                else
                {
                    Matcher lineAddedMatch = diffLineAdded.matcher(line);
                    if(lineAddedMatch.lookingAt())
                    {
                        int lineAdded = lineCounter + initialNumber;
                        patch.get(newPatchedFileName).add(lineAdded);
                        patch.get(newPatchedFileName).add(lineAdded-1);
                        patch.get(newPatchedFileName).add(lineAdded+1);
                        ++lineCounter;
                    }
                    else
                    {
                        Matcher lineDelMatch = diffLineDel.matcher(line);
                        if(!lineDelMatch.lookingAt())
                            ++lineCounter;
                    }
                }
            }
            line = br.readLine();
            if(line==null)   //chck this well
                break;
        }
        System.out.println(patch);
        return patch;
    }
}
