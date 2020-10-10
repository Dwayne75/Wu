package com.wurmonline.server.highways;

import com.wurmonline.server.items.Item;
import com.wurmonline.server.villages.Village;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;

public class Node
  extends AStarNode
{
  private final Item waystone;
  private Village village = null;
  final ConcurrentHashMap<Byte, Route> routes = new ConcurrentHashMap();
  final ConcurrentHashMap<Byte, AStarNode> neighbours = new ConcurrentHashMap();
  final ConcurrentHashMap<Byte, ClosestVillage> pointers = new ConcurrentHashMap();
  
  public Node(Item waystone)
  {
    this.waystone = waystone;
  }
  
  public long getWurmId()
  {
    return this.waystone.getWurmId();
  }
  
  public Item getWaystone()
  {
    return this.waystone;
  }
  
  public void setVillage(Village village)
  {
    this.village = village;
  }
  
  public int getRouteCount()
  {
    return this.routes.size();
  }
  
  public Village getVillage()
  {
    return this.village;
  }
  
  public void AddRoute(byte direction, Route route)
  {
    this.routes.put(Byte.valueOf(direction), route);
    
    this.neighbours.put(Byte.valueOf(direction), route.getEndNode());
  }
  
  public void addClosestVillage(byte direction, String name, short distance)
  {
    this.pointers.put(Byte.valueOf(direction), new ClosestVillage(name, distance));
  }
  
  @Nullable
  public ClosestVillage getClosestVillage(byte direction)
  {
    return (ClosestVillage)this.pointers.get(Byte.valueOf(direction));
  }
  
  @Nullable
  public Route getRoute(byte direction)
  {
    return (Route)this.routes.get(Byte.valueOf(direction));
  }
  
  public byte getNodeDir(Node node)
  {
    byte bestdir = 0;
    float bestCost = 99999.0F;
    for (Map.Entry<Byte, Route> entry : this.routes.entrySet())
    {
      Route route = (Route)entry.getValue();
      byte dir = ((Byte)entry.getKey()).byteValue();
      if ((route.getEndNode() == node) || (route.getStartNode() == node)) {
        if (route.getCost() < bestCost)
        {
          bestCost = route.getCost();
          bestdir = dir;
        }
      }
    }
    return bestdir;
  }
  
  public boolean removeRoute(Route oldRoute)
  {
    for (Map.Entry<Byte, Route> entry : this.routes.entrySet()) {
      if (entry.getValue() == oldRoute) {
        this.routes.remove(entry.getKey());
      }
    }
    for (Map.Entry<Byte, AStarNode> entry : this.neighbours.entrySet()) {
      if (entry.getValue() == oldRoute.getEndNode()) {
        this.neighbours.remove(entry.getKey());
      }
    }
    return (this.village == null) && (this.routes.isEmpty());
  }
  
  public float getCost(AStarNode node)
  {
    Route route = findRoute(node);
    if (route != null) {
      return route.getCost();
    }
    return 99999.0F;
  }
  
  public float getDistance(AStarNode node)
  {
    Route route = findRoute(node);
    if (route != null) {
      return route.getDistance();
    }
    return 99999.0F;
  }
  
  public float getEstimatedCost(AStarNode node)
  {
    Route route = findRoute(node);
    if (route != null)
    {
      int diffx = Math.abs(this.waystone.getTileX() - ((Node)node).waystone.getTileX());
      int diffy = Math.abs(this.waystone.getTileY() - ((Node)node).waystone.getTileY());
      return diffx + diffy;
    }
    return 99999.0F;
  }
  
  @Nullable
  private Route findRoute(AStarNode node)
  {
    for (Map.Entry<Byte, AStarNode> entry : this.neighbours.entrySet()) {
      if (entry.getValue() == node) {
        return (Route)this.routes.get(entry.getKey());
      }
    }
    return null;
  }
  
  public List<AStarNode> getNeighbours(byte dir)
  {
    ArrayList<AStarNode> alist = new ArrayList();
    Route route;
    if (dir != 0)
    {
      route = getRoute(dir);
      if ((route != null) && (route.getEndNode() != null)) {
        alist.add(this.neighbours.get(Byte.valueOf(dir)));
      }
    }
    else
    {
      for (Map.Entry<Byte, AStarNode> entry : this.neighbours.entrySet()) {
        if (!alist.contains(entry.getValue())) {
          alist.add(entry.getValue());
        }
      }
    }
    return alist;
  }
  
  public ConcurrentHashMap<Byte, Route> getRoutes(byte dir)
  {
    if (dir != 0)
    {
      ConcurrentHashMap<Byte, Route> lroutes = new ConcurrentHashMap();
      Route route = (Route)this.routes.get(Byte.valueOf(dir));
      if (route != null) {
        lroutes.put(Byte.valueOf(dir), route);
      }
      return lroutes;
    }
    return this.routes;
  }
  
  public String toString()
  {
    StringBuilder buf = new StringBuilder();
    buf.append("{Node:" + this.waystone.getWurmId());
    boolean first = true;
    for (Map.Entry<Byte, Route> entry : this.routes.entrySet())
    {
      if (first)
      {
        first = false;
        buf.append("{");
      }
      else
      {
        buf.append(",");
      }
      buf.append(" {Dir:");
      buf.append(MethodsHighways.getLinkDirString(((Byte)entry.getKey()).byteValue()));
      buf.append(",Cost:");
      buf.append(((Route)entry.getValue()).getCost());
      buf.append(",Route:");
      buf.append(((Route)entry.getValue()).getId());
      
      buf.append("}");
    }
    if (!first) {
      buf.append("}");
    }
    buf.append("}");
    return buf.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\highways\Node.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */