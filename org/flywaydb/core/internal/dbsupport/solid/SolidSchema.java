package org.flywaydb.core.internal.dbsupport.solid;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

public class SolidSchema
  extends Schema<SolidDbSupport>
{
  public SolidSchema(JdbcTemplate jdbcTemplate, SolidDbSupport dbSupport, String name)
  {
    super(jdbcTemplate, dbSupport, name.toUpperCase());
  }
  
  protected boolean doExists()
    throws SQLException
  {
    return this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM _SYSTEM.SYS_SCHEMAS WHERE NAME = ?", new String[] { this.name }) > 0;
  }
  
  protected boolean doEmpty()
    throws SQLException
  {
    int count = this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM _SYSTEM.SYS_TABLES WHERE TABLE_SCHEMA = ?", new String[] { this.name });
    if (count > 0) {
      return false;
    }
    count = this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM _SYSTEM.SYS_TRIGGERS WHERE TRIGGER_SCHEMA = ?", new String[] { this.name });
    if (count > 0) {
      return false;
    }
    count = this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM _SYSTEM.SYS_PROCEDURES WHERE PROCEDURE_SCHEMA = ?", new String[] { this.name });
    if (count > 0) {
      return false;
    }
    count = this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM _SYSTEM.SYS_FORKEYS WHERE KEY_SCHEMA = ?", new String[] { this.name });
    if (count > 0) {
      return false;
    }
    return true;
  }
  
  protected void doCreate()
    throws SQLException
  {
    this.jdbcTemplate.execute("CREATE SCHEMA " + this.name, new Object[0]);
  }
  
  protected void doDrop()
    throws SQLException
  {
    clean();
    this.jdbcTemplate.execute("DROP SCHEMA " + this.name, new Object[0]);
  }
  
  protected void doClean()
    throws SQLException
  {
    for (Object localObject = dropTriggers().iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = dropProcedures().iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = dropConstraints().iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = dropViews().iterator(); ((Iterator)localObject).hasNext();)
    {
      statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    localObject = allTables();String statement = localObject.length;
    for (String str1 = 0; str1 < statement; str1++)
    {
      Table table = localObject[str1];
      table.drop();
    }
  }
  
  protected Table[] doAllTables()
    throws SQLException
  {
    List<String> tableNames = this.jdbcTemplate.queryForStringList("SELECT TABLE_NAME FROM _SYSTEM.SYS_TABLES WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = 'BASE TABLE'", new String[] { this.name });
    
    Table[] tables = new Table[tableNames.size()];
    for (int i = 0; i < tableNames.size(); i++) {
      tables[i] = new SolidTable(this.jdbcTemplate, this.dbSupport, this, (String)tableNames.get(i));
    }
    return tables;
  }
  
  public Table getTable(String tableName)
  {
    return new SolidTable(this.jdbcTemplate, this.dbSupport, this, tableName);
  }
  
  private Iterable<String> dropTriggers()
    throws SQLException
  {
    List<String> statements = new ArrayList();
    for (Map<String, String> item : this.jdbcTemplate.queryForList("SELECT TRIGGER_NAME FROM _SYSTEM.SYS_TRIGGERS WHERE TRIGGER_SCHEMA = ?", new String[] { this.name })) {
      statements.add("DROP TRIGGER " + ((SolidDbSupport)this.dbSupport).quote(new String[] { this.name, (String)item.get("TRIGGER_NAME") }));
    }
    return statements;
  }
  
  private Iterable<String> dropProcedures()
    throws SQLException
  {
    List<String> statements = new ArrayList();
    for (Map<String, String> item : this.jdbcTemplate.queryForList("SELECT PROCEDURE_NAME FROM _SYSTEM.SYS_PROCEDURES WHERE PROCEDURE_SCHEMA = ?", new String[] { this.name })) {
      statements.add("DROP PROCEDURE " + ((SolidDbSupport)this.dbSupport).quote(new String[] { this.name, (String)item.get("PROCEDURE_NAME") }));
    }
    return statements;
  }
  
  private Iterable<String> dropConstraints()
    throws SQLException
  {
    List<String> statements = new ArrayList();
    for (Map<String, String> item : this.jdbcTemplate.queryForList("SELECT TABLE_NAME, KEY_NAME FROM _SYSTEM.SYS_FORKEYS, _SYSTEM.SYS_TABLES WHERE SYS_FORKEYS.KEY_SCHEMA = ? AND SYS_FORKEYS.CREATE_REL_ID = SYS_FORKEYS.REF_REL_ID AND SYS_FORKEYS.CREATE_REL_ID = SYS_TABLES.ID", new String[] { this.name })) {
      statements.add("ALTER TABLE " + ((SolidDbSupport)this.dbSupport)
        .quote(new String[] { this.name, (String)item.get("TABLE_NAME") }) + " DROP CONSTRAINT " + ((SolidDbSupport)this.dbSupport)
        .quote(new String[] {(String)item.get("KEY_NAME") }));
    }
    return statements;
  }
  
  private Iterable<String> dropViews()
    throws SQLException
  {
    List<String> statements = new ArrayList();
    for (Map<String, String> item : this.jdbcTemplate.queryForList("SELECT TABLE_NAME FROM _SYSTEM.SYS_TABLES WHERE TABLE_TYPE = 'VIEW' AND TABLE_SCHEMA = ?", new String[] { this.name })) {
      statements.add("DROP VIEW " + ((SolidDbSupport)this.dbSupport).quote(new String[] { this.name, (String)item.get("TABLE_NAME") }));
    }
    return statements;
  }
  
  private void commitWork()
    throws SQLException
  {
    this.jdbcTemplate.executeStatement("COMMIT WORK");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\solid\SolidSchema.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */