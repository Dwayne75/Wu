package org.flywaydb.core.internal.dbsupport.redshift;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;

public class RedshiftSchema
  extends Schema<RedshiftDbSupport>
{
  public RedshiftSchema(JdbcTemplate jdbcTemplate, RedshiftDbSupport dbSupport, String name)
  {
    super(jdbcTemplate, dbSupport, name);
  }
  
  protected boolean doExists()
    throws SQLException
  {
    return this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM pg_namespace WHERE nspname=?", new String[] { this.name }) > 0;
  }
  
  protected boolean doEmpty()
    throws SQLException
  {
    int objectCount = this.jdbcTemplate.queryForInt("SELECT count(*) FROM information_schema.tables WHERE table_schema=? AND table_type='BASE TABLE'", new String[] { this.name });
    
    return objectCount == 0;
  }
  
  protected void doCreate()
    throws SQLException
  {
    this.jdbcTemplate.execute("CREATE SCHEMA " + ((RedshiftDbSupport)this.dbSupport).quote(new String[] { this.name }), new Object[0]);
  }
  
  protected void doDrop()
    throws SQLException
  {
    this.jdbcTemplate.execute("DROP SCHEMA " + ((RedshiftDbSupport)this.dbSupport).quote(new String[] { this.name }) + " CASCADE", new Object[0]);
  }
  
  protected void doClean()
    throws SQLException
  {
    for (Table table : allTables()) {
      table.drop();
    }
    for (??? = generateDropStatementsForViews().iterator(); ((Iterator)???).hasNext();)
    {
      String statement = (String)((Iterator)???).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
  }
  
  protected Table[] doAllTables()
    throws SQLException
  {
    List<String> tableNames = this.jdbcTemplate.queryForStringList("SELECT t.table_name FROM information_schema.tables t WHERE table_schema=? AND table_type = 'BASE TABLE'", new String[] { this.name });
    
    Table[] tables = new Table[tableNames.size()];
    for (int i = 0; i < tableNames.size(); i++) {
      tables[i] = new RedshiftTable(this.jdbcTemplate, this.dbSupport, this, (String)tableNames.get(i));
    }
    return tables;
  }
  
  protected List<String> generateDropStatementsForViews()
    throws SQLException
  {
    List<String> viewNames = this.jdbcTemplate.queryForStringList("SELECT t.table_name FROM information_schema.tables t WHERE table_schema=? AND table_type = 'VIEW'", new String[] { this.name });
    
    List<String> statements = new ArrayList();
    for (String viewName : viewNames) {
      statements.add("DROP VIEW " + ((RedshiftDbSupport)this.dbSupport).quote(new String[] { this.name, viewName }) + " CASCADE");
    }
    return statements;
  }
  
  public Table getTable(String tableName)
  {
    return new RedshiftTable(this.jdbcTemplate, this.dbSupport, this, tableName);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\redshift\RedshiftSchema.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */