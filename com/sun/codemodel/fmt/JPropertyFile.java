package com.sun.codemodel.fmt;

import com.sun.codemodel.JResourceFile;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

public class JPropertyFile
  extends JResourceFile
{
  public JPropertyFile(String name)
  {
    super(name);
  }
  
  private final Properties data = new Properties();
  
  public void add(String key, String value)
  {
    this.data.put(key, value);
  }
  
  public void build(OutputStream out)
    throws IOException
  {
    this.data.store(out, null);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\fmt\JPropertyFile.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */