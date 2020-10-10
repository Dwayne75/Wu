package org.kohsuke.rngom.digested;

public class DChoicePattern
  extends DContainerPattern
{
  public boolean isNullable()
  {
    for (DPattern p = firstChild(); p != null; p = p.next) {
      if (p.isNullable()) {
        return true;
      }
    }
    return false;
  }
  
  public <V> V accept(DPatternVisitor<V> visitor)
  {
    return (V)visitor.onChoice(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\DChoicePattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */