package com.wurmonline.server.spells;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.zones.Zones;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SummonWorg
  extends KarmaSpell
  implements CreatureTemplateIds
{
  private static final Logger logger = Logger.getLogger(SummonWorg.class.getName());
  public static final int RANGE = 24;
  
  public SummonWorg()
  {
    super("Summon Worg", 629, 30, 500, 30, 1, 180000L);
  }
  
  boolean precondition(Skill castSkill, Creature performer, int tilex, int tiley, int layer)
  {
    if (performer.getFollowers().length > 0)
    {
      performer.getCommunicator().sendNormalServerMessage("You are too busy leading other creatures and can not focus on summoning.", (byte)3);
      
      return false;
    }
    if (layer < 0) {
      if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(tilex, tiley))))
      {
        performer.getCommunicator().sendNormalServerMessage("You can not summon there.", (byte)3);
        
        return false;
      }
    }
    try
    {
      if (Zones.calculateHeight((tilex << 2) + 2, (tiley << 2) + 2, performer.isOnSurface()) < 0.0F)
      {
        performer.getCommunicator().sendNormalServerMessage("You can not summon there.", (byte)3);
        return false;
      }
    }
    catch (Exception ex)
    {
      performer.getCommunicator().sendNormalServerMessage("You can not summon there.", (byte)3);
      return false;
    }
    return true;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset)
  {
    if (performer.knowsKarmaSpell(629)) {
      spawnCreature(86, performer);
    }
  }
  
  private final void spawnCreature(int templateId, Creature performer)
  {
    try
    {
      CreatureTemplate ct = CreatureTemplateFactory.getInstance().getTemplate(templateId);
      byte sex = 0;
      if (Server.rand.nextInt(2) == 0) {
        sex = 1;
      }
      byte ctype = (byte)Math.max(0, Server.rand.nextInt(22) - 10);
      if (Server.rand.nextInt(20) == 0) {
        ctype = 99;
      }
      Creature c = Creature.doNew(templateId, true, performer.getPosX() - 4.0F + Server.rand.nextFloat() * 9.0F, performer
        .getPosY() - 4.0F + Server.rand
        .nextFloat() * 9.0F, Server.rand
        .nextFloat() * 360.0F, performer.getLayer(), ct.getName(), sex, performer.getKingdomId(), ctype, true);
      if (performer.getPet() != null)
      {
        performer.getCommunicator().sendNormalServerMessage("You can't keep control over " + performer
          .getPet().getNameWithGenus() + " as well.", (byte)2);
        
        performer.getPet().setDominator(-10L);
      }
      c.setLoyalty(100.0F);
      c.setDominator(performer.getWurmId());
      performer.setPet(c.getWurmId());
      c.setLeader(performer);
    }
    catch (NoSuchCreatureTemplateException nst)
    {
      logger.log(Level.WARNING, nst.getMessage(), nst);
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, ex.getMessage(), ex);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\spells\SummonWorg.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */