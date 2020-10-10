package org.fourthline.cling.support.model;

import org.fourthline.cling.model.types.UnsignedIntegerFourBytes;

public class SearchResult
{
  protected String result;
  protected UnsignedIntegerFourBytes count;
  protected UnsignedIntegerFourBytes totalMatches;
  protected UnsignedIntegerFourBytes containerUpdateID;
  
  public SearchResult(String result, UnsignedIntegerFourBytes count, UnsignedIntegerFourBytes totalMatches, UnsignedIntegerFourBytes containerUpdateID)
  {
    this.result = result;
    this.count = count;
    this.totalMatches = totalMatches;
    this.containerUpdateID = containerUpdateID;
  }
  
  public SearchResult(String result, long count, long totalMatches)
  {
    this(result, count, totalMatches, 0L);
  }
  
  public SearchResult(String result, long count, long totalMatches, long updateID)
  {
    this(result, new UnsignedIntegerFourBytes(count), new UnsignedIntegerFourBytes(totalMatches), new UnsignedIntegerFourBytes(updateID));
  }
  
  public String getResult()
  {
    return this.result;
  }
  
  public UnsignedIntegerFourBytes getCount()
  {
    return this.count;
  }
  
  public long getCountLong()
  {
    return this.count.getValue().longValue();
  }
  
  public UnsignedIntegerFourBytes getTotalMatches()
  {
    return this.totalMatches;
  }
  
  public long getTotalMatchesLong()
  {
    return this.totalMatches.getValue().longValue();
  }
  
  public UnsignedIntegerFourBytes getContainerUpdateID()
  {
    return this.containerUpdateID;
  }
  
  public long getContainerUpdateIDLong()
  {
    return this.containerUpdateID.getValue().longValue();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\SearchResult.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */