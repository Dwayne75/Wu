package org.seamless.util.dbunit;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;

public abstract class DBUnitOperations
  extends ArrayList<Op>
{
  private static final Logger log = Logger.getLogger(DBUnitOperations.class.getName());
  public abstract DataSource getDataSource();
  
  public static abstract class Op
  {
    ReplacementDataSet dataSet;
    DatabaseOperation operation;
    
    public Op(String dataLocation)
    {
      this(dataLocation, null, DatabaseOperation.CLEAN_INSERT);
    }
    
    public Op(String dataLocation, String dtdLocation)
    {
      this(dataLocation, dtdLocation, DatabaseOperation.CLEAN_INSERT);
    }
    
    public Op(String dataLocation, String dtdLocation, DatabaseOperation operation)
    {
      try
      {
        this.dataSet = (dtdLocation != null ? new ReplacementDataSet(new FlatXmlDataSet(openStream(dataLocation), openStream(dtdLocation))) : new ReplacementDataSet(new FlatXmlDataSet(openStream(dataLocation))));
      }
      catch (Exception ex)
      {
        throw new RuntimeException(ex);
      }
      this.dataSet.addReplacementObject("[NULL]", null);
      this.operation = operation;
    }
    
    public IDataSet getDataSet()
    {
      return this.dataSet;
    }
    
    public DatabaseOperation getOperation()
    {
      return this.operation;
    }
    
    public void execute(IDatabaseConnection connection)
    {
      try
      {
        this.operation.execute(connection, this.dataSet);
      }
      catch (Exception ex)
      {
        throw new RuntimeException(ex);
      }
    }
    
    protected abstract InputStream openStream(String paramString);
  }
  
  public static class ClasspathOp
    extends DBUnitOperations.Op
  {
    public ClasspathOp(String dataLocation)
    {
      super();
    }
    
    public ClasspathOp(String dataLocation, String dtdLocation)
    {
      super(dtdLocation);
    }
    
    public ClasspathOp(String dataLocation, String dtdLocation, DatabaseOperation operation)
    {
      super(dtdLocation, operation);
    }
    
    protected InputStream openStream(String location)
    {
      return Thread.currentThread().getContextClassLoader().getResourceAsStream(location);
    }
  }
  
  public class FileOp
    extends DBUnitOperations.Op
  {
    public FileOp(String dataLocation)
    {
      super();
    }
    
    public FileOp(String dataLocation, String dtdLocation)
    {
      super(dtdLocation);
    }
    
    public FileOp(String dataLocation, String dtdLocation, DatabaseOperation operation)
    {
      super(dtdLocation, operation);
    }
    
    protected InputStream openStream(String location)
    {
      try
      {
        return new FileInputStream(location);
      }
      catch (FileNotFoundException ex)
      {
        throw new RuntimeException(ex);
      }
    }
  }
  
  public void execute()
  {
    log.info("Executing DBUnit operations: " + size());
    IDatabaseConnection con = null;
    try
    {
      con = getConnection();
      disableReferentialIntegrity(con);
      for (Op op : this) {
        op.execute(con);
      }
      enableReferentialIntegrity(con); return;
    }
    finally
    {
      if (con != null) {
        try
        {
          con.close();
        }
        catch (Exception ex)
        {
          log.log(Level.WARNING, "Failed to close connection after DBUnit operation: " + ex, ex);
        }
      }
    }
  }
  
  protected IDatabaseConnection getConnection()
  {
    try
    {
      DataSource datasource = getDataSource();
      Connection con = datasource.getConnection();
      IDatabaseConnection dbUnitCon = new DatabaseConnection(con);
      editConfig(dbUnitCon.getConfig());
      return dbUnitCon;
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  protected abstract void disableReferentialIntegrity(IDatabaseConnection paramIDatabaseConnection);
  
  protected abstract void enableReferentialIntegrity(IDatabaseConnection paramIDatabaseConnection);
  
  protected void editConfig(DatabaseConfig config) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\util\dbunit\DBUnitOperations.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */