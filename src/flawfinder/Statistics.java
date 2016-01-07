/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package flawfinder;

import java.util.Date;

/**
 *
 * @author afif
 */
public class Statistics {
    public static int NUM_IGNORED_HITS = 0;
    public static int NUM_LINKS_SKIPPED = 0;
    public static int NUM_DOTDIRS_SKIPPED = 0;
    public static int SLOC = 0;
    public static Date startTime = null;
    public static int SUM_LINES = 0;
    
    protected static void reset()
    {
        startTime = new Date();
        NUM_IGNORED_HITS = 0;
        NUM_LINKS_SKIPPED = 0;
        NUM_DOTDIRS_SKIPPED = 0;
        SLOC = 0;
        SUM_LINES = 0;
    }
}
