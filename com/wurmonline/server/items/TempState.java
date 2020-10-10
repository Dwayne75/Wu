package com.wurmonline.server.items;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TempState
  implements ItemTypes
{
  private final int origItemTemplateId;
  private final int newItemTemplateId;
  private final short temperatureChangeLevel;
  private final boolean atIncrease;
  private static final Logger logger = Logger.getLogger(TempState.class.getName());
  private final boolean keepWeight;
  private final boolean keepMaterial;
  
  public TempState(int aOrigItemTemplateId, int aNewItemTemplateId, short aTemperatureChangeLevel, boolean aAtIncrease, boolean aKeepWeight, boolean aKeepMaterial)
  {
    this.origItemTemplateId = aOrigItemTemplateId;
    this.newItemTemplateId = aNewItemTemplateId;
    this.temperatureChangeLevel = aTemperatureChangeLevel;
    this.atIncrease = aAtIncrease;
    this.keepWeight = aKeepWeight;
    this.keepMaterial = aKeepMaterial;
  }
  
  public boolean changeItem(Item parent, Item item, short oldTemp, short newTemp, float qualityRatio)
  {
    int itemPrimarySkill = -10;
    if (passedLevel(oldTemp, newTemp))
    {
      if (newTemp >= this.temperatureChangeLevel)
      {
        if (this.atIncrease)
        {
          try
          {
            Item newItem = null;
            Creature performer = null;
            if (this.keepMaterial) {
              newItem = ItemFactory.createItem(this.newItemTemplateId, item.getCurrentQualityLevel() * qualityRatio, item
                .getMaterial(), item.getRarity(), item.creator);
            } else {
              newItem = ItemFactory.createItem(this.newItemTemplateId, item.getCurrentQualityLevel() * qualityRatio, (byte)0, item
                .getRarity(), item.creator);
            }
            newItem.setDescription(item.getDescription());
            Set<Item> items = item.getItems();
            if (items != null)
            {
              Item[] itarr = (Item[])items.toArray(new Item[items.size()]);
              for (int x = 0; x < itarr.length; x++) {
                try
                {
                  item.dropItem(itarr[x].getWurmId(), false);
                  newItem.insertItem(itarr[x], true);
                }
                catch (NoSuchItemException nsi)
                {
                  logger.log(Level.WARNING, nsi.getMessage(), nsi);
                }
              }
            }
            if (item.isPassFullData()) {
              newItem.setData(item.getData());
            }
            newItem.setLastOwnerId(item.getLastOwnerId());
            if (newItem.hasPrimarySkill())
            {
              try
              {
                itemPrimarySkill = newItem.getPrimarySkill();
              }
              catch (NoSuchSkillException localNoSuchSkillException) {}
              try
              {
                performer = Server.getInstance().getCreature(newItem.getLastOwnerId());
              }
              catch (Exception localException) {}
            }
            Items.destroyItem(item.getWurmId());
            if (this.keepWeight)
            {
              newItem.setWeight(item.getWeightGrams(), false);
            }
            else
            {
              int currweight = item.getWeightGrams();
              float mod = currweight / item.getTemplate().getWeightGrams();
              if (item.getTemplateId() == 684) {
                mod *= 0.8F;
              }
              int newWeight = (int)(newItem.getTemplate().getWeightGrams() * mod);
              newItem.setWeight(newWeight, false);
            }
            if (newItem.getWeightGrams() > 0)
            {
              newItem.setTemperature(newTemp);
              if (!parent.insertItem(newItem, true))
              {
                logger.log(Level.WARNING, parent.getName() + " failed to insert item " + newItem.getName());
                if (newItem.getWeightGrams() > parent.getFreeVolume())
                {
                  logger.log(Level.INFO, "Old weight=" + newItem.getWeightGrams() + ", trying to set weight to " + parent
                    .getFreeVolume());
                  newItem.setWeight(parent.getFreeVolume(), true);
                  if (parent.insertItem(newItem)) {
                    logger.log(Level.INFO, "THAT did the trick:)");
                  } else {
                    logger.log(Level.INFO, "Didn't help.");
                  }
                }
                else
                {
                  logger.log(Level.INFO, newItem.getName() + ": old weight=" + newItem.getWeightGrams() + ", larger than " + parent
                    .getFreeVolume() + " have to change sizes from " + newItem
                    .getSizeX() + ", " + newItem.getSizeY() + ", " + newItem.getSizeZ() + ".");
                  for (int x = 0; x < 10; x++) {
                    if (!newItem.depleteSizeWith(Math.max(1, newItem.getSizeX() / 10), 
                      Math.max(1, newItem.getSizeY() / 10), Math.max(1, newItem.getSizeZ() / 10)))
                    {
                      if (parent.insertItem(newItem))
                      {
                        logger.log(Level.INFO, "Managed to insert item with size " + newItem.getSizeX() + ", " + newItem
                          .getSizeY() + ", " + newItem.getSizeZ() + " after " + x + " iterations.");
                        
                        break;
                      }
                    }
                    else
                    {
                      logger.log(Level.INFO, "Item destroyed. Breaking out.");
                      break;
                    }
                  }
                }
              }
            }
            else
            {
              Items.decay(newItem.getWurmId(), newItem.getDbStrings());
            }
            giveSkillGainForTemplatePrimarySkill(performer, newItem, itemPrimarySkill);
          }
          catch (NoSuchTemplateException nst)
          {
            logger.log(Level.WARNING, nst.getMessage(), nst);
          }
          catch (FailedException fe)
          {
            logger.log(Level.WARNING, fe.getMessage(), fe);
          }
          return true;
        }
      }
      else if (newTemp <= this.temperatureChangeLevel) {
        if (!this.atIncrease)
        {
          try
          {
            Item newItem = null;
            if (this.keepMaterial) {
              newItem = ItemFactory.createItem(this.newItemTemplateId, item.getCurrentQualityLevel() * qualityRatio, item
                .getMaterial(), item.getRarity(), item.creator);
            } else {
              newItem = ItemFactory.createItem(this.newItemTemplateId, item.getCurrentQualityLevel() * qualityRatio, item.creator);
            }
            newItem.setLastOwnerId(item.getLastOwnerId());
            Items.destroyItem(item.getWurmId());
            newItem.setTemperature(newTemp);
            if (this.keepWeight) {
              newItem.setWeight(item.getWeightGrams(), false);
            }
            if (newItem.getWeightGrams() > 0) {
              parent.insertItem(newItem, true);
            } else {
              Items.decay(newItem.getWurmId(), newItem.getDbStrings());
            }
          }
          catch (NoSuchTemplateException nst)
          {
            logger.log(Level.WARNING, nst.getMessage(), nst);
          }
          catch (FailedException fe)
          {
            logger.log(Level.WARNING, fe.getMessage(), fe);
          }
          return true;
        }
      }
    }
    else if (item.isFood()) {
      if (newTemp > 2700) {
        item.setDamage(item.getDamage() + Math.max(0.1F, (newTemp - oldTemp) / 10.0F));
      }
    }
    return false;
  }
  
  boolean passedLevel(short oldTemp, short newTemp)
  {
    if ((oldTemp > this.temperatureChangeLevel) && (newTemp <= this.temperatureChangeLevel)) {
      return true;
    }
    if ((oldTemp < this.temperatureChangeLevel) && (newTemp >= this.temperatureChangeLevel)) {
      return true;
    }
    return false;
  }
  
  int getOrigItemTemplateId()
  {
    return this.origItemTemplateId;
  }
  
  int getNewItemTemplateId()
  {
    return this.newItemTemplateId;
  }
  
  short getTemperatureChangeLevel()
  {
    return this.temperatureChangeLevel;
  }
  
  boolean isAtIncrease()
  {
    return this.atIncrease;
  }
  
  boolean isKeepWeight()
  {
    return this.keepWeight;
  }
  
  boolean isKeepMaterial()
  {
    return this.keepMaterial;
  }
  
  public int hashCode()
  {
    int prime = 31;
    int result = 1;
    result = 31 * result + (this.atIncrease ? 1231 : 1237);
    result = 31 * result + (this.keepMaterial ? 1231 : 1237);
    result = 31 * result + (this.keepWeight ? 1231 : 1237);
    result = 31 * result + this.newItemTemplateId;
    result = 31 * result + this.origItemTemplateId;
    result = 31 * result + this.temperatureChangeLevel;
    return result;
  }
  
  public boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof TempState)) {
      return false;
    }
    TempState other = (TempState)obj;
    if ((this.atIncrease != other.atIncrease) || (this.keepMaterial != other.keepMaterial) || (this.keepWeight != other.keepWeight) || (this.newItemTemplateId != other.newItemTemplateId) || (this.origItemTemplateId != other.origItemTemplateId) || (this.temperatureChangeLevel != other.temperatureChangeLevel)) {
      return false;
    }
    return true;
  }
  
  public String toString()
  {
    return "TempState [atIncrease=" + this.atIncrease + ", keepMaterial=" + this.keepMaterial + ", keepWeight=" + this.keepWeight + ", newItemTemplateId=" + this.newItemTemplateId + ", origItemTemplateId=" + this.origItemTemplateId + ", temperatureChangeLevel=" + this.temperatureChangeLevel + "]";
  }
  
  private void giveSkillGainForTemplatePrimarySkill(Creature performer, Item newItem, int skillId)
  {
    if (performer == null) {
      return;
    }
    if (newItem == null) {
      return;
    }
    Skills skills = performer.getSkills();
    Skill skill;
    try
    {
      skill = skills.getSkill(skillId);
    }
    catch (NoSuchSkillException ss)
    {
      Skill skill;
      skill = skills.learn(skillId, 1.0F);
    }
    float diff = newItem.getTemplate().getDifficulty();
    if (skill != null) {
      skill.skillCheck(diff, newItem, 0.0D, false, 1.0F);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\items\TempState.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */