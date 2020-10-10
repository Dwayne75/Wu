package com.wurmonline.server.database;

import java.sql.Connection;
import java.sql.SQLException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ConnectionFactory
{
  private final String url;
  private final WurmDatabaseSchema schema;
  
  ConnectionFactory(@Nonnull String url, @Nonnull WurmDatabaseSchema schema)
  {
    this.schema = schema;
    this.url = url;
  }
  
  public final String getUrl()
  {
    return this.url;
  }
  
  public abstract Connection createConnection()
    throws SQLException;
  
  public abstract boolean isValid(@Nullable Connection paramConnection)
    throws SQLException;
  
  public abstract boolean isStale(long paramLong, @Nullable Connection paramConnection)
    throws SQLException;
  
  public WurmDatabaseSchema getSchema()
  {
    return this.schema;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\database\ConnectionFactory.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */