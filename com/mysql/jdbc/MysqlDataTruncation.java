package com.mysql.jdbc;

import java.sql.DataTruncation;

public class MysqlDataTruncation
  extends DataTruncation
{
  private String message;
  private int vendorErrorCode;
  
  public MysqlDataTruncation(String message, int index, boolean parameter, boolean read, int dataSize, int transferSize, int vendorErrorCode)
  {
    super(index, parameter, read, dataSize, transferSize);
    
    this.message = message;
    this.vendorErrorCode = vendorErrorCode;
  }
  
  public int getErrorCode()
  {
    return this.vendorErrorCode;
  }
  
  public String getMessage()
  {
    return super.getMessage() + ": " + this.message;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\MysqlDataTruncation.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */