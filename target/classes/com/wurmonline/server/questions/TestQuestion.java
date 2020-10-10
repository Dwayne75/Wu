package com.wurmonline.server.questions;

import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.players.JournalTier;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerJournal;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.shared.util.StringUtilities;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestQuestion
  extends Question
  implements TimeConstants
{
  private static final Logger logger = Logger.getLogger(TestQuestion.class.getName());
  private static final ConcurrentHashMap<Long, Long> armourCreators = new ConcurrentHashMap();
  
  public TestQuestion(Creature aResponder, long aTarget)
  {
    super(aResponder, "Testing", "What do you want to do?", 96, aTarget);
  }
  
  public boolean checkIfMayCreateArmour()
  {
    if (getResponder().getPower() > 0) {
      return true;
    }
    Long last = (Long)armourCreators.get(Long.valueOf(getResponder().getWurmId()));
    if (last != null) {
      if (System.currentTimeMillis() - last.longValue() < 300000L) {
        return false;
      }
    }
    last = new Long(System.currentTimeMillis());
    armourCreators.put(Long.valueOf(getResponder().getWurmId()), last);
    return true;
  }
  
  public void answer(Properties aAnswers)
  {
    if (Servers.localServer.testServer)
    {
      getResponder().getBody().healFully();
      getResponder().getStatus().modifyStamina2(100.0F);
      
      String priestTypeString = aAnswers.getProperty("priestType");
      String faithLevelString = aAnswers.getProperty("faithLevel");
      Deity d;
      if (priestTypeString != null)
      {
        int priestType = Integer.parseInt(priestTypeString);
        switch (priestType)
        {
        case 0: 
          break;
        case 1: 
          if (getResponder().getDeity() != null) {
            try
            {
              getResponder().setFaith(0.0F);
              getResponder().setFavor(0.0F);
              getResponder().setDeity(null);
              getResponder().getCommunicator().sendNormalServerMessage("You follow no deity.");
            }
            catch (IOException e)
            {
              getResponder().getCommunicator().sendNormalServerMessage("Could not remove deity.");
            }
          }
          break;
        default: 
          int count = 2;
          for (d : Deities.getDeities())
          {
            if (count == priestType) {
              try
              {
                getResponder().setDeity(d);
                getResponder().getCommunicator().sendNormalServerMessage("You are now a follower of " + d.getName() + ".");
              }
              catch (IOException e)
              {
                getResponder().getCommunicator().sendNormalServerMessage("Could not set deity.");
              }
            }
            count++;
          }
        }
      }
      if (faithLevelString != null)
      {
        int faithLevel = Integer.parseInt(faithLevelString);
        if (faithLevel > 0)
        {
          faithLevel = Math.min(100, faithLevel);
          if (getResponder().getDeity() != null) {
            try
            {
              getResponder().getCommunicator().sendNormalServerMessage("Faith set to " + faithLevel + ".");
              if ((faithLevel >= 30) && (!getResponder().isPriest()))
              {
                getResponder().setPriest(true);
                getResponder().getCommunicator().sendNormalServerMessage("You are now a priest of " + getResponder().getDeity().getName() + ".");
                if (getResponder().isPlayer()) {
                  PlayerJournal.sendTierUnlock((Player)getResponder(), (JournalTier)PlayerJournal.getAllTiers().get(Byte.valueOf((byte)10)));
                }
              }
              else if ((faithLevel < 30) && (getResponder().isPriest()))
              {
                getResponder().setPriest(false);
                getResponder().getCommunicator().sendNormalServerMessage("You are no longer a priest of " + getResponder().getDeity().getName() + ".");
              }
              getResponder().setFaith(faithLevel);
            }
            catch (IOException e)
            {
              getResponder().getCommunicator().sendNormalServerMessage("Could not set faith.");
            }
          }
        }
      }
      String skillLevel = aAnswers.getProperty("skillLevel");
      Object localObject;
      Skill sk;
      if (skillLevel != null) {
        try
        {
          double slevel = Double.parseDouble(skillLevel);
          slevel = Math.min(slevel, 90.0D);
          if (slevel > 0.0D)
          {
            Skills s = getResponder().getSkills();
            if (s != null)
            {
              Skill[] skills = s.getSkills();
              d = skills;e = d.length;
              for (localObject = 0; localObject < e; localObject++)
              {
                sk = d[localObject];
                if ((sk.getType() != 0) && (sk.getType() != 1)) {
                  sk.setKnowledge(slevel, false);
                }
              }
            }
          }
        }
        catch (Exception e)
        {
          if (logger.isLoggable(Level.FINE)) {
            logger.fine("skill bug?");
          }
        }
      }
      String alignLevel = aAnswers.getProperty("alignmentLevel");
      if (alignLevel != null) {
        try
        {
          float alignment = Float.parseFloat(alignLevel);
          if (alignment != 0.0F)
          {
            if (alignment > 99.0F) {
              alignment = 99.0F;
            }
            if (alignment < -99.0F) {
              alignment = -99.0F;
            }
            getResponder().setAlignment(alignment);
          }
        }
        catch (Exception e)
        {
          if (logger.isLoggable(Level.FINE)) {
            logger.fine("alignment update issue");
          }
        }
      }
      String charLevel = aAnswers.getProperty("characteristicsLevel");
      if (charLevel != null) {
        try
        {
          double slevel = Double.parseDouble(charLevel);
          slevel = Math.min(slevel, 90.0D);
          if (slevel > 0.0D)
          {
            Skills s = getResponder().getSkills();
            if (s != null)
            {
              Skill[] skills = s.getSkills();
              localObject = skills;sk = localObject.length;
              for (Skill localSkill1 = 0; localSkill1 < sk; localSkill1++)
              {
                Skill sk = localObject[localSkill1];
                if ((sk.getType() == 0) || (sk.getType() == 1)) {
                  sk.setKnowledge(slevel, false);
                }
              }
            }
          }
        }
        catch (Exception e)
        {
          if (logger.isLoggable(Level.FINE)) {
            logger.fine("skill bug?");
          }
        }
      }
      String itemtype = aAnswers.getProperty("itemtype");
      if (itemtype != null)
      {
        String quantity = aAnswers.getProperty("quantity");
        int qty = 0;
        try
        {
          qty = Integer.parseInt(quantity);
        }
        catch (NumberFormatException nfs)
        {
          qty = 0;
        }
        if (qty < 0) {
          qty = 0;
        }
        String materialType = aAnswers.getProperty("materialtype");
        byte matType = -1;
        try
        {
          matType = Byte.parseByte(materialType);
        }
        catch (NumberFormatException nfs)
        {
          matType = -1;
        }
        if (matType < 0) {
          matType = -1;
        }
        if (matType > 0)
        {
          matType = (byte)(matType - 1);
          if (matType < MethodsItems.getAllNormalWoodTypes().length)
          {
            matType = MethodsItems.getAllNormalWoodTypes()[matType];
          }
          else
          {
            matType = (byte)(matType - MethodsItems.getAllNormalWoodTypes().length);
            if (matType > MethodsItems.getAllMetalTypes().length) {
              matType = -1;
            } else {
              matType = MethodsItems.getAllMetalTypes()[matType];
            }
          }
        }
        else
        {
          matType = -1;
        }
        String qualityLevel = aAnswers.getProperty("qualitylevel");
        if (qualityLevel != null) {
          try
          {
            int ql = Integer.parseInt(qualityLevel);
            if (ql > 0)
            {
              ql = Math.min(ql, 90);
              try
              {
                int num = Integer.parseInt(itemtype);
                if (num == 0) {
                  return;
                }
                num -= 1;
                if ((num <= 6) && 
                  (!checkIfMayCreateArmour())) {
                  getResponder().getCommunicator().sendNormalServerMessage("You may only create items every 5 minutes in order to save the database.");
                }
                switch (num)
                {
                case 0: 
                  createAndInsertItems(getResponder(), 109, 114, ql, false, matType);
                  
                  createAndInsertItems(getResponder(), 109, 109, ql, false, matType);
                  
                  createAndInsertItems(getResponder(), 114, 114, ql, false, matType);
                  
                  createAndInsertItems(getResponder(), 111, 111, ql, false, matType);
                  
                  createAndInsertItems(getResponder(), 779, 779, ql, false, matType);
                  break;
                case 1: 
                  createAndInsertItems(getResponder(), 103, 108, ql, false, matType);
                  
                  createAndInsertItems(getResponder(), 103, 103, ql, false, matType);
                  
                  createAndInsertItems(getResponder(), 105, 105, ql, false, matType);
                  
                  createAndInsertItems(getResponder(), 106, 106, ql, false, matType);
                  
                  break;
                case 2: 
                  createAndInsertItems(getResponder(), 115, 120, ql, false, matType);
                  
                  createAndInsertItems(getResponder(), 119, 119, ql, false, matType);
                  
                  createAndInsertItems(getResponder(), 116, 116, ql, false, matType);
                  
                  createAndInsertItems(getResponder(), 115, 115, ql, false, matType);
                  
                  break;
                case 3: 
                  createAndInsertItems(getResponder(), 274, 279, ql, false, matType);
                  createAndInsertItems(getResponder(), 278, 278, ql, false, matType);
                  
                  createAndInsertItems(getResponder(), 274, 274, ql, false, matType);
                  createAndInsertItems(getResponder(), 277, 277, ql, false, matType);
                  
                  break;
                case 4: 
                  createAndInsertItems(getResponder(), 280, 287, ql, false, matType);
                  createAndInsertItems(getResponder(), 284, 284, ql, false, matType);
                  
                  createAndInsertItems(getResponder(), 280, 280, ql, false, matType);
                  createAndInsertItems(getResponder(), 283, 283, ql, false, matType);
                  
                  break;
                case 5: 
                  int drakeColor = getRandomDragonColor();
                  createAndInsertItems(getResponder(), 468, 473, ql, drakeColor, false);
                  
                  createAndInsertItems(getResponder(), 472, 472, ql, drakeColor, false);
                  
                  createAndInsertItems(getResponder(), 469, 469, ql, drakeColor, false);
                  
                  createAndInsertItems(getResponder(), 468, 468, ql, drakeColor, false);
                  
                  break;
                case 6: 
                  int scaleColor = getRandomDragonColor();
                  createAndInsertItems(getResponder(), 474, 478, ql, scaleColor, false);
                  
                  createAndInsertItems(getResponder(), 478, 478, ql, scaleColor, false);
                  
                  createAndInsertItems(getResponder(), 474, 474, ql, scaleColor, false);
                  
                  createAndInsertItems(getResponder(), 477, 477, ql, scaleColor, false);
                  
                  break;
                case 7: 
                  createAndInsertItems(getResponder(), 80, 80, ql, false, matType);
                  
                  break;
                case 8: 
                  createAndInsertItems(getResponder(), 21, 21, ql, false, matType);
                  break;
                case 9: 
                  createAndInsertItems(getResponder(), 81, 81, ql, false, matType);
                  
                  break;
                case 10: 
                  createAndInsertItems(getResponder(), 291, 291, ql, false, matType);
                  break;
                case 11: 
                  createAndInsertItems(getResponder(), 292, 292, ql, false, matType);
                  
                  break;
                case 12: 
                  createAndInsertItems(getResponder(), 290, 290, ql, false, matType);
                  break;
                case 13: 
                  createAndInsertItems(getResponder(), 3, 3, ql, false, matType);
                  break;
                case 14: 
                  createAndInsertItems(getResponder(), 90, 90, ql, false, matType);
                  break;
                case 15: 
                  createAndInsertItems(getResponder(), 87, 87, ql, false, matType);
                  break;
                case 16: 
                  createAndInsertItems(getResponder(), 706, 706, ql, false, matType);
                  break;
                case 17: 
                  createAndInsertItems(getResponder(), 705, 705, ql, false, matType);
                  break;
                case 18: 
                  createAndInsertItems(getResponder(), 707, 707, ql, false, matType);
                  
                  break;
                case 19: 
                  createAndInsertItems(getResponder(), 86, 86, ql, false, matType);
                  
                  break;
                case 20: 
                  createAndInsertItems(getResponder(), 4, 4, ql, false, matType);
                  
                  break;
                case 21: 
                  createAndInsertItems(getResponder(), 85, 85, ql, false, matType);
                  
                  break;
                case 22: 
                  createAndInsertItems(getResponder(), 82, 82, ql, false, matType);
                  
                  break;
                case 23: 
                  createMultiple(getResponder(), 25, 1, ql, matType);
                  createMultiple(getResponder(), 20, 1, ql, matType);
                  createMultiple(getResponder(), 24, 1, ql, matType);
                  createMultiple(getResponder(), 480, 1, ql, matType);
                  createMultiple(getResponder(), 8, 1, ql, matType);
                  createMultiple(getResponder(), 143, 1, ql, matType);
                  createMultiple(getResponder(), 7, 1, ql, matType);
                  createMultiple(getResponder(), 62, 1, ql, matType);
                  createMultiple(getResponder(), 63, 1, ql, matType);
                  createMultiple(getResponder(), 493, 1, ql, matType);
                  createMultiple(getResponder(), 97, 1, ql, matType);
                  createMultiple(getResponder(), 313, 1, ql, matType);
                  createMultiple(getResponder(), 296, 1, ql, matType);
                  createMultiple(getResponder(), 388, 1, ql, matType);
                  createMultiple(getResponder(), 421, 1, ql, matType);
                  break;
                case 24: 
                  ItemTemplate[] itemtemps = ItemTemplateFactory.getInstance().getTemplates();
                  for (ItemTemplate temp : itemtemps) {
                    if ((temp.isCombine()) && 
                      (!temp.isFood()) && 
                      (temp.getTemplateId() != 683) && 
                      (temp.getTemplateId() != 737) && 
                      ((temp.getDecayTime() == 86401L) || 
                      (temp.getDecayTime() == 28800L) || (temp.destroyOnDecay)) && 
                      (!temp.getModelName().startsWith("model.resource.scrap."))) {
                      for (int x = 0; x < 5; x++) {
                        createAndInsertItems(getResponder(), temp.getTemplateId(), temp
                          .getTemplateId(), 1 + Server.rand
                          .nextInt(ql), 0, true, false, (byte)-1);
                      }
                    }
                  }
                  break;
                case 25: 
                  createOnGround(getResponder(), 132, qty == 0 ? 10 : qty, ql, matType);
                  break;
                case 26: 
                  createOnGround(getResponder(), 492, qty == 0 ? 10 : qty, ql, matType);
                  break;
                case 27: 
                  createOnGround(getResponder(), 146, qty == 0 ? 10 : qty, ql, matType);
                  break;
                case 28: 
                  createOnGround(getResponder(), 860, qty == 0 ? 4 : qty, ql, matType);
                  break;
                case 29: 
                  createOnGround(getResponder(), 188, qty == 0 ? 10 : qty, ql, matType);
                  break;
                case 30: 
                  createOnGround(getResponder(), 217, qty == 0 ? 10 : qty, ql, matType);
                  break;
                case 31: 
                  createOnGround(getResponder(), 218, qty == 0 ? 10 : qty, ql, matType);
                  break;
                case 32: 
                  createOnGround(getResponder(), 22, qty == 0 ? 10 : qty, ql, matType);
                  break;
                case 33: 
                  createOnGround(getResponder(), 23, qty == 0 ? 10 : qty, ql, matType);
                  break;
                case 34: 
                  createOnGround(getResponder(), 9, qty == 0 ? 4 : qty, ql, matType);
                  break;
                case 35: 
                  createOnGround(getResponder(), 557, qty == 0 ? 4 : qty, ql, matType);
                  break;
                case 36: 
                  createOnGround(getResponder(), 558, qty == 0 ? 4 : qty, ql, matType);
                  break;
                case 37: 
                  createOnGround(getResponder(), 559, qty == 0 ? 4 : qty, ql, matType);
                  break;
                case 38: 
                  createOnGround(getResponder(), 319, qty == 0 ? 4 : qty, ql, matType);
                  break;
                case 39: 
                  createOnGround(getResponder(), 786, qty == 0 ? 10 : qty, ql, matType);
                  break;
                case 40: 
                  createOnGround(getResponder(), 785, qty == 0 ? 10 : qty, ql, matType);
                  break;
                case 41: 
                  createOnGround(getResponder(), 26, qty == 0 ? 10 : qty, ql, matType);
                  break;
                case 42: 
                  createOnGround(getResponder(), 130, qty == 0 ? 10 : qty, ql, matType);
                  break;
                case 43: 
                  createMultiple(getResponder(), 903, 1, ql, matType);
                  createMultiple(getResponder(), 901, 1, ql, matType);
                  break;
                case 44: 
                  createMultiple(getResponder(), 711, 1, ql, matType);
                  createMultiple(getResponder(), 213, 4, ql, matType);
                  createMultiple(getResponder(), 439, 8, ql, matType);
                  break;
                case 45: 
                  createMultiple(getResponder(), 221, 5, ql, matType);
                  createMultiple(getResponder(), 223, 5, ql, matType);
                  createMultiple(getResponder(), 480, 1, ql, matType);
                  createMultiple(getResponder(), 23, 3, ql, matType);
                  createMultiple(getResponder(), 64, 1, ql, matType);
                  break;
                case 46: 
                  if ((matType != 8) || (matType != 7)) {
                    matType = 8;
                  }
                  createMultiple(getResponder(), 505, 1, ql, matType);
                  createMultiple(getResponder(), 507, 1, ql, matType);
                  createMultiple(getResponder(), 508, 1, ql, matType);
                  createMultiple(getResponder(), 506, 1, ql, matType);
                  break;
                case 47: 
                  createMultiple(getResponder(), 376, 1, ql, matType);
                  createMultiple(getResponder(), 374, 1, ql, matType);
                  createMultiple(getResponder(), 380, 1, ql, matType);
                  createMultiple(getResponder(), 382, 1, ql, matType);
                  createMultiple(getResponder(), 378, 1, ql, matType);
                }
              }
              catch (NumberFormatException nfs)
              {
                getResponder().getCommunicator().sendNormalServerMessage("Error: input was " + itemtype + " - failed to parse.");
              }
            }
            getResponder().getCommunicator().sendNormalServerMessage("No quality level selected so not creating.");
          }
          catch (NumberFormatException nfs)
          {
            getResponder().getCommunicator().sendNormalServerMessage("Error: input was " + itemtype + " - failed to parse.");
          }
        }
      }
    }
  }
  
  private void createOnGround(Creature receiver, int itemTemplate, int howMany, float qualityLevel, byte materialType)
  {
    for (int x = 0; x < howMany; x++) {
      createAndInsertItems(receiver, itemTemplate, itemTemplate, qualityLevel, false, materialType);
    }
  }
  
  private void createMultiple(Creature receiver, int itemTemplate, int howMany, float qualityLevel, byte materialType)
  {
    for (int x = 0; x < howMany; x++) {
      createAndInsertItems(receiver, itemTemplate, itemTemplate, qualityLevel, false, materialType);
    }
  }
  
  public static final void createAndInsertItems(Creature receiver, int itemStart, int itemEnd, float qualityLevel, boolean newbieItem, byte materialType)
  {
    createAndInsertItems(receiver, itemStart, itemEnd, qualityLevel, 0, false, newbieItem, materialType);
  }
  
  public static final void createAndInsertItems(Creature receiver, int itemStart, int itemEnd, float qualityLevel, int color, boolean newbieItem)
  {
    createAndInsertItems(receiver, itemStart, itemEnd, qualityLevel, color, false, newbieItem, (byte)-1);
  }
  
  private static final void createAndInsertItems(Creature receiver, int itemStart, int itemEnd, float qualityLevel, int color, boolean onGround, boolean newbieItem, byte material)
  {
    if (itemStart > itemEnd)
    {
      receiver.getCommunicator().sendNormalServerMessage("Error: Bugged test case.");
      return;
    }
    for (int x = itemStart; x <= itemEnd; x++) {
      if (x != 110) {
        if (onGround) {
          try
          {
            ItemFactory.createItem(x, qualityLevel, receiver.getPosX(), receiver
              .getPosY(), Server.rand
              .nextFloat() * 180.0F, receiver.isOnSurface(), (byte)0, -10L, receiver.getName());
          }
          catch (Exception ex)
          {
            receiver.getCommunicator().sendAlertServerMessage(ex.getMessage());
          }
        } else {
          try
          {
            Item i = ItemFactory.createItem(x, qualityLevel, receiver.getName());
            if (newbieItem) {
              i.setAuxData((byte)1);
            }
            if (i.isGem())
            {
              i.setData1(qualityLevel <= 0.0F ? 0 : (int)(qualityLevel * 2.0F));
              i.setDescription("v");
            }
            if (i.isDragonArmour())
            {
              i.setMaterial((byte)16);
              i.setColor(color);
              String dName = i.getDragonColorNameByColor(color);
              if (dName != "") {
                i.setName(dName + " " + i.getName());
              }
            }
            if (material != -1) {
              i.setMaterial(material);
            }
            receiver.getInventory().insertItem(i);
          }
          catch (Exception ex)
          {
            receiver.getCommunicator().sendAlertServerMessage(ex.getMessage());
          }
        }
      }
    }
    receiver.wearItems();
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder(getBmlHeader());
    
    buf.append("text{text='Create an armour set or a weapon and set skills:'}");
    buf.append("harray{label{text='Item'}dropdown{id='itemtype';options=\"");
    buf.append("Nothing,");
    buf.append("Cloth,");
    buf.append("Leather,");
    buf.append("Studded,");
    buf.append("Chain,");
    buf.append("Plate,");
    buf.append("Drake (random color),");
    buf.append("Dragon Scale (random color),");
    buf.append("Shortsword,");
    buf.append("Longsword,");
    buf.append("Twohanded sword,");
    buf.append("Small maul,");
    buf.append("Med maul,");
    buf.append("Large maul,");
    buf.append("Small axe,");
    buf.append("Large axe,");
    buf.append("Twohanded axe,");
    buf.append("Halberd,");
    buf.append("Long spear,");
    buf.append("Steel spear,");
    buf.append("Large Metal Shield,");
    buf.append("Medium Metal Shield,");
    buf.append("Large Wooden Shield,");
    buf.append("Small Wooden Shield,");
    buf.append("Basic Tools,");
    buf.append("Raw materials,");
    buf.append("#10 Stone Bricks,");
    buf.append("#10 Mortar,");
    buf.append("#10 Rock Shards,");
    buf.append("#4 Wood Beams,");
    buf.append("#10 Iron Ribbons,");
    buf.append("#10 Large Nails,");
    buf.append("#10 Small Nails,");
    buf.append("#10 Planks,");
    buf.append("#10 Shafts,");
    buf.append("#4 Logs,");
    buf.append("#4 Thick Ropes,");
    buf.append("#4 Mooring Ropes,");
    buf.append("#4 Cordage Ropes,");
    buf.append("#4 Normal Ropes,");
    buf.append("#10 Marble Bricks,");
    buf.append("#10 Marble Shards,");
    buf.append("#10 Dirt,");
    buf.append("#10 Clay,");
    buf.append("Bridge Tools,");
    buf.append("Make your own RangePole,");
    buf.append("Make your own Dioptra,");
    buf.append("Statuette Set,");
    buf.append("Vesseled Gems Set,");
    buf.append("\";default=\"0\"}}");
    
    buf.append("text{text='Select material:'}");
    buf.append("harray{label{text='Material'}dropdown{id='materialtype';options=\"");
    buf.append("Standard.,");
    for (byte material : MethodsItems.getAllNormalWoodTypes()) {
      buf.append(StringUtilities.raiseFirstLetter(Item.getMaterialString(material)) + ",");
    }
    for (byte material : MethodsItems.getAllMetalTypes()) {
      buf.append(StringUtilities.raiseFirstLetter(Item.getMaterialString(material)) + ",");
    }
    buf.append("\";default=\"0\"}}");
    
    buf.append("harray{label{text='Item qualitylevel (Max 90)'};input{maxchars='2'; id='qualitylevel'; text='50'}}");
    buf.append("harray{label{text='Set skills to (Max 90, 0=no change)'};input{maxchars='2'; id='skillLevel'; text='0'}}");
    buf.append("harray{label{text='Set characteristics to (Max 90, 0=no change)'};input{maxchars='2'; id='characteristicsLevel'; text='0'}}");
    buf.append("harray{label{text='Set Alignment to (Max 99, Min -99, 0=no change)'};input{maxchars='3'; id='alignmentLevel'; text='0'}}");
    buf.append("harray{label{text='Item quantity (0..99, 0 = use default)'};input{maxchars='2'; id='quantity'; text='0'}}");
    buf.append("text{text='Quantity is only used for items with a # before their name, if 0 then the default number after the # is used.'};");
    buf.append("text{text='Set Deity:'}");
    buf.append("harray{label{text='Deity'}dropdown{id='priestType';options=\"");
    buf.append("No Change,");
    buf.append("No Deity,");
    for (Deity d : Deities.getDeities()) {
      buf.append(d.getName() + ",");
    }
    buf.append("\";default=\"0\"}}");
    
    buf.append("harray{label{text='Faith (Max 100, 0=no change)'};input{maxchars='3'; id='faithLevel'; text='0'}}");
    
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  final int getRandomDragonColor()
  {
    int c = Server.rand.nextInt(5);
    switch (c)
    {
    case 0: 
      return WurmColor.createColor(215, 40, 40);
    case 1: 
      return WurmColor.createColor(10, 10, 10);
    case 2: 
      return WurmColor.createColor(10, 210, 10);
    case 3: 
      return WurmColor.createColor(255, 255, 255);
    case 4: 
      return WurmColor.createColor(40, 40, 215);
    }
    return WurmColor.createColor(100, 100, 100);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\TestQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */