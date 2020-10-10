package com.wurmonline.server.utils;

public class ItemLastOwnerDatabaseUpdatable
  implements WurmDbUpdatable
{
  private final long id;
  private final long owner;
  private final String updateStatement;
  
  public ItemLastOwnerDatabaseUpdatable(long aId, long aOwner, String aUpdateStatement)
  {
    this.id = aId;
    this.owner = aOwner;
    this.updateStatement = aUpdateStatement;
  }
  
  public String getDatabaseUpdateStatement()
  {
    return this.updateStatement;
  }
  
  long getId()
  {
    return this.id;
  }
  
  public long getOwner()
  {
    return this.owner;
  }
  
  public String toString()
  {
    return "ItemLastOwnerDatabaseUpdatable [id=" + this.id + ", owner=" + this.owner + ", updateStatement=" + this.updateStatement + "]";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\utils\ItemLastOwnerDatabaseUpdatable.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */