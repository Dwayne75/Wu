package com.wurmonline.server.players;

public class Friend
  implements Comparable<Friend>
{
  private final long id;
  private final Category cat;
  private final String note;
  
  public static enum Category
  {
    Other(0),  Contacts(1),  Friends(2),  Trusted(3);
    
    private final byte cat;
    
    private Category(int numb)
    {
      this.cat = ((byte)numb);
    }
    
    public byte getCatId()
    {
      return this.cat;
    }
    
    private static final Category[] cats = values();
    
    public static final int getCatLength()
    {
      return cats.length;
    }
    
    public static final Category[] getCategories()
    {
      return cats;
    }
    
    public static Category catFromInt(int typeId)
    {
      if (typeId >= getCatLength()) {
        return cats[0];
      }
      return cats[(typeId & 0xFF)];
    }
    
    public static Category catFromName(String catName)
    {
      for (Category c : cats) {
        if (c.name().toLowerCase().startsWith(catName.toLowerCase())) {
          return c;
        }
      }
      return null;
    }
  }
  
  public Friend(long aId, byte catId, String note)
  {
    this(aId, Category.catFromInt(catId), note);
  }
  
  public Friend(long aId, Category category, String note)
  {
    this.id = aId;
    this.cat = category;
    this.note = note;
  }
  
  public long getFriendId()
  {
    return this.id;
  }
  
  public Category getCategory()
  {
    return this.cat;
  }
  
  public byte getCatId()
  {
    return this.cat.getCatId();
  }
  
  public String getName()
  {
    return PlayerInfoFactory.getPlayerName(this.id);
  }
  
  public String getNote()
  {
    return this.note;
  }
  
  public int compareTo(Friend otherFriend)
  {
    if (getCatId() < otherFriend.getCatId()) {
      return 1;
    }
    if (getCatId() > otherFriend.getCatId()) {
      return -1;
    }
    return getName().compareTo(otherFriend.getName());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\players\Friend.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */