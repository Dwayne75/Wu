package com.sun.codemodel.writer;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipCodeWriter
  extends CodeWriter
{
  private final ZipOutputStream zip;
  private final OutputStream filter;
  
  public ZipCodeWriter(OutputStream target)
  {
    this.zip = new ZipOutputStream(target);
    
    this.filter = new FilterOutputStream(this.zip)
    {
      public void close() {}
    };
  }
  
  public OutputStream openBinary(JPackage pkg, String fileName)
    throws IOException
  {
    String name = fileName;
    if (!pkg.isUnnamed()) {
      name = toDirName(pkg) + name;
    }
    this.zip.putNextEntry(new ZipEntry(name));
    return this.filter;
  }
  
  private static String toDirName(JPackage pkg)
  {
    return pkg.name().replace('.', '/') + '/';
  }
  
  public void close()
    throws IOException
  {
    this.zip.close();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\writer\ZipCodeWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */