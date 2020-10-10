package com.mysql.jdbc.integration.jboss;

import java.sql.SQLException;
import org.jboss.resource.adapter.jdbc.vendor.MySQLExceptionSorter;

public final class ExtendedMysqlExceptionSorter
  extends MySQLExceptionSorter
{
  public boolean isExceptionFatal(SQLException ex)
  {
    String sqlState = ex.getSQLState();
    if ((sqlState != null) && (sqlState.startsWith("08"))) {
      return true;
    }
    return super.isExceptionFatal(ex);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\integration\jboss\ExtendedMysqlExceptionSorter.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */