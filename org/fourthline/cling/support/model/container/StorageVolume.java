package org.fourthline.cling.support.model.container;

import org.fourthline.cling.support.model.DIDLObject.Class;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.STORAGE_FREE;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.STORAGE_MEDIUM;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.STORAGE_TOTAL;
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.STORAGE_USED;
import org.fourthline.cling.support.model.StorageMedium;

public class StorageVolume
  extends Container
{
  public static final DIDLObject.Class CLASS = new DIDLObject.Class("object.container.storageVolume");
  
  public StorageVolume()
  {
    setClazz(CLASS);
  }
  
  public StorageVolume(Container other)
  {
    super(other);
  }
  
  public StorageVolume(String id, Container parent, String title, String creator, Integer childCount, Long storageTotal, Long storageUsed, Long storageFree, StorageMedium storageMedium)
  {
    this(id, parent.getId(), title, creator, childCount, storageTotal, storageUsed, storageFree, storageMedium);
  }
  
  public StorageVolume(String id, String parentID, String title, String creator, Integer childCount, Long storageTotal, Long storageUsed, Long storageFree, StorageMedium storageMedium)
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
    if (storageMedium != null) {
      setStorageMedium(storageMedium);
    }
  }
  
  public Long getStorageTotal()
  {
    return (Long)getFirstPropertyValue(DIDLObject.Property.UPNP.STORAGE_TOTAL.class);
  }
  
  public StorageVolume setStorageTotal(Long l)
  {
    replaceFirstProperty(new DIDLObject.Property.UPNP.STORAGE_TOTAL(l));
    return this;
  }
  
  public Long getStorageUsed()
  {
    return (Long)getFirstPropertyValue(DIDLObject.Property.UPNP.STORAGE_USED.class);
  }
  
  public StorageVolume setStorageUsed(Long l)
  {
    replaceFirstProperty(new DIDLObject.Property.UPNP.STORAGE_USED(l));
    return this;
  }
  
  public Long getStorageFree()
  {
    return (Long)getFirstPropertyValue(DIDLObject.Property.UPNP.STORAGE_FREE.class);
  }
  
  public StorageVolume setStorageFree(Long l)
  {
    replaceFirstProperty(new DIDLObject.Property.UPNP.STORAGE_FREE(l));
    return this;
  }
  
  public StorageMedium getStorageMedium()
  {
    return (StorageMedium)getFirstPropertyValue(DIDLObject.Property.UPNP.STORAGE_MEDIUM.class);
  }
  
  public StorageVolume setStorageMedium(StorageMedium storageMedium)
  {
    replaceFirstProperty(new DIDLObject.Property.UPNP.STORAGE_MEDIUM(storageMedium));
    return this;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\container\StorageVolume.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */