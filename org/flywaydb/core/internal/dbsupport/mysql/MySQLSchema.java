package org.flywaydb.core.internal.dbsupport.mysql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

public class MySQLSchema
  extends Schema<MySQLDbSupport>
{
  public MySQLSchema(JdbcTemplate jdbcTemplate, MySQLDbSupport dbSupport, String name)
  {
    super(jdbcTemplate, dbSupport, name);
  }
  
  protected boolean doExists()
    throws SQLException
  {
    return this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name=?", new String[] { this.name }) > 0;
  }
  
  protected boolean doEmpty()
    throws SQLException
  {
    int objectCount = this.jdbcTemplate.queryForInt("Select (Select count(*) from information_schema.TABLES Where TABLE_SCHEMA=?) + (Select count(*) from information_schema.VIEWS Where TABLE_SCHEMA=?) + (Select count(*) from information_schema.TABLE_CONSTRAINTS Where TABLE_SCHEMA=?) + (Select count(*) from information_schema.EVENTS Where EVENT_SCHEMA=?) + (Select count(*) from information_schema.TRIGGERS Where TRIGGER_SCHEMA=?) + (Select count(*) from information_schema.ROUTINES Where ROUTINE_SCHEMA=?)", new String[] { this.name, this.name, this.name, this.name, this.name, this.name });
    
    return objectCount == 0;
  }
  
  protected void doCreate()
    throws SQLException
  {
    this.jdbcTemplate.execute("CREATE SCHEMA " + ((MySQLDbSupport)this.dbSupport).quote(new String[] { this.name }), new Object[0]);
  }
  
  protected void doDrop()
    throws SQLException
  {
    this.jdbcTemplate.execute("DROP SCHEMA " + ((MySQLDbSupport)this.dbSupport).quote(new String[] { this.name }), new Object[0]);
  }
  
  protected void doClean()
    throws SQLException
  {
    for (Object localObject = cleanEvents().iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = cleanRoutines().iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = cleanViews().iterator(); ((Iterator)localObject).hasNext();)
    {
      statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    this.jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0", new Object[0]);
    localObject = allTables();String statement = localObject.length;
    for (String str1 = 0; str1 < statement; str1++)
    {
      Table table = localObject[str1];
      table.drop();
    }
    this.jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1", new Object[0]);
  }
  
  private List<String> cleanEvents()
    throws SQLException
  {
    List<Map<String, String>> eventNames = this.jdbcTemplate.queryForList("SELECT event_name FROM information_schema.events WHERE event_schema=?", new String[] { this.name });
    
    List<String> statements = new ArrayList();
    for (Map<String, String> row : eventNames) {
      statements.add("DROP EVENT " + ((MySQLDbSupport)this.dbSupport).quote(new String[] { this.name, (String)row.get("event_name") }));
    }
    return statements;
  }
  
  private List<String> cleanRoutines()
    throws SQLException
  {
    List<Map<String, String>> routineNames = this.jdbcTemplate.queryForList("SELECT routine_name, routine_type FROM information_schema.routines WHERE routine_schema=?", new String[] { this.name });
    
    List<String> statements = new ArrayList();
    for (Map<String, String> row : routineNames)
    {
      String routineName = (String)row.get("routine_name");
      String routineType = (String)row.get("routine_type");
      statements.add("DROP " + routineType + " " + ((MySQLDbSupport)this.dbSupport).quote(new String[] { this.name, routineName }));
    }
    return statements;
  }
  
  private List<String> cleanViews()
    throws SQLException
  {
    List<String> viewNames = this.jdbcTemplate.queryForStringList("SELECT table_name FROM information_schema.views WHERE table_schema=?", new String[] { this.name });
    
    List<String> statements = new ArrayList();
    for (String viewName : viewNames) {
      statements.add("DROP VIEW " + ((MySQLDbSupport)this.dbSupport).quote(new String[] { this.name, viewName }));
    }
    return statements;
  }
  
  protected Table[] doAllTables()
    throws SQLException
  {
    List<String> tableNames = this.jdbcTemplate.queryForStringList("SELECT table_name FROM information_schema.tables WHERE table_schema=? AND table_type='BASE TABLE'", new String[] { this.name });
    
    Table[] tables = new Table[tableNames.size()];
    for (int i = 0; i < tableNames.size(); i++) {
      tables[i] = new MySQLTable(this.jdbcTemplate, this.dbSupport, this, (String)tableNames.get(i));
    }
    return tables;
  }
  
  public Table getTable(String tableName)
  {
    return new MySQLTable(this.jdbcTemplate, this.dbSupport, this, tableName);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\mysql\MySQLSchema.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */