package org.flywaydb.core.internal.dbsupport.solid;

import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

public class SolidSqlStatementBuilder
  extends SqlStatementBuilder
{
  public Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter)
  {
    if (line.startsWith("\"")) {
      return new Delimiter("\"", false);
    }
    if (line.endsWith("\";")) {
      return getDefaultDelimiter();
    }
    return delimiter;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\solid\SolidSqlStatementBuilder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */