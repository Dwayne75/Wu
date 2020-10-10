package org.flywaydb.core.internal.dbsupport.phoenix;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.util.jdbc.RowMapper;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public class PhoenixSchema
  extends Schema<PhoenixDbSupport>
{
  private static final Log LOG = LogFactory.getLog(PhoenixSchema.class);
  
  public PhoenixSchema(JdbcTemplate jdbcTemplate, PhoenixDbSupport dbSupport, String name)
  {
    super(jdbcTemplate, dbSupport, name);
  }
  
  protected boolean doExists()
    throws SQLException
  {
    ResultSet rs = this.jdbcTemplate.getMetaData().getSchemas();
    while (rs.next())
    {
      String schemaName = rs.getString("TABLE_SCHEM");
      if (schemaName == null)
      {
        if (this.name == null) {
          return true;
        }
      }
      else if ((this.name != null) && (schemaName.equals(this.name))) {
        return true;
      }
    }
    return false;
  }
  
  protected boolean doEmpty()
    throws SQLException
  {
    return allTables().length == 0;
  }
  
  protected void doCreate()
    throws SQLException
  {
    LOG.info("Phoenix does not support creating schemas. Schema not created: " + this.name);
  }
  
  protected void doDrop()
    throws SQLException
  {
    LOG.info("Phoenix does not support dropping schemas directly. Running clean of objects instead");
    doClean();
  }
  
  protected void doClean()
    throws SQLException
  {
    List<String> sequenceNames = listObjectsOfType("sequence");
    for (Iterator localIterator1 = generateDropStatements("SEQUENCE", sequenceNames, "").iterator(); localIterator1.hasNext();)
    {
      statement = (String)localIterator1.next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    String statement;
    Object viewNames = listObjectsOfType("view");
    for (String statement : generateDropStatements("VIEW", (List)viewNames, "")) {
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    List<String> indexPairs = listObjectsOfType("index");
    List<String> indexNames = new ArrayList();
    List<String> indexTables = new ArrayList();
    for (Iterator localIterator2 = indexPairs.iterator(); localIterator2.hasNext();)
    {
      indexPair = (String)localIterator2.next();
      String[] splits = indexPair.split(",");
      indexNames.add(splits[0]);
      indexTables.add("ON " + ((PhoenixDbSupport)this.dbSupport).quote(new String[] { this.name, splits[1] }));
    }
    Object statements = generateDropIndexStatements(indexNames, indexTables);
    for (String indexPair = ((List)statements).iterator(); indexPair.hasNext();)
    {
      statement = (String)indexPair.next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    String statement;
    List<String> tableNames = listObjectsOfType("table");
    for (String statement : generateDropStatements("TABLE", tableNames, "")) {
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
  }
  
  private List<String> generateDropStatements(String objectType, List<String> objectNames, String dropStatementSuffix)
  {
    List<String> statements = new ArrayList();
    for (String objectName : objectNames)
    {
      String dropStatement = "DROP " + objectType + " " + ((PhoenixDbSupport)this.dbSupport).quote(new String[] { this.name, objectName }) + " " + dropStatementSuffix;
      
      statements.add(dropStatement);
    }
    return statements;
  }
  
  private List<String> generateDropIndexStatements(List<String> objectNames, List<String> dropStatementSuffixes)
  {
    List<String> statements = new ArrayList();
    for (int i = 0; i < objectNames.size(); i++)
    {
      String dropStatement = "DROP INDEX " + ((PhoenixDbSupport)this.dbSupport).quote(new String[] { (String)objectNames.get(i) }) + " " + (String)dropStatementSuffixes.get(i);
      
      statements.add(dropStatement);
    }
    return statements;
  }
  
  protected Table[] doAllTables()
    throws SQLException
  {
    List<String> tableNames = listObjectsOfType("table");
    
    Table[] tables = new Table[tableNames.size()];
    for (int i = 0; i < tableNames.size(); i++) {
      tables[i] = new PhoenixTable(this.jdbcTemplate, this.dbSupport, this, (String)tableNames.get(i));
    }
    return tables;
  }
  
  protected List<String> listObjectsOfType(String type)
    throws SQLException
  {
    List<String> retVal = new ArrayList();
    
    String finalName = this.name == null ? "" : this.name;
    if (type.equalsIgnoreCase("view"))
    {
      ResultSet rs = this.jdbcTemplate.getConnection().getMetaData().getTables(null, finalName, null, new String[] { "VIEW" });
      while (rs.next())
      {
        String viewName = rs.getString("TABLE_NAME");
        if (viewName != null) {
          retVal.add(viewName);
        }
      }
    }
    else if (type.equalsIgnoreCase("table"))
    {
      ResultSet rs = this.jdbcTemplate.getMetaData().getTables(null, finalName, null, new String[] { "TABLE" });
      while (rs.next())
      {
        String tableName = rs.getString("TABLE_NAME");
        Set<String> tables = new HashSet();
        if (tableName != null) {
          tables.add(tableName);
        }
        retVal.addAll(tables);
      }
    }
    else
    {
      if (type.equalsIgnoreCase("sequence"))
      {
        if (this.name == null)
        {
          String query = "SELECT SEQUENCE_NAME FROM SYSTEM.\"SEQUENCE\" WHERE SEQUENCE_SCHEMA IS NULL";
          return this.jdbcTemplate.queryForStringList(query, new String[0]);
        }
        String query = "SELECT SEQUENCE_NAME FROM SYSTEM.\"SEQUENCE\" WHERE SEQUENCE_SCHEMA = ?";
        return this.jdbcTemplate.queryForStringList(query, new String[] { this.name });
      }
      if (type.equalsIgnoreCase("index"))
      {
        String query = "SELECT TABLE_NAME, DATA_TABLE_NAME FROM SYSTEM.CATALOG WHERE TABLE_SCHEM";
        if (this.name == null) {
          query = query + " IS NULL";
        } else {
          query = query + " = ?";
        }
        query = query + " AND TABLE_TYPE = 'i'";
        
        String finalQuery = query.replaceFirst("\\?", "'" + this.name + "'");
        
        retVal = this.jdbcTemplate.query(finalQuery, new RowMapper()
        {
          public String mapRow(ResultSet rs)
            throws SQLException
          {
            return rs.getString("TABLE_NAME") + "," + rs.getString("DATA_TABLE_NAME");
          }
        });
      }
    }
    return retVal;
  }
  
  public Table getTable(String tableName)
  {
    return new PhoenixTable(this.jdbcTemplate, this.dbSupport, this, tableName);
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    Schema schema = (Schema)o;
    if (this.name == null) {
      return this.name == schema.getName();
    }
    return this.name.equals(schema.getName());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\phoenix\PhoenixSchema.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */