package org.fourthline.cling.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.StateVariable;
import org.fourthline.cling.model.meta.StateVariableEventDetails;
import org.fourthline.cling.model.state.StateVariableAccessor;
import org.fourthline.cling.model.state.StateVariableValue;
import org.seamless.util.Exceptions;
import org.seamless.util.Reflections;

public class DefaultServiceManager<T>
  implements ServiceManager<T>
{
  private static Logger log = Logger.getLogger(DefaultServiceManager.class.getName());
  protected final LocalService<T> service;
  protected final Class<T> serviceClass;
  protected final ReentrantLock lock = new ReentrantLock(true);
  protected T serviceImpl;
  protected PropertyChangeSupport propertyChangeSupport;
  
  protected DefaultServiceManager(LocalService<T> service)
  {
    this(service, null);
  }
  
  public DefaultServiceManager(LocalService<T> service, Class<T> serviceClass)
  {
    this.service = service;
    this.serviceClass = serviceClass;
  }
  
  protected void lock()
  {
    try
    {
      if (this.lock.tryLock(getLockTimeoutMillis(), TimeUnit.MILLISECONDS))
      {
        if (log.isLoggable(Level.FINEST)) {
          log.finest("Acquired lock");
        }
      }
      else {
        throw new RuntimeException("Failed to acquire lock in milliseconds: " + getLockTimeoutMillis());
      }
    }
    catch (InterruptedException e)
    {
      throw new RuntimeException("Failed to acquire lock:" + e);
    }
  }
  
  protected void unlock()
  {
    if (log.isLoggable(Level.FINEST)) {
      log.finest("Releasing lock");
    }
    this.lock.unlock();
  }
  
  protected int getLockTimeoutMillis()
  {
    return 500;
  }
  
  public LocalService<T> getService()
  {
    return this.service;
  }
  
  public T getImplementation()
  {
    lock();
    try
    {
      if (this.serviceImpl == null) {
        init();
      }
      return (T)this.serviceImpl;
    }
    finally
    {
      unlock();
    }
  }
  
  public PropertyChangeSupport getPropertyChangeSupport()
  {
    lock();
    try
    {
      if (this.propertyChangeSupport == null) {
        init();
      }
      return this.propertyChangeSupport;
    }
    finally
    {
      unlock();
    }
  }
  
  /* Error */
  public void execute(Command<T> cmd)
    throws Exception
  {
    // Byte code:
    //   0: aload_0
    //   1: invokevirtual 29	org/fourthline/cling/model/DefaultServiceManager:lock	()V
    //   4: aload_1
    //   5: aload_0
    //   6: invokeinterface 34 2 0
    //   11: aload_0
    //   12: invokevirtual 32	org/fourthline/cling/model/DefaultServiceManager:unlock	()V
    //   15: goto +10 -> 25
    //   18: astore_2
    //   19: aload_0
    //   20: invokevirtual 32	org/fourthline/cling/model/DefaultServiceManager:unlock	()V
    //   23: aload_2
    //   24: athrow
    //   25: return
    // Line number table:
    //   Java source line #126	-> byte code offset #0
    //   Java source line #128	-> byte code offset #4
    //   Java source line #130	-> byte code offset #11
    //   Java source line #131	-> byte code offset #15
    //   Java source line #130	-> byte code offset #18
    //   Java source line #132	-> byte code offset #25
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	26	0	this	DefaultServiceManager<T>
    //   0	26	1	cmd	Command<T>
    //   18	6	2	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   4	11	18	finally
  }
  
  public Collection<StateVariableValue> getCurrentState()
    throws Exception
  {
    lock();
    try
    {
      Collection<StateVariableValue> values = readInitialEventedStateVariableValues();
      Object localObject1;
      if (values != null)
      {
        log.fine("Obtained initial state variable values for event, skipping individual state variable accessors");
        return values;
      }
      values = new ArrayList();
      for (StateVariable stateVariable : getService().getStateVariables()) {
        if (stateVariable.getEventDetails().isSendEvents())
        {
          StateVariableAccessor accessor = getService().getAccessor(stateVariable);
          if (accessor == null) {
            throw new IllegalStateException("No accessor for evented state variable");
          }
          values.add(accessor.read(stateVariable, getImplementation()));
        }
      }
      return values;
    }
    finally
    {
      unlock();
    }
  }
  
  protected Collection<StateVariableValue> getCurrentState(String[] variableNames)
    throws Exception
  {
    lock();
    try
    {
      Collection<StateVariableValue> values = new ArrayList();
      for (String variableName : variableNames)
      {
        variableName = variableName.trim();
        
        StateVariable stateVariable = getService().getStateVariable(variableName);
        if ((stateVariable == null) || (!stateVariable.getEventDetails().isSendEvents()))
        {
          log.fine("Ignoring unknown or non-evented state variable: " + variableName);
        }
        else
        {
          StateVariableAccessor accessor = getService().getAccessor(stateVariable);
          if (accessor == null) {
            log.warning("Ignoring evented state variable without accessor: " + variableName);
          } else {
            values.add(accessor.read(stateVariable, getImplementation()));
          }
        }
      }
      return values;
    }
    finally
    {
      unlock();
    }
  }
  
  protected void init()
  {
    log.fine("No service implementation instance available, initializing...");
    try
    {
      this.serviceImpl = createServiceInstance();
      
      this.propertyChangeSupport = createPropertyChangeSupport(this.serviceImpl);
      this.propertyChangeSupport.addPropertyChangeListener(createPropertyChangeListener(this.serviceImpl));
    }
    catch (Exception ex)
    {
      throw new RuntimeException("Could not initialize implementation: " + ex, ex);
    }
  }
  
  protected T createServiceInstance()
    throws Exception
  {
    if (this.serviceClass == null) {
      throw new IllegalStateException("Subclass has to provide service class or override createServiceInstance()");
    }
    try
    {
      return (T)this.serviceClass.getConstructor(new Class[] { LocalService.class }).newInstance(new Object[] { getService() });
    }
    catch (NoSuchMethodException ex)
    {
      log.fine("Creating new service implementation instance with no-arg constructor: " + this.serviceClass.getName());
    }
    return (T)this.serviceClass.newInstance();
  }
  
  protected PropertyChangeSupport createPropertyChangeSupport(T serviceImpl)
    throws Exception
  {
    Method m;
    if (((m = Reflections.getGetterMethod(serviceImpl.getClass(), "propertyChangeSupport")) != null) && 
      (PropertyChangeSupport.class.isAssignableFrom(m.getReturnType())))
    {
      log.fine("Service implementation instance offers PropertyChangeSupport, using that: " + serviceImpl.getClass().getName());
      return (PropertyChangeSupport)m.invoke(serviceImpl, new Object[0]);
    }
    log.fine("Creating new PropertyChangeSupport for service implementation: " + serviceImpl.getClass().getName());
    return new PropertyChangeSupport(serviceImpl);
  }
  
  protected PropertyChangeListener createPropertyChangeListener(T serviceImpl)
    throws Exception
  {
    return new DefaultPropertyChangeListener();
  }
  
  protected Collection<StateVariableValue> readInitialEventedStateVariableValues()
    throws Exception
  {
    return null;
  }
  
  public String toString()
  {
    return "(" + getClass().getSimpleName() + ") Implementation: " + this.serviceImpl;
  }
  
  protected class DefaultPropertyChangeListener
    implements PropertyChangeListener
  {
    protected DefaultPropertyChangeListener() {}
    
    public void propertyChange(PropertyChangeEvent e)
    {
      DefaultServiceManager.log.finer("Property change event on local service: " + e.getPropertyName());
      if (e.getPropertyName().equals("_EventedStateVariables")) {
        return;
      }
      String[] variableNames = ModelUtil.fromCommaSeparatedList(e.getPropertyName());
      DefaultServiceManager.log.fine("Changed variable names: " + Arrays.toString(variableNames));
      try
      {
        Collection<StateVariableValue> currentValues = DefaultServiceManager.this.getCurrentState(variableNames);
        if (!currentValues.isEmpty()) {
          DefaultServiceManager.this.getPropertyChangeSupport().firePropertyChange("_EventedStateVariables", null, currentValues);
        }
      }
      catch (Exception ex)
      {
        DefaultServiceManager.log.log(Level.SEVERE, "Error reading state of service after state variable update event: " + 
        
          Exceptions.unwrap(ex), ex);
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\DefaultServiceManager.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */