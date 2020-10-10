package com.sun.codemodel;

import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.codemodel.writer.ProgressCodeWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class JCodeModel
{
  private HashMap<String, JPackage> packages = new HashMap();
  private final HashMap<Class, JReferencedClass> refClasses = new HashMap();
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
  private JClass wildcard;
  public static final Map<Class, Class> primitiveToBox;
  public static final Map<Class, Class> boxToPrimitive;
  
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
  
  public Iterator<JPackage> packages()
  {
    return this.packages.values().iterator();
  }
  
  public JDefinedClass _class(String fullyqualifiedName)
    throws JClassAlreadyExistsException
  {
    return _class(fullyqualifiedName, ClassType.CLASS);
  }
  
  public JClass directClass(String name)
  {
    return new JDirectClass(this, name);
  }
  
  public JDefinedClass _class(String fullyqualifiedName, ClassType t)
    throws JClassAlreadyExistsException
  {
    int idx = fullyqualifiedName.lastIndexOf('.');
    if (idx < 0) {
      return rootPackage()._class(fullyqualifiedName);
    }
    return _package(fullyqualifiedName.substring(0, idx))._class(1, fullyqualifiedName.substring(idx + 1), t);
  }
  
  public JDefinedClass _getClass(String fullyQualifiedName)
  {
    int idx = fullyQualifiedName.lastIndexOf('.');
    if (idx < 0) {
      return rootPackage()._getClass(fullyQualifiedName);
    }
    return _package(fullyQualifiedName.substring(0, idx))._getClass(fullyQualifiedName.substring(idx + 1));
  }
  
  /**
   * @deprecated
   */
  public JDefinedClass newAnonymousClass(JClass baseType)
  {
    return new JAnonymousClass(baseType);
  }
  
  public JDefinedClass anonymousClass(JClass baseType)
  {
    return new JAnonymousClass(baseType);
  }
  
  public JDefinedClass anonymousClass(Class baseType)
  {
    return anonymousClass(ref(baseType));
  }
  
  public void build(File destDir, PrintStream status)
    throws IOException
  {
    build(destDir, destDir, status);
  }
  
  public void build(File srcDir, File resourceDir, PrintStream status)
    throws IOException
  {
    CodeWriter src = new FileCodeWriter(srcDir);
    CodeWriter res = new FileCodeWriter(resourceDir);
    if (status != null)
    {
      src = new ProgressCodeWriter(src, status);
      res = new ProgressCodeWriter(res, status);
    }
    build(src, res);
  }
  
  public void build(File destDir)
    throws IOException
  {
    build(destDir, System.out);
  }
  
  public void build(File srcDir, File resourceDir)
    throws IOException
  {
    build(srcDir, resourceDir, System.out);
  }
  
  public void build(CodeWriter out)
    throws IOException
  {
    build(out, out);
  }
  
  public void build(CodeWriter source, CodeWriter resource)
    throws IOException
  {
    JPackage[] pkgs = (JPackage[])this.packages.values().toArray(new JPackage[this.packages.size()]);
    for (JPackage pkg : pkgs) {
      pkg.build(source, resource);
    }
    source.close();
    resource.close();
  }
  
  public int countArtifacts()
  {
    int r = 0;
    JPackage[] pkgs = (JPackage[])this.packages.values().toArray(new JPackage[this.packages.size()]);
    for (JPackage pkg : pkgs) {
      r += pkg.countArtifacts();
    }
    return r;
  }
  
  public JClass ref(Class clazz)
  {
    JReferencedClass jrc = (JReferencedClass)this.refClasses.get(clazz);
    if (jrc == null)
    {
      if (clazz.isPrimitive()) {
        throw new IllegalArgumentException(clazz + " is a primitive");
      }
      if (clazz.isArray()) {
        return new JArrayClass(this, _ref(clazz.getComponentType()));
      }
      jrc = new JReferencedClass(clazz);
      this.refClasses.put(clazz, jrc);
    }
    return jrc;
  }
  
  public JType _ref(Class c)
  {
    if (c.isPrimitive()) {
      return JType.parse(this, c.getName());
    }
    return ref(c);
  }
  
  public JClass ref(String fullyQualifiedClassName)
  {
    try
    {
      return ref(Thread.currentThread().getContextClassLoader().loadClass(fullyQualifiedClassName));
    }
    catch (ClassNotFoundException e)
    {
      try
      {
        return ref(Class.forName(fullyQualifiedClassName));
      }
      catch (ClassNotFoundException e1) {}
    }
    return new JDirectClass(this, fullyQualifiedClassName);
  }
  
  public JClass wildcard()
  {
    if (this.wildcard == null) {
      this.wildcard = ref(Object.class).wildcard();
    }
    return this.wildcard;
  }
  
  public JType parseType(String name)
    throws ClassNotFoundException
  {
    if (name.endsWith("[]")) {
      return parseType(name.substring(0, name.length() - 2)).array();
    }
    try
    {
      return JType.parse(this, name);
    }
    catch (IllegalArgumentException e) {}
    return new TypeNameParser(name).parseTypeName();
  }
  
  private final class TypeNameParser
  {
    private final String s;
    private int idx;
    
    public TypeNameParser(String s)
    {
      this.s = s;
    }
    
    JClass parseTypeName()
      throws ClassNotFoundException
    {
      int start = this.idx;
      if (this.s.charAt(this.idx) == '?')
      {
        this.idx += 1;
        ws();
        String head = this.s.substring(this.idx);
        if (head.startsWith("extends"))
        {
          this.idx += 7;
          ws();
          return parseTypeName().wildcard();
        }
        if (head.startsWith("super")) {
          throw new UnsupportedOperationException("? super T not implemented");
        }
        throw new IllegalArgumentException("only extends/super can follow ?, but found " + this.s.substring(this.idx));
      }
      while (this.idx < this.s.length())
      {
        char ch = this.s.charAt(this.idx);
        if ((!Character.isJavaIdentifierStart(ch)) && (!Character.isJavaIdentifierPart(ch)) && (ch != '.')) {
          break;
        }
        this.idx += 1;
      }
      JClass clazz = JCodeModel.this.ref(this.s.substring(start, this.idx));
      
      return parseSuffix(clazz);
    }
    
    private JClass parseSuffix(JClass clazz)
      throws ClassNotFoundException
    {
      if (this.idx == this.s.length()) {
        return clazz;
      }
      char ch = this.s.charAt(this.idx);
      if (ch == '<') {
        return parseSuffix(parseArguments(clazz));
      }
      if (ch == '[')
      {
        if (this.s.charAt(this.idx + 1) == ']')
        {
          this.idx += 2;
          return parseSuffix(clazz.array());
        }
        throw new IllegalArgumentException("Expected ']' but found " + this.s.substring(this.idx + 1));
      }
      return clazz;
    }
    
    private void ws()
    {
      while ((Character.isWhitespace(this.s.charAt(this.idx))) && (this.idx < this.s.length())) {
        this.idx += 1;
      }
    }
    
    private JClass parseArguments(JClass rawType)
      throws ClassNotFoundException
    {
      if (this.s.charAt(this.idx) != '<') {
        throw new IllegalArgumentException();
      }
      this.idx += 1;
      
      List<JClass> args = new ArrayList();
      for (;;)
      {
        args.add(parseTypeName());
        if (this.idx == this.s.length()) {
          throw new IllegalArgumentException("Missing '>' in " + this.s);
        }
        char ch = this.s.charAt(this.idx);
        if (ch == '>') {
          return rawType.narrow((JClass[])args.toArray(new JClass[args.size()]));
        }
        if (ch != ',') {
          throw new IllegalArgumentException(this.s);
        }
        this.idx += 1;
      }
    }
  }
  
  private class JReferencedClass
    extends JClass
    implements JDeclaration
  {
    private final Class _class;
    
    JReferencedClass(Class _clazz)
    {
      super();
      this._class = _clazz;
      assert (!this._class.isArray());
    }
    
    public String name()
    {
      return this._class.getSimpleName().replace('$', '.');
    }
    
    public String fullName()
    {
      return this._class.getName().replace('$', '.');
    }
    
    public String binaryName()
    {
      return this._class.getName();
    }
    
    public JClass outer()
    {
      Class p = this._class.getDeclaringClass();
      if (p == null) {
        return null;
      }
      return JCodeModel.this.ref(p);
    }
    
    public JPackage _package()
    {
      String name = fullName();
      if (name.indexOf('[') != -1) {
        return JCodeModel.this._package("");
      }
      int idx = name.lastIndexOf('.');
      if (idx < 0) {
        return JCodeModel.this._package("");
      }
      return JCodeModel.this._package(name.substring(0, idx));
    }
    
    public JClass _extends()
    {
      Class sp = this._class.getSuperclass();
      if (sp == null)
      {
        if (isInterface()) {
          return owner().ref(Object.class);
        }
        return null;
      }
      return JCodeModel.this.ref(sp);
    }
    
    public Iterator<JClass> _implements()
    {
      final Class[] interfaces = this._class.getInterfaces();
      new Iterator()
      {
        private int idx = 0;
        
        public boolean hasNext()
        {
          return this.idx < interfaces.length;
        }
        
        public JClass next()
        {
          return JCodeModel.this.ref(interfaces[(this.idx++)]);
        }
        
        public void remove()
        {
          throw new UnsupportedOperationException();
        }
      };
    }
    
    public boolean isInterface()
    {
      return this._class.isInterface();
    }
    
    public boolean isAbstract()
    {
      return Modifier.isAbstract(this._class.getModifiers());
    }
    
    public JPrimitiveType getPrimitiveType()
    {
      Class v = (Class)JCodeModel.boxToPrimitive.get(this._class);
      if (v != null) {
        return JType.parse(JCodeModel.this, v.getName());
      }
      return null;
    }
    
    public boolean isArray()
    {
      return false;
    }
    
    public void declare(JFormatter f) {}
    
    public JTypeVar[] typeParams()
    {
      return super.typeParams();
    }
    
    protected JClass substituteParams(JTypeVar[] variables, List<JClass> bindings)
    {
      return this;
    }
  }
  
  static
  {
    Map<Class, Class> m1 = new HashMap();
    Map<Class, Class> m2 = new HashMap();
    
    m1.put(Boolean.class, Boolean.TYPE);
    m1.put(Byte.class, Byte.TYPE);
    m1.put(Character.class, Character.TYPE);
    m1.put(Double.class, Double.TYPE);
    m1.put(Float.class, Float.TYPE);
    m1.put(Integer.class, Integer.TYPE);
    m1.put(Long.class, Long.TYPE);
    m1.put(Short.class, Short.TYPE);
    m1.put(Void.class, Void.TYPE);
    for (Map.Entry<Class, Class> e : m1.entrySet()) {
      m2.put(e.getValue(), e.getKey());
    }
    boxToPrimitive = Collections.unmodifiableMap(m1);
    primitiveToBox = Collections.unmodifiableMap(m2);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JCodeModel.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */