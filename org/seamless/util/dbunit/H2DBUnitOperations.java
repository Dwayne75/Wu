package org.seamless.util.dbunit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;

public abstract class H2DBUnitOperations
  extends DBUnitOperations
{
  protected void disableReferentialIntegrity(IDatabaseConnection con)
  {
    try
    {
      con.getConnection().prepareStatement("set referential_integrity FALSE").execute();
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
      con.getConnection().prepareStatement("set referential_integrity TRUE").execute();
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  protected void editConfig(DatabaseConfig config)
  {
    super.editConfig(config);
    
    config.setProperty("http://www.dbunit.org/properties/datatypeFactory", new DefaultDataTypeFactory()
    {
      public DataType createDataType(int sqlType, String sqlTypeName)
        throws DataTypeException
      {
        if (sqlType == 16) {
          return DataType.BOOLEAN;
        }
        return super.createDataType(sqlType, sqlTypeName);
      }
    });
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\util\dbunit\H2DBUnitOperations.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */