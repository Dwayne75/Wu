package com.wurmonline.server.epic;

import java.util.ArrayList;
import java.util.List;

public class CollectedValreiItem
{
  private final long id;
  private final String nameOfCollected;
  private final int typeOfCollected;
  
  public CollectedValreiItem(long _id, String _name, int _type)
  {
    this.id = _id;
    this.nameOfCollected = _name;
    this.typeOfCollected = _type;
  }
  
  public final long getId()
  {
    return this.id;
  }
  
  public final String getName()
  {
    return this.nameOfCollected;
  }
  
  public final int getType()
  {
    return this.typeOfCollected;
  }
  
  public boolean equals(Object obj)
  {
    if (!(obj instanceof CollectedValreiItem)) {
      return false;
    }
    CollectedValreiItem cmp = (CollectedValreiItem)obj;
    
    return (cmp.getId() == this.id) && (cmp.getName().equals(this.nameOfCollected)) && (cmp.typeOfCollected == this.typeOfCollected);
  }
  
  public int hashCode()
  {
    int prime = 31;
    int result = 1;
    result = 31 * result + (int)(this.id ^ this.id >>> 32);
    result = 31 * result + (this.nameOfCollected == null ? 0 : this.nameOfCollected.hashCode());
    result = 31 * result + this.typeOfCollected;
    return result;
  }
  
  public static List<CollectedValreiItem> fromList(List<EpicEntity> entities)
  {
    List<CollectedValreiItem> list = new ArrayList();
    if (entities != null) {
      for (EpicEntity ent : entities) {
        list.add(new CollectedValreiItem(ent.getId(), ent.getName(), ent.getType()));
      }
    }
    return list;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\epic\CollectedValreiItem.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */