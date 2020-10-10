package com.wurmonline.server.behaviours;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.skills.Skill;
import java.util.LinkedList;
import java.util.List;

public class SkillBehaviour
  extends Behaviour
{
  public SkillBehaviour()
  {
    super((short)42);
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, Skill skill)
  {
    List<ActionEntry> toReturn = new LinkedList();
    
    return toReturn;
  }
  
  public List<ActionEntry> getBehavioursFor(Creature performer, Skill skill)
  {
    List<ActionEntry> toReturn = new LinkedList();
    return toReturn;
  }
  
  public boolean action(Action act, Creature performer, Item source, Skill skill, short action, float counter)
  {
    return action(act, performer, skill, action, counter);
  }
  
  public boolean action(Action act, Creature performer, Skill skill, short action, float counter)
  {
    if (action == 1) {
      performer.getCommunicator().sendNormalServerMessage("This is the skill " + skill
        .getName() + ". Use 'Find on Wurmpedia' to see an explanation.");
    }
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\behaviours\SkillBehaviour.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */