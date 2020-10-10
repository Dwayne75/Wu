package org.flywaydb.core.internal.dbsupport.postgresql;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

public class PostgreSQLSqlStatementBuilder
  extends SqlStatementBuilder
{
  private static final Delimiter COPY_DELIMITER = new Delimiter("\\.", true);
  static final String DOLLAR_QUOTE_REGEX = "(\\$[A-Za-z0-9_]*\\$).*";
  private boolean firstLine = true;
  private String copyStatement;
  private boolean pgCopy;
  
  protected String extractAlternateOpenQuote(String token)
  {
    Matcher matcher = Pattern.compile("(\\$[A-Za-z0-9_]*\\$).*").matcher(token);
    if (matcher.find()) {
      return matcher.group(1);
    }
    return null;
  }
  
  protected Delimiter changeDelimiterIfNecessary(String line, Delimiter delimiter)
  {
    if (this.pgCopy) {
      return COPY_DELIMITER;
    }
    if (this.firstLine)
    {
      this.firstLine = false;
      if (line.matches("COPY|COPY\\s.*")) {
        this.copyStatement = line;
      }
    }
    else if (this.copyStatement != null)
    {
      this.copyStatement = (this.copyStatement + " " + line);
    }
    if ((this.copyStatement != null) && (this.copyStatement.contains(" FROM STDIN")))
    {
      this.pgCopy = true;
      return COPY_DELIMITER;
    }
    return delimiter;
  }
  
  public boolean isPgCopyFromStdIn()
  {
    return this.pgCopy;
  }
  
  protected String cleanToken(String token)
  {
    if (token.startsWith("E'")) {
      return token.substring(token.indexOf("'"));
    }
    return token;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\postgresql\PostgreSQLSqlStatementBuilder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */