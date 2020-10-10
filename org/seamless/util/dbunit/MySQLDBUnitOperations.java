package org.seamless.util.dbunit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import org.dbunit.database.IDatabaseConnection;

public abstract class MySQLDBUnitOperations
  extends DBUnitOperations
{
  protected void disableReferentialIntegrity(IDatabaseConnection con)
  {
    try
    {
      con.getConnection().prepareStatement("set foreign_key_checks=0").execute();
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  protected void enableReferentialIntegrity(IDatabaseConnection con)
  {
    try
    {
      con.getConnection().prepareStatement("set foreign_key_checks=1").execute();
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\util\dbunit\MySQLDBUnitOperations.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */