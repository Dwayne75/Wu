package com.wurmonline.server.structures;

public abstract interface StructureSupport
{
  public abstract boolean supports(StructureSupport paramStructureSupport);
  
  public abstract int getFloorLevel();
  
  public abstract int getStartX();
  
  public abstract int getStartY();
  
  public abstract int getMinX();
  
  public abstract int getMinY();
  
  public abstract boolean isHorizontal();
  
  public abstract boolean isFloor();
  
  public abstract boolean isFence();
  
  public abstract boolean isWall();
  
  public abstract int getEndX();
  
  public abstract int getEndY();
  
  public abstract boolean isSupportedByGround();
  
  public abstract boolean supports();
  
  public abstract String getName();
  
  public abstract long getId();
  
  public abstract boolean equals(StructureSupport paramStructureSupport);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\structures\StructureSupport.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */