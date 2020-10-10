package com.wurmonline.math;

public class Transform
{
  public final Quaternion rotation;
  public final Vector3f translation;
  
  public Transform()
  {
    this.rotation = new Quaternion();
    this.translation = new Vector3f();
  }
  
  public final void identity()
  {
    this.rotation.identity();
    this.translation.zero();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\math\Transform.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */