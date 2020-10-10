package org.fourthline.cling.support.model.container;

import org.fourthline.cling.support.model.DIDLObject.Class;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.STORAGE_FREE;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.STORAGE_MAX_PARTITION;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.STORAGE_MEDIUM;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.STORAGE_TOTAL;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.STORAGE_USED;
import org.fourthline.cling.support.model.StorageMedium;

public class StorageSystem
  extends Container
{
  public static final DIDLObject.Class CLASS = new DIDLObject.Class("object.container.storageSystem");
  
  public StorageSystem()
  {
    setClazz(CLASS);
  }
  
  public StorageSystem(Container other)
  {
    super(other);
  }
  
  public StorageSystem(String id, Container parent, String title, String creator, Integer childCount, Long storageTotal, Long storageUsed, Long storageFree, Long storageMaxPartition, StorageMedium storageMedium)
  {
    this(id, parent.getId(), title, creator, childCount, storageTotal, storageUsed, storageFree, storageMaxPartition, storageMedium);
  }
  
  public StorageSystem(String id, String parentID, String title, String creator, Integer childCount, Long storageTotal, Long storageUsed, Long storageFree, Long storageMaxPartition, StorageMedium storageMedium)
  {
    super(id, parentID, title, creator, CLASS, childCount);
    if (storageTotal != null) {
      setStorageTotal(storageTotal);
    }
    if (storageUsed != null) {
      setStorageUsed(storageUsed);
    }
    if (storageFree != null) {
      setStorageFree(storageFree);
    }
    if (storageMaxPartition != null) {
      setStorageMaxPartition(storageMaxPartition);
    }
    if (storageMedium != null) {
      setStorageMedium(storageMedium);
    }
  }
  
  public Long getStorageTotal()
  {
    return (Long)getFirstPropertyValue(DIDLObject.Property.UPNP.STORAGE_TOTAL.class);
  }
  
  public StorageSystem setStorageTotal(Long l)
  {
    replaceFirstProperty(new DIDLObject.Property.UPNP.STORAGE_TOTAL(l));
    return this;
  }
  
  public Long getStorageUsed()
  {
    return (Long)getFirstPropertyValue(DIDLObject.Property.UPNP.STORAGE_USED.class);
  }
  
  public StorageSystem setStorageUsed(Long l)
  {
    replaceFirstProperty(new DIDLObject.Property.UPNP.STORAGE_USED(l));
    return this;
  }
  
  public Long getStorageFree()
  {
    return (Long)getFirstPropertyValue(DIDLObject.Property.UPNP.STORAGE_FREE.class);
  }
  
  public StorageSystem setStorageFree(Long l)
  {
    replaceFirstProperty(new DIDLObject.Property.UPNP.STORAGE_FREE(l));
    return this;
  }
  
  public Long getStorageMaxPartition()
  {
    return (Long)getFirstPropertyValue(DIDLObject.Property.UPNP.STORAGE_MAX_PARTITION.class);
  }
  
  public StorageSystem setStorageMaxPartition(Long l)
  {
    replaceFirstProperty(new DIDLObject.Property.UPNP.STORAGE_MAX_PARTITION(l));
    return this;
  }
  
  public StorageMedium getStorageMedium()
  {
    return (StorageMedium)getFirstPropertyValue(DIDLObject.Property.UPNP.STORAGE_MEDIUM.class);
  }
  
  public StorageSystem setStorageMedium(StorageMedium storageMedium)
  {
    replaceFirstProperty(new DIDLObject.Property.UPNP.STORAGE_MEDIUM(storageMedium));
    return this;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\container\StorageSystem.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */