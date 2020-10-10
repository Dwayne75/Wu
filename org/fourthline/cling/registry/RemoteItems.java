package org.fourthline.cling.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.ExpirationDetails;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.RemoteGENASubscription;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteDeviceIdentity;
import org.fourthline.cling.model.meta.RemoteService;
import org.fourthline.cling.model.resource.Resource;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.protocol.sync.SendingUnsubscribe;

class RemoteItems
  extends RegistryItems<RemoteDevice, RemoteGENASubscription>
{
  private static Logger log = Logger.getLogger(Registry.class.getName());
  
  RemoteItems(RegistryImpl registry)
  {
    super(registry);
  }
  
  void add(final RemoteDevice device)
  {
    if (update((RemoteDeviceIdentity)device.getIdentity()))
    {
      log.fine("Ignoring addition, device already registered: " + device);
      return;
    }
    Resource[] resources = getResources(device);
    for (Resource deviceResource : resources)
    {
      log.fine("Validating remote device resource; " + deviceResource);
      if (this.registry.getResource(deviceResource.getPathQuery()) != null) {
        throw new RegistrationException("URI namespace conflict with already registered resource: " + deviceResource);
      }
    }
    for (Resource validatedResource : resources)
    {
      this.registry.addResource(validatedResource);
      log.fine("Added remote device resource: " + validatedResource);
    }
    RegistryItem item = new RegistryItem(((RemoteDeviceIdentity)device.getIdentity()).getUdn(), device, (this.registry.getConfiguration().getRemoteDeviceMaxAgeSeconds() != null ? this.registry
      .getConfiguration().getRemoteDeviceMaxAgeSeconds() : 
      ((RemoteDeviceIdentity)device.getIdentity()).getMaxAgeSeconds()).intValue());
    
    log.fine("Adding hydrated remote device to registry with " + item
      .getExpirationDetails().getMaxAgeSeconds() + " seconds expiration: " + device);
    getDeviceItems().add(item);
    if (log.isLoggable(Level.FINEST))
    {
      sb = new StringBuilder();
      ((StringBuilder)sb).append("\n");
      ((StringBuilder)sb).append("-------------------------- START Registry Namespace -----------------------------------\n");
      for (Resource resource : this.registry.getResources()) {
        ((StringBuilder)sb).append(resource).append("\n");
      }
      ((StringBuilder)sb).append("-------------------------- END Registry Namespace -----------------------------------");
      log.finest(((StringBuilder)sb).toString());
    }
    log.fine("Completely hydrated remote device graph available, calling listeners: " + device);
    for (Object sb = this.registry.getListeners().iterator(); ((Iterator)sb).hasNext();)
    {
      final RegistryListener listener = (RegistryListener)((Iterator)sb).next();
      this.registry.getConfiguration().getRegistryListenerExecutor().execute(new Runnable()
      {
        public void run()
        {
          listener.remoteDeviceAdded(RemoteItems.this.registry, device);
        }
      });
    }
  }
  
  boolean update(RemoteDeviceIdentity rdIdentity)
  {
    for (LocalDevice localDevice : this.registry.getLocalDevices()) {
      if (localDevice.findDevice(rdIdentity.getUdn()) != null)
      {
        log.fine("Ignoring update, a local device graph contains UDN");
        return true;
      }
    }
    RemoteDevice registeredRemoteDevice = (RemoteDevice)get(rdIdentity.getUdn(), false);
    if (registeredRemoteDevice != null)
    {
      if (!registeredRemoteDevice.isRoot())
      {
        log.fine("Updating root device of embedded: " + registeredRemoteDevice);
        registeredRemoteDevice = registeredRemoteDevice.getRoot();
      }
      final RegistryItem<UDN, RemoteDevice> item = new RegistryItem(((RemoteDeviceIdentity)registeredRemoteDevice.getIdentity()).getUdn(), registeredRemoteDevice, (this.registry.getConfiguration().getRemoteDeviceMaxAgeSeconds() != null ? this.registry
        .getConfiguration().getRemoteDeviceMaxAgeSeconds() : rdIdentity
        .getMaxAgeSeconds()).intValue());
      
      log.fine("Updating expiration of: " + registeredRemoteDevice);
      getDeviceItems().remove(item);
      getDeviceItems().add(item);
      
      log.fine("Remote device updated, calling listeners: " + registeredRemoteDevice);
      for (final RegistryListener listener : this.registry.getListeners()) {
        this.registry.getConfiguration().getRegistryListenerExecutor().execute(new Runnable()
        {
          public void run()
          {
            listener.remoteDeviceUpdated(RemoteItems.this.registry, (RemoteDevice)item.getItem());
          }
        });
      }
      return true;
    }
    return false;
  }
  
  boolean remove(RemoteDevice remoteDevice)
  {
    return remove(remoteDevice, false);
  }
  
  boolean remove(RemoteDevice remoteDevice, boolean shuttingDown)
    throws RegistrationException
  {
    final RemoteDevice registeredDevice = (RemoteDevice)get(((RemoteDeviceIdentity)remoteDevice.getIdentity()).getUdn(), true);
    if (registeredDevice != null)
    {
      log.fine("Removing remote device from registry: " + remoteDevice);
      for (Resource deviceResource : getResources(registeredDevice)) {
        if (this.registry.removeResource(deviceResource)) {
          log.fine("Unregistered resource: " + deviceResource);
        }
      }
      Object it = getSubscriptionItems().iterator();
      final Object outgoingSubscription;
      while (((Iterator)it).hasNext())
      {
        outgoingSubscription = (RegistryItem)((Iterator)it).next();
        
        UDN subscriptionForUDN = ((RemoteDeviceIdentity)((RemoteDevice)((RemoteService)((RemoteGENASubscription)((RegistryItem)outgoingSubscription).getItem()).getService()).getDevice()).getIdentity()).getUdn();
        if (subscriptionForUDN.equals(((RemoteDeviceIdentity)registeredDevice.getIdentity()).getUdn()))
        {
          log.fine("Removing outgoing subscription: " + (String)((RegistryItem)outgoingSubscription).getKey());
          ((Iterator)it).remove();
          if (!shuttingDown) {
            this.registry.getConfiguration().getRegistryListenerExecutor().execute(new Runnable()
            {
              public void run()
              {
                ((RemoteGENASubscription)outgoingSubscription.getItem()).end(CancelReason.DEVICE_WAS_REMOVED, null);
              }
            });
          }
        }
      }
      if (!shuttingDown) {
        for (outgoingSubscription = this.registry.getListeners().iterator(); ((Iterator)outgoingSubscription).hasNext();)
        {
          final RegistryListener listener = (RegistryListener)((Iterator)outgoingSubscription).next();
          this.registry.getConfiguration().getRegistryListenerExecutor().execute(new Runnable()
          {
            public void run()
            {
              listener.remoteDeviceRemoved(RemoteItems.this.registry, registeredDevice);
            }
          });
        }
      }
      getDeviceItems().remove(new RegistryItem(((RemoteDeviceIdentity)registeredDevice.getIdentity()).getUdn()));
      
      return true;
    }
    return false;
  }
  
  void removeAll()
  {
    removeAll(false);
  }
  
  void removeAll(boolean shuttingDown)
  {
    RemoteDevice[] allDevices = (RemoteDevice[])get().toArray(new RemoteDevice[get().size()]);
    for (RemoteDevice device : allDevices) {
      remove(device, shuttingDown);
    }
  }
  
  void start() {}
  
  void maintain()
  {
    if (getDeviceItems().isEmpty()) {
      return;
    }
    Map<UDN, RemoteDevice> expiredRemoteDevices = new HashMap();
    for (RegistryItem<UDN, RemoteDevice> remoteItem : getDeviceItems())
    {
      if (log.isLoggable(Level.FINEST)) {
        log.finest("Device '" + remoteItem.getItem() + "' expires in seconds: " + remoteItem
          .getExpirationDetails().getSecondsUntilExpiration());
      }
      if (remoteItem.getExpirationDetails().hasExpired(false)) {
        expiredRemoteDevices.put(remoteItem.getKey(), remoteItem.getItem());
      }
    }
    for (??? = expiredRemoteDevices.values().iterator(); ???.hasNext();)
    {
      remoteDevice = (RemoteDevice)???.next();
      if (log.isLoggable(Level.FINE)) {
        log.fine("Removing expired: " + remoteDevice);
      }
      remove(remoteDevice);
    }
    RemoteDevice remoteDevice;
    Object expiredOutgoingSubscriptions = new HashSet();
    for (RegistryItem<String, RemoteGENASubscription> item : getSubscriptionItems()) {
      if (item.getExpirationDetails().hasExpired(true)) {
        ((Set)expiredOutgoingSubscriptions).add(item.getItem());
      }
    }
    for (RemoteGENASubscription subscription : (Set)expiredOutgoingSubscriptions)
    {
      if (log.isLoggable(Level.FINEST)) {
        log.fine("Renewing outgoing subscription: " + subscription);
      }
      renewOutgoingSubscription(subscription);
    }
  }
  
  public void resume()
  {
    log.fine("Updating remote device expiration timestamps on resume");
    List<RemoteDeviceIdentity> toUpdate = new ArrayList();
    for (RegistryItem<UDN, RemoteDevice> remoteItem : getDeviceItems()) {
      toUpdate.add(((RemoteDevice)remoteItem.getItem()).getIdentity());
    }
    for (RemoteDeviceIdentity identity : toUpdate) {
      update(identity);
    }
  }
  
  void shutdown()
  {
    log.fine("Cancelling all outgoing subscriptions to remote devices during shutdown");
    List<RemoteGENASubscription> remoteSubscriptions = new ArrayList();
    for (RegistryItem<String, RemoteGENASubscription> item : getSubscriptionItems()) {
      remoteSubscriptions.add(item.getItem());
    }
    for (RemoteGENASubscription remoteSubscription : remoteSubscriptions) {
      this.registry.getProtocolFactory().createSendingUnsubscribe(remoteSubscription).run();
    }
    log.fine("Removing all remote devices from registry during shutdown");
    removeAll(true);
  }
  
  protected void renewOutgoingSubscription(RemoteGENASubscription subscription)
  {
    this.registry.executeAsyncProtocol(this.registry
      .getProtocolFactory().createSendingRenewal(subscription));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\registry\RemoteItems.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */