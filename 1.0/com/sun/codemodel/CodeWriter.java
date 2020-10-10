package com.sun.codemodel;

import java.io.IOException;
import java.io.OutputStream;

public abstract interface CodeWriter
{
  public abstract OutputStream open(JPackage paramJPackage, String paramString)
    throws IOException;
  
  public abstract void close()
    throws IOException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\CodeWriter.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */