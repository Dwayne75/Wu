package com.wurmonline.server.creatures;

public final class AttackAction
{
  private final String name;
  private final AttackIdentifier identifier;
  private final AttackValues attackValues;
  
  public AttackAction(String name, AttackIdentifier identifier, AttackValues attackValues)
  {
    this.name = name;
    this.identifier = identifier;
    this.attackValues = attackValues;
  }
  
  public final String getName()
  {
    return this.name;
  }
  
  public final AttackIdentifier getAttackIdentifier()
  {
    return this.identifier;
  }
  
  public final boolean isUsingWeapon()
  {
    return this.attackValues.isUsingWeapon();
  }
  
  public final AttackValues getAttackValues()
  {
    return this.attackValues;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\creatures\AttackAction.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */