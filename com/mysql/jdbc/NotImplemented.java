package com.mysql.jdbc;

import java.sql.SQLException;

public class NotImplemented
  extends SQLException
{
  public NotImplemented()
  {
    super(Messages.getString("NotImplemented.0"), "S1C00");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\NotImplemented.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */