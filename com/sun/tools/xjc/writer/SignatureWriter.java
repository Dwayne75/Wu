package com.sun.tools.xjc.writer;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.outline.PackageOutline;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class SignatureWriter
{
  private final Collection<? extends ClassOutline> classes;
  
  public static void write(Outline model, Writer out)
    throws IOException
  {
    new SignatureWriter(model, out).dump();
  }
  
  private SignatureWriter(Outline model, Writer out)
  {
    this.out = out;
    this.classes = model.getClasses();
    for (ClassOutline ci : this.classes) {
      this.classSet.put(ci.ref, ci);
    }
  }
  
  private final Map<JDefinedClass, ClassOutline> classSet = new HashMap();
  private final Writer out;
  private int indent = 0;
  
  private void printIndent()
    throws IOException
  {
    for (int i = 0; i < this.indent; i++) {
      this.out.write("  ");
    }
  }
  
  private void println(String s)
    throws IOException
  {
    printIndent();
    this.out.write(s);
    this.out.write(10);
  }
  
  private void dump()
    throws IOException
  {
    Set<JPackage> packages = new TreeSet(new Comparator()
    {
      public int compare(JPackage lhs, JPackage rhs)
      {
        return lhs.name().compareTo(rhs.name());
      }
    });
    for (ClassOutline ci : this.classes) {
      packages.add(ci._package()._package());
    }
    for (JPackage pkg : packages) {
      dump(pkg);
    }
    this.out.flush();
  }
  
  private void dump(JPackage pkg)
    throws IOException
  {
    println("package " + pkg.name() + " {");
    this.indent += 1;
    dumpChildren(pkg);
    this.indent -= 1;
    println("}");
  }
  
  private void dumpChildren(JClassContainer cont)
    throws IOException
  {
    Iterator itr = cont.classes();
    while (itr.hasNext())
    {
      JDefinedClass cls = (JDefinedClass)itr.next();
      ClassOutline ci = (ClassOutline)this.classSet.get(cls);
      if (ci != null) {
        dump(ci);
      }
    }
  }
  
  private void dump(ClassOutline ci)
    throws IOException
  {
    JDefinedClass cls = ci.implClass;
    
    StringBuilder buf = new StringBuilder();
    buf.append("interface ");
    buf.append(cls.name());
    
    boolean first = true;
    Iterator itr = cls._implements();
    while (itr.hasNext())
    {
      if (first)
      {
        buf.append(" extends ");
        first = false;
      }
      else
      {
        buf.append(", ");
      }
      buf.append(printName((JClass)itr.next()));
    }
    buf.append(" {");
    println(buf.toString());
    this.indent += 1;
    for (FieldOutline fo : ci.getDeclaredFields())
    {
      String type = printName(fo.getRawType());
      println(type + ' ' + fo.getPropertyInfo().getName(true) + ';');
    }
    dumpChildren(cls);
    
    this.indent -= 1;
    println("}");
  }
  
  private String printName(JType t)
  {
    String name = t.fullName();
    if (name.startsWith("java.lang.")) {
      name = name.substring(10);
    }
    return name;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\writer\SignatureWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */