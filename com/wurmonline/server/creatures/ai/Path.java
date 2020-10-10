package com.wurmonline.server.creatures.ai;

import java.util.LinkedList;

public final class Path
{
  private LinkedList<PathTile> path;
  
  Path()
  {
    this.path = new LinkedList();
  }
  
  public Path(LinkedList<PathTile> pathlist)
  {
    this.path = pathlist;
  }
  
  public PathTile getFirst()
  {
    return (PathTile)this.path.getFirst();
  }
  
  public PathTile getTargetTile()
  {
    return (PathTile)this.path.getLast();
  }
  
  public int getSize()
  {
    return this.path.size();
  }
  
  public void removeFirst()
  {
    this.path.removeFirst();
  }
  
  public boolean isEmpty()
  {
    return (this.path == null) || (this.path.isEmpty());
  }
  
  public LinkedList<PathTile> getPathTiles()
  {
    return this.path;
  }
  
  public void clear()
  {
    if (this.path != null) {
      this.path.clear();
    }
    this.path = null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\creatures\ai\Path.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */