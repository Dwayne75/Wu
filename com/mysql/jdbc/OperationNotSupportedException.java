package com.mysql.jdbc;

import java.sql.SQLException;

class OperationNotSupportedException
  extends SQLException
{
  OperationNotSupportedException()
  {
    super(Messages.getString("RowDataDynamic.10"), "S1009");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\OperationNotSupportedException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */