package com.sun.codemodel.writer;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class ProgressCodeWriter
  implements CodeWriter
{
  private final CodeWriter output;
  private final PrintStream progress;
  
  public ProgressCodeWriter(CodeWriter output, PrintStream progress)
  {
    this.output = output;
    this.progress = progress;
    if (progress == null) {
      throw new IllegalArgumentException();
    }
  }
  
  public OutputStream open(JPackage pkg, String fileName)
    throws IOException
  {
    if (pkg.isUnnamed()) {
      this.progress.println(fileName);
    } else {
      this.progress.println(pkg.name().replace('.', File.separatorChar) + File.separatorChar + fileName);
    }
    return this.output.open(pkg, fileName);
  }
  
  public void close()
    throws IOException
  {
    this.output.close();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\writer\ProgressCodeWriter.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */