package com.sun.codemodel.writer;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class SingleStreamCodeWriter
  implements CodeWriter
{
  private final PrintStream out;
  
  public SingleStreamCodeWriter(OutputStream os)
  {
    this.out = new PrintStream(os);
  }
  
  public OutputStream open(JPackage pkg, String fileName)
    throws IOException
  {
    String pkgName = pkg.name();
    if (pkgName.length() != 0) {
      pkgName = pkgName + '.';
    }
    this.out.println("-----------------------------------" + pkgName + fileName + "-----------------------------------");
    
    return new SingleStreamCodeWriter.1(this, this.out);
  }
  
  public void close()
    throws IOException
  {
    this.out.close();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\writer\SingleStreamCodeWriter.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */