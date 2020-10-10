package com.sun.codemodel;

import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.codemodel.writer.ProgressCodeWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public final class JCodeModel
{
  private HashMap packages = new HashMap();
  private final HashMap refClasses = new HashMap();
  public final JNullType NULL = new JNullType(this);
  public final JPrimitiveType VOID = new JPrimitiveType(this, "void", Void.class);
  public final JPrimitiveType BOOLEAN = new JPrimitiveType(this, "boolean", Boolean.class);
  public final JPrimitiveType BYTE = new JPrimitiveType(this, "byte", Byte.class);
  public final JPrimitiveType SHORT = new JPrimitiveType(this, "short", Short.class);
  public final JPrimitiveType CHAR = new JPrimitiveType(this, "char", Character.class);
  public final JPrimitiveType INT = new JPrimitiveType(this, "int", Integer.class);
  public final JPrimitiveType FLOAT = new JPrimitiveType(this, "float", Float.class);
  public final JPrimitiveType LONG = new JPrimitiveType(this, "long", Long.class);
  public final JPrimitiveType DOUBLE = new JPrimitiveType(this, "double", Double.class);
  protected static final boolean isCaseSensitiveFileSystem = ;
  
  private static boolean getFileSystemCaseSensitivity()
  {
    try
    {
      if (System.getProperty("com.sun.codemodel.FileSystemCaseSensitive") != null) {
        return true;
      }
    }
    catch (Exception e) {}
    return File.separatorChar == '/';
  }
  
  public JPackage _package(String name)
  {
    JPackage p = (JPackage)this.packages.get(name);
    if (p == null)
    {
      p = new JPackage(name, this);
      this.packages.put(name, p);
    }
    return p;
  }
  
  public final JPackage rootPackage()
  {
    return _package("");
  }
  
  public Iterator packages()
  {
    return this.packages.values().iterator();
  }
  
  public JDefinedClass _class(String fullyqualifiedName)
    throws JClassAlreadyExistsException
  {
    int idx = fullyqualifiedName.lastIndexOf('.');
    if (idx < 0) {
      return rootPackage()._class(fullyqualifiedName);
    }
    return _package(fullyqualifiedName.substring(0, idx))._class(fullyqualifiedName.substring(idx + 1));
  }
  
  public JDefinedClass _getClass(String fullyQualifiedName)
  {
    int idx = fullyQualifiedName.lastIndexOf('.');
    if (idx < 0) {
      return rootPackage()._getClass(fullyQualifiedName);
    }
    return _package(fullyQualifiedName.substring(0, idx))._getClass(fullyQualifiedName.substring(idx + 1));
  }
  
  public JDefinedClass newAnonymousClass(JClass baseType)
  {
    return new JAnonymousClass(baseType, this);
  }
  
  public void build(File destDir, PrintStream status)
    throws IOException
  {
    CodeWriter out = new FileCodeWriter(destDir);
    if (status != null) {
      out = new ProgressCodeWriter(out, status);
    }
    build(out);
  }
  
  public void build(File destDir)
    throws IOException
  {
    build(destDir, System.out);
  }
  
  public void build(CodeWriter out)
    throws IOException
  {
    for (Iterator i = this.packages.values().iterator(); i.hasNext();) {
      ((JPackage)i.next()).build(out);
    }
    out.close();
  }
  
  public JClass ref(Class clazz)
  {
    JCodeModel.JReferencedClass jrc = (JCodeModel.JReferencedClass)this.refClasses.get(clazz);
    if (jrc == null)
    {
      if (clazz.isArray()) {
        return new JArrayClass(this, ref(clazz.getComponentType()));
      }
      jrc = new JCodeModel.JReferencedClass(this, clazz);
      this.refClasses.put(clazz, jrc);
    }
    return jrc;
  }
  
  public JClass ref(String fullyQualifiedClassName)
    throws ClassNotFoundException
  {
    try
    {
      return ref(Thread.currentThread().getContextClassLoader().loadClass(fullyQualifiedClassName));
    }
    catch (ClassNotFoundException e) {}
    return ref(Class.forName(fullyQualifiedClassName));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JCodeModel.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */