package com.wurmonline.server;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.players.Player;
import java.util.StringTokenizer;

public final class ServerTweaksHandler
{
  public static boolean isTweakCommand(String message)
  {
    for (ServerTweaksHandler.Tweak tweak : ) {
      if (message.startsWith(tweak.getCommand())) {
        return true;
      }
    }
    return false;
  }
  
  public static void handleTweakCommand(String message, Player admin)
  {
    StringTokenizer tokenizer = new StringTokenizer(message);
    
    String cmd = tokenizer.nextToken();
    ServerTweaksHandler.Tweak tweak = ServerTweaksHandler.Tweak.getByCommand(cmd);
    
    tweak.execute(tokenizer, admin);
  }
  
  private static boolean validatePassword(String pass, Player admin)
  {
    String adminPass = ServerProperties.getString("ADMINPASSWORD", "");
    if (adminPass.isEmpty())
    {
      admin.getCommunicator().sendNormalServerMessage("There is no admin password on this server, so admin commands is disabled.");
      return false;
    }
    if (pass.equals(adminPass)) {
      return true;
    }
    admin.getCommunicator().sendNormalServerMessage("Incorrect admin password.");
    return false;
  }
  
  public static void handleUnknownCommad(StringTokenizer tokenizer, Player admin)
  {
    admin.getCommunicator().sendNormalServerMessage("Unknown command.");
  }
  
  private static boolean tokenCheck(ServerTweaksHandler.Tweak tweak, StringTokenizer tokenizer, Player admin)
  {
    int numTokens = tokenizer.countTokens();
    if (numTokens != tweak.tokenCount())
    {
      String message = "Incorrect number of parameters! Provided: %d Expected: %d";
      admin.getCommunicator().sendNormalServerMessage(String.format("Incorrect number of parameters! Provided: %d Expected: %d", new Object[] { Integer.valueOf(numTokens), Integer.valueOf(tweak.tokenCount()) }));
      return false;
    }
    return true;
  }
  
  public static void handleSkillGainRateCommand(StringTokenizer tokenizer, Player admin)
  {
    if (!tokenCheck(ServerTweaksHandler.Tweak.SKILL_GAIN_RATE, tokenizer, admin)) {
      return;
    }
    String param = tokenizer.nextToken();
    String pass = tokenizer.nextToken();
    if (!validatePassword(pass, admin)) {
      return;
    }
    try
    {
      float rate = Float.parseFloat(param);
      rate = Math.max(0.01F, rate);
      admin.getCommunicator().sendNormalServerMessage("Changed skill gain multiplier to: " + rate + ".");
      Servers.localServer.setSkillGainRate(rate);
      
      Servers.localServer.saveNewGui(Servers.localServer.id);
    }
    catch (NumberFormatException nfe)
    {
      admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
    }
  }
  
  public static void handleFieldGrowthCommand(StringTokenizer tokenizer, Player admin)
  {
    if (!tokenCheck(ServerTweaksHandler.Tweak.FIELD_GROWTH, tokenizer, admin)) {
      return;
    }
    String param = tokenizer.nextToken();
    String pass = tokenizer.nextToken();
    if (!validatePassword(pass, admin)) {
      return;
    }
    try
    {
      Float time = Float.valueOf(Float.parseFloat(param));
      
      admin.getCommunicator().sendNormalServerMessage("Changed field growth timer to: " + time.toString() + " hours.");
      Servers.localServer.setFieldGrowthTime((time.floatValue() * 3600.0F * 1000.0F));
      
      Servers.localServer.saveNewGui(Servers.localServer.id);
    }
    catch (NumberFormatException nfe)
    {
      admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
    }
  }
  
  public static void handleCharacteristicsStartCommand(StringTokenizer tokenizer, Player admin)
  {
    if (!tokenCheck(ServerTweaksHandler.Tweak.CHARACTERISTICS_START, tokenizer, admin)) {
      return;
    }
    String param = tokenizer.nextToken();
    String pass = tokenizer.nextToken();
    if (!validatePassword(pass, admin)) {
      return;
    }
    try
    {
      Float charVal = Float.valueOf(Float.parseFloat(param));
      
      admin.getCommunicator().sendNormalServerMessage("Changed characteristics start value to: " + charVal.toString() + ".");
      Servers.localServer.setSkillbasicval(charVal.floatValue());
      
      Servers.localServer.saveNewGui(Servers.localServer.id);
      admin.getCommunicator().sendNormalServerMessage("Server restart needed before the changes take effect.");
    }
    catch (NumberFormatException nfe)
    {
      admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
    }
  }
  
  public static void handleMindLogicStartCommand(StringTokenizer tokenizer, Player admin)
  {
    if (!tokenCheck(ServerTweaksHandler.Tweak.MIND_LOGIC_START, tokenizer, admin)) {
      return;
    }
    String param = tokenizer.nextToken();
    String pass = tokenizer.nextToken();
    if (!validatePassword(pass, admin)) {
      return;
    }
    try
    {
      Float val = Float.valueOf(Float.parseFloat(param));
      
      admin.getCommunicator().sendNormalServerMessage("Changed mind logic start value to: " + val.toString() + ".");
      Servers.localServer.setSkillmindval(val.floatValue());
      
      Servers.localServer.saveNewGui(Servers.localServer.id);
      admin.getCommunicator().sendNormalServerMessage("Server restart needed before the changes take effect.");
    }
    catch (NumberFormatException nfe)
    {
      admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
    }
  }
  
  public static void handleBodyControlStartCommand(StringTokenizer tokenizer, Player admin)
  {
    if (!tokenCheck(ServerTweaksHandler.Tweak.BC_START, tokenizer, admin)) {
      return;
    }
    String param = tokenizer.nextToken();
    String pass = tokenizer.nextToken();
    if (!validatePassword(pass, admin)) {
      return;
    }
    try
    {
      Float val = Float.valueOf(Float.parseFloat(param));
      
      admin.getCommunicator().sendNormalServerMessage("Changed body control start value to: " + val.toString() + ".");
      Servers.localServer.setSkillbcval(val.floatValue());
      
      Servers.localServer.saveNewGui(Servers.localServer.id);
      admin.getCommunicator().sendNormalServerMessage("Server restart needed before the changes take effect.");
    }
    catch (NumberFormatException nfe)
    {
      admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
    }
  }
  
  public static void handleFightingStartCommand(StringTokenizer tokenizer, Player admin)
  {
    if (!tokenCheck(ServerTweaksHandler.Tweak.FIGHT_START, tokenizer, admin)) {
      return;
    }
    String param = tokenizer.nextToken();
    String pass = tokenizer.nextToken();
    if (!validatePassword(pass, admin)) {
      return;
    }
    try
    {
      Float val = Float.valueOf(Float.parseFloat(param));
      
      admin.getCommunicator().sendNormalServerMessage("Changed fighting start value to: " + val.toString() + ".");
      Servers.localServer.setSkillfightval(val.floatValue());
      
      Servers.localServer.saveNewGui(Servers.localServer.id);
      admin.getCommunicator().sendNormalServerMessage("Server restart needed before the changes take effect.");
    }
    catch (NumberFormatException nfe)
    {
      admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
    }
  }
  
  public static void handleOverallStartCommand(StringTokenizer tokenizer, Player admin)
  {
    if (!tokenCheck(ServerTweaksHandler.Tweak.OVERALL_START, tokenizer, admin)) {
      return;
    }
    String param = tokenizer.nextToken();
    String pass = tokenizer.nextToken();
    if (!validatePassword(pass, admin)) {
      return;
    }
    try
    {
      Float val = Float.valueOf(Float.parseFloat(param));
      
      admin.getCommunicator().sendNormalServerMessage("Changed overall start skill value to: " + val.toString() + ".");
      Servers.localServer.setSkilloverallval(val.floatValue());
      
      Servers.localServer.saveNewGui(Servers.localServer.id);
      admin.getCommunicator().sendNormalServerMessage("Server restart needed before the changes take effect.");
    }
    catch (NumberFormatException nfe)
    {
      admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
    }
  }
  
  public static void handlePlayerCRCommand(StringTokenizer tokenizer, Player admin)
  {
    if (!tokenCheck(ServerTweaksHandler.Tweak.PLAYER_CR, tokenizer, admin)) {
      return;
    }
    String param = tokenizer.nextToken();
    String pass = tokenizer.nextToken();
    if (!validatePassword(pass, admin)) {
      return;
    }
    try
    {
      Float val = Float.valueOf(Float.parseFloat(param));
      
      admin.getCommunicator().sendNormalServerMessage("Changed player CR mod to: " + val.toString() + ".");
      Servers.localServer.setCombatRatingModifier(val.floatValue());
      
      Servers.localServer.saveNewGui(Servers.localServer.id);
    }
    catch (NumberFormatException nfe)
    {
      admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
    }
  }
  
  public static void handleActionSpeedCommand(StringTokenizer tokenizer, Player admin)
  {
    if (!tokenCheck(ServerTweaksHandler.Tweak.ACTION_SPEED, tokenizer, admin)) {
      return;
    }
    String param = tokenizer.nextToken();
    String pass = tokenizer.nextToken();
    if (!validatePassword(pass, admin)) {
      return;
    }
    try
    {
      Float val = Float.valueOf(Float.parseFloat(param));
      
      admin.getCommunicator().sendNormalServerMessage("Changed action speed mod to: " + val.toString() + ".");
      Servers.localServer.setActionTimer(val.floatValue());
      
      Servers.localServer.saveNewGui(Servers.localServer.id);
    }
    catch (NumberFormatException nfe)
    {
      admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
    }
  }
  
  public static void handleHOTACommand(StringTokenizer tokenizer, Player admin)
  {
    if (!tokenCheck(ServerTweaksHandler.Tweak.HOTA, tokenizer, admin)) {
      return;
    }
    String param = tokenizer.nextToken();
    String pass = tokenizer.nextToken();
    if (!validatePassword(pass, admin)) {
      return;
    }
    try
    {
      Integer val = Integer.valueOf(Integer.parseInt(param));
      
      admin.getCommunicator().sendNormalServerMessage("Changed HOTA delay to: " + val.toString() + ".");
      Servers.localServer.setHotaDelay(val.intValue());
      
      Servers.localServer.saveNewGui(Servers.localServer.id);
    }
    catch (NumberFormatException nfe)
    {
      admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
    }
  }
  
  public static void handleMaxCreaturesCommand(StringTokenizer tokenizer, Player admin)
  {
    if (!tokenCheck(ServerTweaksHandler.Tweak.MAX_CREATURES, tokenizer, admin)) {
      return;
    }
    String param = tokenizer.nextToken();
    String pass = tokenizer.nextToken();
    if (!validatePassword(pass, admin)) {
      return;
    }
    try
    {
      int val = Integer.parseInt(param);
      val = Math.max(0, val);
      
      admin.getCommunicator().sendNormalServerMessage("Changed max creatures to: " + val + ".");
      Servers.localServer.maxCreatures = val;
      
      Servers.localServer.saveNewGui(Servers.localServer.id);
    }
    catch (NumberFormatException nfe)
    {
      admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
    }
  }
  
  public static void handleAggCreaturesCommand(StringTokenizer tokenizer, Player admin)
  {
    if (!tokenCheck(ServerTweaksHandler.Tweak.AGG_PERCENT, tokenizer, admin)) {
      return;
    }
    String param = tokenizer.nextToken();
    String pass = tokenizer.nextToken();
    if (!validatePassword(pass, admin)) {
      return;
    }
    try
    {
      float val = Float.parseFloat(param);
      val = Math.max(0.0F, Math.min(100.0F, val));
      admin.getCommunicator().sendNormalServerMessage("Changed aggressive creature % to: " + val + ".");
      Servers.localServer.percentAggCreatures = val;
      
      Servers.localServer.saveNewGui(Servers.localServer.id);
    }
    catch (NumberFormatException nfe)
    {
      admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
    }
  }
  
  public static void handleUpkeepCommand(StringTokenizer tokenizer, Player admin)
  {
    if (!tokenCheck(ServerTweaksHandler.Tweak.UPKEEP, tokenizer, admin)) {
      return;
    }
    String param = tokenizer.nextToken();
    String pass = tokenizer.nextToken();
    if (!validatePassword(pass, admin)) {
      return;
    }
    boolean val = Boolean.parseBoolean(param);
    
    admin.getCommunicator().sendNormalServerMessage("Changed upkeep to: " + val + ".");
    Servers.localServer.setUpkeep(val);
    
    Servers.localServer.saveNewGui(Servers.localServer.id);
  }
  
  public static void handleFreeDeedsCommand(StringTokenizer tokenizer, Player admin)
  {
    if (!tokenCheck(ServerTweaksHandler.Tweak.FREE_DEEDS, tokenizer, admin)) {
      return;
    }
    String param = tokenizer.nextToken();
    String pass = tokenizer.nextToken();
    if (!validatePassword(pass, admin)) {
      return;
    }
    boolean val = Boolean.parseBoolean(param);
    
    admin.getCommunicator().sendNormalServerMessage("Changed free deeding to: " + val + ".");
    Servers.localServer.setFreeDeeds(val);
    
    Servers.localServer.saveNewGui(Servers.localServer.id);
  }
  
  public static void handleTraderMaxMoneyCommand(StringTokenizer tokenizer, Player admin)
  {
    if (!tokenCheck(ServerTweaksHandler.Tweak.TRADER_MAX_MONEY, tokenizer, admin)) {
      return;
    }
    String param = tokenizer.nextToken();
    String pass = tokenizer.nextToken();
    if (!validatePassword(pass, admin)) {
      return;
    }
    try
    {
      int val = Integer.parseInt(param);
      val = Math.max(0, val);
      admin.getCommunicator().sendNormalServerMessage("Changed trader max money to: " + val + " silver.");
      Servers.localServer.setTraderMaxIrons(val * 10000);
      
      Servers.localServer.saveNewGui(Servers.localServer.id);
    }
    catch (NumberFormatException nfe)
    {
      admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
    }
  }
  
  public static void handleTraderInitialMoneyCommand(StringTokenizer tokenizer, Player admin)
  {
    if (!tokenCheck(ServerTweaksHandler.Tweak.TRADER_INITIAL_MONEY, tokenizer, admin)) {
      return;
    }
    String param = tokenizer.nextToken();
    String pass = tokenizer.nextToken();
    if (!validatePassword(pass, admin)) {
      return;
    }
    try
    {
      int val = Integer.parseInt(param);
      val = Math.max(0, val);
      admin.getCommunicator().sendNormalServerMessage("Changed trader initial money to: " + val + " silver.");
      Servers.localServer.setInitialTraderIrons(val * 10000);
      
      Servers.localServer.saveNewGui(Servers.localServer.id);
    }
    catch (NumberFormatException nfe)
    {
      admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
    }
  }
  
  public static void handleMinimumHitsCommand(StringTokenizer tokenizer, Player admin)
  {
    if (!tokenCheck(ServerTweaksHandler.Tweak.MINING_HITS, tokenizer, admin)) {
      return;
    }
    String param = tokenizer.nextToken();
    String pass = tokenizer.nextToken();
    if (!validatePassword(pass, admin)) {
      return;
    }
    try
    {
      int val = Integer.parseInt(param);
      val = Math.max(0, val);
      admin.getCommunicator().sendNormalServerMessage("Changed minimum mining hits on rock to: " + val + ".");
      Servers.localServer.setTunnelingHits(val);
      
      Servers.localServer.saveNewGui(Servers.localServer.id);
    }
    catch (NumberFormatException nfe)
    {
      admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
    }
  }
  
  public static void handleBreedingTimeCommand(StringTokenizer tokenizer, Player admin)
  {
    if (!tokenCheck(ServerTweaksHandler.Tweak.BREEDING_TIME, tokenizer, admin)) {
      return;
    }
    String param = tokenizer.nextToken();
    String pass = tokenizer.nextToken();
    if (!validatePassword(pass, admin)) {
      return;
    }
    try
    {
      long val = Long.parseLong(param);
      val = Math.max(1L, val);
      admin.getCommunicator().sendNormalServerMessage("Changed breeding time modifier to: " + val + ".");
      Servers.localServer.setBreedingTimer(val);
      
      Servers.localServer.saveNewGui(Servers.localServer.id);
    }
    catch (NumberFormatException nfe)
    {
      admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
    }
  }
  
  public static void handleTreeSpreadOddsCommand(StringTokenizer tokenizer, Player admin)
  {
    if (!tokenCheck(ServerTweaksHandler.Tweak.TREE_GROWTH, tokenizer, admin)) {
      return;
    }
    String param = tokenizer.nextToken();
    String pass = tokenizer.nextToken();
    if (!validatePassword(pass, admin)) {
      return;
    }
    try
    {
      int val = Integer.parseInt(param);
      val = Math.max(0, val);
      admin.getCommunicator().sendNormalServerMessage("Changed tree spread odds to: " + val + ".");
      Servers.localServer.treeGrowth = val;
      
      Servers.localServer.saveNewGui(Servers.localServer.id);
    }
    catch (NumberFormatException nfe)
    {
      admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
    }
  }
  
  public static void handleMoneyPoolCommand(StringTokenizer tokenizer, Player admin)
  {
    if (!tokenCheck(ServerTweaksHandler.Tweak.MONEY_POOL, tokenizer, admin)) {
      return;
    }
    String param = tokenizer.nextToken();
    String pass = tokenizer.nextToken();
    if (!validatePassword(pass, admin)) {
      return;
    }
    try
    {
      int val = Integer.parseInt(param);
      val = Math.max(0, val);
      admin.getCommunicator().sendNormalServerMessage("Money pool will be set to: " + val + " after a restart.");
      Servers.localServer.setKingsmoneyAtRestart(val * 10000);
      
      Servers.localServer.saveNewGui(Servers.localServer.id);
    }
    catch (NumberFormatException nfe)
    {
      admin.getCommunicator().sendNormalServerMessage("'" + param + "' is not in the correct format.");
    }
  }
  
  public static void sendHelp(Player player)
  {
    Communicator com = player.getCommunicator();
    for (ServerTweaksHandler.Tweak tweak : ServerTweaksHandler.Tweak.values()) {
      if (tweak != ServerTweaksHandler.Tweak.UNKNOWN) {
        com.sendHelpMessage(tweak.parameterString + " - " + tweak.helpDescription);
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\ServerTweaksHandler.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */