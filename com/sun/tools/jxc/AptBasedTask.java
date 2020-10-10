package com.sun.tools.jxc;

import com.sun.mirror.apt.AnnotationProcessorFactory;
import java.io.File;
import java.lang.reflect.Method;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.compilers.DefaultCompilerAdapter;
import org.apache.tools.ant.types.Commandline;

public abstract class AptBasedTask
  extends Javac
{
  public AptBasedTask()
  {
    setExecutable("apt");
  }
  
  protected abstract void setupCommandlineSwitches(Commandline paramCommandline);
  
  protected abstract AnnotationProcessorFactory createFactory();
  
  private abstract class AptAdapter
    extends DefaultCompilerAdapter
  {
    protected AptAdapter()
    {
      setJavac(AptBasedTask.this);
    }
    
    protected Commandline setupModernJavacCommandlineSwitches(Commandline cmd)
    {
      super.setupModernJavacCommandlineSwitches(cmd);
      AptBasedTask.this.setupCommandlineSwitches(cmd);
      return cmd;
    }
  }
  
  private final class InternalAptAdapter
    extends AptBasedTask.AptAdapter
  {
    private InternalAptAdapter()
    {
      super();
    }
    
    InternalAptAdapter(AptBasedTask.1 x1)
    {
      this();
    }
    
    public boolean execute()
      throws BuildException
    {
      Commandline cmd = setupModernJavacCommand();
      try
      {
        Class apt = Class.forName("com.sun.tools.apt.Main");
        Method process;
        try
        {
          process = apt.getMethod("process", new Class[] { AnnotationProcessorFactory.class, new String[0].getClass() });
        }
        catch (NoSuchMethodException e)
        {
          throw new BuildException("JDK 1.5.0_01 or later is necessary", e, this.location);
        }
        int result = ((Integer)process.invoke(null, new Object[] { AptBasedTask.this.createFactory(), cmd.getArguments() })).intValue();
        
        return result == 0;
      }
      catch (BuildException e)
      {
        throw e;
      }
      catch (Exception ex)
      {
        throw new BuildException("Error starting apt", ex, this.location);
      }
    }
  }
  
  protected void compile()
  {
    if (this.compileList.length == 0) {
      return;
    }
    log(getCompilationMessage() + this.compileList.length + " source file" + (this.compileList.length == 1 ? "" : "s"));
    if (this.listFiles) {
      for (int i = 0; i < this.compileList.length; i++)
      {
        String filename = this.compileList[i].getAbsolutePath();
        log(filename);
      }
    }
    AptAdapter apt = new InternalAptAdapter(null);
    if (!apt.execute())
    {
      if (this.failOnError) {
        throw new BuildException(getFailedMessage(), getLocation());
      }
      log(getFailedMessage(), 0);
    }
  }
  
  protected abstract String getCompilationMessage();
  
  protected abstract String getFailedMessage();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\jxc\AptBasedTask.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */