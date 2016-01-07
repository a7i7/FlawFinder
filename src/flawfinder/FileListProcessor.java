/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package flawfinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author afif
 */
public class FileListProcessor {
    
    private String [] fileList;
    private Map<String,Set<Integer> > patchInfo;
    private Arguments arguments;
    private RuleSet ruleSet;
    private List<Hit> hitList;
    private List<String> cExtensions;
    
    FileListProcessor(Arguments arguments,String []fileList,Map<String,Set<Integer> > patchInfo,RuleSet ruleSet)
    {
        this.fileList = fileList;
        this.patchInfo = patchInfo;
        this.arguments = arguments;
        this.ruleSet = ruleSet;
        this.hitList = new ArrayList<Hit>();
        this.cExtensions = new ArrayList<String>();
        cExtensions.add(".c");
        cExtensions.add(".h");
        cExtensions.add(".ec");
        cExtensions.add(".ecp");
        cExtensions.add(".pgc");
        cExtensions.add(".C");
        cExtensions.add(".cpp");
        cExtensions.add(".CPP");
        cExtensions.add(".cxx");
        cExtensions.add(".cc");
        cExtensions.add(".CC");
        cExtensions.add(".c++");
        cExtensions.add(".pcc");
        cExtensions.add(".hpp");
        cExtensions.add(".H");
        
    }
    
    protected boolean isLink(String fileName)
    {
        File file = new File(fileName);
        boolean isLink = Files.isSymbolicLink(file.toPath());
        return isLink;
    }
    
    protected boolean isFile(String fileName)
    {
        File f = new File(fileName);
        return f.isFile();
    }
    
    protected boolean isDirectory(String fileName)
    {
        File f = new File(fileName);
        return f.isDirectory();
    }
    
    protected boolean hasExistence(String fileName)
    {
        try
        {
            FileInputStream fin = new FileInputStream(fileName);
        }
        catch(FileNotFoundException e)
        {
            return false;
        }
        return true;
    }
    
    protected String getBaseName(String fileName)
    {
        File f = new File(fileName);
        return f.getName();
    }
    public void processFileArgs() throws IOException
    {
        for(String f:fileList)
        {
            if(!arguments.isAllowLink() && isLink(f))
            {
                if(!arguments.isQuiet())
                {
                    HelperFunctions.printWarning("Skipping symbolic link "+HelperFunctions.h(f, arguments.isOutputFormat()));
                }
                ++Statistics.NUM_LINKS_SKIPPED;
            }
            else if(isFile(f) || f.equals("-")) //DOUBT
            {
                if((patchInfo!=null && patchInfo.containsKey(f)) || patchInfo==null)  //DOUBT again. WTF is k? It is not k. It is f. Fix it in original python file
                {
                    FileProcessor fileProcessor = new FileProcessor(arguments,f,patchInfo,ruleSet);
                    fileProcessor.processCFile();
                }
            }
            else if(isDirectory(f))
            {
                maybeProcessFile(f);
            }
            else if(!hasExistence(f))
            {
                if(!arguments.isQuiet())
                {
                    String data = HelperFunctions.h(f, arguments.isOutputFormat());
                    if(data.startsWith("\342\210\222"))
                        HelperFunctions.printWarning("Skipping non-existent filename starting with UTF-8 long dash "+data);
                    else
                        HelperFunctions.printWarning("Skipping non-existent file "+data);
                }
            }
            else
            {
                String data = HelperFunctions.h(f, arguments.isOutputFormat());
                if(!arguments.isQuiet())
                    HelperFunctions.printWarning("Skipping non-regular file " + data);
            }
        }
    }

    private void maybeProcessFile(String f) throws IOException {
        
        String data = HelperFunctions.h(f, arguments.isOutputFormat());
        if(isDirectory(f))
        {
            if(!arguments.isAllowLink() && isLink(f))
            {
                if(!arguments.isQuiet())
                    HelperFunctions.printWarning("Skipping symbolic link directory " + data);
                ++Statistics.NUM_LINKS_SKIPPED;
                return;
            }
            String baseFileName = getBaseName(f);
            if(arguments.isSkipDotDir() && baseFileName.length()>1 && (baseFileName.charAt(0)=='.'))
            {
                if(!arguments.isQuiet())
                    HelperFunctions.printWarning("Skipping directory with initial dot " + data);
                ++Statistics.NUM_DOTDIRS_SKIPPED;
                return;
            }
            File directory = new File(f);
            String [] fileNames = directory.list();
            
            for(String fileName:fileNames)
                maybeProcessFile(directory.getAbsolutePath()+"/"+fileName);
        }
        int dotPosition = f.lastIndexOf('.');
        if(dotPosition>1)
        {
            String extension = f.substring(dotPosition);
            if(cExtensions.contains(extension))
            {
                if(!arguments.isAllowLink() && isLink(f))
                {
                    if(!arguments.isQuiet())
                        HelperFunctions.printWarning("Skipping symbolic link directory " + data);
                    ++Statistics.NUM_LINKS_SKIPPED;
                }
                else if(!isFile(f))
                {
                    if(!arguments.isQuiet())
                        HelperFunctions.printWarning("Skipping non-regular file " + data);
                }
                else
                {
                    if((patchInfo==null) || (patchInfo!=null && patchInfo.containsKey(f)))
                    {
                        FileProcessor fileProcessor = new FileProcessor(arguments,f,patchInfo,ruleSet);
                        fileProcessor.processCFile();
                    }
                }
            }
        }
    }

}
