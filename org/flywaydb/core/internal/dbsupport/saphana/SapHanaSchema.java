package org.flywaydb.core.internal.dbsupport.saphana;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

public class SapHanaSchema
  extends Schema<SapHanaDbSupport>
{
  public SapHanaSchema(JdbcTemplate jdbcTemplate, SapHanaDbSupport dbSupport, String name)
  {
    super(jdbcTemplate, dbSupport, name);
  }
  
  protected boolean doExists()
    throws SQLException
  {
    return this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM SYS.SCHEMAS WHERE SCHEMA_NAME=?", new String[] { this.name }) > 0;
  }
  
  protected boolean doEmpty()
    throws SQLException
  {
    int objectCount = this.jdbcTemplate.queryForInt("select count(*) from sys.tables where schema_name = ?", new String[] { this.name });
    objectCount += this.jdbcTemplate.queryForInt("select count(*) from sys.views where schema_name = ?", new String[] { this.name });
    objectCount += this.jdbcTemplate.queryForInt("select count(*) from sys.sequences where schema_name = ?", new String[] { this.name });
    objectCount += this.jdbcTemplate.queryForInt("select count(*) from sys.synonyms where schema_name = ?", new String[] { this.name });
    return objectCount == 0;
  }
  
  protected void doCreate()
    throws SQLException
  {
    this.jdbcTemplate.execute("CREATE SCHEMA " + ((SapHanaDbSupport)this.dbSupport).quote(new String[] { this.name }), new Object[0]);
  }
  
  protected void doDrop()
    throws SQLException
  {
    clean();
    this.jdbcTemplate.execute("DROP SCHEMA " + ((SapHanaDbSupport)this.dbSupport).quote(new String[] { this.name }) + " RESTRICT", new Object[0]);
  }
  
  protected void doClean()
    throws SQLException
  {
    for (Object localObject = generateDropStatements("SYNONYM").iterator(); ((Iterator)localObject).hasNext();)
    {
      String dropStatement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(dropStatement, new Object[0]);
    }
    for (localObject = generateDropStatements("VIEW").iterator(); ((Iterator)localObject).hasNext();)
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
    for (localObject = generateDropStatements("SEQUENCE").iterator(); ((Iterator)localObject).hasNext();)
    {
      String dropStatement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(dropStatement, new Object[0]);
    }
  }
  
  private List<String> generateDropStatements(String objectType)
    throws SQLException
  {
    List<String> dropStatements = new ArrayList();
    List<String> dbObjects = this.jdbcTemplate.queryForStringList("select " + objectType + "_NAME from SYS." + objectType + "S where SCHEMA_NAME = '" + this.name + "'", new String[0]);
    for (String dbObject : dbObjects) {
      dropStatements.add("DROP " + objectType + " " + ((SapHanaDbSupport)this.dbSupport).quote(new String[] { this.name, dbObject }) + " CASCADE");
    }
    return dropStatements;
  }
  
  protected Table[] doAllTables()
    throws SQLException
  {
    List<String> tableNames = this.jdbcTemplate.queryForStringList("select TABLE_NAME from SYS.TABLES where SCHEMA_NAME = ?", new String[] { this.name });
    Table[] tables = new Table[tableNames.size()];
    for (int i = 0; i < tableNames.size(); i++) {
      tables[i] = new SapHanaTable(this.jdbcTemplate, this.dbSupport, this, (String)tableNames.get(i));
    }
    return tables;
  }
  
  public Table getTable(String tableName)
  {
    return new SapHanaTable(this.jdbcTemplate, this.dbSupport, this, tableName);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\saphana\SapHanaSchema.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */