package com.sun.codemodel.fmt;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JResourceFile;
import com.sun.codemodel.JTypeVar;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

public final class JStaticJavaFile
  extends JResourceFile
{
  private final JPackage pkg;
  private final String className;
  private final URL source;
  private final JStaticClass clazz;
  private final LineFilter filter;
  
  public JStaticJavaFile(JPackage _pkg, String className, String _resourceName)
  {
    this(_pkg, className, JStaticJavaFile.class.getClassLoader().getResource(_resourceName), null);
  }
  
  public JStaticJavaFile(JPackage _pkg, String _className, URL _source, LineFilter _filter)
  {
    super(_className + ".java");
    if (_source == null) {
      throw new NullPointerException();
    }
    this.pkg = _pkg;
    this.clazz = new JStaticClass();
    this.className = _className;
    this.source = _source;
    this.filter = _filter;
  }
  
  public final JClass getJClass()
  {
    return this.clazz;
  }
  
  protected boolean isResource()
  {
    return false;
  }
  
  protected void build(OutputStream os)
    throws IOException
  {
    InputStream is = this.source.openStream();
    
    BufferedReader r = new BufferedReader(new InputStreamReader(is));
    PrintWriter w = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)));
    LineFilter filter = createLineFilter();
    int lineNumber = 1;
    try
    {
      String line;
      while ((line = r.readLine()) != null)
      {
        line = filter.process(line);
        if (line != null) {
          w.println(line);
        }
        lineNumber++;
      }
    }
    catch (ParseException e)
    {
      throw new IOException("unable to process " + this.source + " line:" + lineNumber + "\n" + e.getMessage());
    }
    w.close();
    r.close();
  }
  
  private LineFilter createLineFilter()
  {
    LineFilter f = new LineFilter()
    {
      public String process(String line)
      {
        if (!line.startsWith("package ")) {
          return line;
        }
        if (JStaticJavaFile.this.pkg.isUnnamed()) {
          return null;
        }
        return "package " + JStaticJavaFile.this.pkg.name() + ";";
      }
    };
    if (this.filter != null) {
      return new ChainFilter(this.filter, f);
    }
    return f;
  }
  
  public static abstract interface LineFilter
  {
    public abstract String process(String paramString)
      throws ParseException;
  }
  
  public static final class ChainFilter
    implements JStaticJavaFile.LineFilter
  {
    private final JStaticJavaFile.LineFilter first;
    private final JStaticJavaFile.LineFilter second;
    
    public ChainFilter(JStaticJavaFile.LineFilter first, JStaticJavaFile.LineFilter second)
    {
      this.first = first;
      this.second = second;
    }
    
    public String process(String line)
      throws ParseException
    {
      line = this.first.process(line);
      if (line == null) {
        return null;
      }
      return this.second.process(line);
    }
  }
  
  private class JStaticClass
    extends JClass
  {
    private final JTypeVar[] typeParams;
    
    JStaticClass()
    {
      super();
      
      this.typeParams = new JTypeVar[0];
    }
    
    public String name()
    {
      return JStaticJavaFile.this.className;
    }
    
    public String fullName()
    {
      if (JStaticJavaFile.this.pkg.isUnnamed()) {
        return JStaticJavaFile.this.className;
      }
      return JStaticJavaFile.this.pkg.name() + '.' + JStaticJavaFile.this.className;
    }
    
    public JPackage _package()
    {
      return JStaticJavaFile.this.pkg;
    }
    
    public JClass _extends()
    {
      throw new UnsupportedOperationException();
    }
    
    public Iterator _implements()
    {
      throw new UnsupportedOperationException();
    }
    
    public boolean isInterface()
    {
      throw new UnsupportedOperationException();
    }
    
    public boolean isAbstract()
    {
      throw new UnsupportedOperationException();
    }
    
    public JTypeVar[] typeParams()
    {
      return this.typeParams;
    }
    
    protected JClass substituteParams(JTypeVar[] variables, List<JClass> bindings)
    {
      return this;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\fmt\JStaticJavaFile.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */