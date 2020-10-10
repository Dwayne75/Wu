package com.wurmonline.math;

public final class Vertex
{
  public float[] point = new float[3];
  public byte flags;
  public float[] vertex = new float[3];
  public byte boneId;
  public byte refCount;
  public long lastRotateTime = 0L;
  public float[] rotatedVertex = new float[3];
  public float[] rotatedNormal = new float[3];
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\math\Vertex.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */