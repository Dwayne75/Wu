package com.google.common.util.concurrent;

import com.google.common.annotations.Beta;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Beta
public abstract interface Service
{
  public abstract Service startAsync();
  
  public abstract boolean isRunning();
  
  public abstract State state();
  
  public abstract Service stopAsync();
  
  public abstract void awaitRunning();
  
  public abstract void awaitRunning(long paramLong, TimeUnit paramTimeUnit)
    throws TimeoutException;
  
  public abstract void awaitTerminated();
  
  public abstract void awaitTerminated(long paramLong, TimeUnit paramTimeUnit)
    throws TimeoutException;
  
  public abstract Throwable failureCause();
  
  public abstract void addListener(Listener paramListener, Executor paramExecutor);
  
  @Beta
  public static abstract class Listener
  {
    public void starting() {}
    
    public void running() {}
    
    public void stopping(Service.State from) {}
    
    public void terminated(Service.State from) {}
    
    public void failed(Service.State from, Throwable failure) {}
  }
  
  @Beta
  public static abstract enum State
  {
    NEW,  STARTING,  RUNNING,  STOPPING,  TERMINATED,  FAILED;
    
    private State() {}
    
    abstract boolean isTerminal();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\util\concurrent\Service.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */