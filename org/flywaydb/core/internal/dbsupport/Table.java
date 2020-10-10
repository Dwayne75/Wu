package org.flywaydb.core.internal.dbsupport;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public abstract class Table
  extends SchemaObject
{
  private static final Log LOG = LogFactory.getLog(Table.class);
  
  public Table(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name)
  {
    super(jdbcTemplate, dbSupport, schema, name);
  }
  
  public boolean exists()
  {
    try
    {
      return doExists();
    }
    catch (SQLException e)
    {
      throw new FlywayException("Unable to check whether table " + this + " exists", e);
    }
  }
  
  protected abstract boolean doExists()
    throws SQLException;
  
  protected boolean exists(Schema catalog, Schema schema, String table, String... tableTypes)
    throws SQLException
  {
    String[] types = tableTypes;
    if (types.length == 0) {
      types = null;
    }
    ResultSet resultSet = null;
    try
    {
      resultSet = this.jdbcTemplate.getMetaData().getTables(catalog == null ? null : catalog
        .getName(), schema == null ? null : schema
        .getName(), table, types);
      
      found = resultSet.next();
    }
    finally
    {
      boolean found;
      JdbcUtils.closeResultSet(resultSet);
    }
    boolean found;
    return found;
  }
  
  public boolean hasPrimaryKey()
  {
    ResultSet resultSet = null;
    try
    {
      if (this.dbSupport.catalogIsSchema()) {
        resultSet = this.jdbcTemplate.getMetaData().getPrimaryKeys(this.schema.getName(), null, this.name);
      } else {
        resultSet = this.jdbcTemplate.getMetaData().getPrimaryKeys(null, this.schema.getName(), this.name);
      }
      found = resultSet.next();
    }
    catch (SQLException e)
    {
      boolean found;
      throw new FlywayException("Unable to check whether table " + this + " has a primary key", e);
    }
    finally
    {
      JdbcUtils.closeResultSet(resultSet);
    }
    boolean found;
    return found;
  }
  
  public boolean hasColumn(String column)
  {
    ResultSet resultSet = null;
    try
    {
      if (this.dbSupport.catalogIsSchema()) {
        resultSet = this.jdbcTemplate.getMetaData().getColumns(this.schema.getName(), null, this.name, column);
      } else {
        resultSet = this.jdbcTemplate.getMetaData().getColumns(null, this.schema.getName(), this.name, column);
      }
      found = resultSet.next();
    }
    catch (SQLException e)
    {
      boolean found;
      throw new FlywayException("Unable to check whether table " + this + " has a column named " + column, e);
    }
    finally
    {
      JdbcUtils.closeResultSet(resultSet);
    }
    boolean found;
    return found;
  }
  
  public int getColumnSize(String column)
  {
    ResultSet resultSet = null;
    try
    {
      if (this.dbSupport.catalogIsSchema()) {
        resultSet = this.jdbcTemplate.getMetaData().getColumns(this.schema.getName(), null, this.name, column);
      } else {
        resultSet = this.jdbcTemplate.getMetaData().getColumns(null, this.schema.getName(), this.name, column);
      }
      resultSet.next();
      columnSize = resultSet.getInt("COLUMN_SIZE");
    }
    catch (SQLException e)
    {
      int columnSize;
      throw new FlywayException("Unable to check the size of column " + column + " in table " + this, e);
    }
    finally
    {
      JdbcUtils.closeResultSet(resultSet);
    }
    int columnSize;
    return columnSize;
  }
  
  public void lock()
  {
    try
    {
      LOG.debug("Locking table " + this + "...");
      doLock();
      LOG.debug("Lock acquired for table " + this);
    }
    catch (SQLException e)
    {
      throw new FlywayException("Unable to lock table " + this, e);
    }
  }
  
  protected abstract void doLock()
    throws SQLException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\Table.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */