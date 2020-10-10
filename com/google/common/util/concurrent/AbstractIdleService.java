package com.google.common.util.concurrent;

import com.google.common.annotations.Beta;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Beta
public abstract class AbstractIdleService
  implements Service
{
  private final Supplier<String> threadNameSupplier = new Supplier()
  {
    public String get()
    {
      String str1 = String.valueOf(String.valueOf(AbstractIdleService.this.serviceName()));String str2 = String.valueOf(String.valueOf(AbstractIdleService.this.state()));return 1 + str1.length() + str2.length() + str1 + " " + str2;
    }
  };
  private final Service delegate = new AbstractService()
  {
    protected final void doStart()
    {
      MoreExecutors.renamingDecorator(AbstractIdleService.this.executor(), AbstractIdleService.this.threadNameSupplier).execute(new Runnable()
      {
        public void run()
        {
          try
          {
            AbstractIdleService.this.startUp();
            AbstractIdleService.2.this.notifyStarted();
          }
          catch (Throwable t)
          {
            AbstractIdleService.2.this.notifyFailed(t);
            throw Throwables.propagate(t);
          }
        }
      });
    }
    
    protected final void doStop()
    {
      MoreExecutors.renamingDecorator(AbstractIdleService.this.executor(), AbstractIdleService.this.threadNameSupplier).execute(new Runnable()
      {
        public void run()
        {
          try
          {
            AbstractIdleService.this.shutDown();
            AbstractIdleService.2.this.notifyStopped();
          }
          catch (Throwable t)
          {
            AbstractIdleService.2.this.notifyFailed(t);
            throw Throwables.propagate(t);
          }
        }
      });
    }
  };
  
  protected abstract void startUp()
    throws Exception;
  
  protected abstract void shutDown()
    throws Exception;
  
  protected Executor executor()
  {
    new Executor()
    {
      public void execute(Runnable command)
      {
        MoreExecutors.newThread((String)AbstractIdleService.this.threadNameSupplier.get(), command).start();
      }
    };
  }
  
  public String toString()
  {
    String str1 = String.valueOf(String.valueOf(serviceName()));String str2 = String.valueOf(String.valueOf(state()));return 3 + str1.length() + str2.length() + str1 + " [" + str2 + "]";
  }
  
  public final boolean isRunning()
  {
    return this.delegate.isRunning();
  }
  
  public final Service.State state()
  {
    return this.delegate.state();
  }
  
  public final void addListener(Service.Listener listener, Executor executor)
  {
    this.delegate.addListener(listener, executor);
  }
  
  public final Throwable failureCause()
  {
    return this.delegate.failureCause();
  }
  
  public final Service startAsync()
  {
    this.delegate.startAsync();
    return this;
  }
  
  public final Service stopAsync()
  {
    this.delegate.stopAsync();
    return this;
  }
  
  public final void awaitRunning()
  {
    this.delegate.awaitRunning();
  }
  
  public final void awaitRunning(long timeout, TimeUnit unit)
    throws TimeoutException
  {
    this.delegate.awaitRunning(timeout, unit);
  }
  
  public final void awaitTerminated()
  {
    this.delegate.awaitTerminated();
  }
  
  public final void awaitTerminated(long timeout, TimeUnit unit)
    throws TimeoutException
  {
    this.delegate.awaitTerminated(timeout, unit);
  }
  
  protected String serviceName()
  {
    return getClass().getSimpleName();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\util\concurrent\AbstractIdleService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */