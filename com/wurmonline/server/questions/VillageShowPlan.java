package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.villages.Village;
import java.util.Properties;

public class VillageShowPlan
  extends Question
{
  private final Village deed;
  
  public VillageShowPlan(Creature aResponder, Village tokenVill)
  {
    super(aResponder, "Plan of " + tokenVill.getName(), "", 125, tokenVill.getId());
    this.deed = tokenVill;
  }
  
  public void answer(Properties aAnswers) {}
  
  public void sendQuestion()
  {
    int perimTiles = this.deed.getTotalPerimeterSize();
    getResponder().getCommunicator().sendShowDeedPlan(getId(), this.deed.getName(), this.deed
      .getTokenX(), this.deed.getTokenY(), this.deed
      .getStartX(), this.deed.getStartY(), this.deed
      .getEndX(), this.deed.getEndY(), perimTiles);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\VillageShowPlan.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */