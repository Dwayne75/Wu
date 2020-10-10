package com.wurmonline.server.highways;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class AStarSearch
{
  protected static List<Route> constructPath(AStarNode startNode, AStarNode node)
  {
    LinkedList<Route> path = new LinkedList();
    LinkedList<AStarNode> nodes = new LinkedList();
    while (node.pathParent != null)
    {
      if (nodes.contains(node)) {
        return null;
      }
      nodes.addFirst(node);
      path.addFirst(node.pathRoute);
      node = node.pathParent;
    }
    return path;
  }
  
  public static List<Route> findPath(AStarNode startNode, AStarNode goalNode, byte initialDir)
  {
    AStarSearch.PriorityList openList = new AStarSearch.PriorityList();
    LinkedList<AStarNode> closedList = new LinkedList();
    
    startNode.costFromStart = 0.0F;
    startNode.estimatedCostToGoal = startNode.getEstimatedCost(goalNode);
    startNode.pathParent = null;
    startNode.pathRoute = null;
    openList.add(startNode);
    byte checkDir = initialDir;
    while (!openList.isEmpty())
    {
      AStarNode node = (AStarNode)openList.removeFirst();
      if (node == goalNode) {
        return constructPath(startNode, goalNode);
      }
      ConcurrentHashMap<Byte, Route> routesMap = node.getRoutes(checkDir);
      for (Map.Entry<Byte, Route> entry : routesMap.entrySet())
      {
        Route route = (Route)entry.getValue();
        AStarNode neighbourNode = route.getEndNode();
        boolean isOpen = openList.contains(neighbourNode);
        boolean isClosed = closedList.contains(neighbourNode);
        float costFromStart = node.costFromStart + route.getCost();
        if (((!isOpen) && (!isClosed)) || (costFromStart < neighbourNode.costFromStart))
        {
          neighbourNode.pathParent = node;
          neighbourNode.costFromStart = costFromStart;
          neighbourNode.estimatedCostToGoal = neighbourNode.getEstimatedCost(goalNode);
          neighbourNode.pathRoute = route;
          if (isClosed) {
            closedList.remove(neighbourNode);
          }
          if (!isOpen) {
            openList.add(neighbourNode);
          }
        }
      }
      closedList.add(node);
      
      checkDir = 0;
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\highways\AStarSearch.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */