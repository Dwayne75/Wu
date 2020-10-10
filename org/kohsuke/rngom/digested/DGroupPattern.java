package org.kohsuke.rngom.digested;

public class DGroupPattern
  extends DContainerPattern
{
  public boolean isNullable()
  {
    for (DPattern p = firstChild(); p != null; p = p.next) {
      if (!p.isNullable()) {
        return false;
      }
    }
    return true;
  }
  
  public <V> V accept(DPatternVisitor<V> visitor)
  {
    return (V)visitor.onGroup(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\DGroupPattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */