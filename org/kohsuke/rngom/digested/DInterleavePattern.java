package org.kohsuke.rngom.digested;

public class DInterleavePattern
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
  
  public Object accept(DPatternVisitor visitor)
  {
    return visitor.onInterleave(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\DInterleavePattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */