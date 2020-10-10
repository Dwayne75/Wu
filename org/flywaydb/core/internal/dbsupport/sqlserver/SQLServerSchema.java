package org.flywaydb.core.internal.dbsupport.sqlserver;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

public class SQLServerSchema
  extends Schema<SQLServerDbSupport>
{
  public SQLServerSchema(JdbcTemplate jdbcTemplate, SQLServerDbSupport dbSupport, String name)
  {
    super(jdbcTemplate, dbSupport, name);
  }
  
  protected boolean doExists()
    throws SQLException
  {
    return this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME=?", new String[] { this.name }) > 0;
  }
  
  protected boolean doEmpty()
    throws SQLException
  {
    int objectCount = this.jdbcTemplate.queryForInt("Select count(*) FROM ( Select TABLE_NAME as OBJECT_NAME, TABLE_SCHEMA as OBJECT_SCHEMA from INFORMATION_SCHEMA.TABLES Union Select TABLE_NAME as OBJECT_NAME, TABLE_SCHEMA as OBJECT_SCHEMA from INFORMATION_SCHEMA.VIEWS Union Select CONSTRAINT_NAME as OBJECT_NAME, TABLE_SCHEMA as OBJECT_SCHEMA from INFORMATION_SCHEMA.TABLE_CONSTRAINTS Union Select ROUTINE_NAME as OBJECT_NAME, ROUTINE_SCHEMA as OBJECT_SCHEMA from INFORMATION_SCHEMA.ROUTINES ) R where OBJECT_SCHEMA = ?", new String[] { this.name });
    
    return objectCount == 0;
  }
  
  protected void doCreate()
    throws SQLException
  {
    this.jdbcTemplate.execute("CREATE SCHEMA " + ((SQLServerDbSupport)this.dbSupport).quote(new String[] { this.name }), new Object[0]);
  }
  
  protected void doDrop()
    throws SQLException
  {
    clean();
    this.jdbcTemplate.execute("DROP SCHEMA " + ((SQLServerDbSupport)this.dbSupport).quote(new String[] { this.name }), new Object[0]);
  }
  
  protected void doClean()
    throws SQLException
  {
    for (Object localObject = cleanForeignKeys().iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = cleanDefaultConstraints().iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = cleanRoutines("PROCEDURE").iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = cleanViews().iterator(); ((Iterator)localObject).hasNext();)
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
    for (localObject = cleanRoutines("FUNCTION").iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = cleanTypes().iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = cleanSynonyms().iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    if (this.jdbcTemplate.getMetaData().getDatabaseMajorVersion() >= 11) {
      for (localObject = cleanSequences().iterator(); ((Iterator)localObject).hasNext();)
      {
        String statement = (String)((Iterator)localObject).next();
        this.jdbcTemplate.execute(statement, new Object[0]);
      }
    }
  }
  
  private List<String> cleanForeignKeys()
    throws SQLException
  {
    List<Map<String, String>> constraintNames = this.jdbcTemplate.queryForList("SELECT table_name, constraint_name FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE constraint_type in ('FOREIGN KEY','CHECK') and table_schema=?", new String[] { this.name });
    
    List<String> statements = new ArrayList();
    for (Map<String, String> row : constraintNames)
    {
      String tableName = (String)row.get("table_name");
      String constraintName = (String)row.get("constraint_name");
      statements.add("ALTER TABLE " + ((SQLServerDbSupport)this.dbSupport).quote(new String[] { this.name, tableName }) + " DROP CONSTRAINT " + ((SQLServerDbSupport)this.dbSupport).quote(new String[] { constraintName }));
    }
    return statements;
  }
  
  private List<String> cleanDefaultConstraints()
    throws SQLException
  {
    List<Map<String, String>> constraintNames = this.jdbcTemplate.queryForList("select t.name as table_name, d.name as constraint_name from sys.tables t inner join sys.default_constraints d on d.parent_object_id = t.object_id\n inner join sys.schemas s on s.schema_id = t.schema_id\n where s.name = ?", new String[] { this.name });
    
    List<String> statements = new ArrayList();
    for (Map<String, String> row : constraintNames)
    {
      String tableName = (String)row.get("table_name");
      String constraintName = (String)row.get("constraint_name");
      statements.add("ALTER TABLE " + ((SQLServerDbSupport)this.dbSupport).quote(new String[] { this.name, tableName }) + " DROP CONSTRAINT " + ((SQLServerDbSupport)this.dbSupport).quote(new String[] { constraintName }));
    }
    return statements;
  }
  
  private List<String> cleanRoutines(String routineType)
    throws SQLException
  {
    List<Map<String, String>> routineNames = this.jdbcTemplate.queryForList("SELECT routine_name FROM INFORMATION_SCHEMA.ROUTINES WHERE routine_schema=? AND routine_type=?", new String[] { this.name, routineType });
    
    List<String> statements = new ArrayList();
    for (Map<String, String> row : routineNames)
    {
      String routineName = (String)row.get("routine_name");
      statements.add("DROP " + routineType + " " + ((SQLServerDbSupport)this.dbSupport).quote(new String[] { this.name, routineName }));
    }
    return statements;
  }
  
  private List<String> cleanViews()
    throws SQLException
  {
    List<String> viewNames = this.jdbcTemplate.queryForStringList("SELECT table_name FROM INFORMATION_SCHEMA.VIEWS WHERE table_schema=?", new String[] { this.name });
    
    List<String> statements = new ArrayList();
    for (String viewName : viewNames) {
      statements.add("DROP VIEW " + ((SQLServerDbSupport)this.dbSupport).quote(new String[] { this.name, viewName }));
    }
    return statements;
  }
  
  private List<String> cleanTypes()
    throws SQLException
  {
    List<String> typeNames = this.jdbcTemplate.queryForStringList("SELECT t.name FROM sys.types t INNER JOIN sys.schemas s ON t.schema_id = s.schema_id WHERE t.is_user_defined = 1 AND s.name = ?", new String[] { this.name });
    
    List<String> statements = new ArrayList();
    for (String typeName : typeNames) {
      statements.add("DROP TYPE " + ((SQLServerDbSupport)this.dbSupport).quote(new String[] { this.name, typeName }));
    }
    return statements;
  }
  
  private List<String> cleanSynonyms()
    throws SQLException
  {
    List<String> synonymNames = this.jdbcTemplate.queryForStringList("SELECT sn.name FROM sys.synonyms sn INNER JOIN sys.schemas s ON sn.schema_id = s.schema_id WHERE s.name = ?", new String[] { this.name });
    
    List<String> statements = new ArrayList();
    for (String synonymName : synonymNames) {
      statements.add("DROP SYNONYM " + ((SQLServerDbSupport)this.dbSupport).quote(new String[] { this.name, synonymName }));
    }
    return statements;
  }
  
  private List<String> cleanSequences()
    throws SQLException
  {
    List<String> names = this.jdbcTemplate.queryForStringList("SELECT sequence_name FROM INFORMATION_SCHEMA.SEQUENCES WHERE sequence_schema=?", new String[] { this.name });
    
    List<String> statements = new ArrayList();
    for (String sequenceName : names) {
      statements.add("DROP SEQUENCE " + ((SQLServerDbSupport)this.dbSupport).quote(new String[] { this.name, sequenceName }));
    }
    return statements;
  }
  
  protected Table[] doAllTables()
    throws SQLException
  {
    List<String> tableNames = this.jdbcTemplate.queryForStringList("SELECT table_name FROM INFORMATION_SCHEMA.TABLES WHERE table_type='BASE TABLE' and table_schema=?", new String[] { this.name });
    
    Table[] tables = new Table[tableNames.size()];
    for (int i = 0; i < tableNames.size(); i++) {
      tables[i] = new SQLServerTable(this.jdbcTemplate, this.dbSupport, this, (String)tableNames.get(i));
    }
    return tables;
  }
  
  public Table getTable(String tableName)
  {
    return new SQLServerTable(this.jdbcTemplate, this.dbSupport, this, tableName);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\sqlserver\SQLServerSchema.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */