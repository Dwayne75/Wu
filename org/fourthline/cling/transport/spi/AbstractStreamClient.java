package org.fourthline.cling.transport.spi;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.StreamResponseMessage;
import org.seamless.util.Exceptions;

public abstract class AbstractStreamClient<C extends StreamClientConfiguration, REQUEST>
  implements StreamClient<C>
{
  private static final Logger log = Logger.getLogger(StreamClient.class.getName());
  
  public StreamResponseMessage sendRequest(StreamRequestMessage requestMessage)
    throws InterruptedException
  {
    if (log.isLoggable(Level.FINE)) {
      log.fine("Preparing HTTP request: " + requestMessage);
    }
    REQUEST request = createRequest(requestMessage);
    if (request == null) {
      return null;
    }
    Callable<StreamResponseMessage> callable = createCallable(requestMessage, request);
    
    long start = System.currentTimeMillis();
    
    Future<StreamResponseMessage> future = getConfiguration().getRequestExecutorService().submit(callable);
    try
    {
      if (log.isLoggable(Level.FINE)) {
        log.fine("Waiting " + 
          getConfiguration().getTimeoutSeconds() + " seconds for HTTP request to complete: " + requestMessage);
      }
      StreamResponseMessage response = (StreamResponseMessage)future.get(getConfiguration().getTimeoutSeconds(), TimeUnit.SECONDS);
      
      elapsed = System.currentTimeMillis() - start;
      if (log.isLoggable(Level.FINEST)) {
        log.finest("Got HTTP response in " + elapsed + "ms: " + requestMessage);
      }
      if ((getConfiguration().getLogWarningSeconds() > 0) && 
        (elapsed > getConfiguration().getLogWarningSeconds() * 1000)) {
        log.warning("HTTP request took a long time (" + elapsed + "ms): " + requestMessage);
      }
      return response;
    }
    catch (InterruptedException ex)
    {
      if (log.isLoggable(Level.FINE)) {
        log.fine("Interruption, aborting request: " + requestMessage);
      }
      throw new InterruptedException("HTTP request interrupted and aborted");
    }
    catch (TimeoutException ex)
    {
      long elapsed;
      log.info("Timeout of " + 
        getConfiguration().getTimeoutSeconds() + " seconds while waiting for HTTP request to complete, aborting: " + requestMessage);
      
      abort(request);
      return null;
    }
    catch (ExecutionException ex)
    {
      Throwable cause = ex.getCause();
      if (!logExecutionException(cause)) {
        log.log(Level.WARNING, "HTTP request failed: " + requestMessage, Exceptions.unwrap(cause));
      }
      return null;
    }
    finally
    {
      onFinally(request);
    }
  }
  
  protected abstract REQUEST createRequest(StreamRequestMessage paramStreamRequestMessage);
  
  protected abstract Callable<StreamResponseMessage> createCallable(StreamRequestMessage paramStreamRequestMessage, REQUEST paramREQUEST);
  
  protected abstract void abort(REQUEST paramREQUEST);
  
  protected abstract boolean logExecutionException(Throwable paramThrowable);
  
  protected void onFinally(REQUEST request) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\transport\spi\AbstractStreamClient.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */