package com.mysql.jdbc;

import java.sql.SQLException;

public class NotUpdatable
  extends SQLException
{
  private static final long serialVersionUID = 8084742846039782258L;
  public static final String NOT_UPDATEABLE_MESSAGE = Messages.getString("NotUpdatable.0") + Messages.getString("NotUpdatable.1") + Messages.getString("NotUpdatable.2") + Messages.getString("NotUpdatable.3") + Messages.getString("NotUpdatable.4") + Messages.getString("NotUpdatable.5");
  
  public NotUpdatable()
  {
    this(NOT_UPDATEABLE_MESSAGE);
  }
  
  public NotUpdatable(String reason)
  {
    super(reason + Messages.getString("NotUpdatable.1") + Messages.getString("NotUpdatable.2") + Messages.getString("NotUpdatable.3") + Messages.getString("NotUpdatable.4") + Messages.getString("NotUpdatable.5"), "S1000");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\NotUpdatable.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */