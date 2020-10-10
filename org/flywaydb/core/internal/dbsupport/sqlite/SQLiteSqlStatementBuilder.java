package org.flywaydb.core.internal.dbsupport.sqlite;

import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

public class SQLiteSqlStatementBuilder
  extends SqlStatementBuilder
{
  private String statementStart = "";
  
  protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter)
  {
    if (StringUtils.countOccurrencesOf(this.statementStart, " ") < 8)
    {
      this.statementStart += line;
      this.statementStart += " ";
      this.statementStart = this.statementStart.replaceAll("\\s+", " ");
    }
    boolean createTriggerStatement = this.statementStart.matches("CREATE( TEMP| TEMPORARY)? TRIGGER.*");
    if ((createTriggerStatement) && (!line.endsWith("END;"))) {
      return null;
    }
    return getDefaultDelimiter();
  }
  
  protected String cleanToken(String token)
  {
    if (token.startsWith("X'")) {
      return token.substring(token.indexOf("'"));
    }
    return token;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\sqlite\SQLiteSqlStatementBuilder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */