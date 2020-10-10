package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;

public class DefaultActiveFunc
  implements ActiveFunc
{
  public boolean active(Creature victim, Creature receiver)
  {
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\loot\DefaultActiveFunc.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */