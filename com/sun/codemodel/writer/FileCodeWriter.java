package com.sun.codemodel.writer;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

public class FileCodeWriter
  extends CodeWriter
{
  private final File target;
  private final boolean readOnly;
  private final Set<File> readonlyFiles = new HashSet();
  
  public FileCodeWriter(File target)
    throws IOException
  {
    this(target, false);
  }
  
  public FileCodeWriter(File target, boolean readOnly)
    throws IOException
  {
    this.target = target;
    this.readOnly = readOnly;
    if ((!target.exists()) || (!target.isDirectory())) {
      throw new IOException(target + ": non-existent directory");
    }
  }
  
  public OutputStream openBinary(JPackage pkg, String fileName)
    throws IOException
  {
    return new FileOutputStream(getFile(pkg, fileName));
  }
  
  protected File getFile(JPackage pkg, String fileName)
    throws IOException
  {
    File dir;
    File dir;
    if (pkg.isUnnamed()) {
      dir = this.target;
    } else {
      dir = new File(this.target, toDirName(pkg));
    }
    if (!dir.exists()) {
      dir.mkdirs();
    }
    File fn = new File(dir, fileName);
    if ((fn.exists()) && 
      (!fn.delete())) {
      throw new IOException(fn + ": Can't delete previous version");
    }
    if (this.readOnly) {
      this.readonlyFiles.add(fn);
    }
    return fn;
  }
  
  public void close()
    throws IOException
  {
    for (File f : this.readonlyFiles) {
      f.setReadOnly();
    }
  }
  
  private static String toDirName(JPackage pkg)
  {
    return pkg.name().replace('.', File.separatorChar);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\writer\FileCodeWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */