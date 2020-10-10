package com.sun.istack.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DynamicConfigurator;
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.Task;

public abstract class ProtectedTask
  extends Task
  implements DynamicConfigurator
{
  private final AntElement root;
  
  public ProtectedTask()
  {
    this.root = new AntElement("root");
  }
  
  public void setDynamicAttribute(String name, String value)
    throws BuildException
  {
    this.root.setDynamicAttribute(name, value);
  }
  
  public Object createDynamicElement(String name)
    throws BuildException
  {
    return this.root.createDynamicElement(name);
  }
  
  public void execute()
    throws BuildException
  {
    ClassLoader ccl = Thread.currentThread().getContextClassLoader();
    try
    {
      ClassLoader cl = createClassLoader();
      Class driver = cl.loadClass(getCoreClassName());
      
      Task t = (Task)driver.newInstance();
      t.setProject(getProject());
      t.setTaskName(getTaskName());
      this.root.configure(t);
      
      Thread.currentThread().setContextClassLoader(cl);
      t.execute();
    }
    catch (UnsupportedClassVersionError e)
    {
      throw new BuildException("Requires JDK 5.0 or later. Please download it from http://java.sun.com/j2se/1.5/");
    }
    catch (ClassNotFoundException e)
    {
      throw new BuildException(e);
    }
    catch (InstantiationException e)
    {
      throw new BuildException(e);
    }
    catch (IllegalAccessException e)
    {
      throw new BuildException(e);
    }
    catch (IOException e)
    {
      throw new BuildException(e);
    }
    finally
    {
      Thread.currentThread().setContextClassLoader(ccl);
    }
  }
  
  protected abstract String getCoreClassName();
  
  protected abstract ClassLoader createClassLoader()
    throws ClassNotFoundException, IOException;
  
  private class AntElement
    implements DynamicConfigurator
  {
    private final String name;
    private final Map attributes = new HashMap();
    private final List elements = new ArrayList();
    
    public AntElement(String name)
    {
      this.name = name;
    }
    
    public void setDynamicAttribute(String name, String value)
      throws BuildException
    {
      this.attributes.put(name, value);
    }
    
    public Object createDynamicElement(String name)
      throws BuildException
    {
      AntElement e = new AntElement(ProtectedTask.this, name);
      this.elements.add(e);
      return e;
    }
    
    public void configure(Object antObject)
    {
      IntrospectionHelper ih = IntrospectionHelper.getHelper(antObject.getClass());
      for (Iterator itr = this.attributes.entrySet().iterator(); itr.hasNext();)
      {
        Map.Entry att = (Map.Entry)itr.next();
        ih.setAttribute(ProtectedTask.this.getProject(), antObject, (String)att.getKey(), (String)att.getValue());
      }
      for (Iterator itr = this.elements.iterator(); itr.hasNext();)
      {
        AntElement e = (AntElement)itr.next();
        Object child = ih.createElement(ProtectedTask.this.getProject(), antObject, e.name);
        e.configure(child);
        ih.storeElement(ProtectedTask.this.getProject(), antObject, child, e.name);
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\istack\tools\ProtectedTask.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */