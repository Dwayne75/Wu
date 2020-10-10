package org.flywaydb.core.internal.dbsupport.db2zos;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.flywaydb.core.internal.dbsupport.Function;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.dbsupport.Type;

public class DB2zosSchema
  extends Schema<DB2zosDbSupport>
{
  public DB2zosSchema(JdbcTemplate jdbcTemplate, DB2zosDbSupport dbSupport, String name)
  {
    super(jdbcTemplate, dbSupport, name);
  }
  
  protected boolean doExists()
    throws SQLException
  {
    return this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM sysibm.sysdatabase WHERE name=?", new String[] { this.name }) > 0;
  }
  
  protected boolean doEmpty()
    throws SQLException
  {
    int objectCount = this.jdbcTemplate.queryForInt("select count(*) from sysibm.systables where dbname = ?", new String[] { this.name });
    objectCount += this.jdbcTemplate.queryForInt("select count(*) from sysibm.systables where creator = ?", new String[] { this.name });
    objectCount += this.jdbcTemplate.queryForInt("select count(*) from sysibm.syssequences where schema = ?", new String[] { this.name });
    objectCount += this.jdbcTemplate.queryForInt("select count(*) from sysibm.sysindexes where dbname = ?", new String[] { this.name });
    objectCount += this.jdbcTemplate.queryForInt("select count(*) from sysibm.sysroutines where schema = ?", new String[] { this.name });
    return objectCount == 0;
  }
  
  protected void doCreate()
    throws SQLException
  {
    throw new UnsupportedOperationException("Create Schema - is not supported in db2 on zOS");
  }
  
  protected void doDrop()
    throws SQLException
  {
    throw new UnsupportedOperationException("Drop Schema - is not supported in db2 on zOS");
  }
  
  protected void doClean()
    throws SQLException
  {
    for (Object localObject = generateDropStatements(this.name, "V", "VIEW").iterator(); ((Iterator)localObject).hasNext();)
    {
      String dropStatement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(dropStatement, new Object[0]);
    }
    for (localObject = generateDropStatements(this.name, "A", "ALIAS").iterator(); ((Iterator)localObject).hasNext();)
    {
      dropStatement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(dropStatement, new Object[0]);
    }
    localObject = allTables();String dropStatement = localObject.length;
    for (String str1 = 0; str1 < dropStatement; str1++)
    {
      Table table = localObject[str1];
      table.drop();
    }
    for (localObject = generateDropStatementsForTestTable(this.name, "T", "TABLE").iterator(); ((Iterator)localObject).hasNext();)
    {
      String dropStatement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(dropStatement, new Object[0]);
    }
    for (localObject = generateDropStatementsForTablespace(this.name).iterator(); ((Iterator)localObject).hasNext();)
    {
      String dropStatement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(dropStatement, new Object[0]);
    }
    for (localObject = generateDropStatementsForSequences(this.name).iterator(); ((Iterator)localObject).hasNext();)
    {
      String dropStatement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(dropStatement, new Object[0]);
    }
    for (localObject = generateDropStatementsForProcedures(this.name).iterator(); ((Iterator)localObject).hasNext();)
    {
      String dropStatement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(dropStatement, new Object[0]);
    }
    for (localObject = generateDropStatementsForFunctions(this.name).iterator(); ((Iterator)localObject).hasNext();)
    {
      String dropStatement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(dropStatement, new Object[0]);
    }
    for (localObject = generateDropStatementsForUserTypes(this.name).iterator(); ((Iterator)localObject).hasNext();)
    {
      String dropStatement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(dropStatement, new Object[0]);
    }
  }
  
  private List<String> generateDropStatementsForProcedures(String schema)
    throws SQLException
  {
    String dropProcGenQuery = "select rtrim(NAME) from SYSIBM.SYSROUTINES where CAST_FUNCTION = 'N'  and ROUTINETYPE  = 'P' and SCHEMA = '" + schema + "'";
    
    return buildDropStatements("DROP PROCEDURE", dropProcGenQuery, schema);
  }
  
  private List<String> generateDropStatementsForFunctions(String schema)
    throws SQLException
  {
    String dropProcGenQuery = "select rtrim(NAME) from SYSIBM.SYSROUTINES where CAST_FUNCTION = 'N'  and ROUTINETYPE  = 'F' and SCHEMA = '" + schema + "'";
    
    return buildDropStatements("DROP FUNCTION", dropProcGenQuery, schema);
  }
  
  private List<String> generateDropStatementsForSequences(String schema)
    throws SQLException
  {
    String dropSeqGenQuery = "select rtrim(NAME) from SYSIBM.SYSSEQUENCES where SCHEMA = '" + schema + "' and SEQTYPE='S'";
    
    return buildDropStatements("DROP SEQUENCE", dropSeqGenQuery, schema);
  }
  
  private List<String> generateDropStatementsForTablespace(String schema)
    throws SQLException
  {
    String dropTablespaceGenQuery = "select rtrim(NAME) FROM SYSIBM.SYSTABLESPACE where DBNAME = '" + schema + "'";
    return buildDropStatements("DROP TABLESPACE", dropTablespaceGenQuery, schema);
  }
  
  private List<String> generateDropStatementsForTestTable(String schema, String tableType, String objectType)
    throws SQLException
  {
    String dropTablesGenQuery = "select rtrim(NAME) from SYSIBM.SYSTABLES where TYPE='" + tableType + "' and creator = '" + schema + "'";
    
    return buildDropStatements("DROP " + objectType, dropTablesGenQuery, schema);
  }
  
  private List<String> generateDropStatementsForUserTypes(String schema)
    throws SQLException
  {
    String dropTablespaceGenQuery = "select rtrim(NAME) from SYSIBM.SYSDATATYPES where schema = '" + schema + "'";
    return buildDropStatements("DROP TYPE", dropTablespaceGenQuery, schema);
  }
  
  private List<String> generateDropStatements(String schema, String tableType, String objectType)
    throws SQLException
  {
    String dropTablesGenQuery = "select rtrim(NAME) from SYSIBM.SYSTABLES where TYPE='" + tableType + "' and (DBNAME = '" + schema + "' OR creator = '" + schema + "')";
    
    return buildDropStatements("DROP " + objectType, dropTablesGenQuery, schema);
  }
  
  private List<String> buildDropStatements(String dropPrefix, String query, String schema)
    throws SQLException
  {
    List<String> dropStatements = new ArrayList();
    List<String> dbObjects = this.jdbcTemplate.queryForStringList(query, new String[0]);
    for (String dbObject : dbObjects) {
      dropStatements.add(dropPrefix + " " + ((DB2zosDbSupport)this.dbSupport).quote(new String[] { schema, dbObject }));
    }
    return dropStatements;
  }
  
  protected Table[] doAllTables()
    throws SQLException
  {
    List<String> tableNames = this.jdbcTemplate.queryForStringList("select rtrim(NAME) from SYSIBM.SYSTABLES where TYPE='T' and DBNAME = ?", new String[] { this.name });
    
    Table[] tables = new Table[tableNames.size()];
    for (int i = 0; i < tableNames.size(); i++) {
      tables[i] = new DB2zosTable(this.jdbcTemplate, this.dbSupport, this, (String)tableNames.get(i));
    }
    return tables;
  }
  
  public Table getTable(String tableName)
  {
    return new DB2zosTable(this.jdbcTemplate, this.dbSupport, this, tableName);
  }
  
  protected Type getType(String typeName)
  {
    return new DB2zosType(this.jdbcTemplate, this.dbSupport, this, typeName);
  }
  
  public Function getFunction(String functionName, String... args)
  {
    return new DB2zosFunction(this.jdbcTemplate, this.dbSupport, this, functionName, args);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\db2zos\DB2zosSchema.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */