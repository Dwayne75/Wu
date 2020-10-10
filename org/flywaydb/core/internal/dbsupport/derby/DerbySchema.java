package org.flywaydb.core.internal.dbsupport.derby;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.util.StringUtils;

public class DerbySchema
  extends Schema<DerbyDbSupport>
{
  public DerbySchema(JdbcTemplate jdbcTemplate, DerbyDbSupport dbSupport, String name)
  {
    super(jdbcTemplate, dbSupport, name);
  }
  
  protected boolean doExists()
    throws SQLException
  {
    return this.jdbcTemplate.queryForInt("SELECT COUNT (*) FROM sys.sysschemas WHERE schemaname=?", new String[] { this.name }) > 0;
  }
  
  protected boolean doEmpty()
    throws SQLException
  {
    return allTables().length == 0;
  }
  
  protected void doCreate()
    throws SQLException
  {
    this.jdbcTemplate.execute("CREATE SCHEMA " + ((DerbyDbSupport)this.dbSupport).quote(new String[] { this.name }), new Object[0]);
  }
  
  protected void doDrop()
    throws SQLException
  {
    clean();
    this.jdbcTemplate.execute("DROP SCHEMA " + ((DerbyDbSupport)this.dbSupport).quote(new String[] { this.name }) + " RESTRICT", new Object[0]);
  }
  
  protected void doClean()
    throws SQLException
  {
    List<String> triggerNames = listObjectNames("TRIGGER", "");
    for (String statement : generateDropStatements("TRIGGER", triggerNames, "")) {
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (??? = generateDropStatementsForConstraints().iterator(); ???.hasNext();)
    {
      statement = (String)???.next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    Object viewNames = listObjectNames("TABLE", "TABLETYPE='V'");
    for (String statement = generateDropStatements("VIEW", (List)viewNames, "").iterator(); statement.hasNext();)
    {
      statement = (String)statement.next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    statement = allTables();String statement = statement.length;
    for (String str1 = 0; str1 < statement; str1++)
    {
      Table table = statement[str1];
      table.drop();
    }
    List<String> sequenceNames = listObjectNames("SEQUENCE", "");
    for (String statement : generateDropStatements("SEQUENCE", sequenceNames, "RESTRICT")) {
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
  }
  
  private List<String> generateDropStatementsForConstraints()
    throws SQLException
  {
    List<Map<String, String>> results = this.jdbcTemplate.queryForList("SELECT c.constraintname, t.tablename FROM sys.sysconstraints c INNER JOIN sys.systables t ON c.tableid = t.tableid INNER JOIN sys.sysschemas s ON c.schemaid = s.schemaid WHERE c.type = 'F' AND s.schemaname = ?", new String[] { this.name });
    
    List<String> statements = new ArrayList();
    for (Map<String, String> result : results)
    {
      String dropStatement = "ALTER TABLE " + ((DerbyDbSupport)this.dbSupport).quote(new String[] { this.name, (String)result.get("TABLENAME") }) + " DROP CONSTRAINT " + ((DerbyDbSupport)this.dbSupport).quote(new String[] { (String)result.get("CONSTRAINTNAME") });
      
      statements.add(dropStatement);
    }
    return statements;
  }
  
  private List<String> generateDropStatements(String objectType, List<String> objectNames, String dropStatementSuffix)
  {
    List<String> statements = new ArrayList();
    for (String objectName : objectNames)
    {
      String dropStatement = "DROP " + objectType + " " + ((DerbyDbSupport)this.dbSupport).quote(new String[] { this.name, objectName }) + " " + dropStatementSuffix;
      
      statements.add(dropStatement);
    }
    return statements;
  }
  
  protected Table[] doAllTables()
    throws SQLException
  {
    List<String> tableNames = listObjectNames("TABLE", "TABLETYPE='T'");
    
    Table[] tables = new Table[tableNames.size()];
    for (int i = 0; i < tableNames.size(); i++) {
      tables[i] = new DerbyTable(this.jdbcTemplate, this.dbSupport, this, (String)tableNames.get(i));
    }
    return tables;
  }
  
  private List<String> listObjectNames(String objectType, String querySuffix)
    throws SQLException
  {
    String query = "SELECT " + objectType + "name FROM sys.sys" + objectType + "s WHERE schemaid in (SELECT schemaid FROM sys.sysschemas where schemaname = ?)";
    if (StringUtils.hasLength(querySuffix)) {
      query = query + " AND " + querySuffix;
    }
    return this.jdbcTemplate.queryForStringList(query, new String[] { this.name });
  }
  
  public Table getTable(String tableName)
  {
    return new DerbyTable(this.jdbcTemplate, this.dbSupport, this, tableName);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\derby\DerbySchema.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */