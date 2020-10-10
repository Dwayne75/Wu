package org.kohsuke.rngom.digested;

import org.kohsuke.rngom.nc.NameClass;

public class DElementPattern
  extends DXmlTokenPattern
{
  public DElementPattern(NameClass name)
  {
    super(name);
  }
  
  public Object accept(DPatternVisitor visitor)
  {
    return visitor.onElement(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\DElementPattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */