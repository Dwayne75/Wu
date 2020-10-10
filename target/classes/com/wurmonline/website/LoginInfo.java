package com.wurmonline.website;

public class LoginInfo
{
  private String name;
  
  public LoginInfo(String aName)
  {
    this.name = aName;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public boolean isAdmin()
  {
    return this.name.equals("admin");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\website\LoginInfo.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */