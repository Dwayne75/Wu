package com.sun.codemodel.writer;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class PrologCodeWriter
  implements CodeWriter
{
  private final CodeWriter core;
  private final String prolog;
  
  public PrologCodeWriter(CodeWriter core, String prolog)
    throws IOException
  {
    this.core = core;
    this.prolog = prolog;
  }
  
  public OutputStream open(JPackage pkg, String fileName)
    throws IOException
  {
    OutputStream fos = this.core.open(pkg, fileName);
    
    PrintWriter out = new PrintWriter(fos);
    if ((this.prolog != null) && (fileName.endsWith(".java")))
    {
      out.println("//");
      
      String s = this.prolog;
      int idx;
      while ((idx = s.indexOf('\n')) != -1)
      {
        out.println("// " + s.substring(0, idx));
        s = s.substring(idx + 1);
      }
      out.println("//");
      out.println();
    }
    out.flush();
    
    return fos;
  }
  
  public void close()
    throws IOException
  {
    this.core.close();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\writer\PrologCodeWriter.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */