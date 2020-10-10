package javax.activation;

import com.sun.activation.registries.LogSupport;
import com.sun.activation.registries.MimeTypeFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Vector;

public class MimetypesFileTypeMap
  extends FileTypeMap
{
  private static MimeTypeFile defDB = null;
  private MimeTypeFile[] DB;
  private static final int PROG = 0;
  private static String defaultType = "application/octet-stream";
  
  public MimetypesFileTypeMap()
  {
    Vector dbv = new Vector(5);
    MimeTypeFile mf = null;
    dbv.addElement(null);
    
    LogSupport.log("MimetypesFileTypeMap: load HOME");
    try
    {
      String user_home = System.getProperty("user.home");
      if (user_home != null)
      {
        String path = user_home + File.separator + ".mime.types";
        mf = loadFile(path);
        if (mf != null) {
          dbv.addElement(mf);
        }
      }
    }
    catch (SecurityException ex) {}
    LogSupport.log("MimetypesFileTypeMap: load SYS");
    try
    {
      String system_mimetypes = System.getProperty("java.home") + File.separator + "lib" + File.separator + "mime.types";
      
      mf = loadFile(system_mimetypes);
      if (mf != null) {
        dbv.addElement(mf);
      }
    }
    catch (SecurityException ex) {}
    LogSupport.log("MimetypesFileTypeMap: load JAR");
    
    loadAllResources(dbv, "META-INF/mime.types");
    
    LogSupport.log("MimetypesFileTypeMap: load DEF");
    synchronized (MimetypesFileTypeMap.class)
    {
      if (defDB == null) {
        defDB = loadResource("/META-INF/mimetypes.default");
      }
    }
    if (defDB != null) {
      dbv.addElement(defDB);
    }
    this.DB = new MimeTypeFile[dbv.size()];
    dbv.copyInto(this.DB);
  }
  
  private MimeTypeFile loadResource(String name)
  {
    InputStream clis = null;
    try
    {
      clis = SecuritySupport.getResourceAsStream(getClass(), name);
      if (clis != null)
      {
        MimeTypeFile mf = new MimeTypeFile(clis);
        if (LogSupport.isLoggable()) {
          LogSupport.log("MimetypesFileTypeMap: successfully loaded mime types file: " + name);
        }
        return mf;
      }
      if (LogSupport.isLoggable()) {
        LogSupport.log("MimetypesFileTypeMap: not loading mime types file: " + name);
      }
      return null;
    }
    catch (IOException e)
    {
      if (LogSupport.isLoggable()) {
        LogSupport.log("MimetypesFileTypeMap: can't load " + name, e);
      }
    }
    catch (SecurityException sex)
    {
      if (LogSupport.isLoggable()) {
        LogSupport.log("MimetypesFileTypeMap: can't load " + name, sex);
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
  
  private void loadAllResources(Vector v, String name)
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
          LogSupport.log("MimetypesFileTypeMap: getResources");
        }
        for (i = 0; i < urls.length;)
        {
          URL url = urls[i];
          InputStream clis = null;
          if (LogSupport.isLoggable()) {
            LogSupport.log("MimetypesFileTypeMap: URL " + url);
          }
          try
          {
            clis = SecuritySupport.openStream(url);
            if (clis != null)
            {
              v.addElement(new MimeTypeFile(clis));
              anyLoaded = true;
              if (LogSupport.isLoggable()) {
                LogSupport.log("MimetypesFileTypeMap: successfully loaded mime types from URL: " + url);
              }
            }
            else if (LogSupport.isLoggable())
            {
              LogSupport.log("MimetypesFileTypeMap: not loading mime types from URL: " + url);
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
              LogSupport.log("MimetypesFileTypeMap: can't load " + url, ioex);
            }
          }
          catch (SecurityException sex)
          {
            if (LogSupport.isLoggable()) {
              LogSupport.log("MimetypesFileTypeMap: can't load " + url, sex);
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
        LogSupport.log("MimetypesFileTypeMap: can't load " + name, ex);
      }
    }
    if (!anyLoaded)
    {
      LogSupport.log("MimetypesFileTypeMap: !anyLoaded");
      MimeTypeFile mf = loadResource("/" + name);
      if (mf != null) {
        v.addElement(mf);
      }
    }
  }
  
  private MimeTypeFile loadFile(String name)
  {
    MimeTypeFile mtf = null;
    try
    {
      mtf = new MimeTypeFile(name);
    }
    catch (IOException e) {}
    return mtf;
  }
  
  public MimetypesFileTypeMap(String mimeTypeFileName)
    throws IOException
  {
    this();
    this.DB[0] = new MimeTypeFile(mimeTypeFileName);
  }
  
  public MimetypesFileTypeMap(InputStream is)
  {
    this();
    try
    {
      this.DB[0] = new MimeTypeFile(is);
    }
    catch (IOException ex) {}
  }
  
  public synchronized void addMimeTypes(String mime_types)
  {
    if (this.DB[0] == null) {
      this.DB[0] = new MimeTypeFile();
    }
    this.DB[0].appendToRegistry(mime_types);
  }
  
  public String getContentType(File f)
  {
    return getContentType(f.getName());
  }
  
  public synchronized String getContentType(String filename)
  {
    int dot_pos = filename.lastIndexOf(".");
    if (dot_pos < 0) {
      return defaultType;
    }
    String file_ext = filename.substring(dot_pos + 1);
    if (file_ext.length() == 0) {
      return defaultType;
    }
    for (int i = 0; i < this.DB.length; i++) {
      if (this.DB[i] != null)
      {
        String result = this.DB[i].getMIMETypeString(file_ext);
        if (result != null) {
          return result;
        }
      }
    }
    return defaultType;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\activation\MimetypesFileTypeMap.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */