package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.kingdom.Appointment;
import com.wurmonline.server.kingdom.Kingdoms;
import java.util.Properties;

public final class EconomicAdvisorInfo
  extends Question
{
  public EconomicAdvisorInfo(Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 74, aTarget);
  }
  
  public void answer(Properties aAnswers) {}
  
  public void sendQuestion()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(getBmlHeader());
    Appointment a = Appointment.getAppointment(1505, getResponder().getKingdomId());
    
    String nam = "Economic advisor";
    if (a != null) {
      nam = a.getNameForGender(getResponder().getSex());
    }
    sb.append("text{type='italic';text=\"" + a + " confidential information.\"}");
    sb.append("text{text=\"  Economic statement for " + Kingdoms.getNameFor(getResponder().getKingdomId()) + ".\"}");
    long sum = 0L;
    StringBuilder sb2 = new StringBuilder();
    Creature[] crets = Creatures.getInstance().getCreatures();
    for (int x = 0; x < crets.length; x++) {
      if ((crets[x].isTrader()) && (crets[x].getKingdomId() == getResponder().getKingdomId())) {
        if (crets[x].isNpcTrader())
        {
          Shop shop = Economy.getEconomy().getShop(crets[x]);
          if (!shop.isPersonal())
          {
            if (shop.getMoney() >= 0L) {
              sb2.append("text{text=\"  Trader - " + new Change(shop.getMoney()).getChangeShortString() + ". Ratio=" + shop
                .getSellRatio() + "\"}");
            } else {
              sb2.append("text{text=\"  Trader - " + shop.getMoney() + " irons. Ratio=" + shop.getSellRatio() + "\"}");
            }
            sum += shop.getMoney();
          }
        }
      }
    }
    Shop kingshop = Economy.getEconomy().getKingsShop();
    sb.append("text{text=\"  Kings coffers: " + new Change(kingshop.getMoney()).getChangeString() + " (" + kingshop
      .getMoney() + " irons).\"}");
    sb.append("text{text=\"  Total money at traders: " + new Change(sum).getChangeString() + ".\"}");
    sb.append("text{text=\"\"}");
    sb.append("text{type='bold';text=\"Trader breakdown:\"}");
    sb.append(sb2.toString());
    sb.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(300, 300, true, true, sb.toString(), 200, 200, 200, this.title);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\EconomicAdvisorInfo.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */