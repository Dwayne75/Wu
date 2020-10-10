package org.flywaydb.core.internal.dbsupport.redshift;

import java.sql.Connection;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;

public class RedshfitDbSupportViaPostgreSQLDriver
  extends RedshiftDbSupport
{
  public RedshfitDbSupportViaPostgreSQLDriver(Connection connection)
  {
    super(new JdbcTemplate(connection, 0));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\redshift\RedshfitDbSupportViaPostgreSQLDriver.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */