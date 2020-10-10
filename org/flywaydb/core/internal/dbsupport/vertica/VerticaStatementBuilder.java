package org.flywaydb.core.internal.dbsupport.vertica;

import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.postgresql.PostgreSQLSqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

public class VerticaStatementBuilder
  extends PostgreSQLSqlStatementBuilder
{
  private boolean insideBeginEndBlock;
  private String statementStart = "";
  
  protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter)
  {
    if (StringUtils.countOccurrencesOf(this.statementStart, " ") < 4)
    {
      this.statementStart += line;
      this.statementStart += " ";
    }
    if ((this.statementStart.startsWith("CREATE FUNCTION")) || 
      (this.statementStart.startsWith("CREATE OR REPLACE FUNCTION")))
    {
      if ((line.startsWith("BEGIN")) || (line.endsWith("BEGIN"))) {
        this.insideBeginEndBlock = true;
      }
      if (line.endsWith("END;")) {
        this.insideBeginEndBlock = false;
      }
    }
    if (this.insideBeginEndBlock) {
      return null;
    }
    return getDefaultDelimiter();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\vertica\VerticaStatementBuilder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */