package org.fourthline.cling.registry;

import org.fourthline.cling.model.ExpirationDetails;

class RegistryItem<K, I>
{
  private K key;
  private I item;
  private ExpirationDetails expirationDetails = new ExpirationDetails();
  
  RegistryItem(K key)
  {
    this.key = key;
  }
  
  RegistryItem(K key, I item, int maxAgeSeconds)
  {
    this.key = key;
    this.item = item;
    this.expirationDetails = new ExpirationDetails(maxAgeSeconds);
  }
  
  public K getKey()
  {
    return (K)this.key;
  }
  
  public I getItem()
  {
    return (I)this.item;
  }
  
  public ExpirationDetails getExpirationDetails()
  {
    return this.expirationDetails;
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    RegistryItem that = (RegistryItem)o;
    
    return this.key.equals(that.key);
  }
  
  public int hashCode()
  {
    return this.key.hashCode();
  }
  
  public String toString()
  {
    return "(" + getClass().getSimpleName() + ") " + getExpirationDetails() + " KEY: " + getKey() + " ITEM: " + getItem();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\registry\RegistryItem.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */