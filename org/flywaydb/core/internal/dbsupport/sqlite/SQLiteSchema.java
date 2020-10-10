package org.flywaydb.core.internal.dbsupport.sqlite;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public class SQLiteSchema
  extends Schema<SQLiteDbSupport>
{
  private static final Log LOG = LogFactory.getLog(SQLiteSchema.class);
  
  public SQLiteSchema(JdbcTemplate jdbcTemplate, SQLiteDbSupport dbSupport, String name)
  {
    super(jdbcTemplate, dbSupport, name);
  }
  
  protected boolean doExists()
    throws SQLException
  {
    try
    {
      doAllTables();
      return true;
    }
    catch (SQLException e) {}
    return false;
  }
  
  protected boolean doEmpty()
    throws SQLException
  {
    Table[] tables = allTables();
    return (tables.length == 0) || ((tables.length == 1) && ("android_metadata".equals(tables[0].getName())));
  }
  
  protected void doCreate()
    throws SQLException
  {
    LOG.info("SQLite does not support creating schemas. Schema not created: " + this.name);
  }
  
  protected void doDrop()
    throws SQLException
  {
    LOG.info("SQLite does not support dropping schemas. Schema not dropped: " + this.name);
  }
  
  protected void doClean()
    throws SQLException
  {
    List<String> viewNames = this.jdbcTemplate.queryForStringList("SELECT tbl_name FROM " + ((SQLiteDbSupport)this.dbSupport).quote(new String[] { this.name }) + ".sqlite_master WHERE type='view'", new String[0]);
    for (Object localObject = viewNames.iterator(); ((Iterator)localObject).hasNext();)
    {
      viewName = (String)((Iterator)localObject).next();
      this.jdbcTemplate.executeStatement("DROP VIEW " + ((SQLiteDbSupport)this.dbSupport).quote(new String[] { this.name, viewName }));
    }
    localObject = allTables();String viewName = localObject.length;
    for (String str1 = 0; str1 < viewName; str1++)
    {
      Table table = localObject[str1];
      table.drop();
    }
  }
  
  protected Table[] doAllTables()
    throws SQLException
  {
    List<String> tableNames = this.jdbcTemplate.queryForStringList("SELECT tbl_name FROM " + ((SQLiteDbSupport)this.dbSupport).quote(new String[] { this.name }) + ".sqlite_master WHERE type='table'", new String[0]);
    
    Table[] tables = new Table[tableNames.size()];
    for (int i = 0; i < tableNames.size(); i++) {
      tables[i] = new SQLiteTable(this.jdbcTemplate, this.dbSupport, this, (String)tableNames.get(i));
    }
    return tables;
  }
  
  public Table getTable(String tableName)
  {
    return new SQLiteTable(this.jdbcTemplate, this.dbSupport, this, tableName);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\sqlite\SQLiteSchema.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */