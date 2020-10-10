package com.wurmonline.server.creatures.ai;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.server.Constants;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.zones.Zones;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class StaticPathFinderAgg
  extends PathFinder
  implements MiscConstants
{
  private static LinkedList<PathTile> pathList;
  private static LinkedList<PathTile> pList;
  private static final int NOT_FOUND = 0;
  private static final int FOUND = 1;
  private static final int NO_PATH = 2;
  private static final int MAX_DISTANCE = 50;
  private static final int MAX_ASTAR_STEPS = 10000;
  private static StaticPathMeshAgg mesh = null;
  private static final int WORLD_SIZE = 1 << Constants.meshSize;
  private static final Logger logger = Logger.getLogger(PathFinder.class.getName());
  
  public StaticPathFinderAgg() {}
  
  public StaticPathFinderAgg(boolean ignoresWalls)
  {
    this.ignoreWalls = ignoresWalls;
  }
  
  public Path findPath(Creature aCreature, int startTileX, int startTileY, int endTileX, int endTileY, boolean surf, int areaSz)
    throws NoPathException
  {
    this.creature = aCreature;
    if (this.creature != null) {
      this.creatureHalfHeight = this.creature.getHalfHeightDecimeters();
    }
    return findPath(startTileX, startTileY, endTileX, endTileY, surf, areaSz);
  }
  
  private Path findPath(int startTileX, int startTileY, int endTileX, int endTileY, boolean surf, int areaSz)
    throws NoPathException
  {
    this.endX = Zones.safeTileX(endTileX);
    this.endY = Zones.safeTileY(endTileY);
    this.startX = Zones.safeTileX(startTileX);
    this.startY = Zones.safeTileY(startTileY);
    int _diffX = Math.abs(this.endX - this.startX);
    int _diffY = Math.abs(this.endY - this.startY);
    if (_diffX > 50)
    {
      int stepsX = Server.rand.nextInt(Math.min(50, _diffX + 1));
      if (this.endX < this.startX) {
        stepsX = -stepsX;
      }
      this.endX = (this.startX + stepsX);
    }
    if (_diffY > 50)
    {
      int stepsY = Server.rand.nextInt(Math.min(50, _diffY + 1));
      if (this.endY < this.startY) {
        stepsY = -stepsY;
      }
      this.endY = (this.startY + stepsY);
    }
    this.startX = Zones.safeTileX(this.startX);
    this.startY = Zones.safeTileY(this.startY);
    this.endX = Zones.safeTileX(this.endX);
    this.endY = Zones.safeTileY(this.endY);
    
    this.surfaced = surf;
    this.areaSize = areaSz;
    setMesh();
    Path toReturn = new Path();
    if (this.surfaced) {
      try
      {
        toReturn = rayCast(this.startX, this.startY, this.endX, this.endY, this.surfaced);
      }
      catch (NoPathException np)
      {
        toReturn = startAstar(this.startX, this.startY, this.endX, this.endY);
      }
    } else {
      toReturn = startAstar(this.startX, this.startY, this.endX, this.endY);
    }
    return toReturn;
  }
  
  Path startAstar(int _startX, int _startY, int _endX, int _endY)
    throws NoPathException
  {
    Path toReturn = new Path();
    try
    {
      toReturn = astar(_startX, _startY, _endX, _endY, this.surfaced);
      if (logger.isLoggable(Level.FINEST)) {
        logger.finest(this.creature.getName() + " astared a path.");
      }
    }
    catch (NoPathException np2)
    {
      if (this.creature != null)
      {
        if (((this.creature.isKingdomGuard()) || (this.creature.isSpiritGuard()) || (this.creature.isUnique()) || (this.creature.isDominated())) && (this.creature.target == -10L))
        {
          int _diffX = Math.max(1, Math.abs(_endX - _startX) / 2);
          int _diffY = Math.max(1, Math.abs(_endY - _startY) / 2);
          
          int stepsX = Server.rand.nextInt(Math.min(50, _diffX + 1));
          if (this.endX < this.startX) {
            stepsX = -stepsX;
          }
          this.endX = (this.startX + stepsX);
          int stepsY = Server.rand.nextInt(Math.min(50, _diffY + 1));
          if (this.endY < this.startY) {
            stepsY = -stepsY;
          }
          this.endY = (this.startY + stepsY);
          if ((stepsY != 0) || (stepsX != 0))
          {
            setMesh();
            if ((!this.surfaced) || (this.creature.isKingdomGuard()) || (this.creature.isUnique()) || (this.creature.isDominated())) {
              toReturn = astar(_startX, _startY, _endX, _endY, this.surfaced);
            } else {
              toReturn = rayCast(_startX, _startY, _endX, _endY, this.surfaced);
            }
          }
        }
        else
        {
          throw np2;
        }
      }
      else {
        throw np2;
      }
    }
    return toReturn;
  }
  
  public Path rayCast(int startTileX, int startTileY, int endTileX, int endTileY, boolean surf, int areaSz)
    throws NoPathException
  {
    this.startX = Math.max(0, startTileX);
    this.startY = Math.max(0, startTileY);
    this.endX = Math.min(WORLD_SIZE - 1, endTileX);
    this.endY = Math.min(WORLD_SIZE - 1, endTileY);
    this.surfaced = surf;
    this.areaSize = areaSz;
    setMesh();
    
    return rayCast(this.startX, this.startY, this.endX, this.endY, this.surfaced);
  }
  
  void setMesh()
  {
    StaticPathMeshAgg.clearPathables();
    mesh = new StaticPathMeshAgg(this.startX, this.startY, this.endX, this.endY, this.surfaced, this.areaSize);
    
    this.current = mesh.getStart();
    this.start = mesh.getStart();
    this.finish = mesh.getFinish();
    if (logger.isLoggable(Level.FINEST)) {
      logger.finest("Start is " + this.start.toString() + ", finish " + this.finish.toString());
    }
  }
  
  Path rayCast(int startTileX, int startTileY, int endTileX, int endTileY, boolean aSurfaced)
    throws NoPathException
  {
    int endHeight = Tiles.decodeHeight(this.finish.getTile());
    int startHeight = Tiles.decodeHeight(this.start.getTile());
    if ((this.creature != null) && (!this.creature.isSwimming()) && (!this.creature.isSubmerged()) && (endHeight < -this.creatureHalfHeight) && 
      (Tiles.decodeType(this.finish.getTile()) != Tiles.Tile.TILE_CAVE_EXIT.id) && 
      (Tiles.decodeType(this.finish.getTile()) != Tiles.Tile.TILE_HOLE.id) && (endHeight < startHeight)) {
      throw new NoPathException("Target in water.");
    }
    this.maxSteps = (Math.max(Math.abs(this.endX - this.startX), Math.abs(this.endY - this.startY)) + 1);
    this.stepsTaken = 0;
    pathList = new LinkedList();
    float diffX = this.endX - this.startX;
    float diffY = this.endY - this.startY;
    if (diffX == 0.0F)
    {
      this.derivX = 0.0F;
      this.derivY = diffY;
    }
    if (diffY == 0.0F)
    {
      this.derivY = 0.0F;
      this.derivX = diffX;
    }
    if ((diffX != 0.0F) && (diffY != 0.0F))
    {
      this.derivX = Math.abs(diffX / diffY);
      this.derivY = Math.abs(diffY / diffX);
    }
    if ((diffY < 0.0F) && (this.derivY > 0.0F)) {
      this.derivY = (-this.derivY);
    }
    if ((diffX < 0.0F) && (this.derivX > 0.0F)) {
      this.derivX = (-this.derivX);
    }
    while (!this.current.equals(this.finish))
    {
      this.current = step();
      pathList.add(this.current);
    }
    return new Path(pathList);
  }
  
  PathTile step()
    throws NoPathException
  {
    int x = this.current.getTileX();
    int y = this.current.getTileY();
    boolean raycend = true;
    if (!this.surfaced) {
      raycend = false;
    }
    if ((raycend) && (Math.abs(this.endX - x) <= 1) && (Math.abs(this.endY - y) <= 1))
    {
      x = this.endX;
      y = this.endY;
    }
    else if ((Math.abs(this.endX - x) < 1) && (Math.abs(this.endY - y) < 1))
    {
      x = this.endX;
      y = this.endY;
      logger.log(Level.INFO, "This really shouldn't happen i guess, since it should have been detected already.");
    }
    else
    {
      if ((this.derivX > 0.0F) && (x < this.endX))
      {
        if (this.derivX >= 1.0F)
        {
          x++;
        }
        else
        {
          this.restX += this.derivX;
          if (this.restX >= 1.0F)
          {
            x++;
            this.restX -= 1.0F;
          }
        }
      }
      else if ((this.derivX < 0.0F) && (x > this.endX)) {
        if (this.derivX <= -1.0F)
        {
          x--;
        }
        else
        {
          this.restX += this.derivX;
          if (this.restX <= -1.0F)
          {
            x--;
            this.restX += 1.0F;
          }
        }
      }
      if ((this.derivY > 0.0F) && (y < this.endY))
      {
        if (this.derivY >= 1.0F)
        {
          y++;
        }
        else
        {
          this.restY += this.derivY;
          if (this.restY >= 1.0F)
          {
            y++;
            this.restY -= 1.0F;
          }
        }
      }
      else if ((this.derivY < 0.0F) && (y > this.endY)) {
        if (this.derivY <= -1.0F)
        {
          y--;
        }
        else
        {
          this.restY += this.derivY;
          if (this.restY <= -1.0F)
          {
            y--;
            this.restY += 1.0F;
          }
        }
      }
    }
    if (!mesh.contains(x, y)) {
      throw new NoPathException("Path missed at " + x + ", " + y);
    }
    PathTile toReturn = null;
    try
    {
      toReturn = mesh.getPathTile(x, y);
      if (!canPass(this.current, toReturn)) {
        throw new NoPathException("Path blocked between " + this.current.toString() + " and " + toReturn.toString());
      }
    }
    catch (ArrayIndexOutOfBoundsException ai)
    {
      logger.log(Level.WARNING, "OUT OF BOUNDS AT RAYCAST: " + x + ", " + y + ": " + ai.getMessage(), ai);
      logger.log(Level.WARNING, "Mesh info: " + mesh.getBorderStartX() + ", " + mesh.getBorderStartY() + ", to " + mesh
        .getBorderEndX() + ", " + mesh.getBorderEndY());
      logger.log(Level.WARNING, "Size of meshx=" + mesh.getSizex() + ", meshy=" + mesh.getSizey());
      throw new NoPathException("Path missed at " + x + ", " + y);
    }
    if (this.stepsTaken > this.maxSteps)
    {
      if (logger.isLoggable(Level.FINEST)) {
        logger.finest("Raycaster stops searching after " + this.stepsTaken + " steps, suspecting it missed the target.");
      }
      throw new NoPathException("Probably missed target using raycaster.");
    }
    this.stepsTaken += 1;
    return toReturn;
  }
  
  static float cbDist(PathTile a, PathTile b, float low)
  {
    return low * (Math.abs(a.getTileX() - b.getTileX()) + Math.abs(a.getTileY() - b.getTileY()) - 1);
  }
  
  static float getCost(int tile)
  {
    if (Tiles.isSolidCave(Tiles.decodeType(tile))) {
      return Float.MAX_VALUE;
    }
    if (Tiles.decodeHeight(tile) < 1) {
      return 3.0F;
    }
    return 1.0F;
  }
  
  Path astar(int startTileX, int startTileY, int endTileX, int endTileY, boolean aSurfaced)
    throws NoPathException
  {
    int endHeight = Tiles.decodeHeight(this.finish.getTile());
    int startHeight = Tiles.decodeHeight(this.start.getTile());
    if ((this.creature != null) && (!this.creature.isSwimming()) && (!this.creature.isSubmerged()) && (endHeight < -this.creatureHalfHeight)) {
      if ((Tiles.decodeType(this.finish.getTile()) != Tiles.Tile.TILE_CAVE_EXIT.id) && 
        (Tiles.decodeType(this.finish.getTile()) != Tiles.Tile.TILE_HOLE.id) && (endHeight < startHeight)) {
        throw new NoPathException("Target in water.");
      }
    }
    pathList = new LinkedList();
    if ((this.start != null) && (this.finish != null) && (this.start.equals(this.finish))) {
      return null;
    }
    if (this.finish == null)
    {
      if (this.creature != null) {
        logger.log(Level.WARNING, this.creature.getName() + " finish=null at " + endTileX + ", " + endTileY);
      } else {
        logger.log(Level.WARNING, "Finish=null at " + endTileX + ", " + endTileY);
      }
      return null;
    }
    if (this.start == null)
    {
      if (this.creature != null) {
        logger.log(Level.WARNING, this.creature.getName() + " start=null at " + startTileX + ", " + startTileY);
      } else {
        logger.log(Level.WARNING, "start=null at " + startTileX + ", " + startTileY);
      }
      return null;
    }
    this.start.setDistanceFromStart(this.start, 0.0F);
    pathList.add(this.start);
    
    int pass = 0;
    
    int lState = 0;
    while ((lState == 0) && (pass < 10000))
    {
      pass++;
      
      lState = step2();
    }
    if (lState == 1)
    {
      if (pass > 4000)
      {
        String cname = "Unknown";
        if (this.creature != null) {
          cname = this.creature.getName();
        }
        logger.log(Level.INFO, cname + " pathed from " + this.startX + ", " + this.startY + " to " + this.endX + ", " + this.endY + " and found path after " + pass + " steps.");
      }
      return setPath();
    }
    if (lState == 2) {
      throw new NoPathException("No path possible after " + pass + " tries.");
    }
    throw new NoPathException("No path found after " + pass + " tries.");
  }
  
  private void setDebug(boolean aDebug)
  {
    this.debug = aDebug;
    if (logger.isLoggable(Level.FINEST)) {
      logger.finest("Debug in pathfinding - " + aDebug);
    }
  }
  
  int step2()
  {
    boolean found = false;
    float min = Float.MAX_VALUE;
    
    float score = 0.0F;
    PathTile best = (PathTile)pathList.get(pathList.size() - 1);
    PathTile now = null;
    for (int i = 0; i < pathList.size(); i++)
    {
      now = (PathTile)pathList.get(i);
      score = now.getDistanceFromStart();
      score += cbDist(now, this.finish, getCost(now.getTile()));
      if (!now.isUsed()) {
        if (score < min)
        {
          min = score;
          best = now;
        }
      }
    }
    now = best;
    pathList.remove(now);
    now.setUsed();
    
    PathTile[] next = mesh.getAdjacent(now);
    for (int i = 0; i < next.length; i++)
    {
      if (next[i] != null) {
        if (canPass(now, next[i]))
        {
          if ((!pathList.contains(next[i])) && (next[i].isNotUsed())) {
            pathList.add(next[i]);
          }
          if (next[i] == this.finish) {
            found = true;
          }
          score = now.getDistanceFromStart() + next[i].getMoveCost();
          next[i].setDistanceFromStart(now, score);
        }
      }
      if (found) {
        return 1;
      }
    }
    if (pathList.isEmpty()) {
      return 2;
    }
    return 0;
  }
  
  private PathTile findLowestDist(PathTile aStart, PathTile now)
  {
    return now.getLink();
  }
  
  Path setPath()
  {
    setDebug(this.debug);
    boolean finished = false;
    
    PathTile now = this.finish;
    PathTile stop = this.start;
    pList = new LinkedList();
    PathTile lastCurrent = now;
    while (!finished)
    {
      pList.add(now);
      
      PathTile next = findLowestDist(this.start, now);
      if (lastCurrent.equals(next))
      {
        finished = true;
        logger.log(Level.WARNING, "Loop in heuristicastar.");
      }
      lastCurrent = now;
      now = next;
      if (now.equals(stop))
      {
        if ((Math.abs(lastCurrent.getTileX() - now.getTileX()) > 1) || 
          (Math.abs(lastCurrent.getTileY() - now.getTileY()) > 1)) {
          pList.add(now);
        }
        finished = true;
      }
    }
    LinkedList<PathTile> inverted = new LinkedList();
    for (Iterator<PathTile> it = pList.iterator(); it.hasNext();) {
      inverted.addFirst(it.next());
    }
    Path path = new Path(inverted);
    
    setDebug(false);
    return path;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\creatures\ai\StaticPathFinderAgg.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */