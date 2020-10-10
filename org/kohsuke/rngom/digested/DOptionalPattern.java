package org.kohsuke.rngom.digested;

public class DOptionalPattern
  extends DUnaryPattern
{
  public boolean isNullable()
  {
    return true;
  }
  
  public Object accept(DPatternVisitor visitor)
  {
    return visitor.onOptional(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\DOptionalPattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */