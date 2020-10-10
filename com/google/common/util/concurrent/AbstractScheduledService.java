package com.google.common.util.concurrent;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

@Beta
public abstract class AbstractScheduledService
  implements Service
{
  private static final Logger logger = Logger.getLogger(AbstractScheduledService.class.getName());
  protected abstract void runOneIteration()
    throws Exception;
  
  protected void startUp()
    throws Exception
  {}
  
  protected void shutDown()
    throws Exception
  {}
  
  protected abstract Scheduler scheduler();
  
  public static abstract class Scheduler
  {
    public static Scheduler newFixedDelaySchedule(long initialDelay, long delay, final TimeUnit unit)
    {
      new Scheduler(initialDelay)
      {
        public Future<?> schedule(AbstractService service, ScheduledExecutorService executor, Runnable task)
        {
          return executor.scheduleWithFixedDelay(task, this.val$initialDelay, unit, this.val$unit);
        }
      };
    }
    
    public static Scheduler newFixedRateSchedule(long initialDelay, long period, final TimeUnit unit)
    {
      new Scheduler(initialDelay)
      {
        public Future<?> schedule(AbstractService service, ScheduledExecutorService executor, Runnable task)
        {
          return executor.scheduleAtFixedRate(task, this.val$initialDelay, unit, this.val$unit);
        }
      };
    }
    
    abstract Future<?> schedule(AbstractService paramAbstractService, ScheduledExecutorService paramScheduledExecutorService, Runnable paramRunnable);
  }
  
  private final AbstractService delegate = new AbstractService()
  {
    private volatile Future<?> runningTask;
    private volatile ScheduledExecutorService executorService;
    private final ReentrantLock lock = new ReentrantLock();
    private final Runnable task = new Runnable()
    {
      public void run()
      {
        AbstractScheduledService.1.this.lock.lock();
        try
        {
          AbstractScheduledService.this.runOneIteration();
        }
        catch (Throwable t)
        {
          try
          {
            AbstractScheduledService.this.shutDown();
          }
          catch (Exception ignored)
          {
            AbstractScheduledService.logger.log(Level.WARNING, "Error while attempting to shut down the service after failure.", ignored);
          }
          AbstractScheduledService.1.this.notifyFailed(t);
          throw Throwables.propagate(t);
        }
        finally
        {
          AbstractScheduledService.1.this.lock.unlock();
        }
      }
    };
    
    protected final void doStart()
    {
      this.executorService = MoreExecutors.renamingDecorator(AbstractScheduledService.this.executor(), new Supplier()
      {
        public String get()
        {
          String str1 = String.valueOf(String.valueOf(AbstractScheduledService.this.serviceName()));String str2 = String.valueOf(String.valueOf(AbstractScheduledService.1.this.state()));return 1 + str1.length() + str2.length() + str1 + " " + str2;
        }
      });
      this.executorService.execute(new Runnable()
      {
        public void run()
        {
          AbstractScheduledService.1.this.lock.lock();
          try
          {
            AbstractScheduledService.this.startUp();
            AbstractScheduledService.1.this.runningTask = AbstractScheduledService.this.scheduler().schedule(AbstractScheduledService.this.delegate, AbstractScheduledService.1.this.executorService, AbstractScheduledService.1.this.task);
            AbstractScheduledService.1.this.notifyStarted();
          }
          catch (Throwable t)
          {
            AbstractScheduledService.1.this.notifyFailed(t);
            throw Throwables.propagate(t);
          }
          finally
          {
            AbstractScheduledService.1.this.lock.unlock();
          }
        }
      });
    }
    
    protected final void doStop()
    {
      this.runningTask.cancel(false);
      this.executorService.execute(new Runnable()
      {
        public void run()
        {
          try
          {
            AbstractScheduledService.1.this.lock.lock();
            try
            {
              if (AbstractScheduledService.1.this.state() != Service.State.STOPPING) {
                return;
              }
              AbstractScheduledService.this.shutDown();
            }
            finally
            {
              AbstractScheduledService.1.this.lock.unlock();
            }
            AbstractScheduledService.1.this.notifyStopped();
          }
          catch (Throwable t)
          {
            AbstractScheduledService.1.this.notifyFailed(t);
            throw Throwables.propagate(t);
          }
        }
      });
    }
  };
  
  protected ScheduledExecutorService executor()
  {
    final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory()
    {
      public Thread newThread(Runnable runnable)
      {
        return MoreExecutors.newThread(AbstractScheduledService.this.serviceName(), runnable);
      }
    });
    addListener(new Service.Listener()
    {
      public void terminated(Service.State from)
      {
        executor.shutdown();
      }
      
      public void failed(Service.State from, Throwable failure)
      {
        executor.shutdown();
      }
    }, MoreExecutors.directExecutor());
    
    return executor;
  }
  
  protected String serviceName()
  {
    return getClass().getSimpleName();
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
  
  @Beta
  public static abstract class CustomScheduler
    extends AbstractScheduledService.Scheduler
  {
    public CustomScheduler()
    {
      super();
    }
    
    private class ReschedulableCallable
      extends ForwardingFuture<Void>
      implements Callable<Void>
    {
      private final Runnable wrappedRunnable;
      private final ScheduledExecutorService executor;
      private final AbstractService service;
      private final ReentrantLock lock = new ReentrantLock();
      @GuardedBy("lock")
      private Future<Void> currentFuture;
      
      ReschedulableCallable(AbstractService service, ScheduledExecutorService executor, Runnable runnable)
      {
        this.wrappedRunnable = runnable;
        this.executor = executor;
        this.service = service;
      }
      
      public Void call()
        throws Exception
      {
        this.wrappedRunnable.run();
        reschedule();
        return null;
      }
      
      public void reschedule()
      {
        this.lock.lock();
        try
        {
          if ((this.currentFuture == null) || (!this.currentFuture.isCancelled()))
          {
            AbstractScheduledService.CustomScheduler.Schedule schedule = AbstractScheduledService.CustomScheduler.this.getNextSchedule();
            this.currentFuture = this.executor.schedule(this, schedule.delay, schedule.unit);
          }
        }
        catch (Throwable e)
        {
          this.service.notifyFailed(e);
        }
        finally
        {
          this.lock.unlock();
        }
      }
      
      public boolean cancel(boolean mayInterruptIfRunning)
      {
        this.lock.lock();
        try
        {
          return this.currentFuture.cancel(mayInterruptIfRunning);
        }
        finally
        {
          this.lock.unlock();
        }
      }
      
      protected Future<Void> delegate()
      {
        throw new UnsupportedOperationException("Only cancel is supported by this future");
      }
    }
    
    final Future<?> schedule(AbstractService service, ScheduledExecutorService executor, Runnable runnable)
    {
      ReschedulableCallable task = new ReschedulableCallable(service, executor, runnable);
      task.reschedule();
      return task;
    }
    
    protected abstract Schedule getNextSchedule()
      throws Exception;
    
    @Beta
    protected static final class Schedule
    {
      private final long delay;
      private final TimeUnit unit;
      
      public Schedule(long delay, TimeUnit unit)
      {
        this.delay = delay;
        this.unit = ((TimeUnit)Preconditions.checkNotNull(unit));
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\util\concurrent\AbstractScheduledService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */