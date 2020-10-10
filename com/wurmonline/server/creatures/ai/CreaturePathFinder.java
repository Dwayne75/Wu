package com.wurmonline.server.creatures.ai;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreaturePathFinder
  extends TimerTask
{
  private final Map<Creature, PathTile> pathTargets = new ConcurrentHashMap();
  private boolean keeprunning = true;
  public static final long SLEEP_TIME = 25L;
  private static final StaticPathFinder pathFinder = new StaticPathFinder();
  private static Logger logger = Logger.getLogger(CreaturePathFinder.class.getName());
  
  public final void addTarget(Creature c, PathTile target)
  {
    this.pathTargets.put(c, target);
  }
  
  public final void removeTarget(Creature c)
  {
    this.pathTargets.remove(c);
  }
  
  private static boolean log = false;
  
  public boolean isLog()
  {
    return log;
  }
  
  public final void toggleLog()
  {
    setLog(!isLog());
  }
  
  public void setLog(boolean nlog)
  {
    log = nlog;
  }
  
  public final void startRunning()
  {
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(this, 40000L, 25L);
  }
  
  public final void shutDown()
  {
    this.keeprunning = false;
  }
  
  public void run()
  {
    if (this.keeprunning)
    {
      long now = System.currentTimeMillis();
      Iterator<Map.Entry<Creature, PathTile>> it;
      if (!this.pathTargets.isEmpty()) {
        for (it = this.pathTargets.entrySet().iterator(); it.hasNext();)
        {
          Map.Entry<Creature, PathTile> entry = (Map.Entry)it.next();
          Creature creature = (Creature)entry.getKey();
          PathTile p = (PathTile)entry.getValue();
          try
          {
            Path path = creature.findPath(p.getTileX(), p.getTileY(), pathFinder);
            if (path != null)
            {
              if (p.hasSpecificPos())
              {
                if (path.getPathTiles().isEmpty()) {
                  path.getPathTiles().add(new PathTile(creature.getTileX(), creature.getTileY(), creature
                    .getCurrentTileNum(), creature.isOnSurface(), creature.getFloorLevel()));
                }
                PathTile lastTile = (PathTile)path.getPathTiles().getLast();
                lastTile.setSpecificPos(p.getPosX(), p.getPosY());
              }
              creature.sendToLoggers("Found path to " + p.getTileX() + "," + p.getTileY());
              creature.getStatus().setPath(path);
              creature.receivedPath = true;
              it.remove();
            }
          }
          catch (NoPathException np)
          {
            creature.sendToLoggers("No Path to " + p.getTileX() + "," + p.getTileY() + " pathfindcounter=" + creature
              .getPathfindCounter() + " || " + np.getMessage());
            it.remove();
            creature.setPathing(false, false);
          }
        }
      }
      if ((log) && (System.currentTimeMillis() - now > 0L)) {
        logger.log(Level.INFO, "Norm Finding paths took " + (
          System.currentTimeMillis() - now) + " ms for " + this.pathTargets.size());
      }
    }
    else
    {
      logger.log(Level.INFO, "Shutting down Norm pathfinder");
      cancel();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\creatures\ai\CreaturePathFinder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */