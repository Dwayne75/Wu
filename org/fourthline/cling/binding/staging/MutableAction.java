package org.fourthline.cling.binding.staging;

import java.util.ArrayList;
import java.util.List;
import org.fourthline.cling.model.meta.Action;
import org.fourthline.cling.model.meta.ActionArgument;

public class MutableAction
{
  public String name;
  public List<MutableActionArgument> arguments = new ArrayList();
  
  public Action build()
  {
    return new Action(this.name, createActionArgumennts());
  }
  
  public ActionArgument[] createActionArgumennts()
  {
    ActionArgument[] array = new ActionArgument[this.arguments.size()];
    int i = 0;
    for (MutableActionArgument argument : this.arguments) {
      array[(i++)] = argument.build();
    }
    return array;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\binding\staging\MutableAction.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */