package com.wurmonline.server.items;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.behaviours.CargoTransportationMethods;
import com.wurmonline.server.behaviours.CreatureBehaviour;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.DbCreatureStatus;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

class ChickenCoops
  implements TimeConstants, MiscConstants
{
  private static final Logger logger = Logger.getLogger(ChickenCoops.class.getName());
  private static int cretCount;
  
  static void poll(Item theItem)
  {
    getCreatureCountAndContinue(theItem);
  }
  
  private static void getCreatureCountAndContinue(Item theItem)
  {
    if (theItem.getTemplateId() == 1436) {
      try
      {
        long delay = System.currentTimeMillis() - 3600000L;
        if (delay > theItem.getParent().getData())
        {
          if (theItem.getParent().getDamage() >= 80.0F) {
            for (Item item : theItem.getParent().getAllItems(true)) {
              if (item.getTemplateId() == 1436) {
                for (Item chickens : item.getAllItems(true)) {
                  unload(chickens);
                }
              }
            }
          }
          cretCount = theItem.getAllItems(true).length;
          if (cretCount > 0) {
            for (Item item : theItem.getParent().getAllItems(true))
            {
              pollFeeder(item);
              pollDrinker(item);
              eggPoller(item);
            }
          }
          theItem.getParent().setData(System.currentTimeMillis());
        }
      }
      catch (NoSuchItemException ex)
      {
        logger.log(Level.WARNING, ex.getMessage(), ex);
      }
    }
  }
  
  private static void pollFeeder(Item theItem)
  {
    if (theItem.getTemplateId() == 1434)
    {
      long delay = System.currentTimeMillis() - 14400000L;
      if (delay > theItem.getData())
      {
        if ((theItem.isEmpty(true)) || (theItem.getAllItems(true).length < cretCount))
        {
          try
          {
            for (Item item : theItem.getParent().getAllItems(true)) {
              if (item.getTemplateId() == 1436) {
                for (Item chickens : item.getAllItems(true)) {
                  unload(chickens);
                }
              }
            }
          }
          catch (NoSuchItemException ex)
          {
            logger.log(Level.WARNING, ex.getMessage(), ex);
          }
        }
        else
        {
          Item[] foodEaten = theItem.getAllItems(true);
          for (int x = 0; x < cretCount; x++) {
            Items.destroyItem(foodEaten[x].getWurmId());
          }
        }
        theItem.setData(System.currentTimeMillis());
      }
    }
  }
  
  private static void pollDrinker(Item theItem)
  {
    if (theItem.getTemplateId() == 1435)
    {
      long delay = System.currentTimeMillis() - 14400000L;
      if (delay > theItem.getData())
      {
        int cretKG = cretCount * 250;
        for (Item water : theItem.getAllItems(true)) {
          if ((theItem.isEmpty(true)) || (water.getWeightGrams() < cretKG)) {
            try
            {
              for (Item item : theItem.getParent().getAllItems(true)) {
                if (item.getTemplateId() == 1436) {
                  for (Item chickens : item.getAllItems(true)) {
                    unload(chickens);
                  }
                }
              }
            }
            catch (NoSuchItemException ex)
            {
              logger.log(Level.WARNING, ex.getMessage(), ex);
            }
          } else {
            water.setWeight(water.getWeightGrams() - cretKG, true);
          }
        }
        theItem.setData(System.currentTimeMillis());
      }
    }
  }
  
  private static void eggPoller(Item theItem)
  {
    if (theItem.getTemplateId() == 1433)
    {
      long delay = System.currentTimeMillis() - 43200000L;
      if (delay > theItem.getData())
      {
        try
        {
          for (int x = 1; x <= cretCount; x++) {
            if (theItem.getAllItems(true).length < 100)
            {
              Item egg = ItemFactory.createItem(464, theItem.getQualityLevel(), null);
              theItem.insertItem(egg);
              if (Server.rand.nextInt(20) == 0)
              {
                egg.setData1(48);
                
                egg.setName("fertile egg");
              }
            }
            else
            {
              Item[] overflow = theItem.getAllItems(true);
              for (int y = 1; y <= overflow.length - 100; y++) {
                Items.destroyItem(overflow[y].getWurmId());
              }
            }
          }
        }
        catch (FailedException|NoSuchTemplateException ex)
        {
          logger.log(Level.WARNING, ex.getMessage(), ex);
        }
        theItem.setData(System.currentTimeMillis());
      }
    }
  }
  
  private static void unload(Item theItem)
  {
    try
    {
      Item parent = theItem.getParent();
      Creature creature = Creatures.getInstance().getCreature(theItem.getData());
      int layer;
      int layer;
      if (parent.isOnSurface()) {
        layer = 0;
      } else {
        layer = -1;
      }
      Creatures cstat = Creatures.getInstance();
      creature.getStatus().setDead(false);
      cstat.removeCreature(creature);
      cstat.addCreature(creature, false);
      creature.putInWorld();
      
      float px = ((int)parent.getParent().getPosX() >> 2) * 4 + 2;
      float py = ((int)parent.getParent().getPosY() >> 2) * 4 + 2;
      CreatureBehaviour.blinkTo(creature, px, py, layer, parent
        .getPosZ(), parent.getBridgeId(), parent.getFloorLevel());
      
      Item coop = parent.getParent();
      
      DbCreatureStatus.setLoaded(0, creature.getWurmId());
      Items.destroyItem(theItem.getWurmId());
      CargoTransportationMethods.updateItemModel(coop);
    }
    catch (IOException|NoSuchItemException|NoSuchCreatureException ex)
    {
      logger.log(Level.WARNING, ex.getMessage(), ex);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\items\ChickenCoops.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */