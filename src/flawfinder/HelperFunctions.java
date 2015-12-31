/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package flawfinder;

/**
 *
 * @author afif
 */
public class HelperFunctions {
    
    private static String version = "1.31";
    
    public static void displayHeader(boolean outputFormat)
    {
        if(outputFormat)
        {
            System.out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" " +
            "\"http://www.w3.org/TR/html4/loose.dtd\">");
            System.out.println("<html>");
            System.out.println("<head>");
            System.out.println("<meta http-equiv=\"Content-type\" content=\"text/html; charset=utf8\">");
            //write other stuff
        }
        else
            System.out.println("Flawfinder version "+version+", (C) 2001-2014 David A. Wheeler.");
    }
    
    public static String htmlize(String s)
    {
        String s1 = s.replace("&","&amp;");
        s1 = s1.replace("<","&lt;");
        s1 = s1.replace(">","&gt;");
        return s1;
    }
    
    public static String printMultiLineText(String s)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    public static String h(String patchFileName,boolean outputFormat) {
        if(outputFormat)
          return htmlize(patchFileName);
        else
            return patchFileName;
//        throw new UnsupportedOperationException("Not yet implemented");
    }
}
