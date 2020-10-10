package com.mysql.jdbc;

import java.sql.SQLException;

public class CommunicationsException
  extends SQLException
  implements StreamingNotifiable
{
  private String exceptionMessage = null;
  private boolean streamingResultSetInPlay = false;
  private ConnectionImpl conn;
  private long lastPacketSentTimeMs;
  private long lastPacketReceivedTimeMs;
  private Exception underlyingException;
  
  public CommunicationsException(ConnectionImpl conn, long lastPacketSentTimeMs, long lastPacketReceivedTimeMs, Exception underlyingException)
  {
    this.conn = conn;
    this.lastPacketReceivedTimeMs = lastPacketReceivedTimeMs;
    this.lastPacketSentTimeMs = lastPacketSentTimeMs;
    this.underlyingException = underlyingException;
    if (underlyingException != null) {
      initCause(underlyingException);
    }
  }
  
  public String getMessage()
  {
    if (this.exceptionMessage == null)
    {
      this.exceptionMessage = SQLError.createLinkFailureMessageBasedOnHeuristics(this.conn, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, this.underlyingException, this.streamingResultSetInPlay);
      
      this.conn = null;
      this.underlyingException = null;
    }
    return this.exceptionMessage;
  }
  
  public String getSQLState()
  {
    return "08S01";
  }
  
  public void setWasStreamingResults()
  {
    this.streamingResultSetInPlay = true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\CommunicationsException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */