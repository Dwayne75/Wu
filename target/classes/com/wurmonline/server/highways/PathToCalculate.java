package com.wurmonline.server.highways;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Wagoner;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.villages.Village;
import com.wurmonline.shared.constants.HighwayConstants;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import javax.annotation.Nullable;

public class PathToCalculate
  implements HighwayConstants
{
  private final Creature creature;
  private final Node startNode;
  private final Village destinationVillage;
  private List<Route> bestPath = null;
  private byte checkDir = 0;
  private float bestDistance = 99999.0F;
  private float bestCost = 99999.0F;
  
  PathToCalculate(@Nullable Creature creature, Node startNode, @Nullable Village village, byte checkDir)
  {
    this.creature = creature;
    this.startNode = startNode;
    this.destinationVillage = village;
    this.checkDir = checkDir;
  }
  
  void calculate()
  {
    if (this.destinationVillage == null)
    {
      Village[] villages = Routes.getVillages();
      HashSet<PathToCalculate.Distanced> distanceSet = new HashSet();
      for (Village village : villages) {
        if (village != this.startNode.getVillage())
        {
          int dx = this.startNode.getWaystone().getTileX() - village.getTokenX();
          int dy = this.startNode.getWaystone().getTileY() - village.getTokenY();
          int crowfly = (int)Math.sqrt(dx * dx + dy * dy);
          distanceSet.add(new PathToCalculate.Distanced(this, village, crowfly));
        }
      }
      if (distanceSet.size() == 0) {
        return;
      }
      PathToCalculate.Distanced[] distanced = (PathToCalculate.Distanced[])distanceSet.toArray(new PathToCalculate.Distanced[distanceSet.size()]);
      
      Arrays.sort(distanced, new PathToCalculate.1(this));
      if (this.checkDir == 0)
      {
        calcClosest(distanced, (byte)1);
        calcClosest(distanced, (byte)2);
        calcClosest(distanced, (byte)4);
        calcClosest(distanced, (byte)8);
        calcClosest(distanced, (byte)16);
        calcClosest(distanced, (byte)32);
        calcClosest(distanced, (byte)64);
        calcClosest(distanced, (byte)Byte.MIN_VALUE);
      }
      else
      {
        calcClosest(distanced, this.checkDir);
      }
      this.startNode.getWaystone().updateModelNameOnGroundItem();
    }
    else
    {
      calculate(this.destinationVillage, this.checkDir);
    }
  }
  
  void calcClosest(PathToCalculate.Distanced[] distanced, byte dir)
  {
    Route route = this.startNode.getRoute(dir);
    if (route == null) {
      return;
    }
    List<Route> closestPath = null;
    float closestDistance = 99999.0F;
    float closestCost = 99999.0F;
    Village closestVillage = null;
    for (PathToCalculate.Distanced distance : distanced)
    {
      if (distance.getDistance() > closestDistance) {
        break;
      }
      calculate(distance.getVillage(), dir);
      if ((this.bestPath != null) && (this.bestCost < closestCost))
      {
        closestPath = this.bestPath;
        closestDistance = this.bestDistance;
        closestCost = this.bestCost;
        closestVillage = distance.getVillage();
      }
    }
    if (closestPath != null)
    {
      short distanceTiles = (short)(int)closestDistance;
      this.startNode.addClosestVillage(dir, closestVillage.getName(), distanceTiles);
    }
    else
    {
      Node endNode = route.getEndNode();
      Item waystone = endNode.getWaystone();
      String wagonerName = Wagoner.getWagonerNameFrom(waystone.getWurmId());
      if (wagonerName.length() > 0) {
        this.startNode.addClosestVillage(dir, wagonerName, (short)(int)route.getDistance());
      } else {
        this.startNode.addClosestVillage(dir, "", (short)0);
      }
    }
  }
  
  void calculate(Village village, byte initialDir)
  {
    this.bestPath = null;
    this.bestCost = 99999.0F;
    this.bestDistance = 99999.0F;
    int pno = 1;
    int bestno = 0;
    for (Node goalNode : Routes.getNodesFor(village))
    {
      List<Route> path = AStarSearch.findPath(this.startNode, goalNode, initialDir);
      if ((path != null) && (!path.isEmpty()))
      {
        float cost = 0.0F;
        float distance = 0.0F;
        for (Route route : path)
        {
          float ncost = route.getCost();
          cost += ncost;
          float ndistance = route.getDistance();
          distance += ndistance;
        }
        if ((this.bestPath == null) || (cost < this.bestCost))
        {
          this.bestCost = cost;
          this.bestPath = path;
          bestno = pno;
          this.bestDistance = distance;
        }
        pno++;
      }
    }
    if (this.creature != null)
    {
      String oldDestination = this.creature.getHighwayPathDestination();
      this.creature.setHighwayPath(village.getName(), this.bestPath);
      if ((this.bestPath != null) && (!this.bestPath.isEmpty())) {
        this.creature.setLastWaystoneChecked(((Route)this.bestPath.get(0)).getStartNode().getWaystone().getWurmId());
      } else {
        this.creature.setLastWaystoneChecked(-10L);
      }
      if (this.creature.isPlayer()) {
        if (this.creature.getPower() > 1) {
          Routes.queuePlayerMessage((Player)this.creature, pno + " route" + (pno != 1 ? "s" : "") + " checked." + (bestno > 0 ? " Best route found was number " + bestno + " and its cost is " + this.bestCost + "." : " No routes found!"));
        } else if (this.bestPath == null) {
          Routes.queuePlayerMessage((Player)this.creature, "No routes found to " + village.getName() + "!");
        } else if (!oldDestination.equals(village.getName())) {
          Routes.queuePlayerMessage((Player)this.creature, "Route found to " + village.getName() + "!");
        }
      }
    }
  }
  
  public static final List<Route> getRoute(long startWaystoneId, long endWaystoneId)
  {
    Node startNode = Routes.getNode(startWaystoneId);
    Node endNode = Routes.getNode(endWaystoneId);
    if ((startNode == null) || (endNode == null)) {
      return null;
    }
    List<Route> path = AStarSearch.findPath(startNode, endNode, (byte)0);
    if ((path != null) && (!path.isEmpty())) {
      return path;
    }
    return null;
  }
  
  public static final float getRouteDistance(long startWaystoneId, long endWaystoneId)
  {
    List<Route> path = getRoute(startWaystoneId, endWaystoneId);
    if (path != null)
    {
      float distance = 0.0F;
      for (Route route : path) {
        distance += route.getDistance();
      }
      return distance;
    }
    return 99999.0F;
  }
  
  public static final boolean isVillageConnected(long startWaystoneId, Village village)
  {
    Node startNode = Routes.getNode(startWaystoneId);
    if (startNode == null) {
      return false;
    }
    for (Node goalNode : Routes.getNodesFor(village))
    {
      List<Route> path = AStarSearch.findPath(startNode, goalNode, (byte)0);
      if ((path != null) && (!path.isEmpty())) {
        return true;
      }
    }
    return false;
  }
  
  public static final boolean isWaystoneConnected(long startWaystoneId, long endWaystoneId)
  {
    Node startNode = Routes.getNode(startWaystoneId);
    if (startNode == null) {
      return false;
    }
    Node endNode = Routes.getNode(endWaystoneId);
    if (endNode == null) {
      return false;
    }
    List<Route> path = AStarSearch.findPath(startNode, endNode, (byte)0);
    if ((path != null) && (!path.isEmpty())) {
      return true;
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\highways\PathToCalculate.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */