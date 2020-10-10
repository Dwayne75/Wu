package org.flywaydb.core.internal.dbsupport.h2;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public class H2Schema
  extends Schema<H2DbSupport>
{
  private static final Log LOG = LogFactory.getLog(H2Schema.class);
  
  public H2Schema(JdbcTemplate jdbcTemplate, H2DbSupport dbSupport, String name)
  {
    super(jdbcTemplate, dbSupport, name);
  }
  
  protected boolean doExists()
    throws SQLException
  {
    return this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM INFORMATION_SCHEMA.schemata WHERE schema_name=?", new String[] { this.name }) > 0;
  }
  
  protected boolean doEmpty()
    throws SQLException
  {
    return allTables().length == 0;
  }
  
  protected void doCreate()
    throws SQLException
  {
    this.jdbcTemplate.execute("CREATE SCHEMA " + ((H2DbSupport)this.dbSupport).quote(new String[] { this.name }), new Object[0]);
  }
  
  protected void doDrop()
    throws SQLException
  {
    this.jdbcTemplate.execute("DROP SCHEMA " + ((H2DbSupport)this.dbSupport).quote(new String[] { this.name }), new Object[0]);
  }
  
  protected void doClean()
    throws SQLException
  {
    for (Table table : allTables()) {
      table.drop();
    }
    Object sequenceNames = listObjectNames("SEQUENCE", "IS_GENERATED = false");
    for (Iterator localIterator = generateDropStatements("SEQUENCE", (List)sequenceNames, "").iterator(); localIterator.hasNext();)
    {
      statement = (String)localIterator.next();
      this.jdbcTemplate.execute((String)statement, new Object[0]);
    }
    Object constantNames = listObjectNames("CONSTANT", "");
    for (Object statement = generateDropStatements("CONSTANT", (List)constantNames, "").iterator(); ((Iterator)statement).hasNext();)
    {
      statement = (String)((Iterator)statement).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    String statement;
    Object domainNames = listObjectNames("DOMAIN", "");
    if (!((List)domainNames).isEmpty()) {
      if (this.name.equals(((H2DbSupport)this.dbSupport).getCurrentSchemaName())) {
        for (String statement : generateDropStatementsForCurrentSchema("DOMAIN", (List)domainNames, "")) {
          this.jdbcTemplate.execute(statement, new Object[0]);
        }
      } else {
        LOG.error("Unable to drop DOMAIN objects in schema " + ((H2DbSupport)this.dbSupport).quote(new String[] { this.name }) + " due to H2 bug! (More info: http://code.google.com/p/h2database/issues/detail?id=306)");
      }
    }
  }
  
  private List<String> generateDropStatements(String objectType, List<String> objectNames, String dropStatementSuffix)
  {
    List<String> statements = new ArrayList();
    for (String objectName : objectNames)
    {
      String dropStatement = "DROP " + objectType + ((H2DbSupport)this.dbSupport).quote(new String[] { this.name, objectName }) + " " + dropStatementSuffix;
      
      statements.add(dropStatement);
    }
    return statements;
  }
  
  private List<String> generateDropStatementsForCurrentSchema(String objectType, List<String> objectNames, String dropStatementSuffix)
  {
    List<String> statements = new ArrayList();
    for (String objectName : objectNames)
    {
      String dropStatement = "DROP " + objectType + ((H2DbSupport)this.dbSupport).quote(new String[] { objectName }) + " " + dropStatementSuffix;
      
      statements.add(dropStatement);
    }
    return statements;
  }
  
  protected Table[] doAllTables()
    throws SQLException
  {
    List<String> tableNames = listObjectNames("TABLE", "TABLE_TYPE = 'TABLE'");
    
    Table[] tables = new Table[tableNames.size()];
    for (int i = 0; i < tableNames.size(); i++) {
      tables[i] = new H2Table(this.jdbcTemplate, this.dbSupport, this, (String)tableNames.get(i));
    }
    return tables;
  }
  
  private List<String> listObjectNames(String objectType, String querySuffix)
    throws SQLException
  {
    String query = "SELECT " + objectType + "_NAME FROM INFORMATION_SCHEMA." + objectType + "s WHERE " + objectType + "_schema = ?";
    if (StringUtils.hasLength(querySuffix)) {
      query = query + " AND " + querySuffix;
    }
    return this.jdbcTemplate.queryForStringList(query, new String[] { this.name });
  }
  
  public Table getTable(String tableName)
  {
    return new H2Table(this.jdbcTemplate, this.dbSupport, this, tableName);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\h2\H2Schema.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */