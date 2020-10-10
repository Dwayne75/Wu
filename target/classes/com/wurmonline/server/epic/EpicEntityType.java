package com.wurmonline.server.epic;

 enum EpicEntityType
{
  TYPE_DEITY(0),  TYPE_SOURCE(1),  TYPE_COLLECT(2),  TYPE_WURM(4),  TYPE_MONSTER_SENTINEL(5),  TYPE_ALLY(6),  TYPE_DEMIGOD(7);
  
  private final int code;
  
  private EpicEntityType(int aCode)
  {
    this.code = aCode;
  }
  
  int getCode()
  {
    return this.code;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\epic\EpicEntityType.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */