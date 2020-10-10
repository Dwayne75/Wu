package org.flywaydb.core.internal.dbsupport.sybase.ase;

import org.flywaydb.core.internal.dbsupport.Delimiter;
import org.flywaydb.core.internal.dbsupport.SqlStatementBuilder;

public class SybaseASESqlStatementBuilder
  extends SqlStatementBuilder
{
  protected Delimiter getDefaultDelimiter()
  {
    return new Delimiter("GO", true);
  }
  
  protected String computeAlternateCloseQuote(String openQuote)
  {
    char specialChar = openQuote.charAt(2);
    switch (specialChar)
    {
    case '(': 
      return ")'";
    }
    return specialChar + "'";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\sybase\ase\SybaseASESqlStatementBuilder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */