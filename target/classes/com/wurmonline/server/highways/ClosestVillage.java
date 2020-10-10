package com.wurmonline.server.highways;

public class ClosestVillage
{
  private final String name;
  private final short distance;
  
  ClosestVillage(String name, short distance)
  {
    this.name = name;
    this.distance = distance;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public short getDistance()
  {
    return this.distance;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\highways\ClosestVillage.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */