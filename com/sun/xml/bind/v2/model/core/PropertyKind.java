package com.sun.xml.bind.v2.model.core;

public enum PropertyKind
{
  VALUE(true, false, Integer.MAX_VALUE),  ATTRIBUTE(false, false, Integer.MAX_VALUE),  ELEMENT(true, true, 0),  REFERENCE(false, true, 1),  MAP(false, true, 2);
  
  public final boolean canHaveXmlMimeType;
  public final boolean isOrdered;
  public final int propertyIndex;
  
  private PropertyKind(boolean canHaveExpectedContentType, boolean isOrdered, int propertyIndex)
  {
    this.canHaveXmlMimeType = canHaveExpectedContentType;
    this.isOrdered = isOrdered;
    this.propertyIndex = propertyIndex;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\core\PropertyKind.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */