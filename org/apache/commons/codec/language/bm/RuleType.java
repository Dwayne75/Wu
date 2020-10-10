package org.apache.commons.codec.language.bm;

public enum RuleType
{
  APPROX("approx"),  EXACT("exact"),  RULES("rules");
  
  private final String name;
  
  private RuleType(String name)
  {
    this.name = name;
  }
  
  public String getName()
  {
    return this.name;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\commons\codec\language\bm\RuleType.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */