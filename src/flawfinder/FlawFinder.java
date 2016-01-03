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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        RuleSet r = new RuleSet();
        r.addRule("strcpy",
                new RuleValue("c_buffer",
                4,
                "Does not check for buffer overflows when copying to destination (CWE-120)",
                "Consider using strcpy_s, strncpy, or strlcpy (warning, strncpy is easily misused)",
                "buffer", "", new HashMap<String, Integer>()));

        r.addRule("lstrcpy|wcscpy|_tcscpy|_mbscpy",new RuleValue("c_buffer",4,"Does not check for buffer overflows when copying to destination (CWE-120)",
      "Consider using a function version that stops copying at the end of the buffer",
      "buffer", "",new HashMap<String,Integer>()));
        
       r.addRule( "memcpy|CopyMemory|bcopy" ,
     new RuleValue("normal", 2, // I've found this to have a lower risk in practice.
      "Does not check for buffer overflows when copying to destination (CWE-120)",
      "Make sure destination can always hold the source data",
      "buffer", "", new HashMap<String,Integer>()));
       
  r.addRule("strcat" ,
     new RuleValue("c_buffer", 4,
      "Does not check for buffer overflows when concatenating to destination (CWE-120)",
      "Consider using strcat_s, strncat, or strlcat (warning, strncat is easily misused)",
      "buffer", "", new HashMap<String,Integer>()));
  
  r.addRule(
  "lstrcat|wcscat|_tcscat|_mbscat" ,
     new RuleValue("c_buffer", 4,
      "Does not check for buffer overflows when concatenating to destination (CWE-120)",
      "",
      "buffer", "", new HashMap<String,Integer>())
          );
 
  r.addRule("strncpy" ,
     new RuleValue("c_buffer",
      1, // Low risk level, because this is often used correctly when FIXING security
      // problems, and raising it to a higher risk level would cause many false positives.
      "Easily used incorrectly; doesn't always \\0-terminate or " +
         "check for invalid pointers (CWE-120)",
      "",
      "buffer", "", new HashMap<String,Integer>()));
  
  r.addRule("lstrcpyn|wcsncpy|_tcsncpy|_mbsnbcpy" ,
     new RuleValue("c_buffer",
      1, // Low risk level, because this is often used correctly when FIXING security
      // problems, and raising it to a higher risk levle would cause many false positives.
      "Easily used incorrectly; doesn't always \\0-terminate or " +
         "check for invalid pointers (CWE-120)",
      "",
      "buffer", "", new HashMap<String,Integer>()));
          
  
    r.addRule(  "strncat" ,
     new RuleValue("c_strncat",
      1, // Low risk level, because this is often used correctly when
         // FIXING security problems, and raising it to a
         // higher risk level would cause many false positives.
      "Easily used incorrectly (e.g., incorrectly computing the correct maximum size to add) (CWE-120)",
      "Consider strcat_s, strlcat, or automatically resizing strings",
      "buffer", "", new HashMap<String,Integer>()));
      
  
     r.addRule( "lstrcatn|wcsncat|_tcsncat|_mbsnbcat" ,
     new RuleValue("c_strncat",
      1, // Low risk level, because this is often used correctly when FIXING security
      // problems, and raising it to a higher risk level would cause many false positives.
      "Easily used incorrectly (e.g., incorrectly computing the correct maximum size to add) (CWE-120)",
      "Consider strcat_s, strlcat, or automatically resizing strings",
      "buffer", "", new HashMap<String,Integer>()));
  
      r.addRule("strccpy|strcadd",
     new RuleValue("normal", 1,
      "Subject to buffer overflow if buffer is not as big as claimed (CWE-120)",
      "Ensure that destination buffer is sufficiently large",
      "buffer", "", new HashMap<String,Integer>()));
      
      Map<String,Integer> other = new HashMap<String,Integer>();
      other.put("extract_lookahead",1);
      r.addRule(
  "char|TCHAR|wchar_t",  // This isn't really a function call, but it works.
     new RuleValue("c_static_array", 2,
      "Statically-sized arrays can be improperly restricted, " +
      "leading to potential overflows or other issues (CWE-119:CWE-120)",
      "Perform bounds checking, use functions that limit length, " +
        "or ensure that the size is larger than the maximum possible length",
      "buffer", "", other));
      
      other = new HashMap<String,Integer>();
      other.put("input", 1);
  r.addRule("gets|_getts",new RuleValue
     ("normal", 5, "Does not check for buffer overflows (CWE-120, CWE-20)",
      "Use fgets() instead", "buffer", "", other));

  // The "sprintf" hook will raise "format" issues instead if appropriate:
  r.addRule("sprintf|vsprintf|swprintf|vswprintf|_stprintf|_vstprintf",
     new RuleValue("c_sprintf", 4,
      "Does not check for buffer overflows (CWE-120)",
      "Use sprintf_s, snprintf, or vsnprintf",
      "buffer", "", new HashMap<String,Integer>()));

  r.addRule("printf|vprintf|vwprintf|vfwprintf|_vtprintf|wprintf",
     new RuleValue("c_printf", 4,
      "If format strings can be influenced by an attacker, they can be exploited (CWE-134)",
      "Use a constant for the format specification",
      "format", "", new HashMap<String,Integer>()));

  other = new HashMap<String,Integer>();
  other.put("format_position",2);
  r.addRule("fprintf|vfprintf|_ftprintf|_vftprintf|fwprintf|fvwprintf",
     new RuleValue("c_printf", 4,
      "If format strings can be influenced by an attacker, they can be exploited (CWE-134)",
      "Use a constant for the format specification",
      "format", "", other));

  // The "syslog" hook will raise "format" issues.
  other = new HashMap<String,Integer>();
  other.put("format_position", 2);
  r.addRule("syslog",
     new RuleValue("c_printf", 4,
      "If syslog's format strings can be influenced by an attacker, " +
      "they can be exploited (CWE-134)",
      "Use a constant format string for syslog",
      "format", "", other ));
  
  other = new HashMap<String,Integer>();
  other.put("format_position", 3);
  r.addRule("snprintf|vsnprintf|_snprintf|_sntprintf|_vsntprintf",
     new RuleValue("c_printf", 4,
      "If format strings can be influenced by an attacker, they can be " +
      "exploited, and note that sprintf variations do not always \\0-terminate (CWE-134)",
      "Use a constant for the format specification",
      "format", "", other));

  
  other = new HashMap<String,Integer>();
  other.put("input", 1);
  r.addRule("scanf|vscanf|wscanf|_tscanf|vwscanf",
     new RuleValue("c_scanf", 4,
      "The scanf() family's %s operation, without a limit specification, " +
        "permits buffer overflows (CWE-120, CWE-20)",
      "Specify a limit to %s, or use a different input function",
      "buffer", "", other));

   other = new HashMap<String,Integer>();
   other.put("input",1);
   other.put("format_position",2);
   r.addRule("fscanf|sscanf|vsscanf|vfscanf|_ftscanf|fwscanf|vfwscanf|vswscanf",
     new RuleValue("c_scanf", 4,
      "The scanf() family's %s operation, without a limit specification, "+
      "permits buffer overflows (CWE-120, CWE-20)",
      "Specify a limit to %s, or use a different input function",
      "buffer", "", other));
  
  other = new HashMap<String,Integer>();
  r.addRule("strlen|wcslen|_tcslen|_mbslen" ,
     new RuleValue("normal",
      1, // Often this isn't really a risk, and even when, it usually at worst causes
      // program crash (and nothing worse).
      "Does not handle strings that are not \\0-terminated; " +
      "if given one it may perform an over-read (it could cause a crash " +
         "if unprotected) (CWE-126)",
      "",
      "buffer", "", other));

      /*
  "MultiByteToWideChar" : # Windows
     (c_multi_byte_to_wide_char,
      2, # Only the default - this will be changed in many cases.
      "Requires maximum length in CHARACTERS, not bytes (CWE-120)",
      "",
      "buffer", "", {}),

  "streadd|strecpy":
     (normal, 4,
      "This function does not protect against buffer overflows (CWE-120)",
      "Ensure the destination has 4 times the size of the source, to leave room for expansion",
      "buffer", "dangers-c", {}),

  "strtrns":
     (normal, 3,
      "This function does not protect against buffer overflows (CWE-120)",
      "Ensure that destination is at least as long as the source",
      "buffer", "dangers-c", {}),

  "realpath":
     (normal, 3,
      "This function does not protect against buffer overflows, " +
        "and some implementations can overflow internally (CWE-120/CWE-785)",
      "Ensure that the destination buffer is at least of size MAXPATHLEN, and" +
        "to protect against implementation problems, the input argument should also " +
        "be checked to ensure it is no larger than MAXPATHLEN",
      "buffer", "dangers-c", {}),

  "getopt|getopt_long":
     (normal, 3,
     "Some older implementations do not protect against internal buffer overflows (CWE-120, CWE-20)",
      "Check implementation on installation, or limit the size of all string inputs",
      "buffer", "dangers-c", {'input' : 1}),

  "getpass":
     (normal, 3,
     "Some implementations may overflow buffers (CWE-120, CWE-20)",
      "",
      "buffer", "dangers-c", {'input' : 1}),

  "getwd":
     (normal, 3,
     "This does not protect against buffer overflows "
     "by itself, so use with caution (CWE-120, CWE-20)",
      "Use getcwd instead",
      "buffer", "dangers-c", {'input' : 1}),

  # fread not included here; in practice I think it's rare to mistake it.
  "getchar|fgetc|getc|read|_gettc":
     (normal, 1,
     "Check buffer boundaries if used in a loop including recursive loops (CWE-120, CWE-20)",
      "",
      "buffer", "dangers-c", {'input' : 1}),

  "access":        # ???: TODO: analyze TOCTOU more carefully.
     (normal, 4,
      "This usually indicates a security flaw.  If an " +
      "attacker can change anything along the path between the " +
      "call to access() and the file's actual use (e.g., by moving " +
      "files), the attacker can exploit the race condition (CWE-362/CWE-367)",
      "Set up the correct permissions (e.g., using setuid()) and " +
      "try to open the file directly",
      "race",
      "avoid-race#atomic-filesystem", {}),
  "chown":
     (normal, 5,
      "This accepts filename arguments; if an attacker " +
      "can move those files, a race condition results. (CWE-362)",
      "Use fchown( ) instead",
      "race", "", {}),
  "chgrp":
     (normal, 5,
      "This accepts filename arguments; if an attacker " +
      "can move those files, a race condition results. (CWE-362)",
      "Use fchgrp( ) instead",
      "race", "", {}),
  "chmod":
     (normal, 5,
      "This accepts filename arguments; if an attacker " +
      "can move those files, a race condition results. (CWE-362)",
      "Use fchmod( ) instead",
      "race", "", {}),
  "vfork":
     (normal, 2,
      "On some old systems, vfork() permits race conditions, and it's " +
      "very difficult to use correctly (CWE-362)",
      "Use fork() instead",
      "race", "", {}),
  "readlink":
     (normal, 5,
      "This accepts filename arguments; if an attacker " +
      "can move those files or change the link content, " +
      "a race condition results.  " +
      "Also, it does not terminate with ASCII NUL. (CWE-362, CWE-20)",
      # This is often just a bad idea, and it's hard to suggest a
      # simple alternative:
      "Reconsider approach",
      "race", "", {'input' : 1}),

  "tmpfile":
     (normal, 2,
      "Function tmpfile() has a security flaw on some systems (e.g., older System V systems) (CWE-377)",
      "",
      "tmpfile", "", {}),
  "tmpnam|tempnam":
     (normal, 3,
      "Temporary file race condition (CWE-377)",
      "",
      "tmpfile", "avoid-race", {}),

  # TODO: Detect GNOME approach to mktemp and ignore it.
  "mktemp":
     (normal, 4,
      "Temporary file race condition (CWE-377)",
      "",
      "tmpfile", "avoid-race", {}),

  "mkstemp":
     (normal, 2,
     "Potential for temporary file vulnerability in some circumstances.  Some older Unix-like systems create temp files with permission to write by all by default, so be sure to set the umask to override this. Also, some older Unix systems might fail to use O_EXCL when opening the file, so make sure that O_EXCL is used by the library (CWE-377)",
      "",
      "tmpfile", "avoid-race", {}),

  "fopen|open":
     (normal, 2,
     "Check when opening files - can an attacker redirect it (via symlinks), force the opening of special file type (e.g., device files), move things around to create a race condition, control its ancestors, or change its contents? (CWE-362)",
      "",
      "misc", "", {}),

  "umask":
     (normal, 1,
      "Ensure that umask is given most restrictive possible setting (e.g., 066 or 077) (CWE-732)",
      "",
      "access", "", {}),

  # Windows.  TODO: Detect correct usage approaches and ignore it.
  "GetTempFileName":
     (normal, 3,
      "Temporary file race condition in certain cases " +
        "(e.g., if run as SYSTEM in many versions of Windows) (CWE-377)",
      "",
      "tmpfile", "avoid-race", {}),

  # TODO: Need to detect varying levels of danger.
  "execl|execlp|execle|execv|execvp|system|popen|WinExec|ShellExecute":
     (normal, 4,
      "This causes a new program to execute and is difficult to use safely (CWE-78)",
      "try using a library call that implements the same functionality " +
      "if available",
      "shell", "", {}),

  # TODO: Need to detect varying levels of danger.
  "execl|execlp|execle|execv|execvp|system|popen|WinExec|ShellExecute":
     (normal, 4,
      "This causes a new program to execute and is difficult to use safely (CWE-78)",
      "try using a library call that implements the same functionality " +
      "if available",
      "shell", "", {}),

  # TODO: Be more specific.  The biggest problem involves "first" param NULL,
  # second param with embedded space. Windows.
  "CreateProcessAsUser|CreateProcessWithLogon":
     (normal, 3,
      "This causes a new process to execute and is difficult to use safely (CWE-78)",
      "Especially watch out for embedded spaces",
      "shell", "", {}),

  # TODO: Be more specific.  The biggest problem involves "first" param NULL,
  # second param with embedded space. Windows.
  "CreateProcess":
     (c_hit_if_null, 3,
      "This causes a new process to execute and is difficult to use safely (CWE-78)",
      "Specify the application path in the first argument, NOT as part of the second, " +
        "or embedded spaces could allow an attacker to force a different program to run",
      "shell", "", {'check_for_null' : 1}),

  "atoi|atol|_wtoi|_wtoi64":
     (normal, 2,
      "Unless checked, the resulting number can exceed the expected range " +
      "(CWE-190)",
      "If source untrusted, check both minimum and maximum, even if the" +
      " input had no minus sign (large numbers can roll over into negative" +
      " number; consider saving to an unsigned value if that is intended)",
      "integer", "dangers-c", {}),

  # Random values.  Don't trigger on "initstate", it's too common a term.
  "drand48|erand48|jrand48|lcong48|lrand48|mrand48|nrand48|random|seed48|setstate|srand|strfry|srandom":
     (normal, 3,
      "This function is not sufficiently random for security-related functions such as key and nonce creation (CWE-327)",
      "use a more secure technique for acquiring random values",
      "random", "", {}),

  "crypt":
     (normal, 4,
      "Function crypt is a poor one-way hashing algorithm; since it only accepts passwords of 8 " +
        "characters or less, and only a two-byte salt, it is excessively vulnerable to " +
        "dictionary attacks given today's faster computing equipment (CWE-327)",
      "Use a different algorithm, such as SHA-1, with a larger non-repeating salt",
      "crypto", "", {}),

  # OpenSSL EVP calls to use DES.
  "EVP_des_ecb|EVP_des_cbc|EVP_des_cfb|EVP_des_ofb|EVP_desx_cbc":
     (normal, 4,
      "DES only supports a 56-bit keysize, which is too small given today's computers (CWE-327)",
      "Use a different patent-free encryption algorithm with a larger keysize, " +
         "such as 3DES or AES",
      "crypto", "", {}),

  # Other OpenSSL EVP calls to use small keys.
  "EVP_rc4_40|EVP_rc2_40_cbc|EVP_rc2_64_cbc":
     (normal, 4,
      "These keysizes are too small given today's computers (CWE-327)",
      "Use a different patent-free encryption algorithm with a larger keysize, " +
        "such as 3DES or AES",
      "crypto", "", {}),

  "chroot":
     (normal, 3,
      "chroot can be very helpful, but is hard to use correctly (CWE-250, CWE-22)",
      "Make sure the program immediately chdir(\"/\")," +
      " closes file descriptors," +
      " and drops root privileges, and that all necessary files" +
      " (and no more!) are in the new root",
      "misc", "", {}),

  "getenv|curl_getenv":
     (normal, 3, "Environment variables are untrustable input if they can be" +
                 " set by an attacker.  They can have any content and" +
                 " length, and the same variable can be set more than once (CWE-807, CWE-20)",
      "Check environment variables carefully before using them",
      "buffer", "", {'input' : 1}),

  "g_get_home_dir":
     (normal, 3, "This function is synonymous with 'getenv(\"HOME\")';" +
                 "it returns untrustable input if the environment can be" +
                 "set by an attacker.  It can have any content and length, " +
                 "and the same variable can be set more than once (CWE-807, CWE-20)",
      "Check environment variables carefully before using them",
      "buffer", "", {'input' : 1}),

  "g_get_tmp_dir":
     (normal, 3, "This function is synonymous with 'getenv(\"TMP\")';" +
                 "it returns untrustable input if the environment can be" +
                 "set by an attacker.  It can have any content and length, " +
                 "and the same variable can be set more than once (CWE-807, CWE-20)",
      "Check environment variables carefully before using them",
      "buffer", "", {'input' : 1}),


  # These are Windows-unique:

  # TODO: Should have lower risk if the program checks return value.
  "RpcImpersonateClient|ImpersonateLoggedOnUser|CoImpersonateClient|" +
     "ImpersonateNamedPipeClient|ImpersonateDdeClientWindow|ImpersonateSecurityContext|" +
     "SetThreadToken":
     (normal, 4, "If this call fails, the program could fail to drop heightened privileges (CWE-250)",
      "Make sure the return value is checked, and do not continue if a failure is reported",
      "access", "", {}),

  "InitializeCriticalSection":
     (normal, 3, "Exceptions can be thrown in low-memory situations",
      "Use InitializeCriticalSectionAndSpinCount instead",
      "misc", "", {}),

  "EnterCriticalSection":
     (normal, 3, "On some versions of Windows, exceptions can be thrown in low-memory situations",
      "Use InitializeCriticalSectionAndSpinCount instead",
      "misc", "", {}),

  "LoadLibrary|LoadLibraryEx":
     (normal, 3, "Ensure that the full path to the library is specified, or current directory may be used (CWE-829, CWE-20)",
      "Use registry entry or GetWindowsDirectory to find library path, if you aren't already",
      "misc", "", {'input' : 1}),

  "SetSecurityDescriptorDacl":
     (c_hit_if_null, 5,
      "Never create NULL ACLs; an attacker can set it to Everyone (Deny All Access), " +
        "which would even forbid administrator access (CWE-732)",
      "",
      "misc", "", {'check_for_null' : 3}),

  "AddAccessAllowedAce":
     (normal, 3,
      "This doesn't set the inheritance bits in the access control entry (ACE) header (CWE-732)",
      "Make sure that you set inheritance by hand if you wish it to inherit",
      "misc", "", {}),

  "getlogin":
     (normal, 4,
      "It's often easy to fool getlogin.  Sometimes it does not work at all, because some program messed up the utmp file.  Often, it gives only the first 8 characters of the login name. The user currently logged in on the controlling tty of our program need not be the user who started it.  Avoid getlogin() for security-related purposes (CWE-807)",
      "Use getpwuid(geteuid()) and extract the desired information instead",
      "misc", "", {}),

  "cuserid":
     (normal, 4,
      "Exactly what cuserid() does is poorly defined (e.g., some systems use the effective uid, like Linux, while others like System V use the real uid). Thus, you can't trust what it does. It's certainly not portable (The cuserid function was included in the 1988 version of POSIX, but removed from the 1990 version).  Also, if passed a non-null parameter, there's a risk of a buffer overflow if the passed-in buffer is not at least L_cuserid characters long (CWE-120)",
      "Use getpwuid(geteuid()) and extract the desired information instead",
      "misc", "", {}),

  "getpw":
     (normal, 4,
      "This function is dangerous; it may overflow the provided buffer. It extracts data from a 'protected' area, but most systems have many commands to let users modify the protected area, and it's not always clear what their limits are.  Best to avoid using this function altogether (CWE-676, CWE-120)",
      "Use getpwuid() instead",
      "buffer", "", {}),

  "getpass":
     (normal, 4,
      "This function is obsolete and not portable. It was in SUSv2 but removed by POSIX.2.  What it does exactly varies considerably between systems, particularly in where its prompt is displayed and where it gets its data (e.g., /dev/tty, stdin, stderr, etc.) (CWE-676)",
      "Make the specific calls to do exactly what you want.  If you continue to use it, or write your own, be sure to zero the password as soon as possible to avoid leaving the cleartext password visible in the process' address space",
      "misc", "", {}),

  "gsignal|ssignal":
     (normal, 2,
      "These functions are considered obsolete on most systems, and very non-poertable (Linux-based systems handle them radically different, basically if gsignal/ssignal were the same as raise/signal respectively, while System V considers them a separate set and obsolete) (CWE-676)",
      "Switch to raise/signal, or some other signalling approach",
      "obsolete", "", {}),

  "memalign":
     (normal, 1,
     "On some systems (though not Linux-based systems) an attempt to free() results from memalign() may fail. This may, on a few systems, be exploitable.  Also note that memalign() may not check that the boundary parameter is correct (CWE-676)",
      "Use posix_memalign instead (defined in POSIX's 1003.1d).  Don't switch to valloc(); it is marked as obsolete in BSD 4.3, as legacy in SUSv2, and is no longer defined in SUSv3.  In some cases, malloc()'s alignment may be sufficient",
      "free", "", {}),

  "ulimit":
     (normal, 1,
     "This C routine is considered obsolete (as opposed to the shell command by the same name, which is NOT obsolete) (CWE-676)",
      "Use getrlimit(2), setrlimit(2), and sysconf(3) instead",
      "obsolete", "", {}),

  "usleep":
     (normal, 1,
     "This C routine is considered obsolete (as opposed to the shell command by the same name).   The interaction of this function with SIGALRM and other timer functions such as sleep(), alarm(), setitimer(), and nanosleep() is unspecified (CWE-676)",
      "Use nanosleep(2) or setitimer(2) instead",
      "obsolete", "", {}),


   # Input functions, useful for -I
  "recv|recvfrom|recvmsg|fread|readv":
     (normal, 0, "Function accepts input from outside program (CWE-20)",
      "Make sure input data is filtered, especially if an attacker could manipulate it",
      "input", "", {'input' : 1}),

*/
        r.expandRuleSet();
//        System.out.println(r);
        return r;
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
        String fileName = "/home/afif/flawfinder/flawfinder-1.31/test.c";
        RuleSet r = HelperFunctions.readRuleSet("/home/afif/flawfinder/flawfinder-1.31/rules.txt");
        r.expandRuleSet();
        
        BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        String text="",line;
        while((line=input.readLine())!=null)
        {
            text = text+line+"\n";
        }
        System.out.println(text.length());
        FileProcessor fileProcessor = new FileProcessor(arguments,fileName,null,r);
        fileProcessor.processCFile();
//        boolean ans = fileProcessor.cValidMatch(text,3246);
//        System.out.println(ans);
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
    public static void main(String[] args) throws IOException {
//            cExtractParametersTest();
        fileProcessorTest();
          
//          String text = "a";
//          for(int i=0;i<text.length()+10;i++)
//          {
////              System.out.println(i);
////              HelperFunctions.getContext(text, i);
//              System.out.println(i+"\t"+HelperFunctions.getContext(text, i));
//          }
//            System.out.println(i+" "+HelperFunctions.getContext(text,i));
//        ruletest();
//        loadPatchInfoTest();
//        fileProcessorTest();
//        String ar[] = "lstrcpy|wcscpy|_tcscpy|_mbscpy".split("\\|");
//        for(String x:ar)
//            System.out.println(x);
        // TODO code application logic here
//        RuleSet r = HelperFunctions.readRuleSet("/home/afif/flawfinder/flawfinder-1.31/rules.txt");
//        r.expandRuleSet();
//        System.out.println(r);
    }
}
