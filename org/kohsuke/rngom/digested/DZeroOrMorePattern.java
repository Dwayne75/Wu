package org.kohsuke.rngom.digested;

public class DZeroOrMorePattern
  extends DUnaryPattern
{
  public boolean isNullable()
  {
    return true;
  }
  
  public Object accept(DPatternVisitor visitor)
  {
    return visitor.onZeroOrMore(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\DZeroOrMorePattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */