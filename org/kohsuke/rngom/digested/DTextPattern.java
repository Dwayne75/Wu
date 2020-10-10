package org.kohsuke.rngom.digested;

public class DTextPattern
  extends DPattern
{
  public boolean isNullable()
  {
    return true;
  }
  
  public Object accept(DPatternVisitor visitor)
  {
    return visitor.onText(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\DTextPattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */