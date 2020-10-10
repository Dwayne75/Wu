package com.wurmonline.server.spells;

import com.wurmonline.mesh.BushData.BushType;
import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.mesh.TreeData.TreeType;
import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.FaithZone;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zones;

public class RiteDeath
  extends ReligiousSpell
{
  public static final int RANGE = 4;
  
  public RiteDeath()
  {
    super("Rite of Death", 402, 100, 300, 60, 50, 43200000L);
    this.isRitual = true;
    this.targetItem = true;
    this.description = "spawns mycelium in your gods domain";
    this.type = 0;
  }
  
  boolean precondition(Skill castSkill, Creature performer, Item target)
  {
    if (!Servers.localServer.PVPSERVER)
    {
      performer.getCommunicator().sendNormalServerMessage("This spell does not work here.", (byte)3);
      return false;
    }
    if (performer.getDeity() != null)
    {
      Deity deity = performer.getDeity();
      Deity templateDeity = Deities.getDeity(deity.getTemplateDeity());
      if ((templateDeity.getFavor() < 1000) && (!Servers.isThisATestServer()))
      {
        performer.getCommunicator().sendNormalServerMessage(deity
          .getName() + " can not grant that power right now.", (byte)3);
        return false;
      }
      if (target.getBless() == deity) {
        if (target.isDomainItem()) {
          return true;
        }
      }
      performer.getCommunicator().sendNormalServerMessage(
        String.format("You need to cast this spell at an altar of %s.", new Object[] {deity.getName() }), (byte)3);
    }
    return false;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Item target)
  {
    Deity deity = performer.getDeity();
    Deity templateDeity = Deities.getDeity(deity.getTemplateDeity());
    
    performer.getCommunicator().sendNormalServerMessage("The domain of " + performer.getDeity().getName() + " is covered in mycelium.", (byte)2);
    Server.getInstance().broadCastSafe("As the Rite of Death is completed, followers of " + deity.getName() + " may now receive a blessing!");
    HistoryManager.addHistory(performer.getName(), "casts " + this.name + ". The domain of " + performer.getDeity().getName() + " is covered in mycelium.");
    templateDeity.setFavor(templateDeity.getFavor() - 1000);
    
    performer.achievement(635);
    
    new RiteEvent.RiteOfDeathEvent(-10, performer.getWurmId(), getNumber(), deity.getNumber(), System.currentTimeMillis(), 86400000L);
    byte type;
    int x;
    int y;
    if (Features.Feature.NEWDOMAINS.isEnabled())
    {
      byte type = 0;
      for (FaithZone f : Zones.getFaithZones()) {
        if ((f != null) && (f.getCurrentRuler().getTemplateDeity() == deity.getTemplateDeity()))
        {
          try
          {
            if (Zones.getFaithZone(f.getCenterX(), f.getCenterY(), true) != f) {
              continue;
            }
          }
          catch (NoSuchZoneException e)
          {
            continue;
          }
          for (int tx = f.getStartX(); tx < f.getEndX(); tx++) {
            for (int ty = f.getStartY(); ty < f.getEndY(); ty++) {
              effectTile(tx, ty, type);
            }
          }
        }
      }
    }
    else
    {
      FaithZone[][] surfaceZones = Zones.getFaithZones(true);
      type = 0;
      for (x = 0; x < Zones.faithSizeX; x++) {
        for (y = 0; y < Zones.faithSizeY; y++) {
          if (surfaceZones[x][y].getCurrentRuler().getTemplateDeity() == deity.getTemplateDeity()) {
            for (int tx = surfaceZones[x][y].getStartX(); tx <= surfaceZones[x][y].getEndX(); tx++) {
              for (int ty = surfaceZones[x][y].getStartY(); ty <= surfaceZones[x][y].getEndY(); ty++) {
                effectTile(tx, ty, type);
              }
            }
          }
        }
      }
    }
    Player[] players = Players.getInstance().getPlayers();
    for (Player lPlayer : players) {
      if ((lPlayer.getDeity() == null) || (lPlayer.getDeity().getTemplateDeity() != deity.getTemplateDeity()))
      {
        lPlayer.getCommunicator().sendAlertServerMessage("You get a sudden headache.", (byte)3);
        lPlayer.addWoundOfType(performer, (byte)9, 1, false, 1.0F, false, 1000.0D, 0.0F, 0.0F, false, true);
      }
    }
  }
  
  private void effectTile(int tx, int ty, byte type)
  {
    int tile = Server.surfaceMesh.getTile(tx, ty);
    type = Tiles.decodeType(tile);
    Tiles.Tile theTile = Tiles.getTile(type);
    byte data = Tiles.decodeData(tile);
    if ((type == Tiles.Tile.TILE_GRASS.id) || (type == Tiles.Tile.TILE_DIRT.id))
    {
      Server.setSurfaceTile(tx, ty, Tiles.decodeHeight(tile), Tiles.Tile.TILE_MYCELIUM.id, (byte)0);
      
      Players.getInstance().sendChangedTile(tx, ty, true, false);
    }
    else if (theTile.isNormalTree())
    {
      Server.setSurfaceTile(tx, ty, Tiles.decodeHeight(tile), theTile
        .getTreeType(data).asMyceliumTree(), data);
      Players.getInstance().sendChangedTile(tx, ty, true, false);
    }
    else if (theTile.isNormalBush())
    {
      Server.setSurfaceTile(tx, ty, Tiles.decodeHeight(tile), theTile
        .getBushType(data).asMyceliumBush(), data);
      Players.getInstance().sendChangedTile(tx, ty, true, false);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\RiteDeath.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */