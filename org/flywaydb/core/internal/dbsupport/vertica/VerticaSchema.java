package org.flywaydb.core.internal.dbsupport.vertica;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.dbsupport.Type;
import org.flywaydb.core.internal.dbsupport.postgresql.PostgreSQLTable;

public class VerticaSchema
  extends Schema<VerticaDbSupport>
{
  public VerticaSchema(JdbcTemplate jdbcTemplate, VerticaDbSupport dbSupport, String name)
  {
    super(jdbcTemplate, dbSupport, name);
  }
  
  protected boolean doExists()
    throws SQLException
  {
    return this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM v_catalog.schemata WHERE schema_name=?", new String[] { this.name }) > 0;
  }
  
  protected boolean doEmpty()
    throws SQLException
  {
    int objectCount = this.jdbcTemplate.queryForInt("SELECT count(*) FROM v_catalog.all_tables WHERE schema_name=? and table_type = 'TABLE'", new String[] { this.name });
    
    return objectCount == 0;
  }
  
  protected void doCreate()
    throws SQLException
  {
    this.jdbcTemplate.execute("CREATE SCHEMA " + ((VerticaDbSupport)this.dbSupport).quote(new String[] { this.name }), new Object[0]);
  }
  
  protected void doDrop()
    throws SQLException
  {
    this.jdbcTemplate.execute("DROP SCHEMA " + ((VerticaDbSupport)this.dbSupport).quote(new String[] { this.name }) + " CASCADE", new Object[0]);
  }
  
  protected void doClean()
    throws SQLException
  {
    for (Object localObject = generateDropStatementsForViews().iterator(); ((Iterator)localObject).hasNext();)
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
    for (localObject = generateDropStatementsForSequences().iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = generateDropStatementsForFunctions().iterator(); ((Iterator)localObject).hasNext();)
    {
      statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    localObject = allTypes();String statement = localObject.length;
    for (String str2 = 0; str2 < statement; str2++)
    {
      Type type = localObject[str2];
      type.drop();
    }
  }
  
  private List<String> generateDropStatementsForSequences()
    throws SQLException
  {
    List<String> sequenceNames = this.jdbcTemplate.queryForStringList("SELECT sequence_name FROM v_catalog.sequences WHERE sequence_schema=?", new String[] { this.name });
    
    List<String> statements = new ArrayList();
    for (String sequenceName : sequenceNames) {
      statements.add("DROP SEQUENCE IF EXISTS " + ((VerticaDbSupport)this.dbSupport).quote(new String[] { this.name, sequenceName }));
    }
    return statements;
  }
  
  private List<String> generateDropStatementsForFunctions()
    throws SQLException
  {
    List<Map<String, String>> rows = this.jdbcTemplate.queryForList("select * from user_functions where schema_name = ? and procedure_type = 'User Defined Function'", new String[] { this.name });
    
    List<String> statements = new ArrayList();
    for (Map<String, String> row : rows) {
      statements.add("DROP FUNCTION IF EXISTS " + ((VerticaDbSupport)this.dbSupport).quote(new String[] { this.name, (String)row.get("function_name") }) + "(" + (String)row.get("function_argument_type") + ")");
    }
    return statements;
  }
  
  private List<String> generateDropStatementsForViews()
    throws SQLException
  {
    List<String> viewNames = this.jdbcTemplate.queryForStringList("SELECT t.table_name FROM v_catalog.all_tables t WHERE schema_name=? and table_type = 'VIEW'", new String[] { this.name });
    
    List<String> statements = new ArrayList();
    for (String viewName : viewNames) {
      statements.add("DROP VIEW IF EXISTS " + ((VerticaDbSupport)this.dbSupport).quote(new String[] { this.name, viewName }));
    }
    return statements;
  }
  
  protected Table[] doAllTables()
    throws SQLException
  {
    List<String> tableNames = this.jdbcTemplate.queryForStringList("SELECT t.table_name FROM v_catalog.all_tables t WHERE schema_name=? and table_type =  'TABLE'", new String[] { this.name });
    
    Table[] tables = new Table[tableNames.size()];
    for (int i = 0; i < tableNames.size(); i++) {
      tables[i] = new PostgreSQLTable(this.jdbcTemplate, this.dbSupport, this, (String)tableNames.get(i));
    }
    return tables;
  }
  
  public Table getTable(String tableName)
  {
    return new PostgreSQLTable(this.jdbcTemplate, this.dbSupport, this, tableName);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\vertica\VerticaSchema.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */