package com.sun.codemodel.writer;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class SingleStreamCodeWriter
  extends CodeWriter
{
  private final PrintStream out;
  
  public SingleStreamCodeWriter(OutputStream os)
  {
    this.out = new PrintStream(os);
  }
  
  public OutputStream openBinary(JPackage pkg, String fileName)
    throws IOException
  {
    String pkgName = pkg.name();
    if (pkgName.length() != 0) {
      pkgName = pkgName + '.';
    }
    this.out.println("-----------------------------------" + pkgName + fileName + "-----------------------------------");
    
    new FilterOutputStream(this.out)
    {
      public void close() {}
    };
  }
  
  public void close()
    throws IOException
  {
    this.out.close();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\writer\SingleStreamCodeWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */