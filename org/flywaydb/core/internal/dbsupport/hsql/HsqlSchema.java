package org.flywaydb.core.internal.dbsupport.hsql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

public class HsqlSchema
  extends Schema<HsqlDbSupport>
{
  public HsqlSchema(JdbcTemplate jdbcTemplate, HsqlDbSupport dbSupport, String name)
  {
    super(jdbcTemplate, dbSupport, name);
  }
  
  protected boolean doExists()
    throws SQLException
  {
    return this.jdbcTemplate.queryForInt("SELECT COUNT (*) FROM information_schema.system_schemas WHERE table_schem=?", new String[] { this.name }) > 0;
  }
  
  protected boolean doEmpty()
    throws SQLException
  {
    return allTables().length == 0;
  }
  
  protected void doCreate()
    throws SQLException
  {
    String user = this.jdbcTemplate.queryForString("SELECT USER() FROM (VALUES(0))", new String[0]);
    this.jdbcTemplate.execute("CREATE SCHEMA " + ((HsqlDbSupport)this.dbSupport).quote(new String[] { this.name }) + " AUTHORIZATION " + user, new Object[0]);
  }
  
  protected void doDrop()
    throws SQLException
  {
    this.jdbcTemplate.execute("DROP SCHEMA " + ((HsqlDbSupport)this.dbSupport).quote(new String[] { this.name }) + " CASCADE", new Object[0]);
  }
  
  protected void doClean()
    throws SQLException
  {
    for (Table table : allTables()) {
      table.drop();
    }
    for (??? = generateDropStatementsForSequences().iterator(); ((Iterator)???).hasNext();)
    {
      String statement = (String)((Iterator)???).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
  }
  
  private List<String> generateDropStatementsForSequences()
    throws SQLException
  {
    List<String> sequenceNames = this.jdbcTemplate.queryForStringList("SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES where SEQUENCE_SCHEMA = ?", new String[] { this.name });
    
    List<String> statements = new ArrayList();
    for (String seqName : sequenceNames) {
      statements.add("DROP SEQUENCE " + ((HsqlDbSupport)this.dbSupport).quote(new String[] { this.name, seqName }));
    }
    return statements;
  }
  
  protected Table[] doAllTables()
    throws SQLException
  {
    List<String> tableNames = this.jdbcTemplate.queryForStringList("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.SYSTEM_TABLES where TABLE_SCHEM = ? AND TABLE_TYPE = 'TABLE'", new String[] { this.name });
    
    Table[] tables = new Table[tableNames.size()];
    for (int i = 0; i < tableNames.size(); i++) {
      tables[i] = new HsqlTable(this.jdbcTemplate, this.dbSupport, this, (String)tableNames.get(i));
    }
    return tables;
  }
  
  public Table getTable(String tableName)
  {
    return new HsqlTable(this.jdbcTemplate, this.dbSupport, this, tableName);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\hsql\HsqlSchema.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */