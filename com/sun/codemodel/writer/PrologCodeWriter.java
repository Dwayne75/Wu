package com.sun.codemodel.writer;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class PrologCodeWriter
  extends FilterCodeWriter
{
  private final String prolog;
  
  public PrologCodeWriter(CodeWriter core, String prolog)
  {
    super(core);
    this.prolog = prolog;
  }
  
  public Writer openSource(JPackage pkg, String fileName)
    throws IOException
  {
    Writer w = super.openSource(pkg, fileName);
    
    PrintWriter out = new PrintWriter(w);
    if (this.prolog != null)
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
    
    return w;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\writer\PrologCodeWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */