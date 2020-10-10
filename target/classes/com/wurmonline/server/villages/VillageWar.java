package com.wurmonline.server.villages;

public abstract class VillageWar
{
  final Village villone;
  public final Village villtwo;
  
  VillageWar(Village vone, Village vtwo)
  {
    this.villone = vone;
    this.villtwo = vtwo;
  }
  
  public final Village getVillone()
  {
    return this.villone;
  }
  
  public final Village getVilltwo()
  {
    return this.villtwo;
  }
  
  abstract void save();
  
  abstract void delete();
  
  public final String toString()
  {
    return "VillageWar [" + this.villone + " and " + this.villtwo + ']';
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\villages\VillageWar.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */