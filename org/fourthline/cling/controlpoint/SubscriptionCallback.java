package org.fourthline.cling.controlpoint;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.gena.LocalGENASubscription;
import org.fourthline.cling.model.gena.RemoteGENASubscription;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.protocol.ProtocolCreationException;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.protocol.sync.SendingSubscribe;
import org.fourthline.cling.registry.Registry;
import org.seamless.util.Exceptions;

public abstract class SubscriptionCallback
  implements Runnable
{
  protected static Logger log = Logger.getLogger(SubscriptionCallback.class.getName());
  protected final Service service;
  protected final Integer requestedDurationSeconds;
  private ControlPoint controlPoint;
  private GENASubscription subscription;
  
  protected SubscriptionCallback(Service service)
  {
    this.service = service;
    this.requestedDurationSeconds = Integer.valueOf(1800);
  }
  
  protected SubscriptionCallback(Service service, int requestedDurationSeconds)
  {
    this.service = service;
    this.requestedDurationSeconds = Integer.valueOf(requestedDurationSeconds);
  }
  
  public Service getService()
  {
    return this.service;
  }
  
  public synchronized ControlPoint getControlPoint()
  {
    return this.controlPoint;
  }
  
  public synchronized void setControlPoint(ControlPoint controlPoint)
  {
    this.controlPoint = controlPoint;
  }
  
  public synchronized GENASubscription getSubscription()
  {
    return this.subscription;
  }
  
  public synchronized void setSubscription(GENASubscription subscription)
  {
    this.subscription = subscription;
  }
  
  public synchronized void run()
  {
    if (getControlPoint() == null) {
      throw new IllegalStateException("Callback must be executed through ControlPoint");
    }
    if ((getService() instanceof LocalService)) {
      establishLocalSubscription((LocalService)this.service);
    } else if ((getService() instanceof RemoteService)) {
      establishRemoteSubscription((RemoteService)this.service);
    }
  }
  
  private void establishLocalSubscription(LocalService service)
  {
    if (getControlPoint().getRegistry().getLocalDevice(service.getDevice().getIdentity().getUdn(), false) == null)
    {
      log.fine("Local device service is currently not registered, failing subscription immediately");
      failed(null, null, new IllegalStateException("Local device is not registered"));
      return;
    }
    LocalGENASubscription localSubscription = null;
    try
    {
      localSubscription = new LocalGENASubscription(service, Integer.valueOf(Integer.MAX_VALUE), Collections.EMPTY_LIST)
      {
        public void failed(Exception ex)
        {
          synchronized (SubscriptionCallback.this)
          {
            SubscriptionCallback.this.setSubscription(null);
            SubscriptionCallback.this.failed(null, null, ex);
          }
        }
        
        public void established()
        {
          synchronized (SubscriptionCallback.this)
          {
            SubscriptionCallback.this.setSubscription(this);
            SubscriptionCallback.this.established(this);
          }
        }
        
        public void ended(CancelReason reason)
        {
          synchronized (SubscriptionCallback.this)
          {
            SubscriptionCallback.this.setSubscription(null);
            SubscriptionCallback.this.ended(this, reason, null);
          }
        }
        
        public void eventReceived()
        {
          synchronized (SubscriptionCallback.this)
          {
            SubscriptionCallback.log.fine("Local service state updated, notifying callback, sequence is: " + getCurrentSequence());
            SubscriptionCallback.this.eventReceived(this);
            incrementSequence();
          }
        }
      };
      log.fine("Local device service is currently registered, also registering subscription");
      getControlPoint().getRegistry().addLocalSubscription(localSubscription);
      
      log.fine("Notifying subscription callback of local subscription availablity");
      localSubscription.establish();
      
      log.fine("Simulating first initial event for local subscription callback, sequence: " + localSubscription.getCurrentSequence());
      eventReceived(localSubscription);
      localSubscription.incrementSequence();
      
      log.fine("Starting to monitor state changes of local service");
      localSubscription.registerOnService();
    }
    catch (Exception ex)
    {
      log.fine("Local callback creation failed: " + ex.toString());
      log.log(Level.FINE, "Exception root cause: ", Exceptions.unwrap(ex));
      if (localSubscription != null) {
        getControlPoint().getRegistry().removeLocalSubscription(localSubscription);
      }
      failed(localSubscription, null, ex);
    }
  }
  
  private void establishRemoteSubscription(RemoteService service)
  {
    RemoteGENASubscription remoteSubscription = new RemoteGENASubscription(service, this.requestedDurationSeconds.intValue())
    {
      public void failed(UpnpResponse responseStatus)
      {
        synchronized (SubscriptionCallback.this)
        {
          SubscriptionCallback.this.setSubscription(null);
          SubscriptionCallback.this.failed(this, responseStatus, null);
        }
      }
      
      public void established()
      {
        synchronized (SubscriptionCallback.this)
        {
          SubscriptionCallback.this.setSubscription(this);
          SubscriptionCallback.this.established(this);
        }
      }
      
      public void ended(CancelReason reason, UpnpResponse responseStatus)
      {
        synchronized (SubscriptionCallback.this)
        {
          SubscriptionCallback.this.setSubscription(null);
          SubscriptionCallback.this.ended(this, reason, responseStatus);
        }
      }
      
      public void eventReceived()
      {
        synchronized (SubscriptionCallback.this)
        {
          SubscriptionCallback.this.eventReceived(this);
        }
      }
      
      public void eventsMissed(int numberOfMissedEvents)
      {
        synchronized (SubscriptionCallback.this)
        {
          SubscriptionCallback.this.eventsMissed(this, numberOfMissedEvents);
        }
      }
      
      public void invalidMessage(UnsupportedDataException ex)
      {
        synchronized (SubscriptionCallback.this)
        {
          SubscriptionCallback.this.invalidMessage(this, ex);
        }
      }
    };
    try
    {
      protocol = getControlPoint().getProtocolFactory().createSendingSubscribe(remoteSubscription);
    }
    catch (ProtocolCreationException ex)
    {
      SendingSubscribe protocol;
      failed(this.subscription, null, ex); return;
    }
    SendingSubscribe protocol;
    protocol.run();
  }
  
  public synchronized void end()
  {
    if (this.subscription == null) {
      return;
    }
    if ((this.subscription instanceof LocalGENASubscription)) {
      endLocalSubscription((LocalGENASubscription)this.subscription);
    } else if ((this.subscription instanceof RemoteGENASubscription)) {
      endRemoteSubscription((RemoteGENASubscription)this.subscription);
    }
  }
  
  private void endLocalSubscription(LocalGENASubscription subscription)
  {
    log.fine("Removing local subscription and ending it in callback: " + subscription);
    getControlPoint().getRegistry().removeLocalSubscription(subscription);
    subscription.end(null);
  }
  
  private void endRemoteSubscription(RemoteGENASubscription subscription)
  {
    log.fine("Ending remote subscription: " + subscription);
    getControlPoint().getConfiguration().getSyncProtocolExecutorService().execute(
      getControlPoint().getProtocolFactory().createSendingUnsubscribe(subscription));
  }
  
  protected void failed(GENASubscription subscription, UpnpResponse responseStatus, Exception exception)
  {
    failed(subscription, responseStatus, exception, createDefaultFailureMessage(responseStatus, exception));
  }
  
  protected abstract void failed(GENASubscription paramGENASubscription, UpnpResponse paramUpnpResponse, Exception paramException, String paramString);
  
  protected abstract void established(GENASubscription paramGENASubscription);
  
  protected abstract void ended(GENASubscription paramGENASubscription, CancelReason paramCancelReason, UpnpResponse paramUpnpResponse);
  
  protected abstract void eventReceived(GENASubscription paramGENASubscription);
  
  protected abstract void eventsMissed(GENASubscription paramGENASubscription, int paramInt);
  
  public static String createDefaultFailureMessage(UpnpResponse responseStatus, Exception exception)
  {
    String message = "Subscription failed: ";
    if (responseStatus != null) {
      message = message + " HTTP response was: " + responseStatus.getResponseDetails();
    } else if (exception != null) {
      message = message + " Exception occured: " + exception;
    } else {
      message = message + " No response received.";
    }
    return message;
  }
  
  protected void invalidMessage(RemoteGENASubscription remoteGENASubscription, UnsupportedDataException ex)
  {
    log.info("Invalid event message received, causing: " + ex);
    if (log.isLoggable(Level.FINE))
    {
      log.fine("------------------------------------------------------------------------------");
      log.fine(ex.getData() != null ? ex.getData().toString() : "null");
      log.fine("------------------------------------------------------------------------------");
    }
  }
  
  public String toString()
  {
    return "(SubscriptionCallback) " + getService();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\controlpoint\SubscriptionCallback.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */