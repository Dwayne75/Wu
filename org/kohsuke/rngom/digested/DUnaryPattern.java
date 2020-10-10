package org.kohsuke.rngom.digested;

public abstract class DUnaryPattern
  extends DPattern
{
  private DPattern child;
  
  public DPattern getChild()
  {
    return this.child;
  }
  
  public void setChild(DPattern child)
  {
    this.child = child;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\DUnaryPattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */