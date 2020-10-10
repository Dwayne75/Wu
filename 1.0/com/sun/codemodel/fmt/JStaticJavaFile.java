package com.sun.codemodel.fmt;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JResourceFile;
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

public final class JStaticJavaFile
  extends JResourceFile
{
  private final JPackage pkg;
  private final String className;
  private final URL source;
  private final JStaticJavaFile.JStaticClass clazz;
  private final JStaticJavaFile.LineFilter filter;
  
  public JStaticJavaFile(JPackage _pkg, String className, String _resourceName)
  {
    this(_pkg, className, JStaticJavaFile.class.getClassLoader().getResource(_resourceName), null);
  }
  
  public JStaticJavaFile(JPackage _pkg, String _className, URL _source, JStaticJavaFile.LineFilter _filter)
  {
    super(_className + ".java");
    if (_source == null) {
      throw new NullPointerException();
    }
    this.pkg = _pkg;
    this.clazz = new JStaticJavaFile.JStaticClass(this);
    this.className = _className;
    this.source = _source;
    this.filter = _filter;
  }
  
  public final JClass getJClass()
  {
    return this.clazz;
  }
  
  protected void build(OutputStream os)
    throws IOException
  {
    InputStream is = this.source.openStream();
    
    BufferedReader r = new BufferedReader(new InputStreamReader(is));
    PrintWriter w = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)));
    JStaticJavaFile.LineFilter filter = createLineFilter();
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
  
  private JStaticJavaFile.LineFilter createLineFilter()
  {
    JStaticJavaFile.LineFilter f = new JStaticJavaFile.1(this);
    if (this.filter != null) {
      return new JStaticJavaFile.ChainFilter(this, this.filter, f);
    }
    return f;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\fmt\JStaticJavaFile.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */