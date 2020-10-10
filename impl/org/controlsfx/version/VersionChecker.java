package impl.org.controlsfx.version;

import com.sun.javafx.runtime.VersionInfo;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class VersionChecker
{
  private static final Package controlsFX = VersionChecker.class.getPackage();
  private static final String javaFXVersion = VersionInfo.getVersion();
  private static final String controlsFXSpecTitle = getControlsFXSpecificationTitle();
  private static final String controlsFXSpecVersion = getControlsFXSpecificationVersion();
  private static final String controlsFXImpVersion = getControlsFXImplementationVersion();
  private static Properties props;
  
  public static void doVersionCheck()
  {
    if (controlsFXSpecVersion == null) {
      return;
    }
    Comparable[] splitSpecVersion = toComparable(controlsFXSpecVersion.split("\\."));
    
    Comparable[] splitJavaVersion = toComparable(javaFXVersion.replace('-', '.').split("\\."));
    
    boolean notSupportedVersion = false;
    if (splitSpecVersion[0].compareTo(splitJavaVersion[0]) > 0) {
      notSupportedVersion = true;
    } else if (splitSpecVersion[0].compareTo(splitJavaVersion[0]) == 0) {
      if (splitSpecVersion[1].compareTo(splitJavaVersion[2]) > 0) {
        notSupportedVersion = true;
      }
    }
    if (notSupportedVersion) {
      throw new RuntimeException("ControlsFX Error: ControlsFX " + controlsFXImpVersion + " requires at least " + controlsFXSpecTitle);
    }
  }
  
  private static Comparable<Comparable>[] toComparable(String[] tokens)
  {
    Comparable[] ret = new Comparable[tokens.length];
    for (int i = 0; i < tokens.length; i++)
    {
      String token = tokens[i];
      try
      {
        ret[i] = new Integer(token);
      }
      catch (NumberFormatException e)
      {
        ret[i] = token;
      }
    }
    return ret;
  }
  
  private static String getControlsFXSpecificationTitle()
  {
    try
    {
      return controlsFX.getSpecificationTitle();
    }
    catch (NullPointerException localNullPointerException) {}
    return getPropertyValue("controlsfx_specification_title");
  }
  
  private static String getControlsFXSpecificationVersion()
  {
    try
    {
      return controlsFX.getSpecificationVersion();
    }
    catch (NullPointerException localNullPointerException) {}
    return getPropertyValue("controlsfx_specification_title");
  }
  
  private static String getControlsFXImplementationVersion()
  {
    try
    {
      return controlsFX.getImplementationVersion();
    }
    catch (NullPointerException localNullPointerException) {}
    return 
      getPropertyValue("controlsfx_specification_title") + getPropertyValue("artifact_suffix");
  }
  
  private static synchronized String getPropertyValue(String key)
  {
    if (props == null) {
      try
      {
        File file = new File("../controlsfx-build.properties");
        if (file.exists()) {
          props.load(new FileReader(file));
        }
      }
      catch (IOException localIOException) {}
    }
    return props.getProperty(key);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\impl\org\controlsfx\version\VersionChecker.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */