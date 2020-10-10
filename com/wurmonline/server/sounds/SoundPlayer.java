package com.wurmonline.server.sounds;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SoundPlayer
{
  private static final Logger logger = Logger.getLogger(SoundPlayer.class.getName());
  
  public static final void playSound(String soundName, Creature creature, float height)
  {
    if (soundName.length() > 0) {
      try
      {
        VolaTile vtile = Zones.getOrCreateTile(creature.getTileX(), creature.getTileY(), creature.isOnSurface());
        
        float offsetx = 4.0F * Server.rand.nextFloat();
        
        float offsety = 4.0F * Server.rand.nextFloat();
        
        Sound so = new Sound(soundName, creature.getPosX() - 2.0F + offsetx, creature.getPosY() - 2.0F + offsety, Zones.calculateHeight(creature.getPosX(), creature.getPosY(), creature
          .isOnSurface()) + height, 1.0F, 1.0F, 5.0F);
        
        vtile.addSound(so);
      }
      catch (NoSuchZoneException nsz)
      {
        logger.log(Level.WARNING, "Can't play sound at " + creature.getPosX() + ", " + creature.getPosY() + " surfaced=" + creature
        
          .isOnSurface(), nsz);
      }
    }
  }
  
  public static final void playSound(String soundName, Item item, float height)
  {
    try
    {
      VolaTile vtile = Zones.getOrCreateTile((int)item.getPosX() >> 2, (int)item.getPosY() >> 2, item
        .isOnSurface());
      Sound so = new Sound(soundName, item.getPosX(), item.getPosY(), Zones.calculateHeight(item.getPosX(), item
        .getPosY(), item.isOnSurface()) + height, 1.0F, 1.0F, 5.0F);
      
      vtile.addSound(so);
    }
    catch (NoSuchZoneException nsz)
    {
      logger.log(Level.WARNING, "Can't play sound at " + item
        .getPosX() + ", " + item.getPosY() + " surfaced=" + item.isOnSurface(), nsz);
    }
  }
  
  public static final void playSound(String soundName, int tilex, int tiley, boolean surfaced, float height)
  {
    try
    {
      VolaTile vtile = Zones.getOrCreateTile(tilex, tiley, surfaced);
      Sound so = new Sound(soundName, (tilex << 2) + 2, (tiley << 2) + 2, Zones.calculateHeight((tilex << 2) + 2, (tiley << 2) + 2, surfaced) + height, 1.0F, 1.0F, 5.0F);
      
      vtile.addSound(so);
    }
    catch (NoSuchZoneException localNoSuchZoneException) {}
  }
  
  public static final void playSong(String songName, Creature creature)
  {
    playSong(songName, creature, 1.0F);
  }
  
  public static final void playSong(String songName, Creature creature, float pitch)
  {
    try
    {
      Sound so = new Sound(songName, creature.getPosX(), creature.getPosY(), Zones.calculateHeight(creature
        .getPosX(), creature.getPosY(), creature.isOnSurface()), 1.0F, pitch, 5.0F);
      
      creature.getCommunicator().sendMusic(so);
    }
    catch (NoSuchZoneException nsz)
    {
      logger.log(Level.WARNING, "Can't play sound at " + creature.getPosX() + ", " + creature.getPosY() + " surfaced=" + creature
        .isOnSurface(), nsz);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\sounds\SoundPlayer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */