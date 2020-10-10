package org.flywaydb.core.internal.dbsupport.saphana;

import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

public class SapHanaSqlStatementBuilder
  extends SqlStatementBuilder
{
  private boolean insideBeginEndBlock;
  private String statementStart = "";
  
  protected String cleanToken(String token)
  {
    if ((token.startsWith("N'")) || (token.startsWith("X'")) || 
      (token.startsWith("DATE'")) || (token.startsWith("TIME'")) || (token.startsWith("TIMESTAMP'"))) {
      return token.substring(token.indexOf("'"));
    }
    return super.cleanToken(token);
  }
  
  protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter)
  {
    if (StringUtils.countOccurrencesOf(this.statementStart, " ") < 4)
    {
      this.statementStart += line;
      this.statementStart += " ";
    }
    if (this.statementStart.startsWith("CREATE TRIGGER"))
    {
      if (line.startsWith("BEGIN")) {
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\saphana\SapHanaSqlStatementBuilder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */