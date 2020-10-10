package org.fourthline.cling.registry;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.model.DiscoveryOptions;
import org.fourthline.cling.model.ExpirationDetails;
import org.fourthline.cling.model.ServiceReference;
import org.fourthline.cling.model.gena.LocalGENASubscription;
import org.fourthline.cling.model.gena.RemoteGENASubscription;
import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.model.meta.RemoteDeviceIdentity;
import org.fourthline.cling.model.meta.Service;
import org.fourthline.cling.model.resource.Resource;
import org.fourthline.cling.model.types.DeviceType;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDN;
import org.fourthline.cling.protocol.ProtocolFactory;

@ApplicationScoped
public class RegistryImpl
  implements Registry
{
  private static Logger log = Logger.getLogger(Registry.class.getName());
  protected UpnpService upnpService;
  protected RegistryMaintainer registryMaintainer;
  protected final Set<RemoteGENASubscription> pendingSubscriptionsLock = new HashSet();
  
  public RegistryImpl() {}
  
  @Inject
  public RegistryImpl(UpnpService upnpService)
  {
    log.fine("Creating Registry: " + getClass().getName());
    
    this.upnpService = upnpService;
    
    log.fine("Starting registry background maintenance...");
    this.registryMaintainer = createRegistryMaintainer();
    if (this.registryMaintainer != null) {
      getConfiguration().getRegistryMaintainerExecutor().execute(this.registryMaintainer);
    }
  }
  
  public UpnpService getUpnpService()
  {
    return this.upnpService;
  }
  
  public UpnpServiceConfiguration getConfiguration()
  {
    return getUpnpService().getConfiguration();
  }
  
  public ProtocolFactory getProtocolFactory()
  {
    return getUpnpService().getProtocolFactory();
  }
  
  protected RegistryMaintainer createRegistryMaintainer()
  {
    return new RegistryMaintainer(this, getConfiguration().getRegistryMaintenanceIntervalMillis());
  }
  
  protected final Set<RegistryListener> registryListeners = new HashSet();
  protected final Set<RegistryItem<URI, Resource>> resourceItems = new HashSet();
  protected final List<Runnable> pendingExecutions = new ArrayList();
  protected final RemoteItems remoteItems = new RemoteItems(this);
  protected final LocalItems localItems = new LocalItems(this);
  
  public synchronized void addListener(RegistryListener listener)
  {
    this.registryListeners.add(listener);
  }
  
  public synchronized void removeListener(RegistryListener listener)
  {
    this.registryListeners.remove(listener);
  }
  
  public synchronized Collection<RegistryListener> getListeners()
  {
    return Collections.unmodifiableCollection(this.registryListeners);
  }
  
  public synchronized boolean notifyDiscoveryStart(final RemoteDevice device)
  {
    if (getUpnpService().getRegistry().getRemoteDevice(((RemoteDeviceIdentity)device.getIdentity()).getUdn(), true) != null)
    {
      log.finer("Not notifying listeners, already registered: " + device);
      return false;
    }
    for (final RegistryListener listener : getListeners()) {
      getConfiguration().getRegistryListenerExecutor().execute(new Runnable()
      {
        public void run()
        {
          listener.remoteDeviceDiscoveryStarted(RegistryImpl.this, device);
        }
      });
    }
    return true;
  }
  
  public synchronized void notifyDiscoveryFailure(final RemoteDevice device, final Exception ex)
  {
    for (final RegistryListener listener : getListeners()) {
      getConfiguration().getRegistryListenerExecutor().execute(new Runnable()
      {
        public void run()
        {
          listener.remoteDeviceDiscoveryFailed(RegistryImpl.this, device, ex);
        }
      });
    }
  }
  
  public synchronized void addDevice(LocalDevice localDevice)
  {
    this.localItems.add(localDevice);
  }
  
  public synchronized void addDevice(LocalDevice localDevice, DiscoveryOptions options)
  {
    this.localItems.add(localDevice, options);
  }
  
  public synchronized void setDiscoveryOptions(UDN udn, DiscoveryOptions options)
  {
    this.localItems.setDiscoveryOptions(udn, options);
  }
  
  public synchronized DiscoveryOptions getDiscoveryOptions(UDN udn)
  {
    return this.localItems.getDiscoveryOptions(udn);
  }
  
  public synchronized void addDevice(RemoteDevice remoteDevice)
  {
    this.remoteItems.add(remoteDevice);
  }
  
  public synchronized boolean update(RemoteDeviceIdentity rdIdentity)
  {
    return this.remoteItems.update(rdIdentity);
  }
  
  public synchronized boolean removeDevice(LocalDevice localDevice)
  {
    return this.localItems.remove(localDevice);
  }
  
  public synchronized boolean removeDevice(RemoteDevice remoteDevice)
  {
    return this.remoteItems.remove(remoteDevice);
  }
  
  public synchronized void removeAllLocalDevices()
  {
    this.localItems.removeAll();
  }
  
  public synchronized void removeAllRemoteDevices()
  {
    this.remoteItems.removeAll();
  }
  
  public synchronized boolean removeDevice(UDN udn)
  {
    Device device = getDevice(udn, true);
    if ((device != null) && ((device instanceof LocalDevice))) {
      return removeDevice((LocalDevice)device);
    }
    if ((device != null) && ((device instanceof RemoteDevice))) {
      return removeDevice((RemoteDevice)device);
    }
    return false;
  }
  
  public synchronized Device getDevice(UDN udn, boolean rootOnly)
  {
    Device device;
    if ((device = this.localItems.get(udn, rootOnly)) != null) {
      return device;
    }
    if ((device = this.remoteItems.get(udn, rootOnly)) != null) {
      return device;
    }
    return null;
  }
  
  public synchronized LocalDevice getLocalDevice(UDN udn, boolean rootOnly)
  {
    return (LocalDevice)this.localItems.get(udn, rootOnly);
  }
  
  public synchronized RemoteDevice getRemoteDevice(UDN udn, boolean rootOnly)
  {
    return (RemoteDevice)this.remoteItems.get(udn, rootOnly);
  }
  
  public synchronized Collection<LocalDevice> getLocalDevices()
  {
    return Collections.unmodifiableCollection(this.localItems.get());
  }
  
  public synchronized Collection<RemoteDevice> getRemoteDevices()
  {
    return Collections.unmodifiableCollection(this.remoteItems.get());
  }
  
  public synchronized Collection<Device> getDevices()
  {
    Set all = new HashSet();
    all.addAll(this.localItems.get());
    all.addAll(this.remoteItems.get());
    return Collections.unmodifiableCollection(all);
  }
  
  public synchronized Collection<Device> getDevices(DeviceType deviceType)
  {
    Collection<Device> devices = new HashSet();
    
    devices.addAll(this.localItems.get(deviceType));
    devices.addAll(this.remoteItems.get(deviceType));
    
    return Collections.unmodifiableCollection(devices);
  }
  
  public synchronized Collection<Device> getDevices(ServiceType serviceType)
  {
    Collection<Device> devices = new HashSet();
    
    devices.addAll(this.localItems.get(serviceType));
    devices.addAll(this.remoteItems.get(serviceType));
    
    return Collections.unmodifiableCollection(devices);
  }
  
  public synchronized Service getService(ServiceReference serviceReference)
  {
    Device device;
    if ((device = getDevice(serviceReference.getUdn(), false)) != null) {
      return device.findService(serviceReference.getServiceId());
    }
    return null;
  }
  
  public synchronized Resource getResource(URI pathQuery)
    throws IllegalArgumentException
  {
    if (pathQuery.isAbsolute()) {
      throw new IllegalArgumentException("Resource URI can not be absolute, only path and query:" + pathQuery);
    }
    for (Iterator localIterator = this.resourceItems.iterator(); localIterator.hasNext();)
    {
      resourceItem = (RegistryItem)localIterator.next();
      Resource resource = (Resource)resourceItem.getItem();
      if (resource.matches(pathQuery)) {
        return resource;
      }
    }
    RegistryItem<URI, Resource> resourceItem;
    URI pathQueryWithoutSlash;
    if (pathQuery.getPath().endsWith("/"))
    {
      pathQueryWithoutSlash = URI.create(pathQuery.toString().substring(0, pathQuery.toString().length() - 1));
      for (RegistryItem<URI, Resource> resourceItem : this.resourceItems)
      {
        Resource resource = (Resource)resourceItem.getItem();
        if (resource.matches(pathQueryWithoutSlash)) {
          return resource;
        }
      }
    }
    return null;
  }
  
  public synchronized <T extends Resource> T getResource(Class<T> resourceType, URI pathQuery)
    throws IllegalArgumentException
  {
    Resource resource = getResource(pathQuery);
    if ((resource != null) && (resourceType.isAssignableFrom(resource.getClass()))) {
      return resource;
    }
    return null;
  }
  
  public synchronized Collection<Resource> getResources()
  {
    Collection<Resource> s = new HashSet();
    for (RegistryItem<URI, Resource> resourceItem : this.resourceItems) {
      s.add(resourceItem.getItem());
    }
    return s;
  }
  
  public synchronized <T extends Resource> Collection<T> getResources(Class<T> resourceType)
  {
    Collection<T> s = new HashSet();
    for (RegistryItem<URI, Resource> resourceItem : this.resourceItems) {
      if (resourceType.isAssignableFrom(((Resource)resourceItem.getItem()).getClass())) {
        s.add((Resource)resourceItem.getItem());
      }
    }
    return s;
  }
  
  public synchronized void addResource(Resource resource)
  {
    addResource(resource, 0);
  }
  
  public synchronized void addResource(Resource resource, int maxAgeSeconds)
  {
    RegistryItem resourceItem = new RegistryItem(resource.getPathQuery(), resource, maxAgeSeconds);
    this.resourceItems.remove(resourceItem);
    this.resourceItems.add(resourceItem);
  }
  
  public synchronized boolean removeResource(Resource resource)
  {
    return this.resourceItems.remove(new RegistryItem(resource.getPathQuery()));
  }
  
  public synchronized void addLocalSubscription(LocalGENASubscription subscription)
  {
    this.localItems.addSubscription(subscription);
  }
  
  public synchronized LocalGENASubscription getLocalSubscription(String subscriptionId)
  {
    return (LocalGENASubscription)this.localItems.getSubscription(subscriptionId);
  }
  
  public synchronized boolean updateLocalSubscription(LocalGENASubscription subscription)
  {
    return this.localItems.updateSubscription(subscription);
  }
  
  public synchronized boolean removeLocalSubscription(LocalGENASubscription subscription)
  {
    return this.localItems.removeSubscription(subscription);
  }
  
  public synchronized void addRemoteSubscription(RemoteGENASubscription subscription)
  {
    this.remoteItems.addSubscription(subscription);
  }
  
  public synchronized RemoteGENASubscription getRemoteSubscription(String subscriptionId)
  {
    return (RemoteGENASubscription)this.remoteItems.getSubscription(subscriptionId);
  }
  
  public synchronized void updateRemoteSubscription(RemoteGENASubscription subscription)
  {
    this.remoteItems.updateSubscription(subscription);
  }
  
  public synchronized void removeRemoteSubscription(RemoteGENASubscription subscription)
  {
    this.remoteItems.removeSubscription(subscription);
  }
  
  public synchronized void advertiseLocalDevices()
  {
    this.localItems.advertiseLocalDevices();
  }
  
  public synchronized void shutdown()
  {
    log.fine("Shutting down registry...");
    if (this.registryMaintainer != null) {
      this.registryMaintainer.stop();
    }
    log.finest("Executing final pending operations on shutdown: " + this.pendingExecutions.size());
    runPendingExecutions(false);
    for (Iterator localIterator = this.registryListeners.iterator(); localIterator.hasNext();)
    {
      listener = (RegistryListener)localIterator.next();
      listener.beforeShutdown(this);
    }
    RegistryListener listener;
    Object resources = (RegistryItem[])this.resourceItems.toArray(new RegistryItem[this.resourceItems.size()]);
    for (RegistryItem<URI, Resource> resourceItem : (RegistryListener)resources) {
      ((Resource)resourceItem.getItem()).shutdown();
    }
    this.remoteItems.shutdown();
    this.localItems.shutdown();
    for (RegistryListener listener : this.registryListeners) {
      listener.afterShutdown();
    }
  }
  
  public synchronized void pause()
  {
    if (this.registryMaintainer != null)
    {
      log.fine("Pausing registry maintenance");
      runPendingExecutions(true);
      this.registryMaintainer.stop();
      this.registryMaintainer = null;
    }
  }
  
  public synchronized void resume()
  {
    if (this.registryMaintainer == null)
    {
      log.fine("Resuming registry maintenance");
      this.remoteItems.resume();
      this.registryMaintainer = createRegistryMaintainer();
      if (this.registryMaintainer != null) {
        getConfiguration().getRegistryMaintainerExecutor().execute(this.registryMaintainer);
      }
    }
  }
  
  public synchronized boolean isPaused()
  {
    return this.registryMaintainer == null;
  }
  
  synchronized void maintain()
  {
    if (log.isLoggable(Level.FINEST)) {
      log.finest("Maintaining registry...");
    }
    Iterator<RegistryItem<URI, Resource>> it = this.resourceItems.iterator();
    RegistryItem<URI, Resource> item;
    while (it.hasNext())
    {
      item = (RegistryItem)it.next();
      if (item.getExpirationDetails().hasExpired())
      {
        if (log.isLoggable(Level.FINER)) {
          log.finer("Removing expired resource: " + item);
        }
        it.remove();
      }
    }
    for (RegistryItem<URI, Resource> resourceItem : this.resourceItems) {
      ((Resource)resourceItem.getItem()).maintain(this.pendingExecutions, resourceItem
      
        .getExpirationDetails());
    }
    this.remoteItems.maintain();
    this.localItems.maintain();
    
    runPendingExecutions(true);
  }
  
  synchronized void executeAsyncProtocol(Runnable runnable)
  {
    this.pendingExecutions.add(runnable);
  }
  
  synchronized void runPendingExecutions(boolean async)
  {
    if (log.isLoggable(Level.FINEST)) {
      log.finest("Executing pending operations: " + this.pendingExecutions.size());
    }
    for (Runnable pendingExecution : this.pendingExecutions) {
      if (async) {
        getConfiguration().getAsyncProtocolExecutor().execute(pendingExecution);
      } else {
        pendingExecution.run();
      }
    }
    if (this.pendingExecutions.size() > 0) {
      this.pendingExecutions.clear();
    }
  }
  
  public void printDebugLog()
  {
    if (log.isLoggable(Level.FINE))
    {
      log.fine("====================================    REMOTE   ================================================");
      for (RemoteDevice remoteDevice : this.remoteItems.get()) {
        log.fine(remoteDevice.toString());
      }
      log.fine("====================================    LOCAL    ================================================");
      for (LocalDevice localDevice : this.localItems.get()) {
        log.fine(localDevice.toString());
      }
      log.fine("====================================  RESOURCES  ================================================");
      for (RegistryItem<URI, Resource> resourceItem : this.resourceItems) {
        log.fine(resourceItem.toString());
      }
      log.fine("=================================================================================================");
    }
  }
  
  public void registerPendingRemoteSubscription(RemoteGENASubscription subscription)
  {
    synchronized (this.pendingSubscriptionsLock)
    {
      this.pendingSubscriptionsLock.add(subscription);
    }
  }
  
  public void unregisterPendingRemoteSubscription(RemoteGENASubscription subscription)
  {
    synchronized (this.pendingSubscriptionsLock)
    {
      if (this.pendingSubscriptionsLock.remove(subscription)) {
        this.pendingSubscriptionsLock.notifyAll();
      }
    }
  }
  
  public RemoteGENASubscription getWaitRemoteSubscription(String subscriptionId)
  {
    synchronized (this.pendingSubscriptionsLock)
    {
      RemoteGENASubscription subscription = getRemoteSubscription(subscriptionId);
      while ((subscription == null) && (!this.pendingSubscriptionsLock.isEmpty()))
      {
        try
        {
          log.finest("Subscription not found, waiting for pending subscription procedure to terminate.");
          this.pendingSubscriptionsLock.wait();
        }
        catch (InterruptedException localInterruptedException) {}
        subscription = getRemoteSubscription(subscriptionId);
      }
      return subscription;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\registry\RegistryImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */