package com.wurmonline.server.utils;

public class ItemOwnerDatabaseUpdatable
  implements WurmDbUpdatable
{
  private final long id;
  private final long owner;
  private final String updateStatement;
  
  public ItemOwnerDatabaseUpdatable(long aId, long aOwner, String aUpdateStatement)
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
    return "ItemDamageDatabaseUpdatable [id=" + this.id + ", owner=" + this.owner + ", updateStatement=" + this.updateStatement + "]";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\utils\ItemOwnerDatabaseUpdatable.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */