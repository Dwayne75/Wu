package org.fourthline.cling.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.logging.Logger;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.DiscoveryOptions;
import org.fourthline.cling.model.ExpirationDetails;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.gena.LocalGENASubscription;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.DeviceIdentity;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.LocalService;
import org.fourthline.cling.model.resource.Resource;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.protocol.SendingAsync;
import org.fourthline.cling.protocol.async.SendingNotificationAlive;

class LocalItems
  extends RegistryItems<LocalDevice, LocalGENASubscription>
{
  private static Logger log = Logger.getLogger(Registry.class.getName());
  protected Map<UDN, DiscoveryOptions> discoveryOptions = new HashMap();
  protected long lastAliveIntervalTimestamp = 0L;
  
  LocalItems(RegistryImpl registry)
  {
    super(registry);
  }
  
  protected void setDiscoveryOptions(UDN udn, DiscoveryOptions options)
  {
    if (options != null) {
      this.discoveryOptions.put(udn, options);
    } else {
      this.discoveryOptions.remove(udn);
    }
  }
  
  protected DiscoveryOptions getDiscoveryOptions(UDN udn)
  {
    return (DiscoveryOptions)this.discoveryOptions.get(udn);
  }
  
  protected boolean isAdvertised(UDN udn)
  {
    return (getDiscoveryOptions(udn) == null) || (getDiscoveryOptions(udn).isAdvertised());
  }
  
  protected boolean isByeByeBeforeFirstAlive(UDN udn)
  {
    return (getDiscoveryOptions(udn) != null) && (getDiscoveryOptions(udn).isByeByeBeforeFirstAlive());
  }
  
  void add(LocalDevice localDevice)
    throws RegistrationException
  {
    add(localDevice, null);
  }
  
  void add(final LocalDevice localDevice, DiscoveryOptions options)
    throws RegistrationException
  {
    setDiscoveryOptions(localDevice.getIdentity().getUdn(), options);
    if (this.registry.getDevice(localDevice.getIdentity().getUdn(), false) != null)
    {
      log.fine("Ignoring addition, device already registered: " + localDevice);
      return;
    }
    log.fine("Adding local device to registry: " + localDevice);
    for (Resource deviceResource : getResources(localDevice))
    {
      if (this.registry.getResource(deviceResource.getPathQuery()) != null) {
        throw new RegistrationException("URI namespace conflict with already registered resource: " + deviceResource);
      }
      this.registry.addResource(deviceResource);
      log.fine("Registered resource: " + deviceResource);
    }
    log.fine("Adding item to registry with expiration in seconds: " + localDevice.getIdentity().getMaxAgeSeconds());
    
    Object localItem = new RegistryItem(localDevice.getIdentity().getUdn(), localDevice, localDevice.getIdentity().getMaxAgeSeconds().intValue());
    
    getDeviceItems().add(localItem);
    log.fine("Registered local device: " + localItem);
    if (isByeByeBeforeFirstAlive((UDN)((RegistryItem)localItem).getKey())) {
      advertiseByebye(localDevice, true);
    }
    if (isAdvertised((UDN)((RegistryItem)localItem).getKey())) {
      advertiseAlive(localDevice);
    }
    for (final RegistryListener listener : this.registry.getListeners()) {
      this.registry.getConfiguration().getRegistryListenerExecutor().execute(new Runnable()
      {
        public void run()
        {
          listener.localDeviceAdded(LocalItems.this.registry, localDevice);
        }
      });
    }
  }
  
  Collection<LocalDevice> get()
  {
    Set<LocalDevice> c = new HashSet();
    for (RegistryItem<UDN, LocalDevice> item : getDeviceItems()) {
      c.add(item.getItem());
    }
    return Collections.unmodifiableCollection(c);
  }
  
  boolean remove(LocalDevice localDevice)
    throws RegistrationException
  {
    return remove(localDevice, false);
  }
  
  boolean remove(final LocalDevice localDevice, boolean shuttingDown)
    throws RegistrationException
  {
    LocalDevice registeredDevice = (LocalDevice)get(localDevice.getIdentity().getUdn(), true);
    if (registeredDevice != null)
    {
      log.fine("Removing local device from registry: " + localDevice);
      
      setDiscoveryOptions(localDevice.getIdentity().getUdn(), null);
      getDeviceItems().remove(new RegistryItem(localDevice.getIdentity().getUdn()));
      for (Resource deviceResource : getResources(localDevice)) {
        if (this.registry.removeResource(deviceResource)) {
          log.fine("Unregistered resource: " + deviceResource);
        }
      }
      Object it = getSubscriptionItems().iterator();
      final Object incomingSubscription;
      while (((Iterator)it).hasNext())
      {
        incomingSubscription = (RegistryItem)((Iterator)it).next();
        
        UDN subscriptionForUDN = ((LocalService)((LocalGENASubscription)((RegistryItem)incomingSubscription).getItem()).getService()).getDevice().getIdentity().getUdn();
        if (subscriptionForUDN.equals(registeredDevice.getIdentity().getUdn()))
        {
          log.fine("Removing incoming subscription: " + (String)((RegistryItem)incomingSubscription).getKey());
          ((Iterator)it).remove();
          if (!shuttingDown) {
            this.registry.getConfiguration().getRegistryListenerExecutor().execute(new Runnable()
            {
              public void run()
              {
                ((LocalGENASubscription)incomingSubscription.getItem()).end(CancelReason.DEVICE_WAS_REMOVED);
              }
            });
          }
        }
      }
      if (isAdvertised(localDevice.getIdentity().getUdn())) {
        advertiseByebye(localDevice, !shuttingDown);
      }
      if (!shuttingDown) {
        for (incomingSubscription = this.registry.getListeners().iterator(); ((Iterator)incomingSubscription).hasNext();)
        {
          final RegistryListener listener = (RegistryListener)((Iterator)incomingSubscription).next();
          this.registry.getConfiguration().getRegistryListenerExecutor().execute(new Runnable()
          {
            public void run()
            {
              listener.localDeviceRemoved(LocalItems.this.registry, localDevice);
            }
          });
        }
      }
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
    LocalDevice[] allDevices = (LocalDevice[])get().toArray(new LocalDevice[get().size()]);
    for (LocalDevice device : allDevices) {
      remove(device, shuttingDown);
    }
  }
  
  public void advertiseLocalDevices()
  {
    for (RegistryItem<UDN, LocalDevice> localItem : this.deviceItems) {
      if (isAdvertised((UDN)localItem.getKey())) {
        advertiseAlive((LocalDevice)localItem.getItem());
      }
    }
  }
  
  void maintain()
  {
    if (getDeviceItems().isEmpty()) {
      return;
    }
    Set<RegistryItem<UDN, LocalDevice>> expiredLocalItems = new HashSet();
    
    int aliveIntervalMillis = this.registry.getConfiguration().getAliveIntervalMillis();
    if (aliveIntervalMillis > 0)
    {
      now = System.currentTimeMillis();
      if (now - this.lastAliveIntervalTimestamp > aliveIntervalMillis)
      {
        this.lastAliveIntervalTimestamp = now;
        for (RegistryItem<UDN, LocalDevice> localItem : getDeviceItems()) {
          if (isAdvertised((UDN)localItem.getKey()))
          {
            log.finer("Flooding advertisement of local item: " + localItem);
            expiredLocalItems.add(localItem);
          }
        }
      }
    }
    else
    {
      this.lastAliveIntervalTimestamp = 0L;
      for (RegistryItem<UDN, LocalDevice> localItem : getDeviceItems()) {
        if ((isAdvertised((UDN)localItem.getKey())) && (localItem.getExpirationDetails().hasExpired(true)))
        {
          log.finer("Local item has expired: " + localItem);
          expiredLocalItems.add(localItem);
        }
      }
    }
    for (long now = expiredLocalItems.iterator(); now.hasNext();)
    {
      expiredLocalItem = (RegistryItem)now.next();
      log.fine("Refreshing local device advertisement: " + expiredLocalItem.getItem());
      advertiseAlive((LocalDevice)expiredLocalItem.getItem());
      expiredLocalItem.getExpirationDetails().stampLastRefresh();
    }
    RegistryItem<UDN, LocalDevice> expiredLocalItem;
    Set<RegistryItem<String, LocalGENASubscription>> expiredIncomingSubscriptions = new HashSet();
    for (Object item : getSubscriptionItems()) {
      if (((RegistryItem)item).getExpirationDetails().hasExpired(false)) {
        expiredIncomingSubscriptions.add(item);
      }
    }
    for (Object subscription : expiredIncomingSubscriptions)
    {
      log.fine("Removing expired: " + subscription);
      removeSubscription((GENASubscription)((RegistryItem)subscription).getItem());
      ((LocalGENASubscription)((RegistryItem)subscription).getItem()).end(CancelReason.EXPIRED);
    }
  }
  
  void shutdown()
  {
    log.fine("Clearing all registered subscriptions to local devices during shutdown");
    getSubscriptionItems().clear();
    
    log.fine("Removing all local devices from registry during shutdown");
    removeAll(true);
  }
  
  protected Random randomGenerator = new Random();
  
  protected void advertiseAlive(final LocalDevice localDevice)
  {
    this.registry.executeAsyncProtocol(new Runnable()
    {
      public void run()
      {
        try
        {
          LocalItems.log.finer("Sleeping some milliseconds to avoid flooding the network with ALIVE msgs");
          Thread.sleep(LocalItems.this.randomGenerator.nextInt(100));
        }
        catch (InterruptedException ex)
        {
          LocalItems.log.severe("Background execution interrupted: " + ex.getMessage());
        }
        LocalItems.this.registry.getProtocolFactory().createSendingNotificationAlive(localDevice).run();
      }
    });
  }
  
  protected void advertiseByebye(LocalDevice localDevice, boolean asynchronous)
  {
    SendingAsync prot = this.registry.getProtocolFactory().createSendingNotificationByebye(localDevice);
    if (asynchronous) {
      this.registry.executeAsyncProtocol(prot);
    } else {
      prot.run();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\registry\LocalItems.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */