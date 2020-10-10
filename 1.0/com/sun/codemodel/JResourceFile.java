package com.sun.codemodel;

import java.io.IOException;
import java.io.OutputStream;

public abstract class JResourceFile
{
  private final String name;
  
  protected JResourceFile(String name)
  {
    this.name = name;
  }
  
  public String name()
  {
    return this.name;
  }
  
  protected abstract void build(OutputStream paramOutputStream)
    throws IOException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JResourceFile.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */