package org.kohsuke.rngom.digested;

import org.kohsuke.rngom.nc.NameClass;

public abstract class DXmlTokenPattern
  extends DUnaryPattern
{
  private final NameClass name;
  
  public DXmlTokenPattern(NameClass name)
  {
    this.name = name;
  }
  
  public NameClass getName()
  {
    return this.name;
  }
  
  public final boolean isNullable()
  {
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\DXmlTokenPattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */