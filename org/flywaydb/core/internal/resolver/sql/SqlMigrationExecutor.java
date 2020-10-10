package org.flywaydb.core.internal.resolver.sql;

import java.sql.Connection;
import org.flywaydb.core.api.resolver.MigrationExecutor;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.SqlScript;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.Resource;

public class SqlMigrationExecutor
  implements MigrationExecutor
{
  private final DbSupport dbSupport;
  private final PlaceholderReplacer placeholderReplacer;
  private final Resource sqlScriptResource;
  private final String encoding;
  
  public SqlMigrationExecutor(DbSupport dbSupport, Resource sqlScriptResource, PlaceholderReplacer placeholderReplacer, String encoding)
  {
    this.dbSupport = dbSupport;
    this.sqlScriptResource = sqlScriptResource;
    this.encoding = encoding;
    this.placeholderReplacer = placeholderReplacer;
  }
  
  public void execute(Connection connection)
  {
    SqlScript sqlScript = new SqlScript(this.dbSupport, this.sqlScriptResource, this.placeholderReplacer, this.encoding);
    sqlScript.execute(new JdbcTemplate(connection, 0));
  }
  
  public boolean executeInTransaction()
  {
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\resolver\sql\SqlMigrationExecutor.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */