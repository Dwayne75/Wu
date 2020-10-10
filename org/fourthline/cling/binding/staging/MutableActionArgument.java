package org.fourthline.cling.binding.staging;

import org.fourthline.cling.model.meta.ActionArgument;
import org.fourthline.cling.model.meta.ActionArgument.Direction;

public class MutableActionArgument
{
  public String name;
  public String relatedStateVariable;
  public ActionArgument.Direction direction;
  public boolean retval;
  
  public ActionArgument build()
  {
    return new ActionArgument(this.name, this.relatedStateVariable, this.direction, this.retval);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\binding\staging\MutableActionArgument.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */