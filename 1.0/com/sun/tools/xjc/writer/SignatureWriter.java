package com.sun.tools.xjc.writer;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.grammar.util.Multiplicity;
import java.io.IOException;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class SignatureWriter
{
  private final ClassItem[] classes;
  
  public static void write(AnnotatedGrammar grammar, Writer out)
    throws IOException
  {
    new SignatureWriter(grammar, out).dump();
  }
  
  private SignatureWriter(AnnotatedGrammar grammar, Writer out)
  {
    this.out = out;
    this.classes = grammar.getClasses();
    for (int i = 0; i < this.classes.length; i++) {
      this.classSet.put(this.classes[i].getTypeAsDefined(), this.classes[i]);
    }
  }
  
  private final Hashtable classSet = new Hashtable();
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
    Set packages = new TreeSet(new SignatureWriter.1(this));
    for (int i = 0; i < this.classes.length; i++)
    {
      JDefinedClass cls = this.classes[i].getTypeAsDefined();
      packages.add(cls._package());
    }
    for (Iterator itr = packages.iterator(); itr.hasNext();) {
      dump((JPackage)itr.next());
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
      ClassItem ci = (ClassItem)this.classSet.get(cls);
      if (ci != null) {
        dump(ci);
      }
    }
  }
  
  private void dump(ClassItem ci)
    throws IOException
  {
    JDefinedClass cls = ci.getTypeAsDefined();
    
    StringBuffer buf = new StringBuffer();
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
    
    FieldUse[] fu = ci.getDeclaredFieldUses();
    for (int i = 0; i < fu.length; i++)
    {
      String type;
      String type;
      if (!fu[i].multiplicity.isAtMostOnce()) {
        type = "List<" + printName(fu[i].type) + ">";
      } else {
        type = printName(fu[i].type);
      }
      println(type + " " + fu[i].name + ";");
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\writer\SignatureWriter.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */