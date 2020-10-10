package com.wurmonline.server.questions;

import com.wurmonline.server.WurmHarvestables;
import com.wurmonline.server.WurmHarvestables.Harvestable;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import java.util.Properties;

public final class GMSelectHarvestable
  extends Question
{
  private WurmHarvestables.Harvestable[] harvestables = null;
  private Item paper;
  
  public GMSelectHarvestable(Creature aResponder, Item apaper)
  {
    super(aResponder, "Select Harvestabke", "Select Harvestabke", 140, -10L);
    this.paper = apaper;
  }
  
  public void answer(Properties answers)
  {
    setAnswer(answers);
    String sel = answers.getProperty("harvestable");
    int selId = Integer.parseInt(sel);
    WurmHarvestables.Harvestable harvestable = this.harvestables[selId];
    
    this.paper.setAuxData((byte)(harvestable.getHarvestableId() + 8));
    
    this.paper.setData1(99);
    
    this.paper.setInscription(harvestable.getName() + " report", getResponder().getName(), 0);
    this.paper.setName(harvestable.getName() + " report", true);
    getResponder().getCommunicator().sendNormalServerMessage("You carefully finish writing the " + harvestable
      .getName() + " report and sign it.");
  }
  
  public void sendQuestion()
  {
    this.harvestables = WurmHarvestables.getHarvestables();
    
    StringBuilder buf = new StringBuilder(getBmlHeader());
    buf.append("harray{label{text=\"Harvestable\"};");
    buf.append("dropdown{id=\"harvestable\";default=\"0\";options=\"");
    for (int i = 0; i < this.harvestables.length; i++)
    {
      if (i > 0) {
        buf.append(",");
      }
      WurmHarvestables.Harvestable harvestable = this.harvestables[i];
      buf.append(harvestable.getName().replace(",", "") + " (" + harvestable.getHarvestableId() + ")");
    }
    buf.append("\"}}");
    buf.append("label{text=\"\"}");
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(300, 120, true, true, buf.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\GMSelectHarvestable.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */