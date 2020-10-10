package com.sun.tools.xjc;

import com.sun.istack.tools.ProtectedTask;
import java.io.IOException;
import org.apache.tools.ant.BuildException;

public class XJCTask
  extends ProtectedTask
{
  private String source = "2.0";
  
  public void setSource(String version)
  {
    if ((version.equals("1.0")) || (version.equals("2.0")))
    {
      this.source = version;
      return;
    }
    throw new BuildException("Illegal version " + version);
  }
  
  protected ClassLoader createClassLoader()
    throws ClassNotFoundException, IOException
  {
    return ClassLoaderBuilder.createProtectiveClassLoader(XJCTask.class.getClassLoader(), this.source);
  }
  
  protected String getCoreClassName()
  {
    if (this.source.equals("2.0")) {
      return "com.sun.tools.xjc.XJC2Task";
    }
    return "com.sun.tools.xjc.XJCTask";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\XJCTask.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */