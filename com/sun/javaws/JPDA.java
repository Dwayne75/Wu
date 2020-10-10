package com.sun.javaws;

import com.sun.deploy.config.JREInfo;
import com.sun.deploy.util.DialogFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class JPDA
{
  private static final int NAPN = -1;
  public static final int JWS = 1;
  public static final int JWSJNL = 2;
  public static final int JNL = 3;
  private static String JWS_str = "1";
  private static String JWSJNL_str = "2";
  private static String JNL_str = "3";
  private static String dbgNotificationTitle = "JPDA Notification";
  private static JPDA o_envCurrentJRE = null;
  private static JPDA o_envNextJRE = null;
  private static String s_envCurrentJRE = null;
  private static int _debuggeeType = 0;
  private static boolean _jpdaConfigIsFromCmdLine = false;
  private static String _portsList = null;
  private static int[] _portsPool = null;
  private int _selectedPort = -1;
  private boolean _portIsAutoSelected = false;
  private String _excludedportsList = null;
  private int[] _excludedportsPool = null;
  private String _jreProductVersion = null;
  private int _jreNestingLevel = -1;
  private static boolean _jreUsesDashClassic = false;
  private String _javaMainArgsList = null;
  private static boolean _nextJreRunsInJpdaMode = false;
  
  public static int getDebuggeeType()
  {
    return _debuggeeType;
  }
  
  public static void setup()
  {
    s_envCurrentJRE = getProperty("jnlpx.jpda.env");
    o_envCurrentJRE = decodeJpdaEnv(s_envCurrentJRE);
    if (getProperty("jpda.notification") != null)
    {
      showJpdaNotificationWindow(o_envCurrentJRE);
      Main.systemExit(0);
    }
  }
  
  public static JPDA decodeJpdaEnv(String paramString)
  {
    if ((paramString == null) || (paramString.equals(""))) {
      return null;
    }
    JPDA localJPDA = new JPDA();
    
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString, "&");
    int j = localStringTokenizer.countTokens();
    
    boolean[] arrayOfBoolean = new boolean[j];
    for (int i = 0; i < j; i++) {
      arrayOfBoolean[i] = true;
    }
    try
    {
      while (localStringTokenizer.hasMoreTokens())
      {
        String[] arrayOfString1 = tokenizeJpdaEnvEntry(localStringTokenizer.nextToken(), "=");
        if ((arrayOfBoolean[0] != 0) && (arrayOfString1[0].equals("debuggeeType")))
        {
          arrayOfBoolean[0] = false;
          if (arrayOfString1[1].equals(JWS_str)) {
            _debuggeeType = 1;
          } else if (arrayOfString1[1].equals(JWSJNL_str)) {
            _debuggeeType = 2;
          } else if (arrayOfString1[1].equals(JNL_str)) {
            _debuggeeType = 3;
          }
        }
        else if ((arrayOfBoolean[1] != 0) && (arrayOfString1[0].equals("jpdaConfigIsFromCmdLine")))
        {
          arrayOfBoolean[1] = false;
          if (arrayOfString1[1].equals("1")) {
            _jpdaConfigIsFromCmdLine = true;
          }
        }
        else
        {
          String[] arrayOfString2;
          if ((arrayOfBoolean[2] != 0) && (arrayOfString1[0].equals("portsList")))
          {
            arrayOfBoolean[2] = false;
            _portsList = arrayOfString1[1];
            if (!_portsList.equals("NONE"))
            {
              arrayOfString2 = tokenizeJpdaEnvEntry(_portsList, ",");
              _portsPool = new int[arrayOfString2.length];
              for (i = 0; i < arrayOfString2.length; i++) {
                _portsPool[i] = string2Int(arrayOfString2[i]);
              }
            }
          }
          else if ((arrayOfBoolean[3] != 0) && (arrayOfString1[0].equals("selectedPort")))
          {
            arrayOfBoolean[3] = false;
            localJPDA._selectedPort = string2Int(arrayOfString1[1]);
          }
          else if ((arrayOfBoolean[4] != 0) && (arrayOfString1[0].equals("portIsAutoSelected")))
          {
            arrayOfBoolean[4] = false;
            if (arrayOfString1[1].equals("1")) {
              localJPDA._portIsAutoSelected = true;
            }
          }
          else if ((arrayOfBoolean[5] != 0) && (arrayOfString1[0].equals("excludedportsList")))
          {
            arrayOfBoolean[5] = false;
            localJPDA._excludedportsList = arrayOfString1[1];
            if (!localJPDA._excludedportsList.equals("NONE"))
            {
              arrayOfString2 = tokenizeJpdaEnvEntry(localJPDA._excludedportsList, ",");
              localJPDA._excludedportsPool = new int[arrayOfString2.length];
              for (i = 0; i < arrayOfString2.length; i++) {
                localJPDA._excludedportsPool[i] = string2Int(arrayOfString2[i]);
              }
            }
          }
          else if ((arrayOfBoolean[6] != 0) && (arrayOfString1[0].equals("jreProductVersion")))
          {
            arrayOfBoolean[6] = false;
            localJPDA._jreProductVersion = arrayOfString1[1];
          }
          else if ((arrayOfBoolean[7] != 0) && (arrayOfString1[0].equals("jreNestingLevel")))
          {
            arrayOfBoolean[7] = false;
            localJPDA._jreNestingLevel = string2Int(arrayOfString1[1]);
          }
          else if ((arrayOfBoolean[8] != 0) && (arrayOfString1[0].equals("jreUsesDashClassic")))
          {
            arrayOfBoolean[8] = false;
            if (arrayOfString1[1].equals("1")) {
              _jreUsesDashClassic = true;
            }
          }
          else if ((arrayOfBoolean[9] != 0) && (arrayOfString1[0].equals("javaMainArgsList")))
          {
            arrayOfBoolean[9] = false;
            localJPDA._javaMainArgsList = arrayOfString1[1];
          }
        }
      }
    }
    catch (NoSuchElementException localNoSuchElementException)
    {
      return null;
    }
    return localJPDA;
  }
  
  public static String encodeJpdaEnv(JPDA paramJPDA)
  {
    if (paramJPDA == null) {
      return "-Djnlpx.jpda.env";
    }
    return "-Djnlpx.jpda.env=debuggeeType=" + _debuggeeType + "&jpdaConfigIsFromCmdLine=" + (_jpdaConfigIsFromCmdLine ? "1" : "0") + "&portsList=" + _portsList + "&selectedPort=" + paramJPDA._selectedPort + "&portIsAutoSelected=" + (paramJPDA._portIsAutoSelected ? "1" : "0") + "&excludedportsList=" + paramJPDA._excludedportsList + "&jreProductVersion=" + paramJPDA._jreProductVersion + "&jreNestingLevel=" + paramJPDA._jreNestingLevel + "&jreUsesDashClassic=" + (_jreUsesDashClassic ? "1" : "0") + "&javaMainArgsList=" + paramJPDA._javaMainArgsList;
  }
  
  private static void setJpdaEnvForNextJRE(boolean paramBoolean1, boolean paramBoolean2, String[] paramArrayOfString, JREInfo paramJREInfo)
  {
    if ((_debuggeeType == 0) || (_debuggeeType == 1))
    {
      o_envNextJRE = o_envCurrentJRE;
      _nextJreRunsInJpdaMode = false;
      return;
    }
    JPDA localJPDA1 = o_envCurrentJRE;
    JPDA localJPDA2 = new JPDA();
    
    localJPDA2._jreProductVersion = paramJREInfo.getProduct();
    localJPDA2._jreNestingLevel = (1 + localJPDA1._jreNestingLevel);
    localJPDA2._javaMainArgsList = localJPDA1._javaMainArgsList;
    if (paramArrayOfString.length > 0) {
      localJPDA2._javaMainArgsList = paramArrayOfString[0];
    }
    for (int i = 1; i < paramArrayOfString.length; i++)
    {
      JPDA tmp101_99 = localJPDA2;tmp101_99._javaMainArgsList = (tmp101_99._javaMainArgsList + "," + paramArrayOfString[i]);
    }
    _nextJreRunsInJpdaMode = true;
    if (_debuggeeType == 3)
    {
      localJPDA2._selectedPort = localJPDA1._selectedPort;
      localJPDA2._portIsAutoSelected = localJPDA1._portIsAutoSelected;
      localJPDA2._excludedportsList = localJPDA1._excludedportsList;
      localJPDA2._excludedportsPool = localJPDA1._excludedportsPool;
      o_envNextJRE = localJPDA2;
      return;
    }
    if (paramBoolean1) {
      if (localJPDA1._excludedportsPool == null)
      {
        localJPDA2._excludedportsList = ("" + localJPDA1._selectedPort);
        localJPDA2._excludedportsPool = new int[] { localJPDA1._selectedPort };
      }
      else
      {
        localJPDA2._excludedportsList = (localJPDA1._excludedportsList + "," + localJPDA1._selectedPort);
        
        localJPDA2._excludedportsPool = new int[localJPDA1._excludedportsPool.length + 1];
        for (i = 0; i < localJPDA1._excludedportsPool.length; i++) {
          localJPDA2._excludedportsPool[i] = localJPDA1._excludedportsPool[i];
        }
        localJPDA2._excludedportsPool[i] = localJPDA1._selectedPort;
      }
    }
    localJPDA2._selectedPort = localJPDA2.getAvailableServerPort(paramBoolean1, paramBoolean2);
    if (localJPDA2._selectedPort < 0)
    {
      localJPDA2 = null;
      _nextJreRunsInJpdaMode = false;
    }
    o_envNextJRE = localJPDA2;
  }
  
  private static String[] tokenizeJpdaEnvEntry(String paramString1, String paramString2)
  {
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString1, paramString2);
    String[] arrayOfString = new String[localStringTokenizer.countTokens()];
    try
    {
      for (int i = 0; localStringTokenizer.hasMoreTokens(); i++) {
        arrayOfString[i] = localStringTokenizer.nextToken();
      }
    }
    catch (NoSuchElementException localNoSuchElementException)
    {
      localNoSuchElementException.printStackTrace();
      return null;
    }
    return arrayOfString;
  }
  
  public static void showJpdaNotificationWindow(JPDA paramJPDA)
  {
    if (paramJPDA == null) {
      DialogFactory.showErrorDialog("ERROR: No JPDA environment.", dbgNotificationTitle);
    } else {
      DialogFactory.showInformationDialog("Starting JRE (version " + paramJPDA._jreProductVersion + ") in JPDA debugging mode, trying server socket port " + paramJPDA._selectedPort + " on this host (" + getLocalHostName() + ").\n\n        Main class  =  " + "com.sun.javaws.Main" + "\n        Arguments to main()  =  " + paramJPDA._javaMainArgsList + "\n\nTo start debugging, please connect a JPDA debugging client to this host at indicated port.\n\n\nDiagnostics:\n\n     Debugging directive was obtained from\n     " + (_jpdaConfigIsFromCmdLine ? "command line:" : "\"javaws-jpda.cfg\" configuration file:") + "\n        - JRE " + (_jreUsesDashClassic ? "uses" : "doesn't use") + "  -classic  option.\n        - Port " + (paramJPDA._portIsAutoSelected ? "automatically selected (by OS);\n          unable to find or use user-specified\n          ports list." : new StringBuffer().append(" selected from user-specified list:\n          ").append(_portsList).append(".").toString()), dbgNotificationTitle + " (" + (paramJPDA._jreNestingLevel < 1 ? "JWS" : "JNL") + ")");
    }
  }
  
  private static String getProperty(String paramString)
  {
    String str = null;
    try
    {
      str = System.getProperty(paramString);
    }
    catch (SecurityException localSecurityException)
    {
      localSecurityException.printStackTrace();
      return str;
    }
    catch (NullPointerException localNullPointerException)
    {
      localNullPointerException.printStackTrace();
      return str;
    }
    catch (IllegalArgumentException localIllegalArgumentException)
    {
      localIllegalArgumentException.printStackTrace();
      return str;
    }
    return str;
  }
  
  private static int string2Int(String paramString)
  {
    int i = -1;
    try
    {
      i = new Integer(paramString).intValue();
    }
    catch (NumberFormatException localNumberFormatException)
    {
      localNumberFormatException.printStackTrace();
      return i;
    }
    return i;
  }
  
  private static String getLocalHostName()
  {
    try
    {
      return InetAddress.getLocalHost().getHostName();
    }
    catch (UnknownHostException localUnknownHostException) {}
    return "localhost";
  }
  
  public int getAvailableServerPort(boolean paramBoolean1, boolean paramBoolean2)
  {
    if (_portsPool == null) {
      return -1;
    }
    this._portIsAutoSelected = false;
    int j;
    for (int i = 0; i < _portsPool.length; i++) {
      if (((j = _portsPool[i]) != 0) && ((!paramBoolean1) || (!isExcludedPort(j)))) {
        try
        {
          new ServerSocket(j).close();
          return j;
        }
        catch (IOException localIOException1) {}
      }
    }
    if (paramBoolean2)
    {
      i = 0;
      try
      {
        do
        {
          ServerSocket localServerSocket = new ServerSocket(0);
          j = localServerSocket.getLocalPort();
          localServerSocket.close();
        } while ((paramBoolean1) && (isExcludedPort(j)));
        this._portIsAutoSelected = true;
        return j;
      }
      catch (IOException localIOException2) {}
    }
    return -1;
  }
  
  private boolean isExcludedPort(int paramInt)
  {
    if (this._excludedportsPool == null) {
      return false;
    }
    for (int i = 0; i < this._excludedportsPool.length; i++) {
      if (paramInt == this._excludedportsPool[i]) {
        return true;
      }
    }
    return false;
  }
  
  public static String[] JpdaSetup(String[] paramArrayOfString, JREInfo paramJREInfo)
  {
    setJpdaEnvForNextJRE(true, true, paramArrayOfString, paramJREInfo);
    if (_nextJreRunsInJpdaMode)
    {
      int i = paramArrayOfString.length + (_jreUsesDashClassic ? 5 : 2);
      String[] arrayOfString = new String[i];
      
      int j = 0;
      arrayOfString[(j++)] = paramArrayOfString[0];
      if (_jreUsesDashClassic)
      {
        arrayOfString[(j++)] = "-classic";
        arrayOfString[(j++)] = "-Xnoagent";
        arrayOfString[(j++)] = "-Djava.compiler=NONE";
      }
      arrayOfString[(j++)] = "-Xdebug";
      arrayOfString[(j++)] = ("-Xrunjdwp:transport=dt_socket,server=y,address=" + o_envNextJRE._selectedPort + ",suspend=y");
      for (int k = 1; k < paramArrayOfString.length; arrayOfString[(j++)] = paramArrayOfString[(k++)]) {}
      showJpdaNotificationWindow(o_envNextJRE);
      return arrayOfString;
    }
    return paramArrayOfString;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\JPDA.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */