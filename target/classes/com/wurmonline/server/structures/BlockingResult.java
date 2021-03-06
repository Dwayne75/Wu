package com.wurmonline.server.structures;

import com.wurmonline.math.Vector3f;
import java.util.LinkedList;

public final class BlockingResult
{
  private LinkedList<Blocker> blockers;
  private LinkedList<Vector3f> intersections;
  private float totalCover;
  private static final Blocker[] emptyBlockers = new Blocker[0];
  private float estimatedBlockingTime;
  private float actualBlockingTime;
  
  public final float addBlocker(Blocker blockerToAdd, Vector3f intersection, float factorToAdd)
  {
    if (this.blockers == null)
    {
      this.blockers = new LinkedList();
      this.intersections = new LinkedList();
    }
    this.blockers.add(blockerToAdd);
    this.intersections.add(intersection);
    addBlockingFactor(factorToAdd);
    return this.totalCover;
  }
  
  public final void addBlockingFactor(float factorToAdd)
  {
    this.totalCover += factorToAdd;
  }
  
  public final Blocker getFirstBlocker()
  {
    if ((this.blockers != null) && (!this.blockers.isEmpty())) {
      return (Blocker)this.blockers.getFirst();
    }
    return null;
  }
  
  public final Blocker getLastBlocker()
  {
    if ((this.blockers != null) && (!this.blockers.isEmpty())) {
      return (Blocker)this.blockers.getLast();
    }
    return null;
  }
  
  public final Vector3f getFirstIntersection()
  {
    if ((this.intersections != null) && (!this.intersections.isEmpty())) {
      return (Vector3f)this.intersections.getFirst();
    }
    return null;
  }
  
  public final Vector3f getLastIntersection()
  {
    if ((this.intersections != null) && (!this.intersections.isEmpty())) {
      return (Vector3f)this.intersections.getLast();
    }
    return null;
  }
  
  public final float getTotalCover()
  {
    return this.totalCover;
  }
  
  public final void removeBlocker(Blocker blocker)
  {
    if (this.blockers != null) {
      this.blockers.remove(blocker);
    }
  }
  
  public final Blocker[] getBlockerArray()
  {
    if ((this.blockers == null) || (this.blockers.isEmpty())) {
      return emptyBlockers;
    }
    return (Blocker[])this.blockers.toArray(new Blocker[this.blockers.size()]);
  }
  
  public float getActualBlockingTime()
  {
    return this.actualBlockingTime;
  }
  
  public void setActualBlockingTime(float aActualBlockingTime)
  {
    this.actualBlockingTime = aActualBlockingTime;
  }
  
  public float getEstimatedBlockingTime()
  {
    return this.estimatedBlockingTime;
  }
  
  public void setEstimatedBlockingTime(float aEstimatedBlockingTime)
  {
    this.estimatedBlockingTime = aEstimatedBlockingTime;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\structures\BlockingResult.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */