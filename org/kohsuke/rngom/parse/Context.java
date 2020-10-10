package org.kohsuke.rngom.parse;

import java.util.Enumeration;
import org.relaxng.datatype.ValidationContext;

public abstract interface Context
  extends ValidationContext
{
  public abstract Enumeration prefixes();
  
  public abstract Context copy();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\parse\Context.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */