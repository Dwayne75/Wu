package org.flywaydb.core.internal.dbsupport.hsql;

import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

public class HsqlSqlStatementBuilder
  extends SqlStatementBuilder
{
  private boolean insideAtomicBlock;
  
  protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter)
  {
    if (line.contains("BEGIN ATOMIC")) {
      this.insideAtomicBlock = true;
    }
    if (line.endsWith("END;")) {
      this.insideAtomicBlock = false;
    }
    if (this.insideAtomicBlock) {
      return null;
    }
    return getDefaultDelimiter();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\hsql\HsqlSqlStatementBuilder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */