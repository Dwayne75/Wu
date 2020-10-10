package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ParsingException;
import javax.mail.FetchProfile.Item;

public abstract class FetchItem
{
  private String name;
  private FetchProfile.Item fetchProfileItem;
  
  public FetchItem(String name, FetchProfile.Item fetchProfileItem)
  {
    this.name = name;
    this.fetchProfileItem = fetchProfileItem;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public FetchProfile.Item getFetchProfileItem()
  {
    return this.fetchProfileItem;
  }
  
  public abstract Object parseItem(FetchResponse paramFetchResponse)
    throws ParsingException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\imap\protocol\FetchItem.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */