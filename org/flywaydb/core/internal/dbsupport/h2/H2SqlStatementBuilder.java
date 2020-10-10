package org.flywaydb.core.internal.dbsupport.h2;

import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

public class H2SqlStatementBuilder
  extends SqlStatementBuilder
{
  protected String extractAlternateOpenQuote(String token)
  {
    if (token.startsWith("$$")) {
      return "$$";
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\h2\H2SqlStatementBuilder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */