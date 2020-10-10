package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Creature;
import java.util.Properties;
import java.util.logging.Logger;

public class GMForceSpawnRiftLootQuestion
  extends Question
{
  private static final Logger logger = Logger.getLogger(GMForceSpawnRiftLootQuestion.class.getName());
  
  public GMForceSpawnRiftLootQuestion(Creature aResponder)
  {
    super(aResponder, "Spawn Rift Loot", "Which item would you like to spawn?", 144, aResponder.getWurmId());
  }
  
  public void answer(Properties aAnswer)
  {
    setAnswer(aAnswer);
  }
  
  public void sendQuestion() {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\GMForceSpawnRiftLootQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */