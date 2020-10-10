package com.wurmonline.mesh;

public final class CaveNode
  extends Node
{
  private int special;
  private float[] normals2;
  private int ceilingTexture;
  private float data;
  
  int getSpecial()
  {
    return this.special;
  }
  
  void setSpecial(int special)
  {
    this.special = special;
  }
  
  float[] getNormals2()
  {
    return this.normals2;
  }
  
  void setNormals2(float[] normals2)
  {
    this.normals2 = normals2;
  }
  
  int getCeilingTexture()
  {
    return this.ceilingTexture;
  }
  
  void setCeilingTexture(int ceilingTexture)
  {
    this.ceilingTexture = ceilingTexture;
  }
  
  float getData()
  {
    return this.data;
  }
  
  void setData(float data)
  {
    this.data = data;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\mesh\CaveNode.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */