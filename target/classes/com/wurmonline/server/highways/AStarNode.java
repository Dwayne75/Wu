package com.wurmonline.server.highways;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AStarNode
  implements Comparable<AStarNode>
{
  AStarNode pathParent;
  float costFromStart;
  float estimatedCostToGoal;
  Route pathRoute;
  
  public float getCost()
  {
    return this.costFromStart + this.estimatedCostToGoal;
  }
  
  public int compareTo(AStarNode other)
  {
    float v = getCost() - other.getCost();
    return v < 0.0F ? -1 : v > 0.0F ? 1 : 0;
  }
  
  public abstract float getCost(AStarNode paramAStarNode);
  
  public abstract float getEstimatedCost(AStarNode paramAStarNode);
  
  public abstract List<AStarNode> getNeighbours(byte paramByte);
  
  public abstract ConcurrentHashMap<Byte, Route> getRoutes(byte paramByte);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\highways\AStarNode.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */