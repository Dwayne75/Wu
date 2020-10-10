package org.kohsuke.rngom.digested;

public class DMixedPattern
  extends DUnaryPattern
{
  public boolean isNullable()
  {
    return getChild().isNullable();
  }
  
  public Object accept(DPatternVisitor visitor)
  {
    return visitor.onMixed(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\DMixedPattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */