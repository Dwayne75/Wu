package com.sun.codemodel;

import com.sun.codemodel.util.UnicodeEscapeWriter;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class JPackage
  implements JDeclaration, JGenerable, JClassContainer
{
  private String name;
  private final JCodeModel owner;
  private final Map classes = new TreeMap();
  private final Set resources = new HashSet();
  private final Map upperCaseClassMap;
  
  JPackage(String name, JCodeModel cw)
  {
    this.owner = cw;
    if (name.equals("."))
    {
      String msg = "JPackage name . is not allowed";
      throw new IllegalArgumentException(msg);
    }
    int dots = 1;
    for (int i = 0; i < name.length(); i++)
    {
      char c = name.charAt(i);
      if (c == '.')
      {
        dots++;
      }
      else
      {
        if (dots > 1)
        {
          String msg = "JPackage name " + name + " missing identifier";
          throw new IllegalArgumentException(msg);
        }
        if ((dots == 1) && (!Character.isJavaIdentifierStart(c)))
        {
          String msg = "JPackage name " + name + " contains illegal " + "character for beginning of identifier: " + c;
          
          throw new IllegalArgumentException(msg);
        }
        if (!Character.isJavaIdentifierPart(c))
        {
          String msg = "JPackage name " + name + "contains illegal " + "character: " + c;
          throw new IllegalArgumentException(msg);
        }
        dots = 0;
      }
    }
    if ((!name.trim().equals("")) && (dots != 0))
    {
      String msg = "JPackage name not allowed to end with .";
      throw new IllegalArgumentException(msg);
    }
    if (JCodeModel.isCaseSensitiveFileSystem) {
      this.upperCaseClassMap = null;
    } else {
      this.upperCaseClassMap = new HashMap();
    }
    this.name = name;
  }
  
  public JClassContainer parentContainer()
  {
    return parent();
  }
  
  public JPackage parent()
  {
    if (this.name.length() == 0) {
      return null;
    }
    int idx = this.name.lastIndexOf('.');
    return this.owner._package(this.name.substring(0, idx));
  }
  
  public JDefinedClass _class(int mods, String name)
    throws JClassAlreadyExistsException
  {
    return _class(mods, name, false);
  }
  
  public JDefinedClass _class(int mods, String name, boolean isInterface)
    throws JClassAlreadyExistsException
  {
    if (this.classes.containsKey(name)) {
      throw new JClassAlreadyExistsException((JDefinedClass)this.classes.get(name));
    }
    JDefinedClass c = new JPackageMemberClass(this, mods, name, isInterface);
    if (this.upperCaseClassMap != null)
    {
      JDefinedClass dc = (JDefinedClass)this.upperCaseClassMap.get(name.toUpperCase());
      if (dc != null) {
        throw new JClassAlreadyExistsException(dc);
      }
      this.upperCaseClassMap.put(name.toUpperCase(), c);
    }
    this.classes.put(name, c);
    return c;
  }
  
  public JDefinedClass _class(String name)
    throws JClassAlreadyExistsException
  {
    return _class(1, name);
  }
  
  public JDefinedClass _getClass(String name)
  {
    if (this.classes.containsKey(name)) {
      return (JDefinedClass)this.classes.get(name);
    }
    return null;
  }
  
  public JDefinedClass _interface(int mods, String name)
    throws JClassAlreadyExistsException
  {
    return _class(mods, name, true);
  }
  
  public JDefinedClass _interface(String name)
    throws JClassAlreadyExistsException
  {
    return _interface(1, name);
  }
  
  public JResourceFile addResourceFile(JResourceFile rsrc)
  {
    this.resources.add(rsrc);
    return rsrc;
  }
  
  public boolean hasResourceFile(String name)
  {
    for (Iterator itr = this.resources.iterator(); itr.hasNext();)
    {
      JResourceFile r = (JResourceFile)itr.next();
      if (r.name().equals(name)) {
        return true;
      }
    }
    return false;
  }
  
  public Iterator propertyFiles()
  {
    return this.resources.iterator();
  }
  
  public void remove(JClass c)
  {
    if (c._package() != this) {
      throw new IllegalArgumentException("the specified class is not a member of this package, or it is a referenced class");
    }
    this.classes.remove(c.name());
    if (this.upperCaseClassMap != null) {
      this.upperCaseClassMap.remove(c.name().toUpperCase());
    }
  }
  
  public JClass ref(String name)
  {
    if (name.indexOf('.') >= 0) {
      throw new IllegalArgumentException("JClass name contains '.': " + name);
    }
    String n = "";
    if (!isUnnamed()) {
      n = this.name + ".";
    }
    n = n + name;
    try
    {
      return this.owner.ref(Class.forName(n));
    }
    catch (ClassNotFoundException e)
    {
      throw new NoClassDefFoundError(e.toString());
    }
  }
  
  public JPackage subPackage(String pkg)
  {
    if (isUnnamed()) {
      return owner()._package(pkg);
    }
    return owner()._package(this.name + "." + pkg);
  }
  
  public Iterator classes()
  {
    return this.classes.values().iterator();
  }
  
  public boolean isDefined(String classLocalName)
  {
    Iterator itr = classes();
    while (itr.hasNext()) {
      if (((JClass)itr.next()).name().equals(classLocalName)) {
        return true;
      }
    }
    return false;
  }
  
  public final boolean isUnnamed()
  {
    return this.name.length() == 0;
  }
  
  public String name()
  {
    return this.name;
  }
  
  public final JCodeModel owner()
  {
    return this.owner;
  }
  
  File toPath(File dir)
  {
    if (this.name == null) {
      return dir;
    }
    return new File(dir, this.name.replace('.', File.separatorChar));
  }
  
  public void declare(JFormatter f)
  {
    if (this.name.length() != 0) {
      f.p("package").p(this.name).p(';').nl();
    }
  }
  
  public void generate(JFormatter f)
  {
    f.p(this.name);
  }
  
  void build(CodeWriter out)
    throws IOException
  {
    for (Iterator i = this.classes.values().iterator(); i.hasNext();)
    {
      JPackageMemberClass c = (JPackageMemberClass)i.next();
      if (!c.isHidden())
      {
        Writer bw = new BufferedWriter(new OutputStreamWriter(out.open(this, c.name() + ".java")));
        try
        {
          bw = new JPackage.1(this, bw);
        }
        catch (Throwable t)
        {
          bw = new UnicodeEscapeWriter(bw);
        }
        JFormatter f = new JFormatter(new PrintWriter(bw));
        c.declare(f);
        f.close();
      }
    }
    for (Iterator i = this.resources.iterator(); i.hasNext();)
    {
      JResourceFile rsrc = (JResourceFile)i.next();
      
      OutputStream os = new BufferedOutputStream(out.open(this, rsrc.name()));
      rsrc.build(os);
      os.close();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JPackage.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */