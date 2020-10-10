package org.seamless.util;

import java.io.Serializable;

public class Pager
  implements Serializable
{
  private Long numOfRecords = Long.valueOf(0L);
  private Integer page = Integer.valueOf(1);
  private Long pageSize = Long.valueOf(15L);
  
  public Pager() {}
  
  public Pager(Long numOfRecords)
  {
    this.numOfRecords = numOfRecords;
  }
  
  public Pager(Long numOfRecords, Integer page)
  {
    this.numOfRecords = numOfRecords;
    this.page = page;
  }
  
  public Pager(Long numOfRecords, Integer page, Long pageSize)
  {
    this.numOfRecords = numOfRecords;
    this.page = page;
    this.pageSize = pageSize;
  }
  
  public Long getNumOfRecords()
  {
    return this.numOfRecords;
  }
  
  public void setNumOfRecords(Long numOfRecords)
  {
    this.numOfRecords = numOfRecords;
  }
  
  public Integer getPage()
  {
    return this.page;
  }
  
  public void setPage(Integer page)
  {
    if (page != null) {
      this.page = page;
    }
  }
  
  public Long getPageSize()
  {
    return this.pageSize;
  }
  
  public void setPageSize(Long pageSize)
  {
    if (pageSize != null) {
      this.pageSize = pageSize;
    }
  }
  
  public int getNextPage()
  {
    return this.page.intValue() + 1;
  }
  
  public int getPreviousPage()
  {
    return this.page.intValue() - 1;
  }
  
  public int getFirstPage()
  {
    return 1;
  }
  
  public long getIndexRangeBegin()
  {
    long retval = (getPage().intValue() - 1) * getPageSize().longValue();
    return Math.max(Math.min(getNumOfRecords().longValue() - 1L, retval >= 0L ? retval : 0L), 0L);
  }
  
  public long getIndexRangeEnd()
  {
    long firstIndex = getIndexRangeBegin();
    long pageIndex = getPageSize().longValue() - 1L;
    long lastIndex = getNumOfRecords().longValue() - 1L;
    return Math.min(firstIndex + pageIndex, lastIndex);
  }
  
  public long getLastPage()
  {
    long lastPage = this.numOfRecords.longValue() / this.pageSize.longValue();
    if (this.numOfRecords.longValue() % this.pageSize.longValue() == 0L) {
      lastPage -= 1L;
    }
    return lastPage + 1L;
  }
  
  public boolean isPreviousPageAvailable()
  {
    return getIndexRangeBegin() + 1L > getPageSize().longValue();
  }
  
  public boolean isNextPageAvailable()
  {
    return this.numOfRecords.longValue() - 1L > getIndexRangeEnd();
  }
  
  public boolean isSeveralPages()
  {
    return (getNumOfRecords().longValue() != 0L) && (getNumOfRecords().longValue() > getPageSize().longValue());
  }
  
  public String toString()
  {
    return "Pager - Records: " + getNumOfRecords() + " Page size: " + getPageSize();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\util\Pager.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */