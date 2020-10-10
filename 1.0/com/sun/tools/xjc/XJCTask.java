package com.sun.tools.xjc;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Commandline.Argument;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XJCTask
  extends Task
{
  public XJCTask()
  {
    this.classpath = new Path(this.project);
    this.options.setSchemaLanguage(1);
  }
  
  private final Options options = new Options();
  private long stackSize = -1L;
  private boolean removeOldOutput = false;
  private final ArrayList dependsSet = new ArrayList();
  private final ArrayList producesSet = new ArrayList();
  private boolean producesSpecified = false;
  private final Path classpath;
  private final Commandline cmdLine = new Commandline();
  
  public void setSchema(File schema)
  {
    this.options.addGrammar(getInputSource(schema));
    this.dependsSet.add(schema);
  }
  
  public void addConfiguredSchema(FileSet fs)
  {
    InputSource[] iss = toInputSources(fs);
    for (int i = 0; i < iss.length; i++) {
      this.options.addGrammar(iss[i]);
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
  
  public void setBinding(File binding)
  {
    this.options.addBindFile(getInputSource(binding));
    this.dependsSet.add(binding);
  }
  
  public void addConfiguredBinding(FileSet fs)
  {
    InputSource[] iss = toInputSources(fs);
    for (int i = 0; i < iss.length; i++) {
      this.options.addBindFile(iss[i]);
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
  
  public void setReadonly(boolean flg)
  {
    this.options.readOnly = flg;
  }
  
  public void setExtension(boolean flg)
  {
    if (flg) {
      this.options.compatibilityMode = 2;
    } else {
      this.options.compatibilityMode = 1;
    }
  }
  
  public void setTarget(File dir)
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
    if (!fs.getDir(this.project).exists()) {
      log(fs.getDir(this.project).getAbsolutePath() + " is not found and thus excluded from the dependency check", 2);
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
    try
    {
      if (this.stackSize == -1L) {
        doXJC();
      } else {
        try
        {
          Throwable[] e = new Throwable[1];
          
          Runnable job = new XJCTask.1(this, e);
          try
          {
            Constructor c = class$java$lang$Thread.getConstructor(new Class[] { ThreadGroup.class, Runnable.class, String.class, Long.TYPE });
            
            t = (Thread)c.newInstance(new Object[] { Thread.currentThread().getThreadGroup(), job, Thread.currentThread().getName() + ":XJC", new Long(this.stackSize) });
          }
          catch (Throwable err)
          {
            Thread t;
            log("Unable to set the stack size. Use JDK1.4 or above", 1);
            doXJC(); return;
          }
          Thread t;
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
      throw e;
    }
  }
  
  private void doXJC()
    throws BuildException
  {
    ClassLoader old = Thread.currentThread().getContextClassLoader();
    try
    {
      Thread.currentThread().setContextClassLoader(new AntClassLoader(this.project, this.classpath));
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
    catch (IOException e)
    {
      throw new BuildException(e);
    }
    if (!this.producesSpecified) {
      log("Consider using <depends>/<produces> so that XJC won't do unnecessary compilation", 2);
    }
    long srcTime = computeTimestampFor(this.dependsSet, true);
    long dstTime = computeTimestampFor(this.producesSet, false);
    log("the last modified time of ths inputs is  " + srcTime, 3);
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
    Iterator itr;
    if (this.removeOldOutput)
    {
      log("removing old output files", 2);
      for (itr = this.producesSet.iterator(); itr.hasNext();)
      {
        File f = (File)itr.next();
        f.delete();
      }
    }
    ErrorReceiver errorReceiver = new XJCTask.ErrorReceiverImpl(this, null);
    
    AnnotatedGrammar grammar = null;
    try
    {
      grammar = GrammarLoader.load(this.options, errorReceiver);
      if (grammar == null) {
        throw new BuildException("unable to parse the schema. Error messages should have been provided");
      }
    }
    catch (IOException e)
    {
      throw new BuildException("Unable to read files: " + e.getMessage(), e);
    }
    catch (SAXException e)
    {
      throw new BuildException("failed to compile a schema", e);
    }
    try
    {
      if (Driver.generateCode(grammar, this.options, errorReceiver) == null) {
        throw new BuildException("failed to compile a schema");
      }
      log("Writing output to " + this.options.targetDir, 2);
      
      grammar.codeModel.build(new XJCTask.AngProgressCodeWriter(this, Driver.createCodeWriter(this.options.targetDir, this.options.readOnly)));
    }
    catch (IOException e)
    {
      throw new BuildException("unable to write files: " + e.getMessage(), e);
    }
  }
  
  private long computeTimestampFor(List files, boolean findNewest)
  {
    long lastModified = findNewest ? Long.MIN_VALUE : Long.MAX_VALUE;
    for (Iterator itr = files.iterator(); itr.hasNext();)
    {
      File file = (File)itr.next();
      
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
  
  private void addIndividualFilesTo(FileSet fs, List lst)
  {
    DirectoryScanner ds = fs.getDirectoryScanner(this.project);
    String[] includedFiles = ds.getIncludedFiles();
    File baseDir = ds.getBasedir();
    for (int j = 0; j < includedFiles.length; j++) {
      lst.add(new File(baseDir, includedFiles[j]));
    }
  }
  
  private InputSource[] toInputSources(FileSet fs)
  {
    DirectoryScanner ds = fs.getDirectoryScanner(this.project);
    String[] includedFiles = ds.getIncludedFiles();
    File baseDir = ds.getBasedir();
    
    ArrayList lst = new ArrayList();
    for (int j = 0; j < includedFiles.length; j++) {
      lst.add(getInputSource(new File(baseDir, includedFiles[j])));
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
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\XJCTask.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */