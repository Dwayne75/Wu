package com.wurmonline.server.behaviours;

import com.wurmonline.server.Constants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.SpellEffectsEnum;
import com.wurmonline.server.creatures.VisionArea;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.RuneUtilities;
import com.wurmonline.server.items.RuneUtilities.ModifierEffect;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.KarmaQuestion;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.Spells;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.util.StringUtilities;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public final class BodyPartBehaviour
  extends ItemBehaviour
{
  private static final Logger logger = Logger.getLogger(BodyPartBehaviour.class.getName());
  
  BodyPartBehaviour()
  {
    super((short)10);
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item object)
  {
    List<ActionEntry> toReturn = new LinkedList();
    if (object.isBodyPartRemoved())
    {
      toReturn.addAll(super.getBehavioursFor(performer, object));
    }
    else if (object.isBodyPartAttached())
    {
      if ((performer.getCultist() != null) && (performer.getCultist().mayRefresh())) {
        toReturn.add(new ActionEntry((short)91, "Refresh", "refreshing", emptyIntArr));
      }
      if ((performer.getCultist() != null) && (performer.getCultist().mayStartLoveEffect())) {
        toReturn.add(Actions.actionEntrys['ƅ']);
      }
      if ((performer.getCultist() != null) && (performer.getCultist().mayStartDoubleWarDamage())) {
        toReturn.add(Actions.actionEntrys['Ɔ']);
      }
      if ((performer.getCultist() != null) && (performer.getCultist().mayStartDoubleStructDamage())) {
        toReturn.add(Actions.actionEntrys['Ƈ']);
      }
      if ((performer.getCultist() != null) && (performer.getCultist().mayStartFearEffect())) {
        toReturn.add(Actions.actionEntrys['ƈ']);
      }
      if ((performer.getCultist() != null) && (performer.getCultist().mayStartNoElementalDamage())) {
        toReturn.add(Actions.actionEntrys['Ɖ']);
      }
      if ((performer.getCultist() != null) && (performer.getCultist().mayStartIgnoreTraps())) {
        toReturn.add(Actions.actionEntrys['Ɗ']);
      }
      if ((performer.getCultist() != null) && (performer.getCultist().mayFillup())) {
        toReturn.add(Actions.actionEntrys['ƌ']);
      }
      if ((performer.getCultist() != null) && (performer.getCultist().mayTeleport()))
      {
        toReturn.add(new ActionEntry((short)-1, "Random Teleport", "Teleporting", new int[0]));
        toReturn.add(new ActionEntry((short)95, "Confirm", "Teleporting", new int[0]));
      }
      if ((performer.getCultist() != null) && (performer.getCultist().mayRecall())) {
        toReturn.add(Actions.actionEntrys['ǩ']);
      }
      if (Constants.isEigcEnabled) {
        toReturn.add(Actions.actionEntrys['Ǯ']);
      }
      if (performer.mayUseLastGasp()) {
        toReturn.add(Actions.actionEntrys['ǯ']);
      }
      if (performer.getKarma() > 0) {
        toReturn.add(Actions.actionEntrys['ǿ']);
      }
      toReturn.add(new ActionEntry((short)585, "Unequip all", "unequipping", emptyIntArr));
      
      toReturn.addAll(ManageMenu.getBehavioursFor(performer));
      if ((performer.getPower() <= 0) && (Kingdoms.isCustomKingdom(performer.getKingdomId()))) {
        toReturn.add(Actions.actionEntrys[89]);
      }
      addEmotes(toReturn);
    }
    return toReturn;
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item object)
  {
    List<ActionEntry> toReturn = new LinkedList();
    toReturn.addAll(getBehavioursFor(performer, object));
    if ((source.isHolyItem()) && (object.isBodyPartAttached()))
    {
      if (source.isHolyItem(performer.getDeity())) {
        if ((performer.isPriest()) || (performer.getPower() > 0))
        {
          float faith = performer.getFaith();
          Spell[] spells = performer.getDeity().getSpellsTargettingCreatures((int)faith);
          if (spells.length > 0)
          {
            toReturn.add(new ActionEntry((short)-spells.length, "Spells", "spells"));
            for (Spell lSpell : spells) {
              toReturn.add(Actions.actionEntrys[lSpell.number]);
            }
          }
        }
      }
    }
    else if ((object.isBodyPartAttached()) && ((source.isMagicStaff()) || (
      (source.getTemplateId() == 176) && (performer.getPower() >= 4) && (Servers.isThisATestServer()))))
    {
      List<ActionEntry> slist = new LinkedList();
      if (performer.knowsKarmaSpell(547)) {
        slist.add(Actions.actionEntrys['ȣ']);
      }
      if (performer.knowsKarmaSpell(554)) {
        slist.add(Actions.actionEntrys['Ȫ']);
      }
      if (performer.knowsKarmaSpell(555)) {
        slist.add(Actions.actionEntrys['ȫ']);
      }
      if (performer.knowsKarmaSpell(560)) {
        slist.add(Actions.actionEntrys['Ȱ']);
      }
      if (object.getOwnerId() == performer.getWurmId())
      {
        if (performer.knowsKarmaSpell(548)) {
          slist.add(Actions.actionEntrys['Ȥ']);
        }
        if (performer.knowsKarmaSpell(552)) {
          slist.add(Actions.actionEntrys['Ȩ']);
        }
        if (performer.knowsKarmaSpell(553)) {
          slist.add(Actions.actionEntrys['ȩ']);
        }
        if (performer.knowsKarmaSpell(562)) {
          slist.add(Actions.actionEntrys['Ȳ']);
        }
      }
      if (performer.getPower() >= 4) {
        toReturn.add(new ActionEntry((short)-slist.size(), "Sorcery", "casting"));
      }
      toReturn.addAll(slist);
    }
    if ((object.isBodyPartAttached()) && (source.getTemplate().isRune()))
    {
      Skill soulDepth = performer.getSoulDepth();
      double diff = 20.0F + source.getDamage() - (source.getCurrentQualityLevel() + source.getRarity() - 45.0D);
      double chance = soulDepth.getChance(diff, null, source.getCurrentQualityLevel());
      if ((RuneUtilities.isSingleUseRune(source)) && (((RuneUtilities.getSpellForRune(source) != null) && 
        (RuneUtilities.getSpellForRune(source).isTargetCreature())) || 
        (RuneUtilities.getModifier(RuneUtilities.getEnchantForRune(source), RuneUtilities.ModifierEffect.SINGLE_REFRESH) > 0.0F))) {
        toReturn.add(new ActionEntry((short)945, "Use Rune: " + chance + "%", "using rune", emptyIntArr));
      }
    }
    if ((object.isBodyPartAttached()) && (source.canHaveInscription())) {
      toReturn.addAll(PapyrusBehaviour.getPapyrusBehavioursFor(performer, source));
    }
    if ((performer.getPower() >= 4) && (object.getOwnerId() == performer.getWurmId())) {
      toReturn.add(Actions.actionEntrys['ˑ']);
    }
    return toReturn;
  }
  
  public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter)
  {
    if (target.isBodyPartRemoved()) {
      return super.action(act, performer, source, target, action, counter);
    }
    if (action == 95)
    {
      if ((performer.getCultist() != null) && (performer.getCultist().mayTeleport())) {
        return action(act, performer, target, action, counter);
      }
    }
    else
    {
      if (action == 489) {
        return action(act, performer, target, action, counter);
      }
      if (action == 721)
      {
        if ((performer.getPower() >= 4) && (target.getOwnerId() == performer.getWurmId())) {
          Methods.sendGmSetMedpathQuestion(performer, performer);
        }
        return true;
      }
    }
    if (action == 945) {
      if (source.getTemplate().isRune()) {
        if ((RuneUtilities.isSingleUseRune(source)) && (((RuneUtilities.getSpellForRune(source) != null) && 
          (RuneUtilities.getSpellForRune(source).isTargetCreature())) || 
          (RuneUtilities.getModifier(RuneUtilities.getEnchantForRune(source), RuneUtilities.ModifierEffect.SINGLE_REFRESH) > 0.0F))) {
          return CreatureBehaviour.useRuneOnCreature(act, performer, source, performer, action, counter);
        }
      }
    }
    if (((action == 6) || (action == 7)) && (target.isBodyPartAttached())) {
      return true;
    }
    if ((act.isSpell()) && (target.isBodyPartAttached()))
    {
      boolean done = true;
      Spell spell = Spells.getSpell(action);
      if (spell != null) {
        try
        {
          Creature targ = Server.getInstance().getCreature(target.getOwnerId());
          if (spell.offensive) {
            if (!targ.equals(performer))
            {
              if (!targ.isInvulnerable())
              {
                if (performer.mayAttack(targ))
                {
                  if ((performer.opponent != null) || (targ.isAggHuman())) {
                    if (performer.opponent == null) {
                      performer.setOpponent(targ);
                    }
                  }
                  if (targ.opponent == null)
                  {
                    targ.setOpponent(performer);
                    targ.setTarget(performer.getWurmId(), false);
                    targ.getCommunicator().sendNormalServerMessage(performer
                      .getNameWithGenus() + " is attacking you with a spell!");
                  }
                }
                else
                {
                  if ((!performer.isStunned()) && (!performer.isUnconscious()))
                  {
                    performer.getCommunicator().sendNormalServerMessage("You are too weak to attack.");
                    return true;
                  }
                  return true;
                }
              }
              else
              {
                performer.getCommunicator().sendNormalServerMessage(target
                  .getName() + " seems to absorb the spell with no effect.");
                return true;
              }
            }
            else
            {
              performer.getCommunicator().sendNormalServerMessage("You can not cast that upon yourself.");
              return true;
            }
          }
          if (spell.religious)
          {
            if (performer.getDeity() != null)
            {
              if (Methods.isActionAllowed(performer, (short)245)) {
                done = Methods.castSpell(performer, spell, targ, counter);
              }
            }
            else
            {
              performer.getCommunicator().sendNormalServerMessage("You have no deity and cannot cast the spell.");
              done = true;
            }
          }
          else if ((source.isMagicStaff()) || (
            (source.getTemplateId() == 176) && (performer.getPower() >= 2) && (Servers.isThisATestServer())))
          {
            if (Methods.isActionAllowed(performer, (short)547)) {
              done = Methods.castSpell(performer, spell, targ, counter);
            }
          }
          else
          {
            performer.getCommunicator().sendNormalServerMessage("You need to use a magic staff.");
            done = true;
          }
        }
        catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
      }
      return done;
    }
    if ((action == 744) && (source.canHaveInscription())) {
      return PapyrusBehaviour.addToCookbook(act, performer, source, target, action, counter);
    }
    if ((action == 1) || (action == 494) || (action == 495) || (action == 511) || (action == 566)) {
      return action(act, performer, target, action, counter);
    }
    return super.action(act, performer, source, target, action, counter);
  }
  
  public boolean action(Action act, Creature performer, Item target, short action, float counter)
  {
    boolean toReturn = true;
    if (target.isBodyPartRemoved()) {
      return super.action(act, performer, target, action, counter);
    }
    Player player;
    if (action == 1)
    {
      performer.getCommunicator().sendNormalServerMessage(target.examine(performer));
      if (target.isBodyPartAttached())
      {
        Creature owner = performer;
        try
        {
          owner = Server.getInstance().getCreature(target.getOwnerId());
          if (owner.equals(performer))
          {
            String kingdom = "a Jenn-Kellon.";
            if (owner.getKingdomId() == 0) {
              kingdom = "not allied to anyone.";
            } else if (owner.getKingdomId() == 2) {
              kingdom = "a Mol Rehan.";
            } else if (owner.getKingdomId() == 3) {
              kingdom = "with the Horde of the Summoned.";
            } else if (owner.getKingdomId() == 4) {
              kingdom = "from the Freedom Isles.";
            } else if (owner.getKingdomId() != 0) {
              kingdom = "a " + Kingdoms.getNameFor(owner.getKingdomId());
            }
            if (owner.isKing())
            {
              King k = King.getKing(owner.getKingdomId());
              if (k != null) {
                performer.getCommunicator().sendNormalServerMessage("You are " + k.getFullTitle() + ".");
              }
            }
            performer.getCommunicator().sendNormalServerMessage("You are " + kingdom);
            
            String appointments = owner.getAppointmentTitles();
            if (appointments.length() > 0) {
              performer.getCommunicator().sendNormalServerMessage(appointments);
            }
            if (performer.getPower() > 0)
            {
              performer.getCommunicator().sendNormalServerMessage("Reputation: " + owner
                .getReputation() + " Alignment: " + owner.getAlignment());
              if (performer.getPower() >= 5) {
                performer.getCommunicator().sendNormalServerMessage("Mount speed mod=" + performer
                  .getMovementScheme().getMountSpeed());
              }
            }
            performer.getCommunicator().sendNormalServerMessage(
              StringUtilities.raiseFirstLetter(owner.getStatus().getBodyType()));
            if (owner.isPlayer()) {
              performer.getCommunicator().sendNormalServerMessage("Rank=" + ((Player)owner)
                .getRank() + ", Max Rank=" + ((Player)owner).getMaxRank());
            }
            if ((Servers.isThisATestServer()) && (performer.isPlayer()))
            {
              player = (Player)performer;
              performer.getCommunicator().sendNormalServerMessage(String.format("Your current alcohol level is %.1f.", new Object[] {
                Float.valueOf(player.getAlcohol()) }));
            }
          }
        }
        catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
        if (performer.getPower() >= 3)
        {
          performer.getCommunicator().sendNormalServerMessage("Position x=" + owner
            .getPosX() + "(" + owner.getTileX() + "), y=" + owner.getPosY() + "(" + owner
            .getTileY() + "), z=" + owner.getPositionZ() + "(+" + owner.getAltOffZ() + "), floorlevel=" + owner
            .getFloorLevel());
          if (performer.getCurrentTile() != null) {
            performer.getCommunicator().sendNormalServerMessage("Tile position x=" + 
              owner.getCurrentTile().tilex + ", " + owner.getCurrentTile().tiley);
          } else {
            performer.getCommunicator().sendNormalServerMessage(owner.getName() + " has no current tile.");
          }
          performer.getCommunicator().sendNormalServerMessage("Layer=" + owner
            .getLayer() + ", Teleporting=" + owner.isTeleporting() + ", vehicle=" + owner
            .getVehicle() + " Origin server " + WurmId.getOrigin(performer.getWurmId()));
          if (owner.getVisionArea() != null) {
            performer.getCommunicator().sendNormalServerMessage("Visionarea initialized=" + owner
              .getVisionArea().isInitialized() + ". Surface startx=" + owner
              .getVisionArea().getSurface().getStartX() + ", startY=" + owner
              .getVisionArea().getSurface().getStartY() + ", endX=" + owner
              .getVisionArea().getSurface().getEndX() + ", endY=" + owner
              .getVisionArea().getSurface().getEndY());
          } else {
            performer.getCommunicator().sendNormalServerMessage("Visionarea not initialized");
          }
        }
      }
    }
    else if (action == 5)
    {
      Server.getInstance().broadCastNormal(performer.getName() + " waves his " + target.getName() + " frantically.");
    }
    else if (action == 389)
    {
      if ((performer.getCultist() != null) && (performer.getCultist().mayStartLoveEffect()))
      {
        performer.getCommunicator().sendNormalServerMessage("You feel the love stream towards you.");
        performer.getCultist().touchCooldown3();
        if (performer.isFighting()) {
          if ((performer.opponent != null) && (performer.opponent.opponent == performer)) {
            performer.opponent.setTarget(-10L, true);
          }
        }
        performer.setTarget(-10L, true);
        
        performer.refreshAttitudes();
        performer.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.LOVE_EFFECT, performer
        
          .getCultist().getLoveEffectTimeLeftSeconds(), 100.0F);
      }
    }
    else if (action == 91)
    {
      if ((performer.getCultist() != null) && (performer.getCultist().mayRefresh()))
      {
        float nut = Math.min(50 + (performer.getCultist().getLevel() - 4) * 5, 99) / 100.0F;
        performer.getStatus().refresh(nut, true);
        performer.getCommunicator().sendNormalServerMessage("You send yourself a warm thought.");
        if (performer.getCultist() != null) {
          performer.getCultist().touchCooldown1();
        }
      }
    }
    else if (action == 390)
    {
      if ((performer.getCultist() != null) && (performer.getCultist().mayStartDoubleWarDamage()))
      {
        performer.getCommunicator().sendNormalServerMessage("You feel the rage pulsate within.");
        performer.getCultist().touchCooldown1();
        performer.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.HATE_DOUBLE_WAR, performer
        
          .getCultist().getDoubleWarDamageTimeLeftSeconds(), 100.0F);
      }
    }
    else if (action == 391)
    {
      if ((performer.getCultist() != null) && (performer.getCultist().mayStartDoubleStructDamage()))
      {
        performer.getCommunicator().sendNormalServerMessage("You feel the rage pulsate within.");
        performer.getCultist().touchCooldown2();
        performer.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.HATE_DOUBLE_STRUCT, performer
        
          .getCultist().getDoubleStructDamageTimeLeftSeconds(), 100.0F);
      }
    }
    else if (action == 392)
    {
      if ((performer.getCultist() != null) && (performer.getCultist().mayStartFearEffect()))
      {
        performer.getCommunicator().sendNormalServerMessage("Everything will fear you now, and rightly so.");
        performer.getCultist().touchCooldown3();
        performer.refreshAttitudes();
        performer.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.HATE_FEAR_EFFECT, performer
        
          .getCultist().getFearEffectTimeLeftSeconds(), 100.0F);
      }
    }
    else if (action == 393)
    {
      if ((performer.getCultist() != null) && (performer.getCultist().mayStartNoElementalDamage()))
      {
        performer.getCommunicator().sendNormalServerMessage("You align yourself with the elements.");
        performer.getCultist().touchCooldown1();
        performer.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.POWER_NO_ELEMENTAL, performer
        
          .getCultist().getElementalImmunityTimeLeftSeconds(), 100.0F);
      }
    }
    else if (action == 394)
    {
      if ((performer.getCultist() != null) && (performer.getCultist().mayStartIgnoreTraps()))
      {
        performer.getCommunicator().sendNormalServerMessage("You alert yourself to traps.");
        performer.getCultist().touchCooldown3();
        performer.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.POWER_IGNORE_TRAPS, performer
        
          .getCultist().getIgnoreTrapsTimeLeftSeconds(), 100.0F);
      }
    }
    else if (action == 396)
    {
      if ((performer.getCultist() != null) && (performer.getCultist().mayFillup()))
      {
        performer.getCommunicator().sendNormalServerMessage("You relax and forget about your hunger.");
        performer.getCultist().touchCooldown2();
        
        performer.getStatus().modifyHunger(-60000, 0.98F);
      }
    }
    else
    {
      int tiley;
      Item[] inventoryItems;
      Item[] bodyItems;
      if (action == 95)
      {
        if ((performer.getCultist() != null) && (performer.getCultist().mayTeleport()))
        {
          if (performer.getEnemyPresense() > 0)
          {
            performer.getCommunicator().sendNormalServerMessage("There is a blocking enemy presence nearby. Nothing happens.");
            
            return true;
          }
          int tilex = Zones.safeTileX(Server.rand.nextInt(Zones.worldTileSizeX));
          tiley = Zones.safeTileY(Server.rand.nextInt(Zones.worldTileSizeY));
          if (!MethodsCreatures.mayTelePortToTile(performer, tilex, tiley, true))
          {
            performer.getCommunicator().sendNormalServerMessage("Something blocked your attempt. Nothing happens.");
            return true;
          }
          if (performer.getPower() <= 0)
          {
            inventoryItems = performer.getInventory().getAllItems(true);
            player = inventoryItems;int i = player.length;
            for (Item localItem1 = 0; localItem1 < i; localItem1++)
            {
              lInventoryItem = player[localItem1];
              if (lInventoryItem.isArtifact())
              {
                performer.getCommunicator().sendNormalServerMessage("The " + lInventoryItem
                  .getName() + " softly hums. Nothing happens.");
                return true;
              }
            }
            bodyItems = performer.getBody().getBodyItem().getAllItems(true);
            Item[] arrayOfItem1 = bodyItems;localItem1 = arrayOfItem1.length;
            for (Item lInventoryItem = 0; lInventoryItem < localItem1; lInventoryItem++)
            {
              Item lInventoryItem = arrayOfItem1[lInventoryItem];
              if (lInventoryItem.isArtifact())
              {
                performer.getCommunicator().sendNormalServerMessage("The " + lInventoryItem
                  .getName() + " softly hums. Nothing happens.");
                return true;
              }
            }
            if (performer.isInPvPZone())
            {
              performer.getCommunicator().sendNormalServerMessage("The magic of the pvp zone interferes. You can not teleport here.");
              
              return true;
            }
          }
          performer.setTeleportPoints((short)tilex, (short)tiley, 0, 0);
          Server.getInstance()
            .broadCastAction(performer
            .getName() + " shuts " + performer.getHisHerItsString() + " eyes, dreaming away.", performer, 5);
          if (performer.startTeleporting())
          {
            performer.getCommunicator().sendNormalServerMessage("You close your eyes and let your spirit fly.");
            
            performer.getCommunicator().sendTeleport(false);
            performer.getCultist().touchCooldown3();
          }
        }
        return true;
      }
      if (action == 489)
      {
        if ((performer.getCultist() != null) && (performer.getCultist().mayRecall()))
        {
          Item[] inventoryItems = performer.getInventory().getAllItems(true);
          tiley = inventoryItems;inventoryItems = tiley.length;
          Item lInventoryItem;
          for (bodyItems = 0; bodyItems < inventoryItems; bodyItems++)
          {
            lInventoryItem = tiley[bodyItems];
            if (lInventoryItem.isArtifact())
            {
              performer.getCommunicator().sendNormalServerMessage("The " + lInventoryItem
                .getName() + " hums and disturbs the weave. You can not teleport right now.");
              
              return true;
            }
          }
          Item[] bodyItems = performer.getBody().getBodyItem().getAllItems(true);
          inventoryItems = bodyItems;bodyItems = inventoryItems.length;
          for (Item[] arrayOfItem2 = 0; arrayOfItem2 < bodyItems; arrayOfItem2++)
          {
            Item lInventoryItem = inventoryItems[arrayOfItem2];
            if (lInventoryItem.isArtifact())
            {
              performer.getCommunicator().sendNormalServerMessage("The " + lInventoryItem
                .getName() + " hums and disturbs the weave. You can not teleport right now.");
              
              return true;
            }
          }
          if (performer.getCitizenVillage() != null)
          {
            if (performer.mayChangeVillageInMillis() > 0L)
            {
              performer.getCommunicator().sendNormalServerMessage("You are still too new to this village. Nothing happens.");
              
              return true;
            }
            if (performer.getEnemyPresense() > 0)
            {
              performer.getCommunicator().sendNormalServerMessage("There is a blocking enemy presence nearby. Nothing happens.");
              
              return true;
            }
            if (performer.isInPvPZone())
            {
              performer.getCommunicator().sendNormalServerMessage("The magic of the pvp zone interferes. You can not teleport here.");
              
              return true;
            }
            performer.setTeleportPoints((short)performer.getCitizenVillage().getTokenX(), 
              (short)performer.getCitizenVillage().getTokenY(), 0, 0);
            if (performer.startTeleporting())
            {
              performer.getCommunicator().sendNormalServerMessage("You close your eyes and let your spirit fly home.");
              Server.getInstance().broadCastAction(performer
                .getName() + " shuts " + performer.getHisHerItsString() + " eyes and suddenly disappears.", performer, 5);
              
              performer.getCommunicator().sendTeleport(false);
              performer.getCultist().touchCooldown4();
            }
          }
          else
          {
            performer.getCommunicator().sendNormalServerMessage("You need to be part of a settlement you can call home.");
          }
        }
        return true;
      }
      if (action == 494)
      {
        Methods.sendVoiceChatQuestion(performer);
      }
      else if (action == 495)
      {
        if (performer.mayUseLastGasp()) {
          performer.useLastGasp();
        }
      }
      else if (action == 511)
      {
        if (performer.getKarma() > 0)
        {
          KarmaQuestion kq = new KarmaQuestion(performer);
          kq.sendQuestion();
        }
        else
        {
          performer.getCommunicator().sendNormalServerMessage("You have no karma to use.");
        }
      }
      else
      {
        if (ManageMenu.isManageAction(performer, action)) {
          return ManageMenu.action(act, performer, action, counter);
        }
        if (action == 89) {
          if (performer.getPower() <= 0) {
            return super.action(act, performer, target, action, counter);
          }
        }
      }
    }
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\behaviours\BodyPartBehaviour.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */