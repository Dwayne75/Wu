package com.wurmonline.server.creatures;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.Message;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Behaviour;
import com.wurmonline.server.behaviours.BehaviourDispatcher;
import com.wurmonline.server.behaviours.BehaviourDispatcher.RequestParam;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.bodys.Wounds;
import com.wurmonline.server.creatures.ai.ChatManager;
import com.wurmonline.server.creatures.ai.NoPathException;
import com.wurmonline.server.creatures.ai.Path;
import com.wurmonline.server.creatures.ai.PathFinder;
import com.wurmonline.server.creatures.ai.PathTile;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.epic.EpicMission;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.epic.Hota;
import com.wurmonline.server.items.CreationEntry;
import com.wurmonline.server.items.CreationMatrix;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.tutorial.MissionTrigger;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.FocusZone;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;

public class Npc
  extends Creature
{
  static final Random faceRandom = new Random();
  int lastX = 0;
  int lastY = 0;
  final ChatManager chatManager;
  LongTarget longTarget;
  int longTargetAttempts = 0;
  int passiveCounter = 0;
  
  public Npc()
    throws Exception
  {
    this.chatManager = new ChatManager(this);
  }
  
  public Npc(CreatureTemplate aTemplate)
    throws Exception
  {
    super(aTemplate);
    this.chatManager = new ChatManager(this);
  }
  
  public Npc(long aId)
    throws Exception
  {
    super(aId);
    this.chatManager = new ChatManager(this);
  }
  
  public final ChatManager getChatManager()
  {
    return this.chatManager;
  }
  
  public final byte getKingdomId()
  {
    if (isAggHuman()) {
      return 0;
    }
    return this.status.kingdom;
  }
  
  public final boolean isAggHuman()
  {
    return this.status.modtype == 2;
  }
  
  public final void pollNPCChat()
  {
    getChatManager().checkChats();
  }
  
  public final void pollNPC()
  {
    checkItemSpawn();
    if (this.passiveCounter-- == 0) {
      doSomething();
    }
  }
  
  int MAXSEED = 100;
  
  private final void doSomething()
  {
    if (!isFighting()) {
      if (this.target == -10L) {
        if (!capturePillar())
        {
          if (!performLongTargetAction())
          {
            if ((getStatus().getPath() == null) && (Server.rand.nextBoolean()))
            {
              startPathing(0);
              setPassiveCounter(120);
            }
            else
            {
              int seed = Server.rand.nextInt(this.MAXSEED);
              if (seed < 10)
              {
                if (getStatus().damage > 0)
                {
                  Wound[] wounds = getBody().getWounds().getWounds();
                  if (wounds.length > 0)
                  {
                    Wound rand = wounds[Server.rand.nextInt(wounds.length)];
                    if (Server.rand.nextBoolean())
                    {
                      rand.setBandaged(true);
                      if (Server.rand.nextBoolean()) {
                        rand.setHealeff((byte)(Server.rand.nextInt(70) + 30));
                      }
                    }
                    else
                    {
                      rand.heal();
                    }
                  }
                }
                setPassiveCounter(30);
              }
              if (seed < 20)
              {
                Item[] allItems = getInventory().getAllItems(false);
                if (allItems.length > 0)
                {
                  Item rand = allItems[Server.rand.nextInt(allItems.length)];
                  try
                  {
                    if (rand.isFood()) {
                      BehaviourDispatcher.action(this, this.communicator, -10L, rand.getWurmId(), (short)182);
                    } else if (rand.isLiquid()) {
                      BehaviourDispatcher.action(this, this.communicator, -10L, rand.getWurmId(), (short)183);
                    } else {
                      BehaviourDispatcher.action(this, this.communicator, -10L, rand.getWurmId(), (short)118);
                    }
                  }
                  catch (Exception localException) {}
                }
                setPassiveCounter(30);
              }
              if (seed < 30)
              {
                if (getCurrentTile() != null) {
                  try
                  {
                    Item[] allItems = getInventory().getAllItems(false);
                    Item[] groundItems = getCurrentTile().getItems();
                    Item rand = null;
                    if (allItems.length > 0) {
                      rand = allItems[Server.rand.nextInt(allItems.length)];
                    }
                    if (groundItems.length > 0)
                    {
                      Item targ = groundItems[Server.rand.nextInt(groundItems.length)];
                      if ((Server.rand.nextBoolean()) && (getCurrentTile().getVillage() == null))
                      {
                        if ((Server.rand.nextInt(4) == 0) && (targ != null) && (targ.isHollow()) && (targ.testInsertItem(rand)))
                        {
                          targ.insertItem(rand);
                        }
                        else if (canCarry(targ.getWeightGrams()))
                        {
                          BehaviourDispatcher.action(this, this.communicator, -10L, targ.getWurmId(), (short)6);
                        }
                        else if (targ.isHollow())
                        {
                          Item[] containeds = targ.getAllItems(false);
                          if (containeds.length > 0)
                          {
                            Item targ2 = containeds[Server.rand.nextInt(containeds.length)];
                            if ((!targ2.isBodyPart()) && (!targ2.isNoTake()))
                            {
                              BehaviourDispatcher.action(this, this.communicator, -10L, targ2.getWurmId(), (short)6);
                              wearItems();
                            }
                          }
                        }
                      }
                      else {
                        BehaviourDispatcher.action(this, this.communicator, -10L, targ.getWurmId(), (short)162);
                      }
                    }
                    else
                    {
                      BehaviourDispatcher.action(this, this.communicator, -10L, rand.getWurmId(), (short)162);
                    }
                    setPassiveCounter(30);
                  }
                  catch (Exception localException1) {}
                }
                setPassiveCounter(10);
              }
              Item targ;
              Behaviour behaviour;
              BehaviourDispatcher.RequestParam param;
              if (seed < 40)
              {
                if (getCurrentTile() != null) {
                  try
                  {
                    Item[] allItems = getInventory().getAllItems(false);
                    Item[] groundItems = getCurrentTile().getItems();
                    Item rand = null;
                    if (allItems.length > 0) {
                      rand = allItems[Server.rand.nextInt(allItems.length)];
                    }
                    if (groundItems.length > 0)
                    {
                      targ = groundItems[Server.rand.nextInt(groundItems.length)];
                      if ((Server.rand.nextBoolean()) && (getCurrentTile().getVillage() == null))
                      {
                        if ((Server.rand.nextInt(4) == 0) && (targ != null) && (targ.isHollow()) && (targ.testInsertItem(rand)))
                        {
                          targ.insertItem(rand);
                        }
                        else if (canCarry(targ.getWeightGrams()))
                        {
                          BehaviourDispatcher.action(this, this.communicator, -10L, targ.getWurmId(), (short)6);
                        }
                        else if (targ.isHollow())
                        {
                          Item[] containeds = targ.getAllItems(false);
                          if (containeds.length > 0)
                          {
                            Item targ2 = containeds[Server.rand.nextInt(containeds.length)];
                            if ((!targ2.isBodyPart()) && (!targ2.isNoTake()))
                            {
                              BehaviourDispatcher.action(this, this.communicator, -10L, targ2.getWurmId(), (short)6);
                              wearItems();
                            }
                          }
                        }
                      }
                      else
                      {
                        if (targ.isHollow())
                        {
                          Item[] containeds = targ.getAllItems(false);
                          if ((containeds.length > 0) && (Server.rand.nextBoolean())) {
                            targ = containeds[Server.rand.nextInt(containeds.length)];
                          }
                        }
                        behaviour = Action.getBehaviour(targ.getWurmId(), isOnSurface());
                        param = BehaviourDispatcher.requestActionForItemsBodyIdsCoinIds(this, targ.getWurmId(), rand, behaviour);
                        List<ActionEntry> actions = param.getAvailableActions();
                        if (actions.size() > 0)
                        {
                          ActionEntry ae = (ActionEntry)actions.get(Server.rand.nextInt(actions.size()));
                          if (ae.getNumber() > 0) {
                            BehaviourDispatcher.action(this, this.communicator, rand.getWurmId(), targ.getWurmId(), ae.getNumber());
                          }
                        }
                      }
                    }
                    else if (rand != null)
                    {
                      BehaviourDispatcher.action(this, this.communicator, -10L, rand.getWurmId(), (short)162);
                    }
                    setPassiveCounter(30);
                  }
                  catch (Exception localException2) {}
                }
                setPassiveCounter(10);
              }
              if (seed < 50)
              {
                if (getCurrentTile() != null) {
                  try
                  {
                    Item[] allItems = getInventory().getAllItems(false);
                    Item rand = null;
                    if (allItems.length > 0)
                    {
                      boolean abilused = false;
                      targ = allItems;behaviour = targ.length;
                      for (param = 0; param < behaviour; param++)
                      {
                        Item abil = targ[param];
                        if (abil.isAbility()) {
                          if (Server.rand.nextBoolean())
                          {
                            BehaviourDispatcher.action(this, this.communicator, -10L, abil.getWurmId(), (short)118);
                            abilused = true;
                            break;
                          }
                        }
                      }
                      if (!abilused)
                      {
                        rand = allItems[Server.rand.nextInt(allItems.length)];
                        if ((Server.rand.nextInt(5) == 0) && (!rand.isEpicTargetItem()) && (rand.isUnique()) && (!rand.isAbility()) && (!rand.isMagicStaff()) && (!rand.isRoyal()))
                        {
                          rand.putItemInfrontof(this);
                        }
                        else if (isOnSurface())
                        {
                          long targTile = Tiles.getTileId(getTileX(), getTileY(), 0);
                          Behaviour behaviour = Action.getBehaviour(targTile, isOnSurface());
                          BehaviourDispatcher.RequestParam param = BehaviourDispatcher.requestActionForTiles(this, targTile, true, rand, behaviour);
                          List<ActionEntry> actions = param.getAvailableActions();
                          if (actions.size() > 0)
                          {
                            ActionEntry ae = (ActionEntry)actions.get(Server.rand.nextInt(actions.size()));
                            if (ae.getNumber() > 0) {
                              BehaviourDispatcher.action(this, this.communicator, rand == null ? -10L : rand.getWurmId(), targTile, ae.getNumber());
                            }
                          }
                        }
                      }
                    }
                    else if (isOnSurface())
                    {
                      long targTile = Tiles.getTileId(getTileX(), getTileY(), 0);
                      Behaviour behaviour = Action.getBehaviour(targTile, isOnSurface());
                      BehaviourDispatcher.RequestParam param = BehaviourDispatcher.requestActionForTiles(this, targTile, true, null, behaviour);
                      List<ActionEntry> actions = param.getAvailableActions();
                      if (actions.size() > 0)
                      {
                        ActionEntry ae = (ActionEntry)actions.get(Server.rand.nextInt(actions.size()));
                        if (ae.getNumber() > 0) {
                          BehaviourDispatcher.action(this, this.communicator, -10L, targTile, ae.getNumber());
                        }
                      }
                    }
                    setPassiveCounter(30);
                  }
                  catch (Exception localException3) {}
                }
                setPassiveCounter(10);
              }
              if (seed < 70)
              {
                if (getCurrentTile() != null) {
                  try
                  {
                    Item[] allItems = getInventory().getAllItems(false);
                    Item rand = null;
                    if (allItems.length > 0) {
                      rand = allItems[Server.rand.nextInt(allItems.length)];
                    }
                    boolean found = false;
                    Creature[] crets = null;
                    for (int x = -2; x <= 2; x++) {
                      for (int y = -2; y <= 2; y++)
                      {
                        VolaTile t = Zones.getTileOrNull(Zones.safeTileX(getTileX() + x), Zones.safeTileY(getTileY() + y), isOnSurface());
                        if (t != null)
                        {
                          crets = t.getCreatures();
                          if (crets.length > 0)
                          {
                            Creature targC = crets[Server.rand.nextInt(crets.length)];
                            Behaviour behaviour = Action.getBehaviour(targC.getWurmId(), isOnSurface());
                            BehaviourDispatcher.RequestParam param = BehaviourDispatcher.requestActionForCreaturesPlayers(this, targC.getWurmId(), rand, targC
                              .isPlayer() ? 0 : 1, behaviour);
                            List<ActionEntry> actions = param.getAvailableActions();
                            if (actions.size() > 0)
                            {
                              ActionEntry ae = (ActionEntry)actions.get(Server.rand.nextInt(actions.size()));
                              if (ae.getNumber() > 0) {
                                BehaviourDispatcher.action(this, this.communicator, rand == null ? -10L : rand.getWurmId(), targC.getWurmId(), ae.getNumber());
                              }
                              setPassiveCounter(30);
                              found = true;
                              break;
                            }
                          }
                        }
                      }
                    }
                    if (!found)
                    {
                      long targTile = Tiles.getTileId(getTileX() - 1 + Server.rand.nextInt(2), getTileY() - 1 + Server.rand.nextInt(2), 0, isOnSurface());
                      Behaviour behaviour = Action.getBehaviour(targTile, isOnSurface());
                      BehaviourDispatcher.RequestParam param = BehaviourDispatcher.requestActionForTiles(this, targTile, true, rand, behaviour);
                      List<ActionEntry> actions = param.getAvailableActions();
                      if (actions.size() > 0)
                      {
                        ActionEntry ae = (ActionEntry)actions.get(Server.rand.nextInt(actions.size()));
                        if (ae.getNumber() > 0) {
                          BehaviourDispatcher.action(this, this.communicator, rand == null ? -10L : rand.getWurmId(), targTile, ae.getNumber());
                        }
                      }
                    }
                    setPassiveCounter(30);
                  }
                  catch (Exception localException4) {}
                }
                setPassiveCounter(10);
              }
              if (seed < 80)
              {
                Creature[] crets = null;
                for (int x = -2; x <= 2; x++) {
                  for (int y = -2; y <= 2; y++)
                  {
                    VolaTile t = Zones.getTileOrNull(Zones.safeTileX(getTileX() + x), Zones.safeTileY(getTileY() + y), isOnSurface());
                    if (t != null)
                    {
                      crets = t.getCreatures();
                      if (crets.length > 0) {
                        try
                        {
                          Creature targC = crets[Server.rand.nextInt(crets.length)];
                          Behaviour behaviour = Action.getBehaviour(targC.getWurmId(), isOnSurface());
                          BehaviourDispatcher.RequestParam param = BehaviourDispatcher.requestActionForCreaturesPlayers(this, targC.getWurmId(), null, targC
                            .isPlayer() ? 0 : 1, behaviour);
                          List<ActionEntry> actions = param.getAvailableActions();
                          if (actions.size() > 0)
                          {
                            ActionEntry ae = (ActionEntry)actions.get(Server.rand.nextInt(actions.size()));
                            if (((!ae.isOffensive()) || (!isFriendlyKingdom(targC.getKingdomId()))) && 
                              (ae.getNumber() > 0)) {
                              BehaviourDispatcher.action(this, this.communicator, -10L, targC.getWurmId(), ae.getNumber());
                            }
                            break;
                          }
                        }
                        catch (Exception localException5) {}
                      }
                    }
                  }
                }
              }
              try
              {
                Item[] allItems = getInventory().getAllItems(false);
                if (allItems.length > 2)
                {
                  Item rand1 = allItems[Server.rand.nextInt(allItems.length)];
                  Item rand2 = allItems[Server.rand.nextInt(allItems.length)];
                  Behaviour behaviour = Action.getBehaviour(rand2.getWurmId(), isOnSurface());
                  BehaviourDispatcher.RequestParam param = BehaviourDispatcher.requestActionForItemsBodyIdsCoinIds(this, rand2.getWurmId(), rand1, behaviour);
                  List<ActionEntry> actions = param.getAvailableActions();
                  if (actions.size() > 0)
                  {
                    ActionEntry ae = (ActionEntry)actions.get(Server.rand.nextInt(actions.size()));
                    if (ae.getNumber() > 0) {
                      BehaviourDispatcher.action(this, this.communicator, rand1 == null ? -10L : rand1.getWurmId(), rand2.getWurmId(), ae.getNumber());
                    }
                  }
                  setPassiveCounter(30);
                }
              }
              catch (Exception localException6) {}
            }
          }
          else {
            setPassiveCounter(180);
          }
        }
        else {
          setPassiveCounter(30);
        }
      }
    }
  }
  
  private void clearLongTarget()
  {
    this.longTarget = null;
    this.longTargetAttempts = 0;
  }
  
  public boolean isOnLongTargetTile()
  {
    if (getStatus() == null) {
      return false;
    }
    return (this.longTarget.getTileX() == (int)this.status.getPositionX() >> 2) && (this.longTarget.getTileY() == (int)this.status.getPositionY() >> 2);
  }
  
  public final Path findPath(int targetX, int targetY, @Nullable PathFinder pathfinder)
    throws NoPathException
  {
    Path path = null;
    PathFinder pf = pathfinder != null ? pathfinder : new PathFinder();
    setPathfindcounter(getPathfindCounter() + 1);
    if ((getPathfindCounter() < 10) || (this.target != -10L) || (getPower() > 0)) {
      path = pf.findPath(this, getTileX(), getTileY(), targetX, targetY, isOnSurface(), 20);
    } else {
      throw new NoPathException("No pathing now");
    }
    if (path != null) {
      setPathfindcounter(0);
    }
    return path;
  }
  
  private final boolean capturePillar()
  {
    if (getCitizenVillage() != null)
    {
      FocusZone hota = FocusZone.getHotaZone();
      if ((hota != null) && (hota.covers(getTileX(), getTileY()))) {
        for (Item i : getCurrentTile().getItems()) {
          if (i.getTemplateId() == 739) {
            if (i.getData1() != getCitizenVillage().getId())
            {
              try
              {
                BehaviourDispatcher.action(this, this.communicator, -10L, i.getWurmId(), (short)504);
              }
              catch (Exception localException) {}
              return true;
            }
          }
        }
      }
    }
    return false;
  }
  
  private final boolean performLongTargetAction()
  {
    Item found;
    Item item;
    int tilenum;
    Item axe;
    if ((this.longTarget != null) && (this.longTarget.getMissionTrigger() > 0))
    {
      MissionTrigger trigger = MissionTriggers.getTriggerWithId(this.longTarget.getMissionTrigger());
      if (trigger != null) {
        if ((Math.abs(this.longTarget.getTileX() - getTileX()) < 3) && (Math.abs(this.longTarget.getTileY() - getTileY()) < 3))
        {
          found = null;
          if (trigger.getItemUsedId() > 0)
          {
            if (trigger.getOnActionPerformed() == 148)
            {
              ce = CreationMatrix.getInstance().getCreationEntry(trigger.getItemUsedId());
              if ((ce != null) && 
                (!ce.isAdvanced())) {
                try
                {
                  found = ItemFactory.createItem(trigger.getItemUsedId(), 20.0F + Server.rand.nextFloat() * 20.0F, getName());
                  getInventory().insertItem(found, true);
                  if (found.getWeightGrams() > 20000) {
                    found.putItemInfrontof(this);
                  } else {
                    wearItems();
                  }
                  MissionTriggers.activateTriggers(this, found, 148, 0L, 1);
                  clearLongTarget();
                  return true;
                }
                catch (Exception localException) {}
              }
            }
            CreationEntry ce = getAllItems();localException = ce.length;
            for (Exception localException4 = 0; localException4 < localException; localException4++)
            {
              item = ce[localException4];
              if (item.getTemplateId() == trigger.getItemUsedId()) {
                found = item;
              }
            }
            if (found == null) {
              try
              {
                found = ItemFactory.createItem(trigger.getItemUsedId(), 20.0F + Server.rand.nextFloat() * 20.0F, getName());
                getInventory().insertItem(found, true);
              }
              catch (Exception localException1) {}
            }
          }
          if ((WurmId.getType(trigger.getTarget()) == 1) || (WurmId.getType(trigger.getTarget()) == 0)) {
            try
            {
              Creature c = Server.getInstance().getCreature(trigger.getTarget());
              if ((c == null) || (c.isDead()))
              {
                clearLongTarget();
                return true;
              }
            }
            catch (NoSuchCreatureException nsc)
            {
              clearLongTarget();
              return true;
            }
            catch (NoSuchPlayerException nsp)
            {
              clearLongTarget();
              return true;
            }
          }
          if ((WurmId.getType(trigger.getTarget()) == 3) && (trigger.getOnActionPerformed() == 492))
          {
            tilenum = Server.surfaceMesh.getTile(getTileX(), getTileY());
            if (!Tiles.isTree(Tiles.decodeType(tilenum))) {
              return true;
            }
            if (found == null)
            {
              localException = getBody().getBodyItem().getAllItems(false);Item localItem1 = localException.length;
              for (item = 0; item < localItem1; item++)
              {
                Item axe = localException[item];
                if ((axe.isWeaponAxe()) || (axe.isWeaponSlash())) {
                  found = axe;
                }
              }
            }
            if (found == null)
            {
              localException = getInventory().getAllItems(false);Item localItem2 = localException.length;
              for (item = 0; item < localItem2; item++)
              {
                axe = localException[item];
                if ((axe.isWeaponAxe()) || (axe.isWeaponSlash())) {
                  found = axe;
                }
              }
            }
            if (found == null) {
              try
              {
                found = ItemFactory.createItem(7, 10.0F, getName());
              }
              catch (Exception localException2) {}
            }
            if (((found != null) && (found.isWeaponAxe())) || (found.isWeaponSlash())) {
              try
              {
                BehaviourDispatcher.action(this, this.communicator, found.getWurmId(), trigger.getTarget(), (short)96);
              }
              catch (Exception localException3) {}
            }
          }
          MissionTriggers.activateTriggers(this, found, trigger.getOnActionPerformed(), trigger.getTarget(), 1);
          
          clearLongTarget();
          return true;
        }
      }
    }
    else if (this.longTarget != null)
    {
      if (isOnLongTargetTile())
      {
        Item[] currentItems = getCurrentTile().getItems();
        found = currentItems;tilenum = found.length;
        for (localException3 = 0; localException3 < tilenum; localException3++)
        {
          Item current = found[localException3];
          if ((current.isCorpse()) && (current.getLastOwnerId() == getWurmId()))
          {
            item = current.getAllItems(false);axe = item.length;
            for (Item localItem3 = 0; localItem3 < axe; localItem3++)
            {
              Item incorpse = item[localItem3];
              if (!incorpse.isBodyPart()) {
                getInventory().insertItem(incorpse);
              }
            }
            wearItems();
            Items.destroyItem(current.getWurmId());
          }
        }
        clearLongTarget();
        return true;
      }
    }
    return false;
  }
  
  public final PathTile getMoveTarget(int seed)
  {
    if (getStatus() == null) {
      return null;
    }
    float lPosX = this.status.getPositionX();
    float lPosY = this.status.getPositionY();
    boolean hasTarget = false;
    int tilePosX = (int)lPosX >> 2;
    int tilePosY = (int)lPosY >> 2;
    int tx = tilePosX;
    int ty = tilePosY;
    int hy;
    int tile;
    int seedh;
    if ((!isAggHuman()) && (getCitizenVillage() != null)) {
      if (this.longTarget == null)
      {
        int x;
        Player p;
        int tile;
        if (Server.rand.nextInt(100) == 0)
        {
          Player[] players = Players.getInstance().getPlayers();
          for (x = 0; x < 10; x++)
          {
            p = players[Server.rand.nextInt(players.length)];
            if ((p.isWithinDistanceTo(this, 200.0F)) && 
              (p.getPower() == 0))
            {
              tile = Server.surfaceMesh.getTile(tilePosX, tilePosY);
              if (!p.isOnSurface()) {
                tile = Server.caveMesh.getTile(tilePosX, tilePosY);
              }
              this.longTarget = new LongTarget(p.getTileX(), p.getTileY(), tile, p.isOnSurface(), p.getFloorLevel(), this);
              if (p.isFriendlyKingdom(getKingdomId()))
              {
                getChatManager().createAndSendMessage(p, "Oi.", false); break;
              }
              getChatManager().createAndSendMessage(p, "Coming for you.", false);
              break;
            }
          }
        }
        boolean surf;
        int tile;
        if ((this.longTarget == null) && (Server.rand.nextInt(10) == 0))
        {
          Item[] allIts = Items.getAllItems();
          for (Item corpse : allIts) {
            if ((corpse.getZoneId() > 0) && (corpse.getTemplateId() == 272) && (corpse.getLastOwnerId() == getWurmId()) && (corpse.getName().toLowerCase().contains(getName().toLowerCase())))
            {
              Item[] contained = corpse.getAllItems(false);
              if (contained.length > 4)
              {
                surf = corpse.isOnSurface();
                tile = Server.surfaceMesh.getTile(corpse.getTileX(), corpse.getTileY());
                if (!surf) {
                  tile = Server.caveMesh.getTile(corpse.getTileX(), corpse.getTileY());
                }
                this.longTarget = new LongTarget(corpse.getTileX(), corpse.getTileY(), tile, surf, surf ? 0 : -1, this);
              }
            }
          }
        }
        if ((this.longTarget == null) && (Server.rand.nextInt(10) == 0))
        {
          EpicMission[] ems = EpicServerStatus.getCurrentEpicMissions();
          for (EpicMission em : ems) {
            if (em.isCurrent())
            {
              Deity deity = Deities.getDeity(em.getEpicEntityId());
              if (deity != null) {
                if (deity.getFavoredKingdom() == getKingdomId()) {
                  for (MissionTrigger trig : MissionTriggers.getAllTriggers()) {
                    if (trig.getMissionRequired() == em.getMissionId())
                    {
                      long target = trig.getTarget();
                      if ((WurmId.getType(target) == 3) || (WurmId.getType(target) == 17))
                      {
                        int x2 = Tiles.decodeTileX(target);
                        int y2 = Tiles.decodeTileY(target);
                        boolean surf = WurmId.getType(target) == 3;
                        int tile = Server.surfaceMesh.getTile(x2, y2);
                        if (!surf) {
                          tile = Server.caveMesh.getTile(x2, y2);
                        }
                        this.longTarget = new LongTarget(x2, y2, tile, surf, surf ? 0 : -1, this);
                      }
                      else if (WurmId.getType(target) == 2)
                      {
                        try
                        {
                          Item i = Items.getItem(target);
                          int tile = Server.surfaceMesh.getTile(i.getTileX(), i.getTileY());
                          if (!i.isOnSurface()) {
                            tile = Server.caveMesh.getTile(i.getTileX(), i.getTileY());
                          }
                          this.longTarget = new LongTarget(i.getTileX(), i.getTileY(), tile, i.isOnSurface(), i.getFloorLevel(), this);
                        }
                        catch (NoSuchItemException localNoSuchItemException) {}
                      }
                      else if ((WurmId.getType(target) == 1) || 
                        (WurmId.getType(target) == 0))
                      {
                        try
                        {
                          Creature c = Server.getInstance().getCreature(target);
                          int tile = Server.surfaceMesh.getTile(c.getTileX(), c.getTileY());
                          if (!c.isOnSurface()) {
                            tile = Server.caveMesh.getTile(c.getTileX(), c.getTileY());
                          }
                          this.longTarget = new LongTarget(c.getTileX(), c.getTileY(), tile, c.isOnSurface(), c.getFloorLevel(), this);
                        }
                        catch (NoSuchCreatureException localNoSuchCreatureException) {}catch (NoSuchPlayerException localNoSuchPlayerException) {}
                      }
                      else if (WurmId.getType(target) == 5)
                      {
                        int x = (int)(target >> 32) & 0xFFFF;
                        int y = (int)(target >> 16) & 0xFFFF;
                        
                        Wall wall = Wall.getWall(target);
                        if (wall != null)
                        {
                          int tile = Server.surfaceMesh.getTile(x, y);
                          this.longTarget = new LongTarget(x, y, tile, true, wall.getFloorLevel(), this);
                        }
                      }
                      if (this.longTarget != null)
                      {
                        this.longTarget.setMissionTrigger(trig.getId());
                        this.longTarget.setEpicMission(em.getMissionId());
                        this.longTarget.setMissionTarget(target);
                      }
                    }
                  }
                }
              }
            }
          }
        }
        if ((this.longTarget == null) && (Server.rand.nextInt(10) == 0))
        {
          int tile;
          if (getCitizenVillage() != null)
          {
            if (getTileRange(this, getCitizenVillage().getTokenX(), getCitizenVillage().getTokenY()) > 300.0D)
            {
              tile = Server.surfaceMesh.getTile(getCitizenVillage().getTokenX(), getCitizenVillage().getTokenY());
              this.longTarget = new LongTarget(getCitizenVillage().getTokenX(), getCitizenVillage().getTokenY(), tile, true, 0, this);
            }
          }
          else
          {
            tile = Villages.getVillages();x = tile.length;
            for (p = 0; p < x; p++)
            {
              Village v = tile[p];
              if ((v.isPermanent) && (v.kingdom == getKingdomId())) {
                if (getTileRange(this, v.getTokenX(), v.getTokenY()) > 300.0D)
                {
                  int tile = Server.surfaceMesh.getTile(v.getTokenX(), v.getTokenY());
                  this.longTarget = new LongTarget(v.getTokenX(), v.getTokenY(), tile, true, 0, this);
                }
              }
            }
          }
          if (this.longTarget != null)
          {
            int seedh = Server.rand.nextInt(5);
            String mess = "Think I'll head home again...";
            switch (seedh)
            {
            case 0: 
              mess = "Time to go home!";
              break;
            case 1: 
              mess = "Enough of this. Home Sweet Home.";
              break;
            case 2: 
              mess = "Heading home. Are you coming?";
              break;
            case 3: 
              mess = "I will go home now.";
              break;
            case 4: 
              mess = "That's it. I'm going home.";
              break;
            default: 
              mess = "Think I'll go home for a while.";
            }
            if (getCurrentTile() != null)
            {
              Message m = new Message(this, (byte)0, ":Local", "<" + getName() + "> " + mess);
              getCurrentTile().broadCastMessage(m);
            }
          }
        }
        if ((this.longTarget == null) && (Server.rand.nextInt(100) == 0)) {
          if (getCitizenVillage() != null)
          {
            FocusZone hota = FocusZone.getHotaZone();
            if (hota != null) {
              if (!hota.covers(getTileX(), getTileY()))
              {
                int hx = hota.getStartX() + Server.rand.nextInt(hota.getEndX() - hota.getStartX());
                hy = hota.getStartY() + Server.rand.nextInt(hota.getEndY() - hota.getStartY());
                tile = Server.surfaceMesh.getTile(hx, hy);
                this.longTarget = new LongTarget(hx, hy, tile, true, 0, this);
                seedh = Server.rand.nextInt(5);
                String mess = "Think I'll go hunt for some pillars a bit...";
                switch (seedh)
                {
                case 0: 
                  mess = "Anyone in the Hunt of the Ancients is in trouble now!";
                  break;
                case 1: 
                  mess = "Going to check out what happens in the Hunt.";
                  break;
                case 2: 
                  mess = "Heading to join the Hunt. Coming with me?";
                  break;
                case 3: 
                  mess = "Going to head to the Hunt of the Ancients. You interested?";
                  break;
                case 4: 
                  mess = "I want to do some gloryhunting in the HOTA.";
                  break;
                default: 
                  mess = "Think I'll go join the hunt a bit...";
                }
                if (getCurrentTile() != null)
                {
                  Message m = new Message(this, (byte)0, ":Local", "<" + getName() + "> " + mess);
                  getCurrentTile().broadCastMessage(m);
                }
              }
            }
          }
        }
        if (this.longTarget != null) {
          return this.longTarget;
        }
      }
      else
      {
        boolean clear = false;
        if ((this.longTarget.getCreatureTarget() != null) && (this.longTarget.getTileX() != this.longTarget.getCreatureTarget().getTileX())) {
          this.longTarget.setTileX(this.longTarget.getCreatureTarget().getTileX());
        }
        if ((this.longTarget.getCreatureTarget() != null) && (this.longTarget.getTileY() != this.longTarget.getCreatureTarget().getTileY())) {
          this.longTarget.setTileY(this.longTarget.getCreatureTarget().getTileY());
        }
        if (this.longTarget.getEpicMission() > 0)
        {
          EpicMission em = EpicServerStatus.getEpicMissionForMission(this.longTarget.getEpicMission());
          if ((em == null) || (!em.isCurrent()) || (em.isCompleted())) {
            clear = true;
          }
        }
        if ((Math.abs(this.longTarget.getTileX() - tx) < 20) && (Math.abs(this.longTarget.getTileY() - ty) < 20))
        {
          if ((Math.abs(this.longTarget.getTileX() - tx) < 10) && (Math.abs(this.longTarget.getTileY() - ty) < 10)) {
            if (this.longTarget.getCreatureTarget() != null) {
              if (!this.longTarget.getCreatureTarget().isFriendlyKingdom(getKingdomId()))
              {
                setTarget(this.longTarget.getCreatureTarget().getWurmId(), false);
                clear = true;
              }
            }
          }
          if ((isOnLongTargetTile()) || (this.longTargetAttempts++ > 50)) {
            clear = true;
          } else {
            return this.longTarget;
          }
        }
        else if (System.currentTimeMillis() - this.longTarget.getStartTime() > 3600000L)
        {
          clear = true;
        }
        if (clear) {
          clearLongTarget();
        }
      }
    }
    boolean flee = false;
    if ((this.target == -10L) || (this.fleeCounter > 0)) {
      if ((isTypeFleeing()) || (this.fleeCounter > 0)) {
        if (isOnSurface())
        {
          Long[] crets;
          if (Server.rand.nextBoolean())
          {
            if ((getCurrentTile() != null) && (getCurrentTile().getVillage() != null))
            {
              crets = getVisionArea().getSurface().getCreatures();
              for (Long lCret : crets) {
                try
                {
                  Creature cret = Server.getInstance().getCreature(lCret.longValue());
                  if ((cret.getPower() == 0) && (
                    (cret.isPlayer()) || (cret.isAggHuman()) || (cret.isCarnivore()) || (cret.isMonster())))
                  {
                    if (cret.getPosX() > getPosX()) {
                      tilePosX -= Server.rand.nextInt(6);
                    } else {
                      tilePosX += Server.rand.nextInt(6);
                    }
                    if (cret.getPosY() > getPosY()) {
                      tilePosY -= Server.rand.nextInt(6);
                    } else {
                      tilePosY += Server.rand.nextInt(6);
                    }
                    flee = true;
                    break;
                  }
                }
                catch (Exception localException) {}
              }
            }
          }
          else {
            for (Player p : Players.getInstance().getPlayers()) {
              if (((p.getPower() == 0) || (Servers.localServer.testServer)) && (p.getVisionArea() != null) && 
                (p.getVisionArea().getSurface() != null) && 
                (p.getVisionArea().getSurface().containsCreature(this)))
              {
                if (p.getPosX() > getPosX()) {
                  tilePosX -= Server.rand.nextInt(6);
                } else {
                  tilePosX += Server.rand.nextInt(6);
                }
                if (p.getPosY() > getPosY()) {
                  tilePosY -= Server.rand.nextInt(6);
                } else {
                  tilePosY += Server.rand.nextInt(6);
                }
                flee = true;
                break;
              }
            }
          }
        }
      }
    }
    if ((!flee) && (!hasTarget))
    {
      VolaTile currTile = getCurrentTile();
      if (currTile != null)
      {
        int rand = Server.rand.nextInt(9);
        int tpx = currTile.getTileX() + 4 - rand;
        rand = Server.rand.nextInt(9);
        int tpy = currTile.getTileY() + 4 - rand;
        totx += currTile.getTileX() - tpx;
        toty += currTile.getTileY() - tpy;
        if (this.longTarget != null)
        {
          if (Math.abs(this.longTarget.getTileX() - getTileX()) < 20)
          {
            tpx = this.longTarget.getTileX();
          }
          else
          {
            tpx = getTileX() + 5 + Server.rand.nextInt(6);
            if (getTileX() > this.longTarget.getTileX()) {
              tpx = getTileX() - 5 - Server.rand.nextInt(6);
            }
          }
          if (Math.abs(this.longTarget.getTileY() - getTileY()) < 20)
          {
            tpy = this.longTarget.getTileY();
          }
          else
          {
            tpy = getTileY() + 5 + Server.rand.nextInt(6);
            if (getTileY() > this.longTarget.getTileY()) {
              tpy = getTileY() - 5 - Server.rand.nextInt(6);
            }
          }
        }
        else if (getCitizenVillage() != null)
        {
          FocusZone hota = FocusZone.getHotaZone();
          if (hota != null) {
            if (hota.covers(getTileX(), getTileY())) {
              for (localException = Hota.getHotaItems().iterator(); localException.hasNext();)
              {
                pillar = (Item)localException.next();
                if ((pillar.getTemplateId() == 739) && (pillar.getZoneId() > 0)) {
                  if (pillar.getData1() != getCitizenVillage().getId()) {
                    if (getTileRange(this, pillar.getTileX(), pillar.getTileY()) < 20.0D)
                    {
                      tpx = pillar.getTileX();
                      tpy = pillar.getTileY();
                    }
                  }
                }
              }
            }
          }
        }
        Item pillar;
        tpx = Zones.safeTileX(tpx);
        tpy = Zones.safeTileY(tpy);
        VolaTile t = Zones.getOrCreateTile(tpx, tpy, isOnSurface());
        if (isOnSurface())
        {
          boolean stepOnBridge = false;
          if (Server.rand.nextInt(5) == 0) {
            for (VolaTile stile : this.currentTile.getThisAndSurroundingTiles(1)) {
              if ((stile.getStructure() != null) && (stile.getStructure().isTypeBridge())) {
                if (stile.getStructure().isHorizontal())
                {
                  if ((stile.getStructure().getMaxX() == stile.getTileX()) || 
                    (stile.getStructure().getMinX() == stile.getTileX())) {
                    if (getTileY() == stile.getTileY())
                    {
                      tilePosX = stile.getTileX();
                      tilePosY = stile.getTileY();
                      stepOnBridge = true;
                      break;
                    }
                  }
                }
                else if ((stile.getStructure().getMaxY() == stile.getTileY()) || 
                  (stile.getStructure().getMinY() == stile.getTileY())) {
                  if (getTileX() == stile.getTileX())
                  {
                    tilePosX = stile.getTileX();
                    tilePosY = stile.getTileY();
                    stepOnBridge = true;
                    break;
                  }
                }
              }
            }
          }
          if (!stepOnBridge) {
            if ((t == null) || (t.getCreatures().length < 3))
            {
              tilePosX = tpx;
              tilePosY = tpy;
            }
          }
        }
        else if ((t == null) || (t.getCreatures().length < 3))
        {
          tilePosX = tpx;
          tilePosY = tpy;
        }
      }
    }
    Creature targ = getTarget();
    if (targ != null)
    {
      if ((targ.getCultist() != null) && (targ.getCultist().hasFearEffect())) {
        setTarget(-10L, true);
      }
      VolaTile currTile = targ.getCurrentTile();
      if (currTile != null)
      {
        tilePosX = currTile.tilex;
        tilePosY = currTile.tiley;
        if (seed == 100)
        {
          tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
          tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
        }
        int targGroup = targ.getGroupSize();
        int myGroup = getGroupSize();
        if (isOnSurface() != currTile.isOnSurface())
        {
          boolean changeLayer = false;
          if (getCurrentTile().isTransition) {
            changeLayer = true;
          }
          VolaTile t = getCurrentTile();
          if (((isAggHuman()) || (isHunter()) || (isDominated())) && 
            ((!currTile.isGuarded()) || ((t != null) && (t.isGuarded()))) && 
            (isWithinTileDistanceTo(currTile.getTileX(), currTile.getTileY(), 
            (int)targ.getPositionZ(), this.template.getMaxHuntDistance())))
          {
            if (!changeLayer)
            {
              int[] tiles = { tilePosX, tilePosY };
              if (isOnSurface()) {
                tiles = findRandomCaveEntrance(tiles);
              } else {
                tiles = findRandomCaveExit(tiles);
              }
              tilePosX = tiles[0];
              tilePosY = tiles[1];
            }
          }
          else {
            setTarget(-10L, true);
          }
          if (changeLayer) {
            if ((!Tiles.isMineDoor(Tiles.decodeType(Server.surfaceMesh.getTile(tx, ty)))) || 
              (MineDoorPermission.getPermission(tx, ty).mayPass(this))) {
              setLayer(isOnSurface() ? -1 : 0, true);
            }
          }
        }
        if ((targ.getCultist() != null) && (targ.getCultist().hasFearEffect()))
        {
          if (Server.rand.nextBoolean()) {
            tilePosX = Math.max(currTile.getTileX() + 10, getTileX());
          } else {
            tilePosX = Math.min(currTile.getTileX() - 10, getTileX());
          }
          if (Server.rand.nextBoolean()) {
            tilePosX = Math.max(currTile.getTileY() + 10, getTileY());
          } else {
            tilePosX = Math.min(currTile.getTileY() - 10, getTileY());
          }
        }
        else
        {
          VolaTile t = getCurrentTile();
          if ((targGroup <= myGroup * getMaxGroupAttackSize()) && 
            ((isAggHuman()) || (isHunter())) && (
            (!currTile.isGuarded()) || ((t != null) && (t.isGuarded()))))
          {
            if (isWithinTileDistanceTo(currTile.getTileX(), currTile.getTileY(), 
              (int)targ.getPositionZ(), this.template.getMaxHuntDistance()))
            {
              if ((targ.getKingdomId() != 0) && 
                (!isFriendlyKingdom(targ.getKingdomId())) && (
                (isDefendKingdom()) || ((isAggWhitie()) && 
                (targ.getKingdomTemplateId() != 3))))
              {
                if (!isFighting()) {
                  if (seed == 100)
                  {
                    tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
                    tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
                  }
                  else
                  {
                    tilePosX = currTile.getTileX();
                    tilePosY = currTile.getTileY();
                    setTarget(targ.getWurmId(), false);
                  }
                }
              }
              else if (seed == 100)
              {
                tilePosX = currTile.tilex - 1 + Server.rand.nextInt(3);
                tilePosY = currTile.tiley - 1 + Server.rand.nextInt(3);
              }
              else
              {
                tilePosX = currTile.getTileX();
                tilePosY = currTile.getTileY();
                if ((getSize() < 5) && 
                  (targ.getBridgeId() != -10L) && 
                  (getBridgeId() < 0L))
                {
                  int[] tiles = findBestBridgeEntrance(targ.getTileX(), targ
                    .getTileY(), targ.getLayer(), targ
                    .getBridgeId());
                  if (tiles[0] > 0)
                  {
                    tilePosX = tiles[0];
                    tilePosY = tiles[1];
                    if ((getTileX() == tilePosX) && (getTileY() == tilePosY))
                    {
                      tilePosX = currTile.tilex;
                      tilePosY = currTile.tiley;
                    }
                  }
                }
                else if (getBridgeId() != targ.getBridgeId())
                {
                  int[] tiles = findBestBridgeEntrance(targ.getTileX(), targ
                    .getTileY(), targ.getLayer(), getBridgeId());
                  if (tiles[0] > 0)
                  {
                    tilePosX = tiles[0];
                    tilePosY = tiles[1];
                    if ((getTileX() == tilePosX) && (getTileY() == tilePosY))
                    {
                      tilePosX = currTile.tilex;
                      tilePosY = currTile.tiley;
                    }
                  }
                }
              }
            }
            else if (!isFighting()) {
              setTarget(-10L, true);
            }
          }
          else if (!isFighting()) {
            setTarget(-10L, true);
          }
        }
      }
    }
    if ((tilePosX == tx) && (tilePosY == ty)) {
      return null;
    }
    tilePosX = Zones.safeTileX(tilePosX);
    tilePosY = Zones.safeTileY(tilePosY);
    if (!isOnSurface())
    {
      int tile = Server.caveMesh.getTile(tilePosX, tilePosY);
      if ((!Tiles.isSolidCave(Tiles.decodeType(tile))) && (
        (Tiles.decodeHeight(tile) > -getHalfHeightDecimeters()) || (isSwimming()) || (isSubmerged()))) {
        return new PathTile(tilePosX, tilePosY, tile, isOnSurface(), -1);
      }
    }
    else
    {
      int tile = Server.surfaceMesh.getTile(tilePosX, tilePosY);
      if ((Tiles.decodeHeight(tile) > -getHalfHeightDecimeters()) || (isSwimming()) || (isSubmerged())) {
        return new PathTile(tilePosX, tilePosY, tile, isOnSurface(), getFloorLevel());
      }
    }
    setTarget(-10L, true);
    if ((isDominated()) && (hasOrders())) {
      removeOrder(getFirstOrder());
    }
    return null;
  }
  
  private final void setPassiveCounter(int counter)
  {
    this.passiveCounter = counter;
  }
  
  private final void checkItemSpawn()
  {
    if (this.lastX == 0) {
      this.lastX = getTileX();
    }
    if (this.lastY == 0) {
      this.lastY = getTileY();
    }
    if ((this.lastX - getTileX() > 50) || (this.lastY - getTileY() > 50))
    {
      this.lastX = getTileX();
      this.lastY = getTileY();
      if ((Server.rand.nextInt(10) == 0) && (getBody().getContainersAndWornItems().length < 10)) {
        try
        {
          int templateId = Server.rand.nextInt(1437);
          ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(templateId);
          if ((template.isArmour()) || (template.isWeapon())) {
            if ((!template.isRoyal) && (!template.artifact)) {
              try
              {
                Item toInsert = ItemFactory.createItem(templateId, Server.rand.nextFloat() * 80.0F + 20.0F, getName());
                getInventory().insertItem(toInsert, true);
                wearItems();
                if (toInsert.getParentId() == getInventory().getWurmId()) {
                  Items.destroyItem(toInsert.getWurmId());
                }
              }
              catch (FailedException localFailedException) {}
            }
          }
        }
        catch (NoSuchTemplateException localNoSuchTemplateException) {}
      }
    }
  }
  
  public final boolean isMoveLocal()
  {
    if (hasTrait(8)) {
      return true;
    }
    return this.template.isMoveLocal();
  }
  
  public final boolean isSentinel()
  {
    if (hasTrait(9)) {
      return true;
    }
    return this.template.isSentinel();
  }
  
  public final boolean isMoveGlobal()
  {
    if (hasTrait(1)) {
      return true;
    }
    return this.template.isMoveGlobal();
  }
  
  public boolean isNpc()
  {
    return true;
  }
  
  public long getFace()
  {
    faceRandom.setSeed(getWurmId());
    return faceRandom.nextLong();
  }
  
  public float getSpeed()
  {
    if ((getVehicle() > -10L) && (WurmId.getType(getVehicle()) == 1)) {
      return 1.7F;
    }
    return 1.1F;
  }
  
  public boolean isTypeFleeing()
  {
    return (getStatus().modtype == 10) || (getStatus().damage > 45000);
  }
  
  public boolean isRespawn()
  {
    return !hasTrait(19);
  }
  
  public final boolean isDominatable(Creature aDominator)
  {
    if ((getLeader() != null) && (getLeader() != aDominator)) {
      return false;
    }
    if ((isRidden()) || (this.hitchedTo != null)) {
      return false;
    }
    return hasTrait(22);
  }
  
  public final float getBaseCombatRating()
  {
    double fskill = 1.0D;
    try
    {
      fskill = this.skills.getSkill(1023).getKnowledge();
    }
    catch (NoSuchSkillException nss)
    {
      this.skills.learn(1023, 1.0F);
      fskill = 1.0D;
    }
    if (getLoyalty() > 0.0F) {
      return (float)Math.max(1.0D, (isReborn() ? 0.7F : 0.5F) * fskill / 5.0D * this.status.getBattleRatingTypeModifier()) * Servers.localServer.getCombatRatingModifier();
    }
    return (float)Math.max(1.0D, fskill / 5.0D * this.status.getBattleRatingTypeModifier()) * Servers.localServer.getCombatRatingModifier();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\creatures\Npc.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */