package com.wurmonline.server.skills;

public final class SkillsFactory
{
  public static Skills createSkills(long id)
  {
    return new DbSkills(id);
  }
  
  public static Skills createSkills(String templateName)
  {
    return new DbSkills(templateName);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\skills\SkillsFactory.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */