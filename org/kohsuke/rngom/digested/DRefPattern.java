package org.kohsuke.rngom.digested;

public class DRefPattern
  extends DPattern
{
  private final DDefine target;
  
  public DRefPattern(DDefine target)
  {
    this.target = target;
  }
  
  public boolean isNullable()
  {
    return this.target.isNullable();
  }
  
  public DDefine getTarget()
  {
    return this.target;
  }
  
  public String getName()
  {
    return this.target.getName();
  }
  
  public Object accept(DPatternVisitor visitor)
  {
    return visitor.onRef(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\DRefPattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */