package org.flywaydb.core.internal.dbsupport.db2;

import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;
import org.flywaydb.core.internal.util.StringUtils;

public class DB2SqlStatementBuilder
  extends SqlStatementBuilder
{
  private boolean insideBeginEndBlock;
  private String statementStart = "";
  
  protected String cleanToken(String token)
  {
    if (token.startsWith("X'")) {
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
    if ((this.statementStart.startsWith("CREATE FUNCTION")) || 
      (this.statementStart.startsWith("CREATE PROCEDURE")) || 
      (this.statementStart.startsWith("CREATE TRIGGER")) || 
      (this.statementStart.startsWith("CREATE OR REPLACE FUNCTION")) || 
      (this.statementStart.startsWith("CREATE OR REPLACE PROCEDURE")) || 
      (this.statementStart.startsWith("CREATE OR REPLACE TRIGGER")))
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\db2\DB2SqlStatementBuilder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */