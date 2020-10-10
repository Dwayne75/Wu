package com.wurmonline.server.villages;

public abstract class Alliance
{
  final Village villone;
  final Village villtwo;
  
  Alliance(Village vone, Village vtwo)
  {
    this.villone = vone;
    this.villtwo = vtwo;
  }
  
  final Village getVillone()
  {
    return this.villone;
  }
  
  final Village getVilltwo()
  {
    return this.villtwo;
  }
  
  abstract void save();
  
  abstract void delete();
  
  public final String toString()
  {
    return "Alliance [" + this.villone + " and " + this.villtwo + ']';
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\villages\Alliance.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */