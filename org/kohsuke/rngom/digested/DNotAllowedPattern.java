package org.kohsuke.rngom.digested;

public class DNotAllowedPattern
  extends DPattern
{
  public boolean isNullable()
  {
    return false;
  }
  
  public Object accept(DPatternVisitor visitor)
  {
    return visitor.onNotAllowed(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\DNotAllowedPattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */