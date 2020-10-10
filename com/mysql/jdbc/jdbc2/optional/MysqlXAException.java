package com.mysql.jdbc.jdbc2.optional;

import javax.transaction.xa.XAException;

class MysqlXAException
  extends XAException
{
  private static final long serialVersionUID = -9075817535836563004L;
  private String message;
  private String xidAsString;
  
  public MysqlXAException(int errorCode, String message, String xidAsString)
  {
    super(errorCode);
    this.message = message;
    this.xidAsString = xidAsString;
  }
  
  public MysqlXAException(String message, String xidAsString)
  {
    this.message = message;
    this.xidAsString = xidAsString;
  }
  
  public String getMessage()
  {
    String superMessage = super.getMessage();
    StringBuffer returnedMessage = new StringBuffer();
    if (superMessage != null)
    {
      returnedMessage.append(superMessage);
      returnedMessage.append(":");
    }
    if (this.message != null) {
      returnedMessage.append(this.message);
    }
    return returnedMessage.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\jdbc2\optional\MysqlXAException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */