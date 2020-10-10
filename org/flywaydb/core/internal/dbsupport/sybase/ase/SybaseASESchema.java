package org.flywaydb.core.internal.dbsupport.sybase.ase;

import java.sql.SQLException;
import java.util.List;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

public class SybaseASESchema
  extends Schema<SybaseASEDbSupport>
{
  public SybaseASESchema(JdbcTemplate jdbcTemplate, SybaseASEDbSupport dbSupport, String name)
  {
    super(jdbcTemplate, dbSupport, name);
  }
  
  protected boolean doExists()
    throws SQLException
  {
    return true;
  }
  
  protected boolean doEmpty()
    throws SQLException
  {
    return this.jdbcTemplate.queryForInt("select count(*) from sysobjects ob where (ob.type='U' or ob.type = 'V' or ob.type = 'P' or ob.type = 'TR') and ob.name != 'sysquerymetrics'", new String[0]) == 0;
  }
  
  protected void doCreate()
    throws SQLException
  {}
  
  protected void doDrop()
    throws SQLException
  {}
  
  protected void doClean()
    throws SQLException
  {
    dropObjects("U");
    
    dropObjects("V");
    
    dropObjects("P");
    
    dropObjects("TR");
  }
  
  protected Table[] doAllTables()
    throws SQLException
  {
    List<String> tableNames = retrieveAllTableNames();
    
    Table[] result = new Table[tableNames.size()];
    for (int i = 0; i < tableNames.size(); i++)
    {
      String tableName = (String)tableNames.get(i);
      result[i] = new SybaseASETable(this.jdbcTemplate, this.dbSupport, this, tableName);
    }
    return result;
  }
  
  public Table getTable(String tableName)
  {
    return new SybaseASETable(this.jdbcTemplate, this.dbSupport, this, tableName);
  }
  
  private List<String> retrieveAllTableNames()
    throws SQLException
  {
    List<String> objNames = this.jdbcTemplate.queryForStringList("select ob.name from sysobjects ob where ob.type=? order by ob.name", new String[] { "U" });
    
    return objNames;
  }
  
  private void dropObjects(String sybaseObjType)
    throws SQLException
  {
    List<String> objNames = this.jdbcTemplate.queryForStringList("select ob.name from sysobjects ob where ob.type=? order by ob.name", new String[] { sybaseObjType });
    for (String name : objNames)
    {
      String sql = "";
      if ("U".equals(sybaseObjType)) {
        sql = "drop table ";
      } else if ("V".equals(sybaseObjType)) {
        sql = "drop view ";
      } else if ("P".equals(sybaseObjType)) {
        sql = "drop procedure ";
      } else if ("TR".equals(sybaseObjType)) {
        sql = "drop trigger ";
      } else {
        throw new IllegalArgumentException("Unknown database object type " + sybaseObjType);
      }
      this.jdbcTemplate.execute(sql + name, new Object[0]);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\sybase\ase\SybaseASESchema.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */