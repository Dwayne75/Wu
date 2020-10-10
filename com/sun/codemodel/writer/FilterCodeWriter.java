package com.sun.codemodel.writer;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public class FilterCodeWriter
  extends CodeWriter
{
  protected CodeWriter core;
  
  public FilterCodeWriter(CodeWriter core)
  {
    this.core = core;
  }
  
  public OutputStream openBinary(JPackage pkg, String fileName)
    throws IOException
  {
    return this.core.openBinary(pkg, fileName);
  }
  
  public Writer openSource(JPackage pkg, String fileName)
    throws IOException
  {
    return this.core.openSource(pkg, fileName);
  }
  
  public void close()
    throws IOException
  {
    this.core.close();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\writer\FilterCodeWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */