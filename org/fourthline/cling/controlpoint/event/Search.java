package org.fourthline.cling.controlpoint.event;

import org.fourthline.cling.model.message.header.MXHeader;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.message.header.UpnpHeader;

public class Search
{
  protected UpnpHeader searchType = new STAllHeader();
  protected int mxSeconds = MXHeader.DEFAULT_VALUE.intValue();
  
  public Search() {}
  
  public Search(UpnpHeader searchType)
  {
    this.searchType = searchType;
  }
  
  public Search(UpnpHeader searchType, int mxSeconds)
  {
    this.searchType = searchType;
    this.mxSeconds = mxSeconds;
  }
  
  public Search(int mxSeconds)
  {
    this.mxSeconds = mxSeconds;
  }
  
  public UpnpHeader getSearchType()
  {
    return this.searchType;
  }
  
  public int getMxSeconds()
  {
    return this.mxSeconds;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\controlpoint\event\Search.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */