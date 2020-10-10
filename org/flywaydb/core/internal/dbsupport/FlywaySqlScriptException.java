package org.flywaydb.core.internal.dbsupport;

import java.sql.SQLException;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.scanner.Resource;

public class FlywaySqlScriptException
  extends FlywayException
{
  private final Resource resource;
  private final SqlStatement statement;
  
  public FlywaySqlScriptException(Resource resource, SqlStatement statement, SQLException sqlException)
  {
    super(sqlException);
    this.resource = resource;
    this.statement = statement;
  }
  
  public int getLineNumber()
  {
    return this.statement.getLineNumber();
  }
  
  public String getStatement()
  {
    return this.statement.getSql();
  }
  
  public String getMessage()
  {
    String title = "Migration " + this.resource.getFilename() + " failed";
    String underline = StringUtils.trimOrPad("", title.length(), '-');
    
    SQLException cause = (SQLException)getCause();
    while (cause.getNextException() != null) {
      cause = cause.getNextException();
    }
    String message = "\n" + title + "\n" + underline + "\n";
    message = message + "SQL State  : " + cause.getSQLState() + "\n";
    message = message + "Error Code : " + cause.getErrorCode() + "\n";
    if (cause.getMessage() != null) {
      message = message + "Message    : " + cause.getMessage().trim() + "\n";
    }
    if (this.resource != null) {
      message = message + "Location   : " + this.resource.getLocation() + " (" + this.resource.getLocationOnDisk() + ")\n";
    }
    message = message + "Line       : " + getLineNumber() + "\n";
    message = message + "Statement  : " + getStatement() + "\n";
    
    return message;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\FlywaySqlScriptException.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */