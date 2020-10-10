package com.sun.tools.xjc.api.util;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.apt.Filer.Location;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public final class FilerCodeWriter
  extends CodeWriter
{
  private final Filer filer;
  
  public FilerCodeWriter(Filer filer)
  {
    this.filer = filer;
  }
  
  public OutputStream openBinary(JPackage pkg, String fileName)
    throws IOException
  {
    Filer.Location loc;
    Filer.Location loc;
    if (fileName.endsWith(".java")) {
      loc = Filer.Location.SOURCE_TREE;
    } else {
      loc = Filer.Location.CLASS_TREE;
    }
    return this.filer.createBinaryFile(loc, pkg.name(), new File(fileName));
  }
  
  public Writer openSource(JPackage pkg, String fileName)
    throws IOException
  {
    String name;
    if (pkg.isUnnamed()) {
      name = fileName;
    } else {
      name = pkg.name() + '.' + fileName;
    }
    String name = name.substring(0, name.length() - 5);
    
    return this.filer.createSourceFile(name);
  }
  
  public void close() {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\api\util\FilerCodeWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */