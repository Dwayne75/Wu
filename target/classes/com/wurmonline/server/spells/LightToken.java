package com.wurmonline.server.spells;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.skills.Skill;

public class LightToken
  extends ReligiousSpell
{
  public static final int RANGE = 4;
  
  public LightToken()
  {
    super("Light Token", 421, 10, 5, 10, 20, 0L);
    this.targetItem = true;
    this.targetTile = true;
    this.targetCreature = true;
    this.description = "creates a bright light item";
    this.type = 0;
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, int tilex, int tiley, int layer, int heightOffset)
  {
    createToken(performer, power);
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Item target)
  {
    createToken(performer, power);
  }
  
  void doEffect(Skill castSkill, double power, Creature performer, Creature target)
  {
    createToken(performer, power);
  }
  
  void createToken(Creature performer, double power)
  {
    try
    {
      Item token = ItemFactory.createItem(649, (float)Math.max(50.0D, power), performer.getName());
      performer.getInventory().insertItem(token);
      performer.getCommunicator().sendNormalServerMessage("Something starts shining in your pocket.", (byte)2);
    }
    catch (NoSuchTemplateException|FailedException localNoSuchTemplateException) {}
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\spells\LightToken.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */