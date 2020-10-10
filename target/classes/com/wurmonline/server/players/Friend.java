package com.wurmonline.server.players;

public class Friend
  implements Comparable<Friend>
{
  private final long id;
  private final Friend.Category cat;
  private final String note;
  
  public Friend(long aId, byte catId, String note)
  {
    this(aId, Friend.Category.catFromInt(catId), note);
  }
  
  public Friend(long aId, Friend.Category category, String note)
  {
    this.id = aId;
    this.cat = category;
    this.note = note;
  }
  
  public long getFriendId()
  {
    return this.id;
  }
  
  public Friend.Category getCategory()
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\players\Friend.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */