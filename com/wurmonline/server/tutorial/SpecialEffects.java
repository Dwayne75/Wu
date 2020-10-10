package com.wurmonline.server.tutorial;

import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.combat.CombatEngine;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;

public final class SpecialEffects
  implements CounterTypes, MiscConstants
{
  private final String name;
  private final int id;
  private byte requiredPower = 0;
  private static final int numEffects = 6;
  private static final SpecialEffects[] effects = new SpecialEffects[6];
  public static final int NO_EFFECT = 0;
  public static final int OPEN_DOOR = 1;
  public static final int HEAL = 2;
  public static final int WOUND = 3;
  public static final int DELETE_TILE_ITEMS = 4;
  public static final int SEND_PLONK = 5;
  
  static
  {
    effects[0] = new SpecialEffects(0, "Do nothing");
    effects[0].requiredPower = 0;
    effects[1] = new SpecialEffects(1, "Open door or gate");
    effects[1].requiredPower = 2;
    effects[2] = new SpecialEffects(2, "Heal all wounds");
    effects[2].requiredPower = 2;
    effects[3] = new SpecialEffects(3, "Create a wound");
    effects[3].requiredPower = 2;
    effects[4] = new SpecialEffects(4, "Delete items on tile");
    effects[4].requiredPower = 2;
    effects[5] = new SpecialEffects(5, "Send a notification");
    effects[5].requiredPower = 2;
  }
  
  private SpecialEffects(int _id, String _name)
  {
    this.id = _id;
    this.name = _name;
  }
  
  public void setPowerRequired(byte power)
  {
    this.requiredPower = power;
  }
  
  public byte getPowerRequired()
  {
    return this.requiredPower;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public int getId()
  {
    return this.id;
  }
  
  public static final SpecialEffects[] getEffects()
  {
    return effects;
  }
  
  public static final SpecialEffects getEffect(int number)
  {
    try
    {
      return effects[number];
    }
    catch (Exception ex) {}
    return null;
  }
  
  public boolean run(Creature performer, int tilex, int tiley, int layer)
  {
    boolean toReturn = false;
    Object localObject;
    Creature c;
    Creature c;
    switch (this.id)
    {
    case 0: 
      break;
    case 1: 
      break;
    case 2: 
      VolaTile t = Zones.getTileOrNull(tilex, tiley, layer >= 0);
      if (t != null)
      {
        Creature[] creatures = t.getCreatures();
        Creature[] arrayOfCreature1 = creatures;int i = arrayOfCreature1.length;
        for (localObject = 0; localObject < i; localObject++)
        {
          c = arrayOfCreature1[localObject];
          
          c.getBody().healFully();
        }
      }
      break;
    case 3: 
      VolaTile t12 = Zones.getTileOrNull(tilex, tiley, layer >= 0);
      if (t12 != null)
      {
        Creature[] creatures = t12.getCreatures();
        Creature[] arrayOfCreature2 = creatures;localObject = arrayOfCreature2.length;
        for (c = 0; c < localObject; c++)
        {
          c = arrayOfCreature2[c];
          
          CombatEngine.addWound(c, c, (byte)3, 13, 1000.0D, 1.0F, "bite", null, 0.0F, 0.0F, false, false, false, false);
        }
      }
      break;
    case 4: 
      VolaTile t22 = Zones.getTileOrNull(tilex, tiley, layer >= 0);
      if (t22 != null)
      {
        Item[] items = t22.getItems();
        localObject = items;c = localObject.length;
        for (c = 0; c < c; c++)
        {
          Item i = localObject[c];
          if (!i.isIndestructible()) {
            Items.destroyItem(i.getWurmId());
          }
        }
      }
      break;
    }
    return false;
  }
  
  public boolean run(Creature performer, long target)
  {
    boolean toReturn = false;
    switch (this.id)
    {
    case 0: 
      break;
    case 1: 
      break;
    case 2: 
      performer.getBody().healFully();
      break;
    case 3: 
      CombatEngine.addWound(performer, performer, (byte)3, 13, 1000.0D, 1.0F, "bite", null, 0.0F, 0.0F, false, false, false, false);
      
      break;
    }
    return false;
  }
  
  public boolean run(Creature performer, long target, int numbers)
  {
    boolean toReturn = false;
    switch (this.id)
    {
    case 0: 
      break;
    case 5: 
      performer.getCommunicator().sendPlonk((short)numbers);
      break;
    default: 
      return run(performer, target);
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\tutorial\SpecialEffects.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */