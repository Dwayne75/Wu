package com.sun.javaws;

import com.sun.deploy.config.Config;
import com.sun.deploy.util.Trace;
import com.sun.deploy.util.TraceLevel;
import com.sun.deploy.util.WinRegistry;
import java.io.File;
import java.io.IOException;

public class WinOperaSupport
  extends OperaSupport
{
  private static final String OPERA_SUBKEY = "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\Opera.exe";
  private static final String OPERA_PATH = "Path";
  private static final String USER_HOME = "user.home";
  
  public boolean isInstalled()
  {
    return getInstallPath().length() != 0;
  }
  
  public void enableJnlp(File paramFile, boolean paramBoolean)
  {
    String str = getInstallPath();
    if (str.length() > 0) {
      try
      {
        File localFile1 = new File(str);
        File localFile2 = enableSystemJnlp(localFile1, paramFile);
        if (localFile2 == null)
        {
          localFile2 = new File(localFile1, "Opera6.ini");
          if (!localFile2.exists())
          {
            localFile2 = new File(localFile1, "Opera.ini");
            if (!localFile2.exists()) {
              localFile2 = new File(Config.getOSHome(), "Opera.ini");
            }
          }
        }
        enableJnlp(null, localFile2, paramFile, paramBoolean);
      }
      catch (Exception localException)
      {
        Trace.ignoredException(localException);
      }
    }
  }
  
  public WinOperaSupport(boolean paramBoolean)
  {
    super(paramBoolean);
  }
  
  private File enableSystemJnlp(File paramFile1, File paramFile2)
    throws IOException
  {
    OperaPreferences localOperaPreferences = null;
    File localFile1 = null;
    File localFile2 = null;
    
    localFile1 = new File(paramFile1, "OperaDef6.ini");
    localOperaPreferences = getPreferences(localFile1);
    if (localOperaPreferences != null)
    {
      int i = 1;
      
      enableJnlp(localOperaPreferences, localFile1, paramFile2, true);
      Object localObject;
      if (localOperaPreferences.containsKey("System", "Multi User"))
      {
        localObject = localOperaPreferences.get("System", "Multi User").trim();
        
        localObject = ((String)localObject).substring(0, ((String)localObject).indexOf(' '));
        try
        {
          int j = Integer.decode((String)localObject).intValue();
          if (j == 0)
          {
            i = 0;
            
            Trace.println("Multi-user support is turned off in the Opera system preference file (" + localFile1.getAbsolutePath() + ").", TraceLevel.BASIC);
          }
        }
        catch (NumberFormatException localNumberFormatException)
        {
          i = 0;
          
          Trace.println("The Opera system preference file (" + localFile1.getAbsolutePath() + ") has '" + "Multi User" + "=" + (String)localObject + "' in the " + "System" + " section, so multi-user " + "support is not enabled.", TraceLevel.BASIC);
        }
      }
      if (i == 1)
      {
        localObject = new StringBuffer(512);
        
        ((StringBuffer)localObject).append(System.getProperty("user.home")).append(File.separator).append(USER_DATA_INFIX).append(File.separator).append(paramFile1.getName()).append(File.separator).append("Profile").append(File.separator).append("Opera6.ini");
        
        localFile2 = new File(((StringBuffer)localObject).toString());
      }
    }
    return localFile2;
  }
  
  private String getInstallPath()
  {
    String str = WinRegistry.getString(-2147483646, "Software\\Microsoft\\Windows\\CurrentVersion\\App Paths\\Opera.exe", "Path");
    
    return str != null ? str : "";
  }
  
  private static final String USER_DATA_INFIX = "Application Data" + File.separator + "Opera";
  private static final String USER_DATA_POSTFIX = "Profile";
  private static final String SYSTEM_PREFERENCES = "OperaDef6.ini";
  private static final String MULTI_USER_SECTION = "System";
  private static final String MULTI_USER_KEY = "Multi User";
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\WinOperaSupport.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */