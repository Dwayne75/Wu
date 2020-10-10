package com.sun.xml.dtdparser;

final class InternalEntity
  extends EntityDecl
{
  char[] buf;
  
  InternalEntity(String name, char[] value)
  {
    this.name = name;
    this.buf = value;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\dtdparser\InternalEntity.class
 * Java compiler version: 3 (47.0)
 * JD-Core Version:       0.7.1
 */