package org.fourthline.cling.support.model.container;

import java.util.ArrayList;
import java.util.List;
import org.fourthline.cling.support.model.DIDLObject;
import org.fourthline.cling.support.model.DIDLObject.Class;
import org.fourthline.cling.support.model.DIDLObject.Property;
import org.fourthline.cling.support.model.DescMeta;
import org.fourthline.cling.support.model.Res;
import org.fourthline.cling.support.model.WriteStatus;
import org.fourthline.cling.support.model.item.Item;

public class Container
  extends DIDLObject
{
  protected Integer childCount = null;
  protected boolean searchable;
  protected List<DIDLObject.Class> createClasses = new ArrayList();
  protected List<DIDLObject.Class> searchClasses = new ArrayList();
  protected List<Container> containers = new ArrayList();
  protected List<Item> items = new ArrayList();
  
  public Container() {}
  
  public Container(Container other)
  {
    super(other);
    setChildCount(other.getChildCount());
    setSearchable(other.isSearchable());
    setCreateClasses(other.getCreateClasses());
    setSearchClasses(other.getSearchClasses());
    setItems(other.getItems());
  }
  
  public Container(String id, String parentID, String title, String creator, boolean restricted, WriteStatus writeStatus, DIDLObject.Class clazz, List<Res> resources, List<DIDLObject.Property> properties, List<DescMeta> descMetadata)
  {
    super(id, parentID, title, creator, restricted, writeStatus, clazz, resources, properties, descMetadata);
  }
  
  public Container(String id, String parentID, String title, String creator, boolean restricted, WriteStatus writeStatus, DIDLObject.Class clazz, List<Res> resources, List<DIDLObject.Property> properties, List<DescMeta> descMetadata, Integer childCount, boolean searchable, List<DIDLObject.Class> createClasses, List<DIDLObject.Class> searchClasses, List<Item> items)
  {
    super(id, parentID, title, creator, restricted, writeStatus, clazz, resources, properties, descMetadata);
    this.childCount = childCount;
    this.searchable = searchable;
    this.createClasses = createClasses;
    this.searchClasses = searchClasses;
    this.items = items;
  }
  
  public Container(String id, Container parent, String title, String creator, DIDLObject.Class clazz, Integer childCount)
  {
    this(id, parent.getId(), title, creator, true, null, clazz, new ArrayList(), new ArrayList(), new ArrayList(), childCount, false, new ArrayList(), new ArrayList(), new ArrayList());
  }
  
  public Container(String id, String parentID, String title, String creator, DIDLObject.Class clazz, Integer childCount)
  {
    this(id, parentID, title, creator, true, null, clazz, new ArrayList(), new ArrayList(), new ArrayList(), childCount, false, new ArrayList(), new ArrayList(), new ArrayList());
  }
  
  public Container(String id, Container parent, String title, String creator, DIDLObject.Class clazz, Integer childCount, boolean searchable, List<DIDLObject.Class> createClasses, List<DIDLObject.Class> searchClasses, List<Item> items)
  {
    this(id, parent.getId(), title, creator, true, null, clazz, new ArrayList(), new ArrayList(), new ArrayList(), childCount, searchable, createClasses, searchClasses, items);
  }
  
  public Container(String id, String parentID, String title, String creator, DIDLObject.Class clazz, Integer childCount, boolean searchable, List<DIDLObject.Class> createClasses, List<DIDLObject.Class> searchClasses, List<Item> items)
  {
    this(id, parentID, title, creator, true, null, clazz, new ArrayList(), new ArrayList(), new ArrayList(), childCount, searchable, createClasses, searchClasses, items);
  }
  
  public Integer getChildCount()
  {
    return this.childCount;
  }
  
  public void setChildCount(Integer childCount)
  {
    this.childCount = childCount;
  }
  
  public boolean isSearchable()
  {
    return this.searchable;
  }
  
  public void setSearchable(boolean searchable)
  {
    this.searchable = searchable;
  }
  
  public List<DIDLObject.Class> getCreateClasses()
  {
    return this.createClasses;
  }
  
  public void setCreateClasses(List<DIDLObject.Class> createClasses)
  {
    this.createClasses = createClasses;
  }
  
  public List<DIDLObject.Class> getSearchClasses()
  {
    return this.searchClasses;
  }
  
  public void setSearchClasses(List<DIDLObject.Class> searchClasses)
  {
    this.searchClasses = searchClasses;
  }
  
  public Container getFirstContainer()
  {
    return (Container)getContainers().get(0);
  }
  
  public Container addContainer(Container container)
  {
    getContainers().add(container);
    return this;
  }
  
  public List<Container> getContainers()
  {
    return this.containers;
  }
  
  public void setContainers(List<Container> containers)
  {
    this.containers = containers;
  }
  
  public List<Item> getItems()
  {
    return this.items;
  }
  
  public void setItems(List<Item> items)
  {
    this.items = items;
  }
  
  public Container addItem(Item item)
  {
    getItems().add(item);
    return this;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\container\Container.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */