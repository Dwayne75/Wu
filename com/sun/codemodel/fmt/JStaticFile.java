package com.sun.codemodel.fmt;

import com.sun.codemodel.JResourceFile;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class JStaticFile
  extends JResourceFile
{
  private final ClassLoader classLoader;
  private final String resourceName;
  private final boolean isResource;
  
  public JStaticFile(String _resourceName)
  {
    this(_resourceName, !_resourceName.endsWith(".java"));
  }
  
  public JStaticFile(String _resourceName, boolean isResource)
  {
    this(JStaticFile.class.getClassLoader(), _resourceName, isResource);
  }
  
  public JStaticFile(ClassLoader _classLoader, String _resourceName, boolean isResource)
  {
    super(_resourceName.substring(_resourceName.lastIndexOf('/') + 1));
    this.classLoader = _classLoader;
    this.resourceName = _resourceName;
    this.isResource = isResource;
  }
  
  protected boolean isResource()
  {
    return this.isResource;
  }
  
  protected void build(OutputStream os)
    throws IOException
  {
    DataInputStream dis = new DataInputStream(this.classLoader.getResourceAsStream(this.resourceName));
    
    byte[] buf = new byte['Ā'];
    int sz;
    while ((sz = dis.read(buf)) > 0) {
      os.write(buf, 0, sz);
    }
    dis.close();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\fmt\JStaticFile.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */