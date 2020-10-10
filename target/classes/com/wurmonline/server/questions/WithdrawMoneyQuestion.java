package com.wurmonline.server.questions;

import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import java.util.Properties;

public final class WithdrawMoneyQuestion
  extends Question
{
  public WithdrawMoneyQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget)
  {
    super(aResponder, aTitle, aQuestion, 36, aTarget);
  }
  
  public void answer(Properties answers)
  {
    setAnswer(answers);
    QuestionParser.parseWithdrawMoneyQuestion(this);
  }
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    fillDialogText(buf);
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  private void fillDialogText(StringBuilder buf)
  {
    long money = getResponder().getMoney();
    if ((!Server.getInstance().isPS()) && ((Servers.localServer.entryServer) || 
      (getResponder().getPower() > 0)) && (!Servers.localServer.testServer))
    {
      buf.append("text{text='You are not allowed to withdraw money on this server since it will be lost when you use a portal.'}");
      return;
    }
    if (money <= 0L)
    {
      buf.append("text{text='You have no money in the bank.'}");
      return;
    }
    Change change = Economy.getEconomy().getChangeFor(money);
    buf.append("text{text='You may withdraw up to " + change.getChangeString() + ".'}");
    buf.append("text{text='The money will end up in your inventory.'}");
    long gold = change.getGoldCoins();
    long silver = change.getSilverCoins();
    long copper = change.getCopperCoins();
    long iron = change.getIronCoins();
    if (money >= 1000000L) {
      buf.append("harray{input{text='0'; id='gold'; maxchars='10'}label{text='(" + gold + ") Gold coins'}}");
    }
    if (money >= 10000L) {
      buf.append("harray{input{text='0'; id='silver'; maxchars='10'}label{text='(" + silver + ") Silver coins'}}");
    }
    if (money >= 100L) {
      buf.append("harray{input{text='0'; id='copper'; maxchars='10'}label{text='(" + copper + ") Copper coins'}}");
    }
    if (money >= 1L) {
      buf.append("harray{input{text='0'; id='iron'; maxchars='10'}label{text='(" + iron + ") Iron coins'}}");
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\questions\WithdrawMoneyQuestion.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */