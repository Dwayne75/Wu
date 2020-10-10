package org.flywaydb.core.internal.dbsupport;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;

public abstract class Schema<S extends DbSupport>
{
  protected final JdbcTemplate jdbcTemplate;
  protected final S dbSupport;
  protected final String name;
  
  public Schema(JdbcTemplate jdbcTemplate, S dbSupport, String name)
  {
    this.jdbcTemplate = jdbcTemplate;
    this.dbSupport = dbSupport;
    this.name = name;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public boolean exists()
  {
    try
    {
      return doExists();
    }
    catch (SQLException e)
    {
      throw new FlywayException("Unable to check whether schema " + this + " exists", e);
    }
  }
  
  protected abstract boolean doExists()
    throws SQLException;
  
  public boolean empty()
  {
    try
    {
      return doEmpty();
    }
    catch (SQLException e)
    {
      throw new FlywayException("Unable to check whether schema " + this + " is empty", e);
    }
  }
  
  protected abstract boolean doEmpty()
    throws SQLException;
  
  public void create()
  {
    try
    {
      doCreate();
    }
    catch (SQLException e)
    {
      throw new FlywayException("Unable to create schema " + this, e);
    }
  }
  
  protected abstract void doCreate()
    throws SQLException;
  
  public void drop()
  {
    try
    {
      doDrop();
    }
    catch (SQLException e)
    {
      throw new FlywayException("Unable to drop schema " + this, e);
    }
  }
  
  protected abstract void doDrop()
    throws SQLException;
  
  public void clean()
  {
    try
    {
      doClean();
    }
    catch (SQLException e)
    {
      throw new FlywayException("Unable to clean schema " + this, e);
    }
  }
  
  protected abstract void doClean()
    throws SQLException;
  
  public Table[] allTables()
  {
    try
    {
      return doAllTables();
    }
    catch (SQLException e)
    {
      throw new FlywayException("Unable to retrieve all tables in schema " + this, e);
    }
  }
  
  protected abstract Table[] doAllTables()
    throws SQLException;
  
  public final Type[] allTypes()
  {
    ResultSet resultSet = null;
    try
    {
      resultSet = this.jdbcTemplate.getMetaData().getUDTs(null, this.name, null, null);
      
      List<Type> types = new ArrayList();
      while (resultSet.next()) {
        types.add(getType(resultSet.getString("TYPE_NAME")));
      }
      return (Type[])types.toArray(new Type[types.size()]);
    }
    catch (SQLException e)
    {
      throw new FlywayException("Unable to retrieve all types in schema " + this, e);
    }
    finally
    {
      JdbcUtils.closeResultSet(resultSet);
    }
  }
  
  protected Type getType(String typeName)
  {
    return null;
  }
  
  public abstract Table getTable(String paramString);
  
  public Function getFunction(String functionName, String... args)
  {
    throw new UnsupportedOperationException("getFunction()");
  }
  
  public final Function[] allFunctions()
  {
    try
    {
      return doAllFunctions();
    }
    catch (SQLException e)
    {
      throw new FlywayException("Unable to retrieve all functions in schema " + this, e);
    }
  }
  
  protected Function[] doAllFunctions()
    throws SQLException
  {
    return new Function[0];
  }
  
  public String toString()
  {
    return this.dbSupport.quote(new String[] { this.name });
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    Schema schema = (Schema)o;
    return this.name.equals(schema.name);
  }
  
  public int hashCode()
  {
    return this.name.hashCode();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\Schema.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */