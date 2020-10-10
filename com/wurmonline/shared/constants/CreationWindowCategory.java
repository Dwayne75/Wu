package com.wurmonline.shared.constants;

public enum CreationWindowCategory
{
  NONE(0, ""),  EPIC(1, "Epic"),  ARMOUR(2, "Armour"),  CONTAINERS(3, "Containers"),  FOOD(4, "Food"),  LOCKS(5, "Locks"),  MAGIC(6, "Magic"),  POTTERY(7, "Pottery"),  DECORATIONS(8, "Decorations"),  TOOLS(9, "Tools"),  SHIELDS(10, "Shields"),  WEAPONS(11, "Weapons"),  MISCELLANEOUS(12, "Miscellaneous");
  
  private final byte id;
  private final String name;
  
  private CreationWindowCategory(int id, String name)
  {
    this.id = ((byte)id);
    this.name = name;
  }
  
  public byte getId()
  {
    return this.id;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  private static final CreationWindowCategory[] types = values();
  
  public static CreationWindowCategory creationWindowCategoryFromId(byte aId)
  {
    for (int i = 0; i < types.length; i++) {
      if (aId == types[i].getId()) {
        return types[i];
      }
    }
    return NONE;
  }
  
  public static byte idFromName(String aName)
  {
    for (int i = 0; i < types.length; i++) {
      if (aName.equals(types[i].getName())) {
        return types[i].getId();
      }
    }
    return NONE.getId();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\shared\constants\CreationWindowCategory.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */