package com.sun.codemodel.writer;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipCodeWriter
  implements CodeWriter
{
  private final ZipOutputStream zip;
  private final OutputStream filter;
  
  public ZipCodeWriter(OutputStream target)
  {
    this.zip = new ZipOutputStream(target);
    
    this.filter = new ZipCodeWriter.1(this, this.zip);
  }
  
  public OutputStream open(JPackage pkg, String fileName)
    throws IOException
  {
    String name = fileName;
    if (!pkg.isUnnamed()) {
      name = toDirName(pkg) + name;
    }
    this.zip.putNextEntry(new ZipEntry(name));
    return this.filter;
  }
  
  private String toDirName(JPackage pkg)
  {
    return pkg.name().replace('.', '/') + '/';
  }
  
  public void close()
    throws IOException
  {
    this.zip.close();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\writer\ZipCodeWriter.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */