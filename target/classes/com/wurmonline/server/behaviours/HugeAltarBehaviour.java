package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.combat.Weapon;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.endgames.EndGameItem;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.zones.NoSuchZoneException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class HugeAltarBehaviour
  extends DomainItemBehaviour
{
  private static final Logger logger = Logger.getLogger(HugeAltarBehaviour.class.getName());
  private static final float MAX_DAM = 99.9F;
  public static final int maxCharges = 30;
  
  HugeAltarBehaviour()
  {
    super((short)34);
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item target)
  {
    List<ActionEntry> toReturn = new LinkedList();
    toReturn.addAll(super.getBehavioursFor(performer, target));
    toReturn.addAll(getCommonBehaviours(performer, target));
    return toReturn;
  }
  
  private List<ActionEntry> getCommonBehaviours(Creature performer, Item target)
  {
    List<ActionEntry> toReturn = new LinkedList();
    if (EndGameItems.getEndGameItem(target) != null) {
      if (EndGameItems.getEndGameItem(target).isHoly())
      {
        int nums = -3;
        
        Deity rand = Deities.getRandomNonHateDeity();
        if (rand != null) {
          nums--;
        }
        toReturn.add(new ActionEntry((short)nums, "Inscriptions", "inscriptions"));
        toReturn.add(Actions.actionEntrys['Ù']);
        toReturn.add(Actions.actionEntrys['Ú']);
        toReturn.add(Actions.actionEntrys['Û']);
        if (rand != null) {
          toReturn.add(Actions.actionEntrys['Ǣ']);
        }
        if ((performer.getDeity() != null) && (!performer.getDeity().isHateGod()) && (performer.getFaith() == 30.0F) && 
          (!performer.isPriest())) {
          toReturn.add(Actions.actionEntrys['Ğ']);
        }
        if (!performer.isChampion()) {
          if (!Servers.localServer.isChallengeServer()) {
            if ((performer.getDeity() != null) && (!performer.getDeity().isHateGod()) && (performer.getFaith() >= 50.0F)) {
              if (performer.getDeity().isWarrior()) {
                try
                {
                  if (performer.getSkills().getSkill(105).getKnowledge(0.0D) > 25.0D) {
                    toReturn.add(Actions.actionEntrys['Ü']);
                  }
                }
                catch (NoSuchSkillException nss)
                {
                  logger.log(Level.WARNING, "Weird - " + performer.getName() + " has no soul strength.");
                  performer.getSkills().learn(105, 1.0F);
                }
              } else {
                try
                {
                  if (performer.getSkills().getSkill(106).getKnowledge(0.0D) > 25.0D) {
                    toReturn.add(Actions.actionEntrys['Ü']);
                  }
                }
                catch (NoSuchSkillException nss)
                {
                  logger.log(Level.WARNING, "Weird - " + performer.getName() + " has no soul depth.");
                  performer.getSkills().learn(106, 1.0F);
                }
              }
            }
          }
        }
      }
      else
      {
        int nums = -1;
        Deity rand = Deities.getRandomHateDeity();
        if (rand != null) {
          nums--;
        }
        toReturn.add(new ActionEntry((short)nums, "Inscriptions", "inscriptions"));
        toReturn.add(Actions.actionEntrys['Ù']);
        if (rand != null) {
          toReturn.add(Actions.actionEntrys['Ǣ']);
        }
        if ((performer.getDeity() != null) && (performer.getDeity().isHateGod()) && (performer.getFaith() == 30.0F) && 
          (!performer.isPriest())) {
          toReturn.add(Actions.actionEntrys['Ğ']);
        }
        if (!performer.isChampion()) {
          if (!Servers.localServer.isChallengeServer()) {
            if ((performer.getDeity() != null) && (performer.getDeity().isHateGod()) && (performer.getFaith() >= 50.0F)) {
              try
              {
                if (performer.getSkills().getSkill(105).getKnowledge(0.0D) > 25.0D) {
                  toReturn.add(Actions.actionEntrys['Ü']);
                }
              }
              catch (NoSuchSkillException nss)
              {
                logger.log(Level.WARNING, "Weird - " + performer.getName() + " has no soul strength.");
                performer.getSkills().learn(105, 1.0F);
              }
            }
          }
        }
      }
    }
    return toReturn;
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item target)
  {
    List<ActionEntry> toReturn = super.getBehavioursFor(performer, source, target);
    toReturn.addAll(getCommonBehaviours(performer, target));
    if (EndGameItems.getEndGameItem(target) != null) {
      toReturn.add(Actions.actionEntrys['Ý']);
    }
    if (source.isArtifact()) {
      toReturn.add(Actions.actionEntrys['Ų']);
    }
    return toReturn;
  }
  
  public boolean action(Action act, Creature performer, Item target, short action, float counter)
  {
    boolean done = true;
    boolean reachable = true;
    if (target.getOwnerId() == -10L)
    {
      reachable = false;
      if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 4.0F)) {
        reachable = true;
      }
    }
    if (reachable)
    {
      if (EndGameItems.getEndGameItem(target) != null)
      {
        if (EndGameItems.getEndGameItem(target).isHoly())
        {
          if (action == 217)
          {
            Methods.sendAltarConversion(performer, target, Deities.getDeity(1));
          }
          else if (action == 218)
          {
            Methods.sendAltarConversion(performer, target, Deities.getDeity(2));
          }
          else if (action == 219)
          {
            Methods.sendAltarConversion(performer, target, Deities.getDeity(3));
          }
          else if (action == 482)
          {
            Deity rand = Deities.getRandomNonHateDeity();
            if (rand != null) {
              Methods.sendAltarConversion(performer, target, rand);
            }
          }
        }
        else if ((action == 217) || (action == 482)) {
          if (action == 217)
          {
            Methods.sendAltarConversion(performer, target, Deities.getDeity(4));
          }
          else
          {
            Deity rand = Deities.getRandomHateDeity();
            if (rand != null) {
              Methods.sendAltarConversion(performer, target, rand);
            }
          }
        }
        if (action == 220)
        {
          if (Servers.localServer.isChallengeServer()) {
            return true;
          }
          if (performer.isChampion())
          {
            performer.getCommunicator().sendNormalServerMessage("You are already the Champion of a deity.");
            return true;
          }
          if (System.currentTimeMillis() - performer.getChampTimeStamp() < 14515200000L)
          {
            String timefor = Server.getTimeFor(14515200000L + performer.getChampTimeStamp() - 
              System.currentTimeMillis());
            performer.getCommunicator()
              .sendNormalServerMessage("You may become champion again in " + timefor + ".");
            return true;
          }
          if (EndGameItems.getEndGameItem(target) != null) {
            if (EndGameItems.getEndGameItem(target).isHoly())
            {
              if ((performer.getDeity() != null) && (!performer.getDeity().isHateGod()) && 
                (performer.getFaith() >= 50.0F))
              {
                if (performer.isKing())
                {
                  performer.getCommunicator().sendNormalServerMessage(performer
                    .getDeity().getName() + " will not let a ruler such as yourself risk " + performer
                    .getHisHerItsString() + " life as a champion.");
                  return true;
                }
                if (performer.getDeity().isWarrior()) {
                  try
                  {
                    if (performer.getSkills().getSkill(105).getKnowledge(0.0D) > 25.0D) {
                      Methods.sendRealDeathQuestion(performer, target, performer.getDeity());
                    }
                  }
                  catch (NoSuchSkillException nss)
                  {
                    logger.log(Level.WARNING, "Weird - " + performer.getName() + " has no soul strength.");
                    performer.getSkills().learn(105, 1.0F);
                  }
                } else {
                  try
                  {
                    if (performer.getSkills().getSkill(106).getKnowledge(0.0D) > 25.0D) {
                      Methods.sendRealDeathQuestion(performer, target, performer.getDeity());
                    }
                  }
                  catch (NoSuchSkillException nss)
                  {
                    logger.log(Level.WARNING, "Weird - " + performer.getName() + " has no soul depth.");
                    performer.getSkills().learn(106, 1.0F);
                  }
                }
              }
            }
            else if ((performer.getDeity() != null) && (performer.getDeity().isHateGod()) && (performer.getFaith() >= 50.0F))
            {
              if (performer.isKing())
              {
                performer.getCommunicator().sendNormalServerMessage(performer
                  .getDeity().getName() + " will not let a ruler such as yourself risk " + performer
                  .getHisHerItsString() + " life as a champion.");
                return true;
              }
              try
              {
                if (performer.getSkills().getSkill(105).getKnowledge(0.0D) > 25.0D) {
                  Methods.sendRealDeathQuestion(performer, target, performer.getDeity());
                }
              }
              catch (NoSuchSkillException nss)
              {
                logger.log(Level.WARNING, "Weird - " + performer.getName() + " has no soul strength.");
                performer.getSkills().learn(106, 1.0F);
              }
            }
          }
        }
        else if (action == 286)
        {
          if (EndGameItems.getEndGameItem(target) != null) {
            if (EndGameItems.getEndGameItem(target).isHoly())
            {
              if ((performer.getDeity() != null) && (!performer.getDeity().isHateGod()) && 
                (performer.getFaith() == 30.0F)) {
                MethodsCreatures.sendAskPriestQuestion(performer, target, null);
              }
            }
            else if ((performer.getDeity() != null) && (performer.getDeity().isHateGod()) && (performer.getFaith() == 30.0F)) {
              MethodsCreatures.sendAskPriestQuestion(performer, target, null);
            }
          }
        }
        else
        {
          done = super.action(act, performer, target, action, counter);
        }
      }
      else if ((action == 7) && (Constants.loadEndGameItems) && (performer.getPower() >= 5))
      {
        boolean holy = target.getRealTemplateId() == 327;
        if (((holy) && (EndGameItems.getGoodAltar() != null)) || ((!holy) && (EndGameItems.getEvilAltar() != null)))
        {
          performer.getCommunicator().sendNormalServerMessage("You must remove the current " + target.getActualName() + " before placing a new one.");
          return done;
        }
        try
        {
          target.putItemInfrontof(performer);
          
          EndGameItem eg = new EndGameItem(target, holy, (short)68, true);
          EndGameItems.altars.put(new Long(eg.getWurmid()), eg);
          
          target.bless(holy ? 1 : 4);
          target.enchant((byte)(holy ? 5 : 8));
          
          short eff = holy ? 2 : 3;
          for (Player lPlayer : Players.getInstance().getPlayers()) {
            lPlayer.getCommunicator().sendAddEffect(eg.getWurmid(), eff, eg
              .getItem().getPosX(), eg.getItem().getPosY(), eg.getItem().getPosZ(), (byte)0);
          }
          logger.log(Level.INFO, "Created " + (holy ? "holy" : "unholy") + " altar at " + target.getPosX() + ", " + target.getPosY() + ".");
          performer.getCommunicator().sendNormalServerMessage("You successfully place " + target.getNameWithGenus() + " into the world.");
        }
        catch (NoSuchItemException nsi)
        {
          logger.log(Level.WARNING, nsi.getMessage(), nsi);
        }
        catch (NoSuchCreatureException nsc)
        {
          logger.log(Level.WARNING, "Failed to locate creature " + performer.getWurmId(), nsc);
        }
        catch (NoSuchPlayerException nsp)
        {
          logger.log(Level.WARNING, "Failed to locate player " + performer.getWurmId(), nsp);
        }
        catch (NoSuchZoneException nsz)
        {
          Items.destroyItem(target.getWurmId());
        }
      }
      else
      {
        done = super.action(act, performer, target, action, counter);
      }
    }
    else {
      done = super.action(act, performer, target, action, counter);
    }
    return done;
  }
  
  public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter)
  {
    boolean done = false;
    boolean reachable = true;
    if (target.getOwnerId() == -10L)
    {
      reachable = false;
      if (performer.isWithinDistanceTo(target.getPosX(), target.getPosY(), target.getPosZ(), 4.0F)) {
        reachable = true;
      }
    }
    if (reachable)
    {
      if ((action == 217) || (action == 218) || (action == 219) || (action == 482) || (action == 220))
      {
        done = action(act, performer, target, action, counter);
      }
      else if (action == 221)
      {
        if (source.getOwnerId() != performer.getWurmId()) {
          return true;
        }
        if (EndGameItems.getEndGameItem(target) != null) {
          done = destroy(act, performer, source, target, counter);
        } else {
          done = true;
        }
      }
      else if (action == 370)
      {
        if (source.getOwnerId() != performer.getWurmId()) {
          return true;
        }
        if (source.isArtifact())
        {
          if (!Deities.mayDestroyAltars())
          {
            performer.getCommunicator().sendNormalServerMessage("The " + target
            
              .getName() + " is vigilantly protected from Valrei. The position of the moons make it possible to recharge the " + source
              
              .getName() + " on Wrath day and the day of Awakening in the first and third week of a Starfall.");
            
            return true;
          }
          if (EndGameItems.getEndGameItem(target) != null)
          {
            if (EndGameItems.getEndGameItem(target).isHoly())
            {
              if ((EndGameItems.getEndGameItem(source) == null) || 
                (!EndGameItems.getEndGameItem(source).isHoly()))
              {
                performer.getCommunicator().sendNormalServerMessage("Nothing happens. Maybe you need to try at the other altar?");
                
                return true;
              }
              if (counter == 1.0F)
              {
                if (!EndGameItems.mayRechargeItem())
                {
                  performer.getCommunicator().sendNormalServerMessage("Nothing happens. You have to wait a minute between recharge attempts.");
                  
                  return true;
                }
                Server.getInstance().broadCastNormal("The sky darkens as someone starts to recharge the " + source
                  .getName() + " at the " + target
                  .getName() + ".");
                Players.getInstance().sendGlobalNonPersistantEffect(-10L, (short)16, target
                
                  .getTileX(), target
                  .getTileY(), 
                  Tiles.decodeHeightAsFloat(Server.surfaceMesh.getTile(target.getTileX(), target
                  .getTileY())));
                EndGameItems.touchRecharge();
              }
            }
          }
          else {
            return true;
          }
          if (EndGameItems.getEndGameItem(target) != null)
          {
            if (!EndGameItems.getEndGameItem(target).isHoly())
            {
              if ((EndGameItems.getEndGameItem(source) == null) || 
                (EndGameItems.getEndGameItem(source).isHoly()))
              {
                performer.getCommunicator().sendNormalServerMessage("Nothing happens. Maybe you need to try at the other altar?");
                
                return true;
              }
              if (counter == 1.0F)
              {
                if (!EndGameItems.mayRechargeItem())
                {
                  performer.getCommunicator().sendNormalServerMessage("Nothing happens. You have to wait a minute between recharge attempts.");
                  
                  return true;
                }
                Players.getInstance().sendGlobalNonPersistantEffect(-10L, (short)17, target
                
                  .getTileX(), target
                  .getTileY(), 
                  Tiles.decodeHeightAsFloat(Server.surfaceMesh.getTile(target.getTileX(), target
                  .getTileY())));
                Server.getInstance().broadCastNormal("The sky gets brighter as someone starts to recharge the " + source
                  .getName() + " at the " + target
                  .getName() + ".");
                EndGameItems.touchRecharge();
              }
            }
          }
          else {
            return true;
          }
          done = false;
          if (counter == 1.0F)
          {
            performer.getCommunicator().sendNormalServerMessage("You start to recharge the " + source
              .getName() + ".");
            Server.getInstance().broadCastAction(performer
              .getName() + " starts to recharge the " + source.getName() + ".", performer, 5);
            
            performer.sendActionControl(Actions.actionEntrys['Ų'].getVerbString(), true, 2400);
            
            performer.getStatus().modifyStamina(-500.0F);
          }
          else if (act.currentSecond() % 10 == 0)
          {
            performer.getCommunicator().sendNormalServerMessage("The " + source.getName() + " vibrates faintly.");
            source.setAuxData((byte)Math.min(30, source.getAuxData() + 1));
            EndGameItems.touchRecharge();
          }
          else if ((counter >= 300.0F) || (source.getAuxData() == 30))
          {
            performer.getCommunicator().sendNormalServerMessage("The " + source
              .getName() + " is now fully charged.");
            source.setAuxData((byte)30);
            done = true;
          }
        }
      }
      else
      {
        done = super.action(act, performer, source, target, action, counter);
      }
    }
    else {
      done = super.action(act, performer, source, target, action, counter);
    }
    return done;
  }
  
  private static boolean destroy(Action act, Creature performer, Item source, Item altar, float counter)
  {
    boolean done = false;
    if (Deities.mayDestroyAltars())
    {
      float dam = Weapon.getBaseDamageForWeapon(source);
      if (dam <= 0.0F)
      {
        performer.getCommunicator().sendNormalServerMessage("That wouldn't leave a dent in the " + altar
          .getName() + ".");
        return true;
      }
      if (counter == 1.0F)
      {
        performer.getCommunicator().sendNormalServerMessage("You start to destroy the " + altar.getName() + ".");
        Server.getInstance().broadCastAction(performer.getName() + " starts to destroy the " + altar.getName() + ".", performer, 5);
        
        performer.sendActionControl(Actions.actionEntrys['Ý'].getVerbString(), true, 1000);
        
        performer.getStatus().modifyStamina(-500.0F);
      }
      else if (act.currentSecond() % 10 == 0)
      {
        Skills skills = performer.getSkills();
        Skill weapon = null;
        Skill soul = null;
        try
        {
          weapon = skills.getSkill(source.getPrimarySkill());
        }
        catch (NoSuchSkillException nss)
        {
          try
          {
            weapon = skills.learn(source.getPrimarySkill(), 1.0F);
          }
          catch (NoSuchSkillException ns2)
          {
            logger.log(Level.WARNING, performer.getName() + " using " + source.getName() + " which has no primary skill!");
          }
        }
        try
        {
          soul = skills.getSkill(105);
        }
        catch (NoSuchSkillException nss)
        {
          soul = skills.learn(105, 1.0F);
        }
        if ((weapon != null) && 
          (weapon.skillCheck(80.0D, soul.getKnowledge(0.0D), true, act.currentSecond()) > 0.0D)) {
          if (altar.getDamage() >= 99.9F)
          {
            performer.getCommunicator().sendSafeServerMessage("You destroy the " + altar.getName() + "!");
            EndGameItems.destroyHugeAltar(altar, performer);
            done = true;
          }
          else
          {
            if ((performer.getPower() > 0) && (Servers.isThisATestServer())) {
              altar.setDamage(Math.min(99.9F, altar.getDamage() + 5.0F));
            } else if (source.isDestroyHugeAltar()) {
              altar.setDamage(Math.min(99.9F, altar.getDamage() + 0.1F));
            } else {
              altar.setDamage(Math.min(99.9F, altar
              
                .getDamage() + 
                (float)Weapon.getModifiedDamageForWeapon(source, performer.getBodyStrength()) / 200000.0F));
            }
            performer.getCommunicator().sendNormalServerMessage("Using your skills and drawing from the strength of your soul, you manage to dent the huge altar a little!");
          }
        }
        performer.getStatus().modifyStamina(-500.0F);
      }
      else if (act.currentSecond() > 100)
      {
        done = true;
        if (altar.getDamage() >= 99.9F)
        {
          performer.getCommunicator().sendSafeServerMessage("You destroy the " + altar.getName() + "!");
          EndGameItems.destroyHugeAltar(altar, performer);
        }
        else
        {
          if ((performer.getPower() > 0) && (Servers.isThisATestServer())) {
            altar.setDamage(Math.min(99.9F, altar.getDamage() + 10.0F));
          } else if (source.isDestroyHugeAltar()) {
            altar.setDamage(Math.min(99.9F, altar.getDamage() + 0.3F));
          } else {
            altar.setDamage(Math.min(99.9F, altar
            
              .getDamage() + 
              (float)Weapon.getModifiedDamageForWeapon(source, performer.getBodyStrength()) / 100000.0F));
          }
          performer.getCommunicator().sendNormalServerMessage("You manage to damage the altar a bit.");
        }
      }
    }
    else
    {
      performer.getCommunicator().sendNormalServerMessage("The " + altar
        .getName() + " is vigilantly protected from Valrei. The position of the moons make the " + altar
        .getName() + " vulnerable on Wrath day and the day of Awakening in the first and third week of a Starfall.");
      
      done = true;
    }
    return done;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\behaviours\HugeAltarBehaviour.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */