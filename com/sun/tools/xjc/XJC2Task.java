package com.sun.tools.xjc;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.writer.FilterCodeWriter;
import com.sun.tools.xjc.api.SpecVersion;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.reader.Util;
import com.sun.tools.xjc.util.ForkEntityResolver;
import com.sun.xml.bind.v2.util.EditDistance;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Commandline.Argument;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.XMLCatalog;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

public class XJC2Task
  extends Task
{
  public XJC2Task()
  {
    this.classpath = new Path(null);
    this.options.setSchemaLanguage(Language.XMLSCHEMA);
  }
  
  public final Options options = new Options();
  private long stackSize = -1L;
  private boolean failonerror = true;
  private boolean removeOldOutput = false;
  private final ArrayList<File> dependsSet = new ArrayList();
  private final ArrayList<File> producesSet = new ArrayList();
  private boolean producesSpecified = false;
  private final Path classpath;
  private final Commandline cmdLine = new Commandline();
  private XMLCatalog xmlCatalog = null;
  
  public void setSchema(String schema)
  {
    try
    {
      this.options.addGrammar(getInputSource(new URL(schema)));
    }
    catch (MalformedURLException e)
    {
      File f = getProject().resolveFile(schema);
      this.options.addGrammar(f);
      this.dependsSet.add(f);
    }
  }
  
  public void addConfiguredSchema(FileSet fs)
  {
    for (InputSource value : toInputSources(fs)) {
      this.options.addGrammar(value);
    }
    addIndividualFilesTo(fs, this.dependsSet);
  }
  
  public void setClasspath(Path cp)
  {
    this.classpath.createPath().append(cp);
  }
  
  public Path createClasspath()
  {
    return this.classpath.createPath();
  }
  
  public void setClasspathRef(Reference r)
  {
    this.classpath.createPath().setRefid(r);
  }
  
  public void setLanguage(String language)
  {
    Language l = Language.valueOf(language.toUpperCase());
    if (l == null)
    {
      Language[] languages = Language.values();
      String[] candidates = new String[languages.length];
      for (int i = 0; i < candidates.length; i++) {
        candidates[i] = languages[i].name();
      }
      throw new BuildException("Unrecognized language: " + language + ". Did you mean " + EditDistance.findNearest(language.toUpperCase(), candidates) + " ?");
    }
    this.options.setSchemaLanguage(l);
  }
  
  public void setBinding(String binding)
  {
    try
    {
      this.options.addBindFile(getInputSource(new URL(binding)));
    }
    catch (MalformedURLException e)
    {
      File f = getProject().resolveFile(binding);
      this.options.addBindFile(f);
      this.dependsSet.add(f);
    }
  }
  
  public void addConfiguredBinding(FileSet fs)
  {
    for (InputSource is : toInputSources(fs)) {
      this.options.addBindFile(is);
    }
    addIndividualFilesTo(fs, this.dependsSet);
  }
  
  public void setPackage(String pkg)
  {
    this.options.defaultPackage = pkg;
  }
  
  public void setCatalog(File catalog)
  {
    try
    {
      this.options.addCatalog(catalog);
    }
    catch (IOException e)
    {
      throw new BuildException(e);
    }
  }
  
  public void setFailonerror(boolean value)
  {
    this.failonerror = value;
  }
  
  /**
   * @deprecated
   */
  public void setStackSize(String ss)
  {
    try
    {
      this.stackSize = Long.parseLong(ss);
      return;
    }
    catch (NumberFormatException e)
    {
      if (ss.length() > 2)
      {
        String head = ss.substring(0, ss.length() - 2);
        String tail = ss.substring(ss.length() - 2);
        if (tail.equalsIgnoreCase("kb")) {
          try
          {
            this.stackSize = (Long.parseLong(head) * 1024L);
            return;
          }
          catch (NumberFormatException ee) {}
        }
        if (tail.equalsIgnoreCase("mb")) {
          try
          {
            this.stackSize = (Long.parseLong(head) * 1024L * 1024L);
            return;
          }
          catch (NumberFormatException ee) {}
        }
      }
      throw new BuildException("Unrecognizable stack size: " + ss);
    }
  }
  
  public void addConfiguredXMLCatalog(XMLCatalog xmlCatalog)
  {
    if (this.xmlCatalog == null)
    {
      this.xmlCatalog = new XMLCatalog();
      this.xmlCatalog.setProject(getProject());
    }
    this.xmlCatalog.addConfiguredXMLCatalog(xmlCatalog);
  }
  
  public void setReadonly(boolean flg)
  {
    this.options.readOnly = flg;
  }
  
  public void setHeader(boolean flg)
  {
    this.options.noFileHeader = (!flg);
  }
  
  public void setXexplicitAnnotation(boolean flg)
  {
    this.options.runtime14 = flg;
  }
  
  public void setExtension(boolean flg)
  {
    if (flg) {
      this.options.compatibilityMode = 2;
    } else {
      this.options.compatibilityMode = 1;
    }
  }
  
  public void setTarget(String version)
  {
    this.options.target = SpecVersion.parse(version);
    if (this.options.target == null) {
      throw new BuildException(version + " is not a valid version number. Perhaps you meant @destdir?");
    }
  }
  
  public void setDestdir(File dir)
  {
    this.options.targetDir = dir;
  }
  
  public void addConfiguredDepends(FileSet fs)
  {
    addIndividualFilesTo(fs, this.dependsSet);
  }
  
  public void addConfiguredProduces(FileSet fs)
  {
    this.producesSpecified = true;
    if (!fs.getDir(getProject()).exists()) {
      log(fs.getDir(getProject()).getAbsolutePath() + " is not found and thus excluded from the dependency check", 2);
    } else {
      addIndividualFilesTo(fs, this.producesSet);
    }
  }
  
  public void setRemoveOldOutput(boolean roo)
  {
    this.removeOldOutput = roo;
  }
  
  public Commandline.Argument createArg()
  {
    return this.cmdLine.createArgument();
  }
  
  public void execute()
    throws BuildException
  {
    log("build id of XJC is " + Driver.getBuildID(), 3);
    
    this.classpath.setProject(getProject());
    try
    {
      if (this.stackSize == -1L) {
        doXJC();
      } else {
        try
        {
          final Throwable[] e = new Throwable[1];
          
          Runnable job = new Runnable()
          {
            public void run()
            {
              try
              {
                XJC2Task.this.doXJC();
              }
              catch (Throwable be)
              {
                e[0] = be;
              }
            }
          };
          Thread t;
          try
          {
            Constructor c = Thread.class.getConstructor(new Class[] { ThreadGroup.class, Runnable.class, String.class, Long.TYPE });
            
            t = (Thread)c.newInstance(new Object[] { Thread.currentThread().getThreadGroup(), job, Thread.currentThread().getName() + ":XJC", Long.valueOf(this.stackSize) });
          }
          catch (Throwable err)
          {
            log("Unable to set the stack size. Use JDK1.4 or above", 1);
            doXJC();
            return;
          }
          t.start();
          t.join();
          if ((e[0] instanceof Error)) {
            throw ((Error)e[0]);
          }
          if ((e[0] instanceof RuntimeException)) {
            throw ((RuntimeException)e[0]);
          }
          if ((e[0] instanceof BuildException)) {
            throw ((BuildException)e[0]);
          }
          if (e[0] != null) {
            throw new BuildException(e[0]);
          }
        }
        catch (InterruptedException e)
        {
          throw new BuildException(e);
        }
      }
    }
    catch (BuildException e)
    {
      log("failure in the XJC task. Use the Ant -verbose switch for more details");
      if (this.failonerror) {
        throw e;
      }
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      getProject().log(sw.toString(), 1);
    }
  }
  
  private void doXJC()
    throws BuildException
  {
    ClassLoader old = Thread.currentThread().getContextClassLoader();
    try
    {
      Thread.currentThread().setContextClassLoader(new AntClassLoader(getProject(), this.classpath));
      _doXJC();
    }
    finally
    {
      Thread.currentThread().setContextClassLoader(old);
    }
  }
  
  private void _doXJC()
    throws BuildException
  {
    try
    {
      this.options.parseArguments(this.cmdLine.getArguments());
    }
    catch (BadCommandLineException e)
    {
      throw new BuildException(e.getMessage(), e);
    }
    if (this.xmlCatalog != null) {
      if (this.options.entityResolver == null) {
        this.options.entityResolver = this.xmlCatalog;
      } else {
        this.options.entityResolver = new ForkEntityResolver(this.options.entityResolver, this.xmlCatalog);
      }
    }
    if (!this.producesSpecified) {
      log("Consider using <depends>/<produces> so that XJC won't do unnecessary compilation", 2);
    }
    long srcTime = computeTimestampFor(this.dependsSet, true);
    long dstTime = computeTimestampFor(this.producesSet, false);
    log("the last modified time of the inputs is  " + srcTime, 3);
    log("the last modified time of the outputs is " + dstTime, 3);
    if (srcTime < dstTime)
    {
      log("files are up to date");
      return;
    }
    InputSource[] grammars = this.options.getGrammars();
    
    String msg = "Compiling " + grammars[0].getSystemId();
    if (grammars.length > 1) {
      msg = msg + " and others";
    }
    log(msg, 2);
    if (this.removeOldOutput)
    {
      log("removing old output files", 2);
      for (File f : this.producesSet) {
        f.delete();
      }
    }
    ErrorReceiver errorReceiver = new ErrorReceiverImpl(null);
    
    Model model = ModelLoader.load(this.options, new JCodeModel(), errorReceiver);
    if (model == null) {
      throw new BuildException("unable to parse the schema. Error messages should have been provided");
    }
    try
    {
      if (model.generateCode(this.options, errorReceiver) == null) {
        throw new BuildException("failed to compile a schema");
      }
      log("Writing output to " + this.options.targetDir, 2);
      
      model.codeModel.build(new AntProgressCodeWriter(this.options.createCodeWriter()));
    }
    catch (IOException e)
    {
      throw new BuildException("unable to write files: " + e.getMessage(), e);
    }
  }
  
  private long computeTimestampFor(List<File> files, boolean findNewest)
  {
    long lastModified = findNewest ? Long.MIN_VALUE : Long.MAX_VALUE;
    for (File file : files)
    {
      log("Checking timestamp of " + file.toString(), 3);
      if (findNewest) {
        lastModified = Math.max(lastModified, file.lastModified());
      } else {
        lastModified = Math.min(lastModified, file.lastModified());
      }
    }
    if (lastModified == Long.MIN_VALUE) {
      return Long.MAX_VALUE;
    }
    if (lastModified == Long.MAX_VALUE) {
      return Long.MIN_VALUE;
    }
    return lastModified;
  }
  
  private void addIndividualFilesTo(FileSet fs, List<File> lst)
  {
    DirectoryScanner ds = fs.getDirectoryScanner(getProject());
    String[] includedFiles = ds.getIncludedFiles();
    File baseDir = ds.getBasedir();
    for (String value : includedFiles) {
      lst.add(new File(baseDir, value));
    }
  }
  
  private InputSource[] toInputSources(FileSet fs)
  {
    DirectoryScanner ds = fs.getDirectoryScanner(getProject());
    String[] includedFiles = ds.getIncludedFiles();
    File baseDir = ds.getBasedir();
    
    ArrayList<InputSource> lst = new ArrayList();
    for (String value : includedFiles) {
      lst.add(getInputSource(new File(baseDir, value)));
    }
    return (InputSource[])lst.toArray(new InputSource[lst.size()]);
  }
  
  private InputSource getInputSource(File f)
  {
    try
    {
      return new InputSource(f.toURL().toExternalForm());
    }
    catch (MalformedURLException e) {}
    return new InputSource(f.getPath());
  }
  
  private InputSource getInputSource(URL url)
  {
    return Util.getInputSource(url.toExternalForm());
  }
  
  private class AntProgressCodeWriter
    extends FilterCodeWriter
  {
    public AntProgressCodeWriter(CodeWriter output)
    {
      super();
    }
    
    public OutputStream openBinary(JPackage pkg, String fileName)
      throws IOException
    {
      if (pkg.isUnnamed()) {
        XJC2Task.this.log("generating " + fileName, 3);
      } else {
        XJC2Task.this.log("generating " + pkg.name().replace('.', File.separatorChar) + File.separatorChar + fileName, 3);
      }
      return super.openBinary(pkg, fileName);
    }
  }
  
  private class ErrorReceiverImpl
    extends ErrorReceiver
  {
    private ErrorReceiverImpl() {}
    
    public void warning(SAXParseException e)
    {
      print(1, "Driver.WarningMessage", e);
    }
    
    public void error(SAXParseException e)
    {
      print(0, "Driver.ErrorMessage", e);
    }
    
    public void fatalError(SAXParseException e)
    {
      print(0, "Driver.ErrorMessage", e);
    }
    
    public void info(SAXParseException e)
    {
      print(3, "Driver.InfoMessage", e);
    }
    
    private void print(int logLevel, String header, SAXParseException e)
    {
      XJC2Task.this.log(Messages.format(header, new Object[] { e.getMessage() }), logLevel);
      XJC2Task.this.log(getLocationString(e), logLevel);
      XJC2Task.this.log("", logLevel);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\XJC2Task.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */