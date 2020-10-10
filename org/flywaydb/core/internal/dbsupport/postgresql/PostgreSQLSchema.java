package org.flywaydb.core.internal.dbsupport.postgresql;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.dbsupport.Type;

public class PostgreSQLSchema
  extends Schema<PostgreSQLDbSupport>
{
  public PostgreSQLSchema(JdbcTemplate jdbcTemplate, PostgreSQLDbSupport dbSupport, String name)
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
    this.jdbcTemplate.execute("CREATE SCHEMA " + ((PostgreSQLDbSupport)this.dbSupport).quote(new String[] { this.name }), new Object[0]);
  }
  
  protected void doDrop()
    throws SQLException
  {
    this.jdbcTemplate.execute("DROP SCHEMA " + ((PostgreSQLDbSupport)this.dbSupport).quote(new String[] { this.name }) + " CASCADE", new Object[0]);
  }
  
  protected void doClean()
    throws SQLException
  {
    int databaseMajorVersion = this.jdbcTemplate.getMetaData().getDatabaseMajorVersion();
    int databaseMinorVersion = this.jdbcTemplate.getMetaData().getDatabaseMinorVersion();
    if ((databaseMajorVersion > 9) || ((databaseMajorVersion == 9) && (databaseMinorVersion >= 3))) {
      for (localObject = generateDropStatementsForMaterializedViews().iterator(); ((Iterator)localObject).hasNext();)
      {
        String statement = (String)((Iterator)localObject).next();
        this.jdbcTemplate.execute(statement, new Object[0]);
      }
    }
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
    for (localObject = generateDropStatementsForBaseTypes(true).iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = generateDropStatementsForAggregates().iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = generateDropStatementsForRoutines().iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = generateDropStatementsForEnums().iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = generateDropStatementsForDomains().iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = generateDropStatementsForBaseTypes(false).iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
  }
  
  private List<String> generateDropStatementsForSequences()
    throws SQLException
  {
    List<String> sequenceNames = this.jdbcTemplate.queryForStringList("SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema=?", new String[] { this.name });
    
    List<String> statements = new ArrayList();
    for (String sequenceName : sequenceNames) {
      statements.add("DROP SEQUENCE IF EXISTS " + ((PostgreSQLDbSupport)this.dbSupport).quote(new String[] { this.name, sequenceName }));
    }
    return statements;
  }
  
  private List<String> generateDropStatementsForBaseTypes(boolean recreate)
    throws SQLException
  {
    List<Map<String, String>> rows = this.jdbcTemplate.queryForList("select typname, typcategory from pg_catalog.pg_type t where (t.typrelid = 0 OR (SELECT c.relkind = 'c' FROM pg_catalog.pg_class c WHERE c.oid = t.typrelid)) and NOT EXISTS(SELECT 1 FROM pg_catalog.pg_type el WHERE el.oid = t.typelem AND el.typarray = t.oid) and t.typnamespace in (select oid from pg_catalog.pg_namespace where nspname = ?)", new String[] { this.name });
    
    List<String> statements = new ArrayList();
    for (Map<String, String> row : rows) {
      statements.add("DROP TYPE IF EXISTS " + ((PostgreSQLDbSupport)this.dbSupport).quote(new String[] { this.name, (String)row.get("typname") }) + " CASCADE");
    }
    if (recreate) {
      for (Map<String, String> row : rows) {
        if (Arrays.asList(new String[] { "P", "U" }).contains(row.get("typcategory"))) {
          statements.add("CREATE TYPE " + ((PostgreSQLDbSupport)this.dbSupport).quote(new String[] { this.name, (String)row.get("typname") }));
        }
      }
    }
    return statements;
  }
  
  private List<String> generateDropStatementsForAggregates()
    throws SQLException
  {
    List<Map<String, String>> rows = this.jdbcTemplate.queryForList("SELECT proname, oidvectortypes(proargtypes) AS args FROM pg_proc INNER JOIN pg_namespace ns ON (pg_proc.pronamespace = ns.oid) WHERE pg_proc.proisagg = true AND ns.nspname = ?", new String[] { this.name });
    
    List<String> statements = new ArrayList();
    for (Map<String, String> row : rows) {
      statements.add("DROP AGGREGATE IF EXISTS " + ((PostgreSQLDbSupport)this.dbSupport).quote(new String[] { this.name, (String)row.get("proname") }) + "(" + (String)row.get("args") + ") CASCADE");
    }
    return statements;
  }
  
  private List<String> generateDropStatementsForRoutines()
    throws SQLException
  {
    List<Map<String, String>> rows = this.jdbcTemplate.queryForList("SELECT proname, oidvectortypes(proargtypes) AS args FROM pg_proc INNER JOIN pg_namespace ns ON (pg_proc.pronamespace = ns.oid) LEFT JOIN pg_depend dep ON dep.objid = pg_proc.oid AND dep.deptype = 'e' WHERE pg_proc.proisagg = false AND ns.nspname = ? AND dep.objid IS NULL", new String[] { this.name });
    
    List<String> statements = new ArrayList();
    for (Map<String, String> row : rows) {
      statements.add("DROP FUNCTION IF EXISTS " + ((PostgreSQLDbSupport)this.dbSupport).quote(new String[] { this.name, (String)row.get("proname") }) + "(" + (String)row.get("args") + ") CASCADE");
    }
    return statements;
  }
  
  private List<String> generateDropStatementsForEnums()
    throws SQLException
  {
    List<String> enumNames = this.jdbcTemplate.queryForStringList("SELECT t.typname FROM pg_catalog.pg_type t INNER JOIN pg_catalog.pg_namespace n ON n.oid = t.typnamespace WHERE n.nspname = ? and t.typtype = 'e'", new String[] { this.name });
    
    List<String> statements = new ArrayList();
    for (String enumName : enumNames) {
      statements.add("DROP TYPE " + ((PostgreSQLDbSupport)this.dbSupport).quote(new String[] { this.name, enumName }));
    }
    return statements;
  }
  
  private List<String> generateDropStatementsForDomains()
    throws SQLException
  {
    List<String> domainNames = this.jdbcTemplate.queryForStringList("SELECT domain_name FROM information_schema.domains WHERE domain_schema=?", new String[] { this.name });
    
    List<String> statements = new ArrayList();
    for (String domainName : domainNames) {
      statements.add("DROP DOMAIN " + ((PostgreSQLDbSupport)this.dbSupport).quote(new String[] { this.name, domainName }));
    }
    return statements;
  }
  
  private List<String> generateDropStatementsForMaterializedViews()
    throws SQLException
  {
    List<String> viewNames = this.jdbcTemplate.queryForStringList("SELECT relname FROM pg_catalog.pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace WHERE c.relkind = 'm' AND n.nspname = ?", new String[] { this.name });
    
    List<String> statements = new ArrayList();
    for (String domainName : viewNames) {
      statements.add("DROP MATERIALIZED VIEW IF EXISTS " + ((PostgreSQLDbSupport)this.dbSupport).quote(new String[] { this.name, domainName }) + " CASCADE");
    }
    return statements;
  }
  
  private List<String> generateDropStatementsForViews()
    throws SQLException
  {
    List<String> viewNames = this.jdbcTemplate.queryForStringList("SELECT relname FROM pg_catalog.pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace LEFT JOIN pg_depend dep ON dep.objid = c.oid AND dep.deptype = 'e' WHERE c.relkind = 'v' AND  n.nspname = ? AND dep.objid IS NULL", new String[] { this.name });
    
    List<String> statements = new ArrayList();
    for (String domainName : viewNames) {
      statements.add("DROP VIEW IF EXISTS " + ((PostgreSQLDbSupport)this.dbSupport).quote(new String[] { this.name, domainName }) + " CASCADE");
    }
    return statements;
  }
  
  protected Table[] doAllTables()
    throws SQLException
  {
    List<String> tableNames = this.jdbcTemplate.queryForStringList("SELECT t.table_name FROM information_schema.tables t WHERE table_schema=? AND table_type='BASE TABLE' AND NOT (SELECT EXISTS (SELECT inhrelid FROM pg_catalog.pg_inherits WHERE inhrelid = (quote_ident(t.table_schema)||'.'||quote_ident(t.table_name))::regclass::oid))", new String[] { this.name });
    
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
  
  protected Type getType(String typeName)
  {
    return new PostgreSQLType(this.jdbcTemplate, this.dbSupport, this, typeName);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\postgresql\PostgreSQLSchema.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */