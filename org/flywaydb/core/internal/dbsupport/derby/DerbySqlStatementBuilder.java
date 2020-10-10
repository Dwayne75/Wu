package org.flywaydb.core.internal.dbsupport.derby;

import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

public class DerbySqlStatementBuilder
  extends SqlStatementBuilder
{
  protected String extractAlternateOpenQuote(String token)
  {
    if (token.startsWith("$$")) {
      return "$$";
    }
    return null;
  }
  
  protected String cleanToken(String token)
  {
    if (token.startsWith("X'")) {
      return token.substring(token.indexOf("'"));
    }
    return super.cleanToken(token);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\derby\DerbySqlStatementBuilder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */