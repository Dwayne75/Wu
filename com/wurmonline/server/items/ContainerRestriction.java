package com.wurmonline.server.items;

import java.util.ArrayList;

public class ContainerRestriction
{
  private final boolean onlyOneOf;
  private ArrayList<Integer> itemTemplateIds;
  private String emptySlotName = null;
  
  public ContainerRestriction(boolean onlyOneOf, int... itemTemplateId)
  {
    this.onlyOneOf = onlyOneOf;
    this.itemTemplateIds = new ArrayList();
    for (int i : itemTemplateId) {
      this.itemTemplateIds.add(Integer.valueOf(i));
    }
  }
  
  public ContainerRestriction(boolean onlyOneOf, String emptySlotName, int... itemTemplateId)
  {
    this(onlyOneOf, itemTemplateId);
    setEmptySlotName(emptySlotName);
  }
  
  public boolean canInsertItem(Item[] existing, Item toInsert)
  {
    if (!this.itemTemplateIds.contains(Integer.valueOf(toInsert.getTemplateId()))) {
      return false;
    }
    if (this.onlyOneOf) {
      for (Item i : existing) {
        if (this.itemTemplateIds.contains(Integer.valueOf(i.getTemplateId()))) {
          return false;
        }
      }
    }
    return true;
  }
  
  public void setEmptySlotName(String name)
  {
    this.emptySlotName = name;
  }
  
  public String getEmptySlotName()
  {
    if (this.emptySlotName != null) {
      return this.emptySlotName;
    }
    return "empty " + ItemTemplateFactory.getInstance().getTemplateName(getEmptySlotTemplateId()) + " slot";
  }
  
  public int getEmptySlotTemplateId()
  {
    return ((Integer)this.itemTemplateIds.get(0)).intValue();
  }
  
  public boolean contains(int id)
  {
    return this.itemTemplateIds.contains(Integer.valueOf(id));
  }
  
  public boolean doesItemOverrideSlot(Item toInsert)
  {
    if (this.itemTemplateIds.contains(Integer.valueOf(toInsert.getTemplateId()))) {
      return true;
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\items\ContainerRestriction.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */