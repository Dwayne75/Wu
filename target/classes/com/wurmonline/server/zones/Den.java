package com.wurmonline.server.zones;

public final class Den
{
  private int templateId;
  private final int tilex;
  private final int tiley;
  private final boolean surfaced;
  
  Den(int creatureTemplateId, int tileX, int tileY, boolean _surfaced)
  {
    this.templateId = creatureTemplateId;
    this.tilex = tileX;
    this.tiley = tileY;
    this.surfaced = _surfaced;
  }
  
  public int getTemplateId()
  {
    return this.templateId;
  }
  
  void setTemplateId(int aTemplateId)
  {
    this.templateId = aTemplateId;
  }
  
  public int getTilex()
  {
    return this.tilex;
  }
  
  public int getTiley()
  {
    return this.tiley;
  }
  
  public boolean isSurfaced()
  {
    return this.surfaced;
  }
  
  public String toString()
  {
    return "Den [CreatureTemplate: " + this.templateId + ", Tile: " + this.tilex + ", " + this.tiley + ", surfaced: " + this.surfaced + ']';
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\zones\Den.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */