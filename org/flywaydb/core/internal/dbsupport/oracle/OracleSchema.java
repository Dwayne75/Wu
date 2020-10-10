package org.flywaydb.core.internal.dbsupport.oracle;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public class OracleSchema
  extends Schema<OracleDbSupport>
{
  private static final Log LOG = LogFactory.getLog(OracleSchema.class);
  
  public OracleSchema(JdbcTemplate jdbcTemplate, OracleDbSupport dbSupport, String name)
  {
    super(jdbcTemplate, dbSupport, name);
  }
  
  protected boolean doExists()
    throws SQLException
  {
    return this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM all_users WHERE username=?", new String[] { this.name }) > 0;
  }
  
  protected boolean doEmpty()
    throws SQLException
  {
    return this.jdbcTemplate.queryForInt("SELECT count(*) FROM all_objects WHERE owner = ?", new String[] { this.name }) == 0;
  }
  
  protected void doCreate()
    throws SQLException
  {
    this.jdbcTemplate.execute("CREATE USER " + ((OracleDbSupport)this.dbSupport).quote(new String[] { this.name }) + " IDENTIFIED BY flyway", new Object[0]);
    this.jdbcTemplate.execute("GRANT RESOURCE TO " + ((OracleDbSupport)this.dbSupport).quote(new String[] { this.name }), new Object[0]);
    this.jdbcTemplate.execute("GRANT UNLIMITED TABLESPACE TO " + ((OracleDbSupport)this.dbSupport).quote(new String[] { this.name }), new Object[0]);
  }
  
  protected void doDrop()
    throws SQLException
  {
    this.jdbcTemplate.execute("DROP USER " + ((OracleDbSupport)this.dbSupport).quote(new String[] { this.name }) + " CASCADE", new Object[0]);
  }
  
  protected void doClean()
    throws SQLException
  {
    if ("SYSTEM".equals(this.name.toUpperCase())) {
      throw new FlywayException("Clean not supported on Oracle for user 'SYSTEM'! You should NEVER add your own objects to the SYSTEM schema!");
    }
    String user = ((OracleDbSupport)this.dbSupport).doGetCurrentSchemaName();
    boolean defaultSchemaForUser = user.equalsIgnoreCase(this.name);
    if (!defaultSchemaForUser) {
      LOG.warn("Cleaning schema " + this.name + " by a different user (" + user + "): " + "spatial extensions, queue tables, flashback tables and scheduled jobs will not be cleaned due to Oracle limitations");
    }
    for (Object localObject = generateDropStatementsForSpatialExtensions(defaultSchemaForUser).iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    if (defaultSchemaForUser)
    {
      for (localObject = generateDropStatementsForQueueTables().iterator(); ((Iterator)localObject).hasNext();)
      {
        String statement = (String)((Iterator)localObject).next();
        try
        {
          this.jdbcTemplate.execute(statement, new Object[0]);
        }
        catch (SQLException e)
        {
          if (e.getErrorCode() == 65040) {
            LOG.error("Missing required grant to clean queue tables: GRANT EXECUTE ON DBMS_AQADM");
          }
          throw e;
        }
      }
      if (flashbackAvailable()) {
        executeAlterStatementsForFlashbackTables();
      }
    }
    for (localObject = generateDropStatementsForScheduledJobs().iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = generateDropStatementsForObjectType("TRIGGER", "").iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = generateDropStatementsForObjectType("SEQUENCE", "").iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = generateDropStatementsForObjectType("FUNCTION", "").iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = generateDropStatementsForObjectType("MATERIALIZED VIEW", "PRESERVE TABLE").iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = generateDropStatementsForObjectType("PACKAGE", "").iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = generateDropStatementsForObjectType("PROCEDURE", "").iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = generateDropStatementsForObjectType("SYNONYM", "").iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = generateDropStatementsForObjectType("VIEW", "CASCADE CONSTRAINTS").iterator(); ((Iterator)localObject).hasNext();)
    {
      statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    localObject = allTables();String statement = localObject.length;
    for (e = 0; e < statement; e++)
    {
      Table table = localObject[e];
      table.drop();
    }
    for (localObject = generateDropStatementsForXmlTables().iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = generateDropStatementsForObjectType("CLUSTER", "").iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = generateDropStatementsForObjectType("TYPE", "FORCE").iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    for (localObject = generateDropStatementsForObjectType("JAVA SOURCE", "").iterator(); ((Iterator)localObject).hasNext();)
    {
      String statement = (String)((Iterator)localObject).next();
      this.jdbcTemplate.execute(statement, new Object[0]);
    }
    this.jdbcTemplate.execute("PURGE RECYCLEBIN", new Object[0]);
  }
  
  private void executeAlterStatementsForFlashbackTables()
    throws SQLException
  {
    List<String> tableNames = this.jdbcTemplate.queryForStringList("SELECT table_name FROM DBA_FLASHBACK_ARCHIVE_TABLES WHERE owner_name = ?", new String[] { this.name });
    for (String tableName : tableNames)
    {
      this.jdbcTemplate.execute("ALTER TABLE " + ((OracleDbSupport)this.dbSupport).quote(new String[] { this.name, tableName }) + " NO FLASHBACK ARCHIVE", new Object[0]);
      String queryForOracleTechnicalTables = "SELECT count(archive_table_name) FROM user_flashback_archive_tables WHERE table_name = ?";
      while (this.jdbcTemplate.queryForInt(queryForOracleTechnicalTables, new String[] { tableName }) > 0) {
        try
        {
          LOG.debug("Actively waiting for Flashback cleanup on table: " + tableName);
          Thread.sleep(1000L);
        }
        catch (InterruptedException e)
        {
          throw new FlywayException("Waiting for Flashback cleanup interrupted", e);
        }
      }
    }
  }
  
  private boolean flashbackAvailable()
    throws SQLException
  {
    return this.jdbcTemplate.queryForInt("select count(*) from all_objects where object_name like 'DBA_FLASHBACK_ARCHIVE_TABLES'", new String[0]) > 0;
  }
  
  private List<String> generateDropStatementsForXmlTables()
    throws SQLException
  {
    List<String> dropStatements = new ArrayList();
    if (!xmlDBExtensionsAvailable())
    {
      LOG.debug("Oracle XML DB Extensions are not available. No cleaning of XML tables.");
      return dropStatements;
    }
    List<String> objectNames = this.jdbcTemplate.queryForStringList("SELECT table_name FROM all_xml_tables WHERE owner = ?", new String[] { this.name });
    for (String objectName : objectNames) {
      dropStatements.add("DROP TABLE " + ((OracleDbSupport)this.dbSupport).quote(new String[] { this.name, objectName }) + " PURGE");
    }
    return dropStatements;
  }
  
  private boolean xmlDBExtensionsAvailable()
    throws SQLException
  {
    return (this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM all_users WHERE username = 'XDB'", new String[0]) > 0) && (this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM all_views WHERE view_name = 'RESOURCE_VIEW'", new String[0]) > 0);
  }
  
  private List<String> generateDropStatementsForObjectType(String objectType, String extraArguments)
    throws SQLException
  {
    String query = "SELECT object_name FROM all_objects WHERE object_type = ? AND owner = ? AND object_name NOT LIKE 'MDRS_%$' AND object_name NOT LIKE 'ISEQ$$_%'";
    
    List<String> objectNames = this.jdbcTemplate.queryForStringList(query, new String[] { objectType, this.name });
    List<String> dropStatements = new ArrayList();
    for (String objectName : objectNames) {
      dropStatements.add("DROP " + objectType + " " + ((OracleDbSupport)this.dbSupport).quote(new String[] { this.name, objectName }) + " " + extraArguments);
    }
    return dropStatements;
  }
  
  private List<String> generateDropStatementsForSpatialExtensions(boolean defaultSchemaForUser)
    throws SQLException
  {
    List<String> statements = new ArrayList();
    if (!spatialExtensionsAvailable())
    {
      LOG.debug("Oracle Spatial Extensions are not available. No cleaning of MDSYS tables and views.");
      return statements;
    }
    if (!((OracleDbSupport)this.dbSupport).getCurrentSchemaName().equalsIgnoreCase(this.name))
    {
      int count = this.jdbcTemplate.queryForInt("SELECT COUNT (*) FROM all_sdo_geom_metadata WHERE owner=?", new String[] { this.name });
      count += this.jdbcTemplate.queryForInt("SELECT COUNT (*) FROM all_sdo_index_info WHERE sdo_index_owner=?", new String[] { this.name });
      if (count > 0) {
        LOG.warn("Unable to clean Oracle Spatial objects for schema '" + this.name + "' as they do not belong to the default schema for this connection!");
      }
      return statements;
    }
    if (defaultSchemaForUser)
    {
      statements.add("DELETE FROM mdsys.user_sdo_geom_metadata");
      
      List<String> indexNames = this.jdbcTemplate.queryForStringList("select INDEX_NAME from USER_SDO_INDEX_INFO", new String[0]);
      for (String indexName : indexNames) {
        statements.add("DROP INDEX \"" + indexName + "\"");
      }
    }
    return statements;
  }
  
  private List<String> generateDropStatementsForScheduledJobs()
    throws SQLException
  {
    List<String> statements = new ArrayList();
    
    List<String> jobNames = this.jdbcTemplate.queryForStringList("select JOB_NAME from ALL_SCHEDULER_JOBS WHERE owner=?", new String[] { this.name });
    for (String jobName : jobNames) {
      statements.add("begin DBMS_SCHEDULER.DROP_JOB(job_name => '" + jobName + "', defer => false, force => true); end;");
    }
    return statements;
  }
  
  private List<String> generateDropStatementsForQueueTables()
    throws SQLException
  {
    List<String> statements = new ArrayList();
    
    List<String> queueTblNames = this.jdbcTemplate.queryForStringList("select QUEUE_TABLE from USER_QUEUE_TABLES", new String[0]);
    for (String queueTblName : queueTblNames) {
      statements.add("begin DBMS_AQADM.drop_queue_table (queue_table=> '" + queueTblName + "', FORCE => TRUE); end;");
    }
    return statements;
  }
  
  private boolean spatialExtensionsAvailable()
    throws SQLException
  {
    return this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM all_views WHERE owner = 'MDSYS' AND view_name = 'USER_SDO_GEOM_METADATA'", new String[0]) > 0;
  }
  
  protected Table[] doAllTables()
    throws SQLException
  {
    List<String> tableNames = this.jdbcTemplate.queryForStringList(" SELECT r FROM   (SELECT CONNECT_BY_ROOT t r FROM     (SELECT DISTINCT c1.table_name f, NVL(c2.table_name, at.table_name) t     FROM all_constraints c1       RIGHT JOIN all_constraints c2 ON c2.constraint_name = c1.r_constraint_name       RIGHT JOIN all_tables at ON at.table_name = c2.table_name     WHERE at.owner = ?       AND at.table_name NOT LIKE 'BIN$%'       AND at.table_name NOT LIKE 'MDRT_%$'       AND at.table_name NOT LIKE 'MLOG$%' AND at.table_name NOT LIKE 'RUPD$%'       AND at.table_name NOT LIKE 'DR$%'       AND at.table_name NOT LIKE 'SYS_IOT_OVER_%'       AND at.nested != 'YES'       AND at.secondary != 'Y')   CONNECT BY NOCYCLE PRIOR f = t) GROUP BY r ORDER BY COUNT(*)", new String[] { this.name });
    
    Table[] tables = new Table[tableNames.size()];
    for (int i = 0; i < tableNames.size(); i++) {
      tables[i] = new OracleTable(this.jdbcTemplate, this.dbSupport, this, (String)tableNames.get(i));
    }
    return tables;
  }
  
  public Table getTable(String tableName)
  {
    return new OracleTable(this.jdbcTemplate, this.dbSupport, this, tableName);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\oracle\OracleSchema.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */