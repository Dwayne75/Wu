package org.flywaydb.core.internal.dbsupport.db2;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.flywaydb.core.internal.dbsupport.Function;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.dbsupport.Type;
import org.flywaydb.core.internal.util.StringUtils;

public class DB2Schema
  extends Schema<DB2DbSupport>
{
  public DB2Schema(JdbcTemplate jdbcTemplate, DB2DbSupport dbSupport, String name)
  {
    super(jdbcTemplate, dbSupport, name);
  }
  
  protected boolean doExists()
    throws SQLException
  {
    return this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM syscat.schemata WHERE schemaname=?", new String[] { this.name }) > 0;
  }
  
  protected boolean doEmpty()
    throws SQLException
  {
    int objectCount = this.jdbcTemplate.queryForInt("select count(*) from syscat.tables where tabschema = ?", new String[] { this.name });
    objectCount += this.jdbcTemplate.queryForInt("select count(*) from syscat.views where viewschema = ?", new String[] { this.name });
    objectCount += this.jdbcTemplate.queryForInt("select count(*) from syscat.sequences where seqschema = ?", new String[] { this.name });
    objectCount += this.jdbcTemplate.queryForInt("select count(*) from syscat.indexes where indschema = ?", new String[] { this.name });
    objectCount += this.jdbcTemplate.queryForInt("select count(*) from syscat.procedures where procschema = ?", new String[] { this.name });
    objectCount += this.jdbcTemplate.queryForInt("select count(*) from syscat.functions where funcschema = ?", new String[] { this.name });
    objectCount += this.jdbcTemplate.queryForInt("select count(*) from syscat.triggers where trigschema = ?", new String[] { this.name });
    return objectCount == 0;
  }
  
  protected void doCreate()
    throws SQLException
  {
    this.jdbcTemplate.execute("CREATE SCHEMA " + ((DB2DbSupport)this.dbSupport).quote(new String[] { this.name }), new Object[0]);
  }
  
  protected void doDrop()
    throws SQLException
  {
    clean();
    this.jdbcTemplate.execute("DROP SCHEMA " + ((DB2DbSupport)this.dbSupport).quote(new String[] { this.name }) + " RESTRICT", new Object[0]);
  }
  
  protected void doClean()
    throws SQLException
  {
    if (((DB2DbSupport)this.dbSupport).getDb2MajorVersion() >= 10) {
      for (localObject = generateDropVersioningStatement().iterator(); ((Iterator)localObject).hasNext();)
      {
        String dropVersioningStatement = (String)((Iterator)localObject).next();
        this.jdbcTemplate.execute(dropVersioningStatement, new Object[0]);
      }
    }
    for (Object localObject = generateDropStatementsForViews().iterator(); ((Iterator)localObject).hasNext();)
    {
      String dropStatement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(dropStatement, new Object[0]);
    }
    for (localObject = generateDropStatements("A", "ALIAS").iterator(); ((Iterator)localObject).hasNext();)
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
    for (localObject = generateDropStatementsForSequences().iterator(); ((Iterator)localObject).hasNext();)
    {
      String dropStatement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(dropStatement, new Object[0]);
    }
    for (localObject = generateDropStatementsForProcedures().iterator(); ((Iterator)localObject).hasNext();)
    {
      String dropStatement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(dropStatement, new Object[0]);
    }
    for (localObject = generateDropStatementsForTriggers().iterator(); ((Iterator)localObject).hasNext();)
    {
      dropStatement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(dropStatement, new Object[0]);
    }
    localObject = allFunctions();String dropStatement = localObject.length;
    for (String str2 = 0; str2 < dropStatement; str2++)
    {
      Function function = localObject[str2];
      function.drop();
    }
    localObject = allTypes();dropStatement = localObject.length;
    for (String str3 = 0; str3 < dropStatement; str3++)
    {
      Type type = localObject[str3];
      type.drop();
    }
  }
  
  private List<String> generateDropStatementsForProcedures()
    throws SQLException
  {
    String dropProcGenQuery = "select PROCNAME from SYSCAT.PROCEDURES where PROCSCHEMA = '" + this.name + "'";
    return buildDropStatements("DROP PROCEDURE", dropProcGenQuery);
  }
  
  private List<String> generateDropStatementsForTriggers()
    throws SQLException
  {
    String dropTrigGenQuery = "select TRIGNAME from SYSCAT.TRIGGERS where TRIGSCHEMA = '" + this.name + "'";
    return buildDropStatements("DROP TRIGGER", dropTrigGenQuery);
  }
  
  private List<String> generateDropStatementsForSequences()
    throws SQLException
  {
    String dropSeqGenQuery = "select SEQNAME from SYSCAT.SEQUENCES where SEQSCHEMA = '" + this.name + "' and SEQTYPE='S'";
    
    return buildDropStatements("DROP SEQUENCE", dropSeqGenQuery);
  }
  
  private List<String> generateDropStatementsForViews()
    throws SQLException
  {
    String dropSeqGenQuery = "select TABNAME from SYSCAT.TABLES where TABSCHEMA = '" + this.name + "' and TABNAME NOT LIKE '%_V' and TYPE='V'";
    
    return buildDropStatements("DROP VIEW", dropSeqGenQuery);
  }
  
  private List<String> generateDropStatements(String tableType, String objectType)
    throws SQLException
  {
    String dropTablesGenQuery = "select TABNAME from SYSCAT.TABLES where TYPE='" + tableType + "' and TABSCHEMA = '" + this.name + "'";
    
    return buildDropStatements("DROP " + objectType, dropTablesGenQuery);
  }
  
  private List<String> buildDropStatements(String dropPrefix, String query)
    throws SQLException
  {
    List<String> dropStatements = new ArrayList();
    List<String> dbObjects = this.jdbcTemplate.queryForStringList(query, new String[0]);
    for (String dbObject : dbObjects) {
      dropStatements.add(dropPrefix + " " + ((DB2DbSupport)this.dbSupport).quote(new String[] { this.name, dbObject }));
    }
    return dropStatements;
  }
  
  private List<String> generateDropVersioningStatement()
    throws SQLException
  {
    List<String> dropVersioningStatements = new ArrayList();
    Table[] versioningTables = findTables("select TABNAME from SYSCAT.TABLES where TEMPORALTYPE <> 'N' and TABSCHEMA = ?", new String[] { this.name });
    for (Table table : versioningTables) {
      dropVersioningStatements.add("ALTER TABLE " + table.toString() + " DROP VERSIONING");
    }
    return dropVersioningStatements;
  }
  
  private Table[] findTables(String sqlQuery, String... params)
    throws SQLException
  {
    List<String> tableNames = this.jdbcTemplate.queryForStringList(sqlQuery, params);
    Table[] tables = new Table[tableNames.size()];
    for (int i = 0; i < tableNames.size(); i++) {
      tables[i] = new DB2Table(this.jdbcTemplate, this.dbSupport, this, (String)tableNames.get(i));
    }
    return tables;
  }
  
  protected Table[] doAllTables()
    throws SQLException
  {
    return findTables("select TABNAME from SYSCAT.TABLES where TYPE='T' and TABSCHEMA = ?", new String[] { this.name });
  }
  
  protected Function[] doAllFunctions()
    throws SQLException
  {
    List<Map<String, String>> rows = this.jdbcTemplate.queryForList("select p.SPECIFICNAME, p.FUNCNAME, substr( xmlserialize( xmlagg( xmltext( concat( ', ', TYPENAME ) ) ) as varchar( 1024 ) ), 3 ) as PARAMS from SYSCAT.FUNCTIONS f inner join SYSCAT.FUNCPARMS p on f.SPECIFICNAME = p.SPECIFICNAME where f.ORIGIN = 'Q' and p.FUNCSCHEMA = ? and p.ROWTYPE = 'P' group by p.SPECIFICNAME, p.FUNCNAME order by p.SPECIFICNAME", new String[] { this.name });
    
    List<Function> functions = new ArrayList();
    for (Map<String, String> row : rows) {
      functions.add(getFunction(
        (String)row.get("FUNCNAME"), 
        StringUtils.tokenizeToStringArray((String)row.get("PARAMS"), ",")));
    }
    return (Function[])functions.toArray(new Function[functions.size()]);
  }
  
  public Table getTable(String tableName)
  {
    return new DB2Table(this.jdbcTemplate, this.dbSupport, this, tableName);
  }
  
  protected Type getType(String typeName)
  {
    return new DB2Type(this.jdbcTemplate, this.dbSupport, this, typeName);
  }
  
  public Function getFunction(String functionName, String... args)
  {
    return new DB2Function(this.jdbcTemplate, this.dbSupport, this, functionName, args);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\db2\DB2Schema.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */