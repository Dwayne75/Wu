package com.wurmonline.server.creatures;

import java.util.logging.Level;
import java.util.logging.Logger;

final class CreatureStatusFactory
{
  private static final Logger logger = Logger.getLogger(CreatureStatusFactory.class.getName());
  
  static CreatureStatus createCreatureStatus(Creature creature, float posx, float posy, float rot, int layer)
    throws Exception
  {
    CreatureStatus toReturn = null;
    
    toReturn = new DbCreatureStatus(creature, posx, posy, rot, layer);
    if (logger.isLoggable(Level.FINEST)) {
      logger.finest("Created new CreatureStatus: " + toReturn);
    }
    return toReturn;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\creatures\CreatureStatusFactory.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */