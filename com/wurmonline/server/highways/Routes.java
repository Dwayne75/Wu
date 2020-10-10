package com.wurmonline.server.highways;

import com.wurmonline.server.Items;
import com.wurmonline.server.Players;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.support.Trello;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.webinterface.WcTrelloHighway;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.HighwayConstants;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class Routes
  implements HighwayConstants
{
  private static Logger logger = Logger.getLogger(Routes.class.getName());
  private static int nextId = 1;
  private static ConcurrentHashMap<Integer, Route> allRoutes = new ConcurrentHashMap();
  private static ConcurrentHashMap<Long, Node> allNodes = new ConcurrentHashMap();
  private static final ConcurrentLinkedDeque<PlayerMessageToSend> playerMessagesToSend = new ConcurrentLinkedDeque();
  
  public static final void generateAllRoutes()
  {
    logger.info("Calculating All routes.");
    long start = System.nanoTime();
    for (Item waystone : Items.getWaystones()) {
      makeNodeFrom(waystone);
    }
    for (Item waystone : Items.getWaystones()) {
      checkForRoutes(waystone, false, null);
    }
    for (Item waystone : Items.getWaystones())
    {
      Node startNode = getNode(waystone);
      HighwayFinder.queueHighwayFinding(null, startNode, null, (byte)0);
    }
    logger.log(Level.INFO, "Calculated " + allRoutes.size() + " routes and " + allNodes.size() + " nodes.That took " + 
      (float)(System.nanoTime() - start) / 1000000.0F + " ms.");
    
    Players.getInstance().sendGmMessage(null, "Roads", "Calculated " + allRoutes.size() + " routes and " + allNodes.size() + " nodes. That took " + 
      (float)(System.nanoTime() - start) / 1000000.0F + " ms.", false);
  }
  
  private static final boolean checkForRoutes(Item waystone, boolean tellGms, Item marker)
  {
    boolean foundRoute = false;
    foundRoute |= checkForRoute(waystone, (byte)1, tellGms, marker);
    foundRoute |= checkForRoute(waystone, (byte)2, tellGms, marker);
    foundRoute |= checkForRoute(waystone, (byte)4, tellGms, marker);
    foundRoute |= checkForRoute(waystone, (byte)8, tellGms, marker);
    foundRoute |= checkForRoute(waystone, (byte)16, tellGms, marker);
    foundRoute |= checkForRoute(waystone, (byte)32, tellGms, marker);
    foundRoute |= checkForRoute(waystone, (byte)64, tellGms, marker);
    foundRoute |= checkForRoute(waystone, (byte)Byte.MIN_VALUE, tellGms, marker);
    return foundRoute;
  }
  
  @Nullable
  private static final boolean checkForRoute(Item waystone, byte checkdir, boolean tellGms, Item planted)
  {
    if (!MethodsHighways.hasLink(waystone.getAuxData(), checkdir)) {
      return false;
    }
    Node startNode = getNode(waystone);
    if (startNode.getRoute(checkdir) != null) {
      return false;
    }
    HighwayPos highwayPos = MethodsHighways.getHighwayPos(waystone);
    
    Route newRoute = new Route(startNode, checkdir, nextId);
    boolean checking = true;
    byte linkdir = checkdir;
    while (checking)
    {
      int lastx = highwayPos.getTilex();
      int lasty = highwayPos.getTiley();
      boolean lastSurf = highwayPos.isOnSurface();
      long lastbp = highwayPos.getBridgeId();
      int lastfl = highwayPos.getFloorLevel();
      byte lastdir = linkdir;
      
      highwayPos = MethodsHighways.getNewHighwayPosLinked(highwayPos, linkdir);
      Item marker = MethodsHighways.getMarker(highwayPos);
      if (marker == null)
      {
        logger.warning("Lost! " + MethodsHighways.getLinkAsString(lastdir) + " from:x:" + lastx + ",y:" + lasty + ",Surface:" + lastSurf + ",bp:" + lastbp + ",fl:" + lastfl);
        
        return false;
      }
      byte fromdir = MethodsHighways.getOppositedir(linkdir);
      if (!MethodsHighways.hasLink(marker.getAuxData(), fromdir))
      {
        logger.info("Missing Link! " + MethodsHighways.getLinkAsString(lastdir) + " from:x:" + lastx + ",y:" + lasty + ",bp:" + lastbp + ",fl:" + lastfl + "  to:x" + highwayPos
        
          .getTilex() + ",y:" + highwayPos.getTiley() + "Surf:" + highwayPos.isOnSurface() + ",bp:" + highwayPos
          .getBridgeId() + ",fl:" + highwayPos.getFloorLevel());
        return false;
      }
      if (marker.getTemplateId() == 1114)
      {
        byte todir = MethodsHighways.getOtherdir(marker.getAuxData(), fromdir);
        
        newRoute.AddCatseye(marker, false, todir);
        if (MethodsHighways.numberOfSetBits(todir) != 1)
        {
          if (Servers.isThisATestServer()) {
            logger.info("End of road! @" + marker.getTileX() + "," + marker.getTileY() + " (from:" + 
              MethodsHighways.getLinkAsString(fromdir) + ",to:" + 
              MethodsHighways.getLinkAsString(todir) + ")");
          }
          return false;
        }
        linkdir = todir;
      }
      else
      {
        if (marker.getTemplateId() == 1112)
        {
          Node endNode = getNode(marker);
          newRoute.AddEndNode(endNode);
          startNode.AddRoute(checkdir, newRoute);
          allRoutes.put(Integer.valueOf(newRoute.getId()), newRoute);
          
          LinkedList<Item> catseyes = new LinkedList();
          for (Item catseye : newRoute.getCatseyesList()) {
            catseyes.addFirst(catseye);
          }
          byte backdir = fromdir;
          Route backRoute = new Route(endNode, backdir, ++nextId);
          for (Object localObject = catseyes.iterator(); ((Iterator)localObject).hasNext();)
          {
            catseye = (Item)((Iterator)localObject).next();
            
            oppdir = MethodsHighways.getOppositedir(backdir);
            backRoute.AddCatseye(catseye, false, oppdir);
            backdir = MethodsHighways.getOtherdir(catseye.getAuxData(), oppdir);
          }
          Item catseye;
          byte oppdir;
          backRoute.AddEndNode(startNode);
          endNode.AddRoute(fromdir, backRoute);
          allRoutes.put(Integer.valueOf(backRoute.getId()), backRoute);
          
          newRoute.SetOppositeRoute(backRoute);
          backRoute.SetOppositeRoute(newRoute);
          if (tellGms)
          {
            waystone.updateModelNameOnGroundItem();
            localObject = newRoute.getCatseyes();catseye = localObject.length;
            for (oppdir = 0; oppdir < catseye; oppdir++)
            {
              Item catseye = localObject[oppdir];
              catseye.updateModelNameOnGroundItem();
            }
            marker.updateModelNameOnGroundItem();
            
            HighwayFinder.queueHighwayFinding(null, startNode, null, checkdir);
            HighwayFinder.queueHighwayFinding(null, endNode, null, fromdir);
          }
          nextId += 1;
          
          checking = false;
          return true;
        }
        return false;
      }
    }
    return false;
  }
  
  public static final Node getNode(Item waystone)
  {
    Node node = (Node)allNodes.get(Long.valueOf(waystone.getWurmId()));
    if (node != null) {
      return node;
    }
    return makeNodeFrom(waystone);
  }
  
  private static final Node makeNodeFrom(Item waystone)
  {
    Node node = new Node(waystone);
    VolaTile vt = Zones.getTileOrNull(waystone.getTileX(), waystone.getTileY(), waystone.isOnSurface());
    if ((vt != null) && (vt.getVillage() != null)) {
      node.setVillage(vt.getVillage());
    }
    allNodes.put(Long.valueOf(waystone.getWurmId()), node);
    return node;
  }
  
  public static final void remove(Item marker)
  {
    if (marker.getTemplateId() == 1114)
    {
      for (Map.Entry<Integer, Route> entry : allRoutes.entrySet()) {
        if (((Route)entry.getValue()).containsCatseye(marker))
        {
          removeRoute((Route)entry.getValue(), marker);
          break;
        }
      }
    }
    else
    {
      Node node = (Node)allNodes.remove(Long.valueOf(marker.getWurmId()));
      if (node != null)
      {
        removeRoute(node, (byte)1, marker);
        removeRoute(node, (byte)2, marker);
        removeRoute(node, (byte)4, marker);
        removeRoute(node, (byte)8, marker);
        removeRoute(node, (byte)16, marker);
        removeRoute(node, (byte)32, marker);
        removeRoute(node, (byte)64, marker);
        removeRoute(node, (byte)Byte.MIN_VALUE, marker);
      }
    }
  }
  
  private static final void removeRoute(Node node, byte checkdir, Item marker)
  {
    Route route = node.getRoute(checkdir);
    if (route != null) {
      removeRoute(route, marker);
    }
  }
  
  private static final void removeRoute(Route route, Item marker)
  {
    Node nodeStart = route.getStartNode();
    Node nodeEnd = route.getEndNode();
    boolean doCatseyes = nodeStart.removeRoute(route);
    allRoutes.remove(Integer.valueOf(route.getId()));
    Route oppRoute = route.getOppositeRoute();
    Node oppStart;
    if (oppRoute != null)
    {
      oppStart = oppRoute.getStartNode();
      doCatseyes |= oppStart.removeRoute(oppRoute);
      allRoutes.remove(Integer.valueOf(oppRoute.getId()));
    }
    if (doCatseyes)
    {
      nodeStart.getWaystone().updateModelNameOnGroundItem();
      for (Item catseye : route.getCatseyes()) {
        catseye.updateModelNameOnGroundItem();
      }
      if (nodeEnd != null) {
        nodeEnd.getWaystone().updateModelNameOnGroundItem();
      }
    }
    if (!marker.isReplacing())
    {
      String whatHappened = marker.getWhatHappened();
      if (whatHappened.length() == 0) {
        whatHappened = "unknown";
      }
      StringBuffer ttl = new StringBuffer();
      ttl.append(marker.getName());
      ttl.append(" @");
      ttl.append(marker.getTileX());
      ttl.append(",");
      ttl.append(marker.getTileY());
      ttl.append(",");
      ttl.append(marker.isOnSurface());
      ttl.append(" ");
      ttl.append(whatHappened);
      String title = ttl.toString();
      
      StringBuffer dsc = new StringBuffer();
      dsc.append("Routes removed between ");
      dsc.append(nodeStart.getWaystone().getTileX());
      dsc.append(",");
      dsc.append(nodeStart.getWaystone().getTileY());
      dsc.append(",");
      dsc.append(nodeStart.getWaystone().isOnSurface());
      dsc.append(" and ");
      if (nodeEnd != null)
      {
        dsc.append(nodeEnd.getWaystone().getTileX());
        dsc.append(",");
        dsc.append(nodeEnd.getWaystone().getTileY());
        dsc.append(",");
        dsc.append(nodeEnd.getWaystone().isOnSurface());
      }
      else
      {
        dsc.append(" end node missing!");
      }
      String description = dsc.toString();
      
      sendToTrello(title, description);
    }
  }
  
  public static final void sendToTrello(String title, String description)
  {
    Players.getInstance().sendGmMessage(null, "Roads", title, false);
    if (Servers.isThisLoginServer())
    {
      Trello.addHighwayMessage(Servers.localServer.getAbbreviation(), title, description);
    }
    else
    {
      WcTrelloHighway wtc = new WcTrelloHighway(title, description);
      wtc.sendToLoginServer();
    }
  }
  
  public static final boolean checkForNewRoutes(Item marker)
  {
    if (marker.getTemplateId() == 1112)
    {
      getNode(marker);
      
      return checkForRoutes(marker, true, marker);
    }
    if (MethodsHighways.numberOfSetBits(marker.getAuxData()) == 2)
    {
      byte startdir = getStartdir(marker);
      if (startdir != 0)
      {
        Set<Item> markersDone = new HashSet();
        HighwayPos highwayPos = MethodsHighways.getHighwayPos(marker);
        boolean checking = true;
        byte linkdir = startdir;
        while (checking)
        {
          int lastx = highwayPos.getTilex();
          int lasty = highwayPos.getTiley();
          boolean lastSurf = highwayPos.isOnSurface();
          long lastbp = highwayPos.getBridgeId();
          int lastfl = highwayPos.getFloorLevel();
          byte lastdir = linkdir;
          
          highwayPos = MethodsHighways.getNewHighwayPosLinked(highwayPos, linkdir);
          Item nextMarker = MethodsHighways.getMarker(highwayPos);
          if (nextMarker == null)
          {
            logger.warning("Dead End! " + MethodsHighways.getLinkAsString(lastdir) + " from:x:" + lastx + ",y:" + lasty + ",Surface:" + lastSurf + ",bp:" + lastbp + ",fl:" + lastfl);
            
            return false;
          }
          if (markersDone.contains(nextMarker))
          {
            logger.warning("Circular! " + MethodsHighways.getLinkAsString(lastdir) + " from:x:" + lastx + ",y:" + lasty + ",Surface:" + lastSurf + ",bp:" + lastbp + ",fl:" + lastfl);
            
            return false;
          }
          markersDone.add(nextMarker);
          byte fromdir = MethodsHighways.getOppositedir(linkdir);
          if (MethodsHighways.numberOfSetBits(fromdir) != 1)
          {
            logger.warning("Lost! " + MethodsHighways.getLinkAsString(lastdir) + " from:x:" + lastx + ",y:" + lasty + ",Surface:" + lastSurf + ",bp:" + lastbp + ",fl:" + lastfl);
            
            return false;
          }
          if (!MethodsHighways.hasLink(nextMarker.getAuxData(), fromdir))
          {
            logger.info("Missing Link! " + MethodsHighways.getLinkAsString(lastdir) + " from:x:" + lastx + ",y:" + lasty + ",bp:" + lastbp + ",fl:" + lastfl + "  to:x" + highwayPos
            
              .getTilex() + ",y:" + highwayPos.getTiley() + "Surf:" + highwayPos.isOnSurface() + ",bp:" + highwayPos
              .getBridgeId() + ",fl:" + highwayPos.getFloorLevel());
            return false;
          }
          if (nextMarker.getTemplateId() == 1114)
          {
            byte todir = MethodsHighways.getOtherdir(nextMarker.getAuxData(), fromdir);
            if (MethodsHighways.numberOfSetBits(todir) != 1)
            {
              if (Servers.isThisATestServer()) {
                logger.info("End of road! @" + nextMarker.getTileX() + "," + nextMarker.getTileY() + " (from:" + 
                  MethodsHighways.getLinkAsString(fromdir) + ",to:" + 
                  MethodsHighways.getLinkAsString(todir) + ")");
              }
              return false;
            }
            linkdir = todir;
          }
          else
          {
            if (nextMarker.getTemplateId() == 1112)
            {
              checking = false;
              return checkForRoute(nextMarker, fromdir, true, marker);
            }
            return false;
          }
        }
      }
    }
    return false;
  }
  
  private static final byte getStartdir(Item marker)
  {
    byte startdir = 0;
    byte dirs = marker.getAuxData();
    if (MethodsHighways.hasLink(dirs, (byte)1)) {
      startdir = 1;
    } else if (MethodsHighways.hasLink(dirs, (byte)2)) {
      startdir = 2;
    } else if (MethodsHighways.hasLink(dirs, (byte)4)) {
      startdir = 4;
    } else if (MethodsHighways.hasLink(dirs, (byte)8)) {
      startdir = 8;
    } else if (MethodsHighways.hasLink(dirs, (byte)16)) {
      startdir = 16;
    } else if (MethodsHighways.hasLink(dirs, (byte)32)) {
      startdir = 32;
    } else if (MethodsHighways.hasLink(dirs, (byte)64)) {
      startdir = 64;
    } else if (MethodsHighways.hasLink(dirs, (byte)Byte.MIN_VALUE)) {
      startdir = Byte.MIN_VALUE;
    }
    return startdir;
  }
  
  public static final Item[] getMarkers()
  {
    ConcurrentHashMap<Long, Item> markers = new ConcurrentHashMap();
    for (Route route : allRoutes.values())
    {
      Item waystone = route.getStartNode().getWaystone();
      markers.put(Long.valueOf(waystone.getWurmId()), waystone);
      for (Item catseye : route.getCatseyes()) {
        markers.put(Long.valueOf(catseye.getWurmId()), catseye);
      }
      Node node = route.getEndNode();
      if (node != null) {
        markers.put(Long.valueOf(node.getWaystone().getWurmId()), node.getWaystone());
      }
    }
    return (Item[])markers.values().toArray(new Item[markers.size()]);
  }
  
  public static final Item[] getRouteMarkers(Item marker)
  {
    ConcurrentHashMap<Long, Item> markers = new ConcurrentHashMap();
    Object endWaystone;
    if (marker.getTemplateId() == 1114) {
      for (Route route : allRoutes.values()) {
        if (route.containsCatseye(marker))
        {
          Item startWaystone = route.getStartNode().getWaystone();
          markers.put(Long.valueOf(startWaystone.getWurmId()), startWaystone);
          for (Item catseye : route.getCatseyes()) {
            markers.put(Long.valueOf(catseye.getWurmId()), catseye);
          }
          endWaystone = route.getEndNode().getWaystone();
          markers.put(Long.valueOf(((Item)endWaystone).getWurmId()), endWaystone);
          
          break;
        }
      }
    } else {
      for (Route route : allRoutes.values())
      {
        Object endWaystone;
        if (route.getStartNode().getWurmId() == marker.getWurmId())
        {
          Item startWaystone = route.getStartNode().getWaystone();
          markers.put(Long.valueOf(startWaystone.getWurmId()), startWaystone);
          for (Item catseye : route.getCatseyes()) {
            markers.put(Long.valueOf(catseye.getWurmId()), catseye);
          }
          endWaystone = route.getEndNode().getWaystone();
          markers.put(Long.valueOf(((Item)endWaystone).getWurmId()), endWaystone);
        }
        if (route.getEndNode().getWurmId() == marker.getWurmId())
        {
          Item startWaystone = route.getStartNode().getWaystone();
          markers.put(Long.valueOf(startWaystone.getWurmId()), startWaystone);
          for (Item catseye : route.getCatseyes()) {
            markers.put(Long.valueOf(catseye.getWurmId()), catseye);
          }
          Item endWaystone = route.getEndNode().getWaystone();
          markers.put(Long.valueOf(endWaystone.getWurmId()), endWaystone);
        }
      }
    }
    return (Item[])markers.values().toArray(new Item[markers.size()]);
  }
  
  public static final boolean isCatseyeUsed(Item catseye)
  {
    for (Route route : allRoutes.values()) {
      if (route.containsCatseye(catseye)) {
        return true;
      }
    }
    return false;
  }
  
  public static final boolean isMarkerUsed(Item marker)
  {
    if (marker.getTemplateId() == 1114) {
      return isCatseyeUsed(marker);
    }
    for (Route route : allRoutes.values())
    {
      if (route.getStartNode().getWaystone().getWurmId() == marker.getWurmId()) {
        return true;
      }
      if ((route.getEndNode() != null) && (route.getEndNode().getWaystone().getWurmId() == marker.getWurmId())) {
        return true;
      }
    }
    return false;
  }
  
  @Nullable
  public static final Route getRoute(int id)
  {
    return (Route)allRoutes.get(Integer.valueOf(id));
  }
  
  @Nullable
  public static final Node getNode(long wurmId)
  {
    return (Node)allNodes.get(Long.valueOf(wurmId));
  }
  
  public static final Route[] getAllRoutes()
  {
    return (Route[])allRoutes.values().toArray(new Route[allRoutes.size()]);
  }
  
  public static final Node[] getAllNodes()
  {
    return (Node[])allNodes.values().toArray(new Node[allNodes.size()]);
  }
  
  public static final Village[] getVillages()
  {
    ConcurrentHashMap<Integer, Village> villages = new ConcurrentHashMap();
    for (Node node : allNodes.values())
    {
      Village vill = node.getVillage();
      if ((vill != null) && (vill.isHighwayFound())) {
        villages.put(Integer.valueOf(vill.getId()), vill);
      }
    }
    return (Village[])villages.values().toArray(new Village[villages.size()]);
  }
  
  public static final Village[] getVillages(long waystoneId)
  {
    ConcurrentHashMap<Integer, Village> villages = new ConcurrentHashMap();
    for (Node node : allNodes.values())
    {
      Village vill = node.getVillage();
      if ((vill != null) && (vill.isHighwayFound())) {
        if (PathToCalculate.isVillageConnected(waystoneId, vill)) {
          villages.put(Integer.valueOf(vill.getId()), vill);
        }
      }
    }
    return (Village[])villages.values().toArray(new Village[villages.size()]);
  }
  
  public static final Node[] getNodesFor(Village village)
  {
    ConcurrentHashMap<Long, Node> nodes = new ConcurrentHashMap();
    for (Node node : allNodes.values())
    {
      Village vill = node.getVillage();
      if ((vill != null) && (vill.equals(village))) {
        nodes.put(Long.valueOf(node.getWaystone().getWurmId()), node);
      }
    }
    return (Node[])nodes.values().toArray(new Node[nodes.size()]);
  }
  
  public static final void handlePathsToSend()
  {
    PlayerMessageToSend playerMessageToSend = (PlayerMessageToSend)playerMessagesToSend.pollFirst();
    while (playerMessageToSend != null)
    {
      playerMessageToSend.send();
      playerMessageToSend = (PlayerMessageToSend)playerMessagesToSend.pollFirst();
    }
  }
  
  public static final void queuePlayerMessage(Player player, String text)
  {
    playerMessagesToSend.add(new PlayerMessageToSend(player, text));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\highways\Routes.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */