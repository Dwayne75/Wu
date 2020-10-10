package org.flywaydb.core.internal.dbsupport.sqlserver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

public class SQLServerSqlStatementBuilder
  extends SqlStatementBuilder
{
  private static final Pattern KEYWORDS_BEFORE_STRING_LITERAL_REGEX = Pattern.compile("^(LIKE)('.*)");
  
  protected Delimiter getDefaultDelimiter()
  {
    return new Delimiter("GO", true);
  }
  
  protected String cleanToken(String token)
  {
    if (token.startsWith("N'")) {
      return token.substring(token.indexOf("'"));
    }
    Matcher beforeMatcher = KEYWORDS_BEFORE_STRING_LITERAL_REGEX.matcher(token);
    if (beforeMatcher.find()) {
      token = beforeMatcher.group(2);
    }
    return token;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\sqlserver\SQLServerSqlStatementBuilder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */