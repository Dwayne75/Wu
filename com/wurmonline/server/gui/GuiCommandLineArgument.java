package com.wurmonline.server.gui;

public enum GuiCommandLineArgument
{
  START("start"),  QUERY_PORT("queryport"),  INTERNAL_PORT("internalport"),  EXTERNAL_PORT("externalport"),  IP_ADDR("ip"),  RMI_REG("rmiregport"),  RMI_PORT("rmiport"),  SERVER_PASS("serverpassword"),  PLAYER_NUM("maxplayers"),  LOGIN_SERVER("loginserver"),  PVP("pvp"),  HOME_SERVER("homeserver"),  HOME_KINGDOM("homekingdom"),  EPIC_SETTINGS("epicsettings"),  SERVER_NAME("servername"),  ADMIN_PWD("adminpwd");
  
  private final String argString;
  
  private GuiCommandLineArgument(String arg)
  {
    this.argString = arg;
  }
  
  public String getArgumentString()
  {
    return this.argString;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\gui\GuiCommandLineArgument.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */