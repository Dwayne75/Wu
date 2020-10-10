package com.wurmonline.server.questions;

import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.WurmMail;
import java.util.HashSet;
import java.util.Set;

final class ReiceverReturnMails
{
  private final Set<WurmMail> returnWurmMailSet = new HashSet();
  private final Set<Item> returnItemSet = new HashSet();
  private int serverId;
  private long receiverId;
  
  void addMail(WurmMail mail, Item item)
  {
    if (!this.returnWurmMailSet.contains(mail)) {
      this.returnWurmMailSet.add(mail);
    }
    this.returnItemSet.add(item);
  }
  
  int getServerId()
  {
    return this.serverId;
  }
  
  void setServerId(int aServerId)
  {
    this.serverId = aServerId;
  }
  
  void setReceiverId(long aReceiverId)
  {
    this.receiverId = aReceiverId;
  }
  
  Set<WurmMail> getReturnWurmMailSet()
  {
    return this.returnWurmMailSet;
  }
  
  Item[] getReturnItemSetAsArray()
  {
    return (Item[])this.returnItemSet.toArray(new Item[this.returnItemSet.size()]);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\ReiceverReturnMails.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */