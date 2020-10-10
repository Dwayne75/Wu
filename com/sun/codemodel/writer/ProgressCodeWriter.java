package com.sun.codemodel.writer;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;

public class ProgressCodeWriter
  extends FilterCodeWriter
{
  private final PrintStream progress;
  
  public ProgressCodeWriter(CodeWriter output, PrintStream progress)
  {
    super(output);
    this.progress = progress;
    if (progress == null) {
      throw new IllegalArgumentException();
    }
  }
  
  public OutputStream openBinary(JPackage pkg, String fileName)
    throws IOException
  {
    report(pkg, fileName);
    return super.openBinary(pkg, fileName);
  }
  
  public Writer openSource(JPackage pkg, String fileName)
    throws IOException
  {
    report(pkg, fileName);
    return super.openSource(pkg, fileName);
  }
  
  private void report(JPackage pkg, String fileName)
  {
    if (pkg.isUnnamed()) {
      this.progress.println(fileName);
    } else {
      this.progress.println(pkg.name().replace('.', File.separatorChar) + File.separatorChar + fileName);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\writer\ProgressCodeWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */