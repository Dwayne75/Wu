package com.sun.codemodel;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public final class JFormatter
{
  private HashMap<String, ReferenceList> collectedReferences;
  private HashSet<JClass> importedClasses;
  
  private static enum Mode
  {
    COLLECTING,  PRINTING;
    
    private Mode() {}
  }
  
  private Mode mode = Mode.PRINTING;
  private int indentLevel;
  private final String indentSpace;
  private final PrintWriter pw;
  
  public JFormatter(PrintWriter s, String space)
  {
    this.pw = s;
    this.indentSpace = space;
    this.collectedReferences = new HashMap();
    
    this.importedClasses = new HashSet();
  }
  
  public JFormatter(PrintWriter s)
  {
    this(s, "    ");
  }
  
  public JFormatter(Writer w)
  {
    this(new PrintWriter(w));
  }
  
  public void close()
  {
    this.pw.close();
  }
  
  public boolean isPrinting()
  {
    return this.mode == Mode.PRINTING;
  }
  
  public JFormatter o()
  {
    this.indentLevel -= 1;
    return this;
  }
  
  public JFormatter i()
  {
    this.indentLevel += 1;
    return this;
  }
  
  private boolean needSpace(char c1, char c2)
  {
    if ((c1 == ']') && (c2 == '{')) {
      return true;
    }
    if (c1 == ';') {
      return true;
    }
    if (c1 == 65535)
    {
      if (c2 == '(') {
        return false;
      }
      return true;
    }
    if ((c1 == ')') && (c2 == '{')) {
      return true;
    }
    if ((c1 == ',') || (c1 == '=')) {
      return true;
    }
    if (c2 == '=') {
      return true;
    }
    if (Character.isDigit(c1))
    {
      if ((c2 == '(') || (c2 == ')') || (c2 == ';') || (c2 == ',')) {
        return false;
      }
      return true;
    }
    if (Character.isJavaIdentifierPart(c1))
    {
      switch (c2)
      {
      case '+': 
      case '>': 
      case '@': 
      case '{': 
      case '}': 
        return true;
      }
      return Character.isJavaIdentifierStart(c2);
    }
    if (Character.isJavaIdentifierStart(c2))
    {
      switch (c1)
      {
      case ')': 
      case '+': 
      case ']': 
      case '}': 
        return true;
      }
      return false;
    }
    if (Character.isDigit(c2))
    {
      if (c1 == '(') {
        return false;
      }
      return true;
    }
    return false;
  }
  
  private char lastChar = '\000';
  private boolean atBeginningOfLine = true;
  private JPackage javaLang;
  static final char CLOSE_TYPE_ARGS = '￿';
  
  private void spaceIfNeeded(char c)
  {
    if (this.atBeginningOfLine)
    {
      for (int i = 0; i < this.indentLevel; i++) {
        this.pw.print(this.indentSpace);
      }
      this.atBeginningOfLine = false;
    }
    else if ((this.lastChar != 0) && (needSpace(this.lastChar, c)))
    {
      this.pw.print(' ');
    }
  }
  
  public JFormatter p(char c)
  {
    if (this.mode == Mode.PRINTING)
    {
      if (c == 65535)
      {
        this.pw.print('>');
      }
      else
      {
        spaceIfNeeded(c);
        this.pw.print(c);
      }
      this.lastChar = c;
    }
    return this;
  }
  
  public JFormatter p(String s)
  {
    if (this.mode == Mode.PRINTING)
    {
      spaceIfNeeded(s.charAt(0));
      this.pw.print(s);
      this.lastChar = s.charAt(s.length() - 1);
    }
    return this;
  }
  
  public JFormatter t(JType type)
  {
    if (type.isReference()) {
      return t((JClass)type);
    }
    return g(type);
  }
  
  public JFormatter t(JClass type)
  {
    switch (this.mode)
    {
    case PRINTING: 
      if (this.importedClasses.contains(type)) {
        p(type.name());
      } else if (type.outer() != null) {
        t(type.outer()).p('.').p(type.name());
      } else {
        p(type.fullName());
      }
      break;
    case COLLECTING: 
      String shortName = type.name();
      if (this.collectedReferences.containsKey(shortName))
      {
        ((ReferenceList)this.collectedReferences.get(shortName)).add(type);
      }
      else
      {
        ReferenceList tl = new ReferenceList();
        tl.add(type);
        this.collectedReferences.put(shortName, tl);
      }
      break;
    }
    return this;
  }
  
  public JFormatter id(String id)
  {
    switch (this.mode)
    {
    case PRINTING: 
      p(id);
      break;
    case COLLECTING: 
      if (this.collectedReferences.containsKey(id))
      {
        if (!((ReferenceList)this.collectedReferences.get(id)).getClasses().isEmpty()) {
          for (JClass type : ((ReferenceList)this.collectedReferences.get(id)).getClasses()) {
            if (type.outer() != null)
            {
              ((ReferenceList)this.collectedReferences.get(id)).setId(false);
              return this;
            }
          }
        }
        ((ReferenceList)this.collectedReferences.get(id)).setId(true);
      }
      else
      {
        ReferenceList tl = new ReferenceList();
        tl.setId(true);
        this.collectedReferences.put(id, tl);
      }
      break;
    }
    return this;
  }
  
  public JFormatter nl()
  {
    if (this.mode == Mode.PRINTING)
    {
      this.pw.println();
      this.lastChar = '\000';
      this.atBeginningOfLine = true;
    }
    return this;
  }
  
  public JFormatter g(JGenerable g)
  {
    g.generate(this);
    return this;
  }
  
  public JFormatter g(Collection<? extends JGenerable> list)
  {
    boolean first = true;
    if (!list.isEmpty()) {
      for (JGenerable item : list)
      {
        if (!first) {
          p(',');
        }
        g(item);
        first = false;
      }
    }
    return this;
  }
  
  public JFormatter d(JDeclaration d)
  {
    d.declare(this);
    return this;
  }
  
  public JFormatter s(JStatement s)
  {
    s.state(this);
    return this;
  }
  
  public JFormatter b(JVar v)
  {
    v.bind(this);
    return this;
  }
  
  void write(JDefinedClass c)
  {
    this.mode = Mode.COLLECTING;
    d(c);
    
    this.javaLang = c.owner()._package("java.lang");
    for (ReferenceList tl : this.collectedReferences.values()) {
      if ((!tl.collisions(c)) && (!tl.isId()))
      {
        assert (tl.getClasses().size() == 1);
        
        this.importedClasses.add(tl.getClasses().get(0));
      }
    }
    this.importedClasses.add(c);
    
    this.mode = Mode.PRINTING;
    
    assert (c.parentContainer().isPackage()) : "this method is only for a pacakge-level class";
    JPackage pkg = (JPackage)c.parentContainer();
    if (!pkg.isUnnamed())
    {
      nl().d(pkg);
      nl();
    }
    JClass[] imports = (JClass[])this.importedClasses.toArray(new JClass[this.importedClasses.size()]);
    Arrays.sort(imports);
    for (JClass clazz : imports) {
      if (!supressImport(clazz, c)) {
        p("import").p(clazz.fullName()).p(';').nl();
      }
    }
    nl();
    
    d(c);
  }
  
  private boolean supressImport(JClass clazz, JClass c)
  {
    if (clazz._package().isUnnamed()) {
      return true;
    }
    String packageName = clazz._package().name();
    if (packageName.equals("java.lang")) {
      return true;
    }
    if (clazz._package() == c._package()) {
      if (clazz.outer() == null) {
        return true;
      }
    }
    return false;
  }
  
  final class ReferenceList
  {
    private final ArrayList<JClass> classes = new ArrayList();
    private boolean id;
    
    ReferenceList() {}
    
    public boolean collisions(JDefinedClass enclosingClass)
    {
      if (this.classes.size() > 1) {
        return true;
      }
      if ((this.id) && (this.classes.size() != 0)) {
        return true;
      }
      for (JClass c : this.classes)
      {
        if (c._package() == JFormatter.this.javaLang)
        {
          Iterator itr = enclosingClass._package().classes();
          while (itr.hasNext())
          {
            JDefinedClass n = (JDefinedClass)itr.next();
            if (n.name().equals(c.name())) {
              return true;
            }
          }
        }
        if (c.outer() != null) {
          return true;
        }
      }
      return false;
    }
    
    public void add(JClass clazz)
    {
      if (!this.classes.contains(clazz)) {
        this.classes.add(clazz);
      }
    }
    
    public List<JClass> getClasses()
    {
      return this.classes;
    }
    
    public void setId(boolean value)
    {
      this.id = value;
    }
    
    public boolean isId()
    {
      return (this.id) && (this.classes.size() == 0);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JFormatter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */