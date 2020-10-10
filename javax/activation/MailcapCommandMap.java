package javax.activation;

import com.sun.activation.registries.LogSupport;
import com.sun.activation.registries.MailcapFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MailcapCommandMap
  extends CommandMap
{
  private static MailcapFile defDB = null;
  private MailcapFile[] DB;
  private static final int PROG = 0;
  
  public MailcapCommandMap()
  {
    List dbv = new ArrayList(5);
    MailcapFile mf = null;
    dbv.add(null);
    
    LogSupport.log("MailcapCommandMap: load HOME");
    try
    {
      String user_home = System.getProperty("user.home");
      if (user_home != null)
      {
        String path = user_home + File.separator + ".mailcap";
        mf = loadFile(path);
        if (mf != null) {
          dbv.add(mf);
        }
      }
    }
    catch (SecurityException ex) {}
    LogSupport.log("MailcapCommandMap: load SYS");
    try
    {
      String system_mailcap = System.getProperty("java.home") + File.separator + "lib" + File.separator + "mailcap";
      
      mf = loadFile(system_mailcap);
      if (mf != null) {
        dbv.add(mf);
      }
    }
    catch (SecurityException ex) {}
    LogSupport.log("MailcapCommandMap: load JAR");
    
    loadAllResources(dbv, "META-INF/mailcap");
    
    LogSupport.log("MailcapCommandMap: load DEF");
    synchronized (MailcapCommandMap.class)
    {
      if (defDB == null) {
        defDB = loadResource("/META-INF/mailcap.default");
      }
    }
    if (defDB != null) {
      dbv.add(defDB);
    }
    this.DB = new MailcapFile[dbv.size()];
    this.DB = ((MailcapFile[])dbv.toArray(this.DB));
  }
  
  private MailcapFile loadResource(String name)
  {
    InputStream clis = null;
    try
    {
      clis = SecuritySupport.getResourceAsStream(getClass(), name);
      if (clis != null)
      {
        MailcapFile mf = new MailcapFile(clis);
        if (LogSupport.isLoggable()) {
          LogSupport.log("MailcapCommandMap: successfully loaded mailcap file: " + name);
        }
        return mf;
      }
      if (LogSupport.isLoggable()) {
        LogSupport.log("MailcapCommandMap: not loading mailcap file: " + name);
      }
      return null;
    }
    catch (IOException e)
    {
      if (LogSupport.isLoggable()) {
        LogSupport.log("MailcapCommandMap: can't load " + name, e);
      }
    }
    catch (SecurityException sex)
    {
      if (LogSupport.isLoggable()) {
        LogSupport.log("MailcapCommandMap: can't load " + name, sex);
      }
    }
    finally
    {
      try
      {
        if (clis != null) {
          clis.close();
        }
      }
      catch (IOException ex) {}
    }
  }
  
  private void loadAllResources(List v, String name)
  {
    boolean anyLoaded = false;
    try
    {
      ClassLoader cld = null;
      
      cld = SecuritySupport.getContextClassLoader();
      if (cld == null) {
        cld = getClass().getClassLoader();
      }
      URL[] urls;
      if (cld != null) {
        urls = SecuritySupport.getResources(cld, name);
      } else {
        urls = SecuritySupport.getSystemResources(name);
      }
      if (urls != null)
      {
        if (LogSupport.isLoggable()) {
          LogSupport.log("MailcapCommandMap: getResources");
        }
        for (i = 0; i < urls.length;)
        {
          URL url = urls[i];
          InputStream clis = null;
          if (LogSupport.isLoggable()) {
            LogSupport.log("MailcapCommandMap: URL " + url);
          }
          try
          {
            clis = SecuritySupport.openStream(url);
            if (clis != null)
            {
              v.add(new MailcapFile(clis));
              anyLoaded = true;
              if (LogSupport.isLoggable()) {
                LogSupport.log("MailcapCommandMap: successfully loaded mailcap file from URL: " + url);
              }
            }
            else if (LogSupport.isLoggable())
            {
              LogSupport.log("MailcapCommandMap: not loading mailcap file from URL: " + url);
            }
            try
            {
              if (clis != null) {
                clis.close();
              }
            }
            catch (IOException cex) {}
            i++;
          }
          catch (IOException ioex)
          {
            if (LogSupport.isLoggable()) {
              LogSupport.log("MailcapCommandMap: can't load " + url, ioex);
            }
          }
          catch (SecurityException sex)
          {
            if (LogSupport.isLoggable()) {
              LogSupport.log("MailcapCommandMap: can't load " + url, sex);
            }
          }
          finally
          {
            try
            {
              if (clis != null) {
                clis.close();
              }
            }
            catch (IOException cex) {}
          }
        }
      }
    }
    catch (Exception ex)
    {
      URL[] urls;
      int i;
      if (LogSupport.isLoggable()) {
        LogSupport.log("MailcapCommandMap: can't load " + name, ex);
      }
    }
    if (!anyLoaded)
    {
      if (LogSupport.isLoggable()) {
        LogSupport.log("MailcapCommandMap: !anyLoaded");
      }
      MailcapFile mf = loadResource("/" + name);
      if (mf != null) {
        v.add(mf);
      }
    }
  }
  
  private MailcapFile loadFile(String name)
  {
    MailcapFile mtf = null;
    try
    {
      mtf = new MailcapFile(name);
    }
    catch (IOException e) {}
    return mtf;
  }
  
  public MailcapCommandMap(String fileName)
    throws IOException
  {
    this();
    if (LogSupport.isLoggable()) {
      LogSupport.log("MailcapCommandMap: load PROG from " + fileName);
    }
    if (this.DB[0] == null) {
      this.DB[0] = new MailcapFile(fileName);
    }
  }
  
  public MailcapCommandMap(InputStream is)
  {
    this();
    
    LogSupport.log("MailcapCommandMap: load PROG");
    if (this.DB[0] == null) {
      try
      {
        this.DB[0] = new MailcapFile(is);
      }
      catch (IOException ex) {}
    }
  }
  
  public synchronized CommandInfo[] getPreferredCommands(String mimeType)
  {
    List cmdList = new ArrayList();
    if (mimeType != null) {
      mimeType = mimeType.toLowerCase(Locale.ENGLISH);
    }
    for (int i = 0; i < this.DB.length; i++) {
      if (this.DB[i] != null)
      {
        Map cmdMap = this.DB[i].getMailcapList(mimeType);
        if (cmdMap != null) {
          appendPrefCmdsToList(cmdMap, cmdList);
        }
      }
    }
    for (int i = 0; i < this.DB.length; i++) {
      if (this.DB[i] != null)
      {
        Map cmdMap = this.DB[i].getMailcapFallbackList(mimeType);
        if (cmdMap != null) {
          appendPrefCmdsToList(cmdMap, cmdList);
        }
      }
    }
    CommandInfo[] cmdInfos = new CommandInfo[cmdList.size()];
    cmdInfos = (CommandInfo[])cmdList.toArray(cmdInfos);
    
    return cmdInfos;
  }
  
  private void appendPrefCmdsToList(Map cmdHash, List cmdList)
  {
    Iterator verb_enum = cmdHash.keySet().iterator();
    while (verb_enum.hasNext())
    {
      String verb = (String)verb_enum.next();
      if (!checkForVerb(cmdList, verb))
      {
        List cmdList2 = (List)cmdHash.get(verb);
        String className = (String)cmdList2.get(0);
        cmdList.add(new CommandInfo(verb, className));
      }
    }
  }
  
  private boolean checkForVerb(List cmdList, String verb)
  {
    Iterator ee = cmdList.iterator();
    while (ee.hasNext())
    {
      String enum_verb = ((CommandInfo)ee.next()).getCommandName();
      if (enum_verb.equals(verb)) {
        return true;
      }
    }
    return false;
  }
  
  public synchronized CommandInfo[] getAllCommands(String mimeType)
  {
    List cmdList = new ArrayList();
    if (mimeType != null) {
      mimeType = mimeType.toLowerCase(Locale.ENGLISH);
    }
    for (int i = 0; i < this.DB.length; i++) {
      if (this.DB[i] != null)
      {
        Map cmdMap = this.DB[i].getMailcapList(mimeType);
        if (cmdMap != null) {
          appendCmdsToList(cmdMap, cmdList);
        }
      }
    }
    for (int i = 0; i < this.DB.length; i++) {
      if (this.DB[i] != null)
      {
        Map cmdMap = this.DB[i].getMailcapFallbackList(mimeType);
        if (cmdMap != null) {
          appendCmdsToList(cmdMap, cmdList);
        }
      }
    }
    CommandInfo[] cmdInfos = new CommandInfo[cmdList.size()];
    cmdInfos = (CommandInfo[])cmdList.toArray(cmdInfos);
    
    return cmdInfos;
  }
  
  private void appendCmdsToList(Map typeHash, List cmdList)
  {
    Iterator verb_enum = typeHash.keySet().iterator();
    while (verb_enum.hasNext())
    {
      String verb = (String)verb_enum.next();
      List cmdList2 = (List)typeHash.get(verb);
      Iterator cmd_enum = cmdList2.iterator();
      while (cmd_enum.hasNext())
      {
        String cmd = (String)cmd_enum.next();
        cmdList.add(new CommandInfo(verb, cmd));
      }
    }
  }
  
  public synchronized CommandInfo getCommand(String mimeType, String cmdName)
  {
    if (mimeType != null) {
      mimeType = mimeType.toLowerCase(Locale.ENGLISH);
    }
    for (int i = 0; i < this.DB.length; i++) {
      if (this.DB[i] != null)
      {
        Map cmdMap = this.DB[i].getMailcapList(mimeType);
        if (cmdMap != null)
        {
          List v = (List)cmdMap.get(cmdName);
          if (v != null)
          {
            String cmdClassName = (String)v.get(0);
            if (cmdClassName != null) {
              return new CommandInfo(cmdName, cmdClassName);
            }
          }
        }
      }
    }
    for (int i = 0; i < this.DB.length; i++) {
      if (this.DB[i] != null)
      {
        Map cmdMap = this.DB[i].getMailcapFallbackList(mimeType);
        if (cmdMap != null)
        {
          List v = (List)cmdMap.get(cmdName);
          if (v != null)
          {
            String cmdClassName = (String)v.get(0);
            if (cmdClassName != null) {
              return new CommandInfo(cmdName, cmdClassName);
            }
          }
        }
      }
    }
    return null;
  }
  
  public synchronized void addMailcap(String mail_cap)
  {
    LogSupport.log("MailcapCommandMap: add to PROG");
    if (this.DB[0] == null) {
      this.DB[0] = new MailcapFile();
    }
    this.DB[0].appendToMailcap(mail_cap);
  }
  
  public synchronized DataContentHandler createDataContentHandler(String mimeType)
  {
    if (LogSupport.isLoggable()) {
      LogSupport.log("MailcapCommandMap: createDataContentHandler for " + mimeType);
    }
    if (mimeType != null) {
      mimeType = mimeType.toLowerCase(Locale.ENGLISH);
    }
    for (int i = 0; i < this.DB.length; i++) {
      if (this.DB[i] != null)
      {
        if (LogSupport.isLoggable()) {
          LogSupport.log("  search DB #" + i);
        }
        Map cmdMap = this.DB[i].getMailcapList(mimeType);
        if (cmdMap != null)
        {
          List v = (List)cmdMap.get("content-handler");
          if (v != null)
          {
            String name = (String)v.get(0);
            DataContentHandler dch = getDataContentHandler(name);
            if (dch != null) {
              return dch;
            }
          }
        }
      }
    }
    for (int i = 0; i < this.DB.length; i++) {
      if (this.DB[i] != null)
      {
        if (LogSupport.isLoggable()) {
          LogSupport.log("  search fallback DB #" + i);
        }
        Map cmdMap = this.DB[i].getMailcapFallbackList(mimeType);
        if (cmdMap != null)
        {
          List v = (List)cmdMap.get("content-handler");
          if (v != null)
          {
            String name = (String)v.get(0);
            DataContentHandler dch = getDataContentHandler(name);
            if (dch != null) {
              return dch;
            }
          }
        }
      }
    }
    return null;
  }
  
  private DataContentHandler getDataContentHandler(String name)
  {
    if (LogSupport.isLoggable()) {
      LogSupport.log("    got content-handler");
    }
    if (LogSupport.isLoggable()) {
      LogSupport.log("      class " + name);
    }
    try
    {
      ClassLoader cld = null;
      
      cld = SecuritySupport.getContextClassLoader();
      if (cld == null) {
        cld = getClass().getClassLoader();
      }
      Class cl = null;
      try
      {
        cl = cld.loadClass(name);
      }
      catch (Exception ex)
      {
        cl = Class.forName(name);
      }
      if (cl != null) {
        return (DataContentHandler)cl.newInstance();
      }
    }
    catch (IllegalAccessException e)
    {
      if (LogSupport.isLoggable()) {
        LogSupport.log("Can't load DCH " + name, e);
      }
    }
    catch (ClassNotFoundException e)
    {
      if (LogSupport.isLoggable()) {
        LogSupport.log("Can't load DCH " + name, e);
      }
    }
    catch (InstantiationException e)
    {
      if (LogSupport.isLoggable()) {
        LogSupport.log("Can't load DCH " + name, e);
      }
    }
    return null;
  }
  
  public synchronized String[] getMimeTypes()
  {
    List mtList = new ArrayList();
    for (int i = 0; i < this.DB.length; i++) {
      if (this.DB[i] != null)
      {
        String[] ts = this.DB[i].getMimeTypes();
        if (ts != null) {
          for (int j = 0; j < ts.length; j++) {
            if (!mtList.contains(ts[j])) {
              mtList.add(ts[j]);
            }
          }
        }
      }
    }
    String[] mts = new String[mtList.size()];
    mts = (String[])mtList.toArray(mts);
    
    return mts;
  }
  
  public synchronized String[] getNativeCommands(String mimeType)
  {
    List cmdList = new ArrayList();
    if (mimeType != null) {
      mimeType = mimeType.toLowerCase(Locale.ENGLISH);
    }
    for (int i = 0; i < this.DB.length; i++) {
      if (this.DB[i] != null)
      {
        String[] cmds = this.DB[i].getNativeCommands(mimeType);
        if (cmds != null) {
          for (int j = 0; j < cmds.length; j++) {
            if (!cmdList.contains(cmds[j])) {
              cmdList.add(cmds[j]);
            }
          }
        }
      }
    }
    String[] cmds = new String[cmdList.size()];
    cmds = (String[])cmdList.toArray(cmds);
    
    return cmds;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\activation\MailcapCommandMap.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */