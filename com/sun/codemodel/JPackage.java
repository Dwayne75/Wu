package com.sun.codemodel;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class JPackage
  implements JDeclaration, JGenerable, JClassContainer, JAnnotatable, Comparable<JPackage>
{
  private String name;
  private final JCodeModel owner;
  private final Map<String, JDefinedClass> classes = new TreeMap();
  private final Set<JResourceFile> resources = new HashSet();
  private final Map<String, JDefinedClass> upperCaseClassMap;
  private List<JAnnotationUse> annotations = null;
  private JDocComment jdoc = null;
  
  JPackage(String name, JCodeModel cw)
  {
    this.owner = cw;
    if (name.equals("."))
    {
      String msg = "Package name . is not allowed";
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
  
  public boolean isClass()
  {
    return false;
  }
  
  public boolean isPackage()
  {
    return true;
  }
  
  public JPackage getPackage()
  {
    return this;
  }
  
  public JDefinedClass _class(int mods, String name)
    throws JClassAlreadyExistsException
  {
    return _class(mods, name, ClassType.CLASS);
  }
  
  /**
   * @deprecated
   */
  public JDefinedClass _class(int mods, String name, boolean isInterface)
    throws JClassAlreadyExistsException
  {
    return _class(mods, name, isInterface ? ClassType.INTERFACE : ClassType.CLASS);
  }
  
  public JDefinedClass _class(int mods, String name, ClassType classTypeVal)
    throws JClassAlreadyExistsException
  {
    if (this.classes.containsKey(name)) {
      throw new JClassAlreadyExistsException((JDefinedClass)this.classes.get(name));
    }
    JDefinedClass c = new JDefinedClass(this, mods, name, classTypeVal);
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
  
  public int compareTo(JPackage that)
  {
    return this.name.compareTo(that.name);
  }
  
  public JDefinedClass _interface(int mods, String name)
    throws JClassAlreadyExistsException
  {
    return _class(mods, name, ClassType.INTERFACE);
  }
  
  public JDefinedClass _interface(String name)
    throws JClassAlreadyExistsException
  {
    return _interface(1, name);
  }
  
  public JDefinedClass _annotationTypeDeclaration(String name)
    throws JClassAlreadyExistsException
  {
    return _class(1, name, ClassType.ANNOTATION_TYPE_DECL);
  }
  
  public JDefinedClass _enum(String name)
    throws JClassAlreadyExistsException
  {
    return _class(1, name, ClassType.ENUM);
  }
  
  public JResourceFile addResourceFile(JResourceFile rsrc)
  {
    this.resources.add(rsrc);
    return rsrc;
  }
  
  public boolean hasResourceFile(String name)
  {
    for (JResourceFile r : this.resources) {
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
  
  public JDocComment javadoc()
  {
    if (this.jdoc == null) {
      this.jdoc = new JDocComment(owner());
    }
    return this.jdoc;
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
    throws ClassNotFoundException
  {
    if (name.indexOf('.') >= 0) {
      throw new IllegalArgumentException("JClass name contains '.': " + name);
    }
    String n = "";
    if (!isUnnamed()) {
      n = this.name + '.';
    }
    n = n + name;
    
    return this.owner.ref(Class.forName(n));
  }
  
  public JPackage subPackage(String pkg)
  {
    if (isUnnamed()) {
      return owner()._package(pkg);
    }
    return owner()._package(this.name + '.' + pkg);
  }
  
  public Iterator<JDefinedClass> classes()
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
  
  public JAnnotationUse annotate(JClass clazz)
  {
    if (isUnnamed()) {
      throw new IllegalArgumentException("the root package cannot be annotated");
    }
    if (this.annotations == null) {
      this.annotations = new ArrayList();
    }
    JAnnotationUse a = new JAnnotationUse(clazz);
    this.annotations.add(a);
    return a;
  }
  
  public JAnnotationUse annotate(Class<? extends Annotation> clazz)
  {
    return annotate(this.owner.ref(clazz));
  }
  
  public <W extends JAnnotationWriter> W annotate2(Class<W> clazz)
  {
    return TypedAnnotationWriter.create(clazz, this);
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
  
  void build(CodeWriter src, CodeWriter res)
    throws IOException
  {
    for (JDefinedClass c : this.classes.values()) {
      if (!c.isHidden())
      {
        JFormatter f = createJavaSourceFileWriter(src, c.name());
        f.write(c);
        f.close();
      }
    }
    if ((this.annotations != null) || (this.jdoc != null))
    {
      JFormatter f = createJavaSourceFileWriter(src, "package-info");
      if (this.jdoc != null) {
        f.g(this.jdoc);
      }
      if (this.annotations != null) {
        for (JAnnotationUse a : this.annotations) {
          f.g(a).nl();
        }
      }
      f.d(this);
      
      f.close();
    }
    for (JResourceFile rsrc : this.resources)
    {
      CodeWriter cw = rsrc.isResource() ? res : src;
      OutputStream os = new BufferedOutputStream(cw.openBinary(this, rsrc.name()));
      rsrc.build(os);
      os.close();
    }
  }
  
  int countArtifacts()
  {
    int r = 0;
    for (JDefinedClass c : this.classes.values()) {
      if (!c.isHidden()) {
        r++;
      }
    }
    if ((this.annotations != null) || (this.jdoc != null)) {
      r++;
    }
    r += this.resources.size();
    
    return r;
  }
  
  private JFormatter createJavaSourceFileWriter(CodeWriter src, String className)
    throws IOException
  {
    Writer bw = new BufferedWriter(src.openSource(this, className + ".java"));
    return new JFormatter(new PrintWriter(bw));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JPackage.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */