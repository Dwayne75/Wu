package org.flywaydb.core.internal.metadatatable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlScript;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.jdbc.RowMapper;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;

public class MetaDataTableImpl
  implements MetaDataTable
{
  private static final Log LOG = LogFactory.getLog(MetaDataTableImpl.class);
  private final DbSupport dbSupport;
  private final Table table;
  private final JdbcTemplate jdbcTemplate;
  
  public MetaDataTableImpl(DbSupport dbSupport, Table table)
  {
    this.jdbcTemplate = dbSupport.getJdbcTemplate();
    this.dbSupport = dbSupport;
    this.table = table;
  }
  
  public boolean upgradeIfNecessary()
  {
    if ((this.table.exists()) && (this.table.hasColumn("version_rank")))
    {
      LOG.info("Upgrading metadata table " + this.table + " to the Flyway 4.0 format ...");
      String resourceName = "org/flywaydb/core/internal/dbsupport/" + this.dbSupport.getDbName() + "/upgradeMetaDataTable.sql";
      String source = new ClassPathResource(resourceName, getClass().getClassLoader()).loadAsString("UTF-8");
      
      Map<String, String> placeholders = new HashMap();
      placeholders.put("schema", this.table.getSchema().getName());
      placeholders.put("table", this.table.getName());
      String sourceNoPlaceholders = new PlaceholderReplacer(placeholders, "${", "}").replacePlaceholders(source);
      
      SqlScript sqlScript = new SqlScript(sourceNoPlaceholders, this.dbSupport);
      sqlScript.execute(this.jdbcTemplate);
      return true;
    }
    return false;
  }
  
  private void createIfNotExists()
  {
    if (this.table.exists()) {
      return;
    }
    LOG.info("Creating Metadata table: " + this.table);
    
    String resourceName = "org/flywaydb/core/internal/dbsupport/" + this.dbSupport.getDbName() + "/createMetaDataTable.sql";
    String source = new ClassPathResource(resourceName, getClass().getClassLoader()).loadAsString("UTF-8");
    
    Map<String, String> placeholders = new HashMap();
    placeholders.put("schema", this.table.getSchema().getName());
    placeholders.put("table", this.table.getName());
    String sourceNoPlaceholders = new PlaceholderReplacer(placeholders, "${", "}").replacePlaceholders(source);
    
    SqlScript sqlScript = new SqlScript(sourceNoPlaceholders, this.dbSupport);
    sqlScript.execute(this.jdbcTemplate);
    
    LOG.debug("Metadata table " + this.table + " created.");
  }
  
  public void lock()
  {
    createIfNotExists();
    this.table.lock();
  }
  
  public void addAppliedMigration(AppliedMigration appliedMigration)
  {
    createIfNotExists();
    
    MigrationVersion version = appliedMigration.getVersion();
    try
    {
      String versionStr = version == null ? null : version.toString();
      
      String resourceName = "org/flywaydb/core/internal/dbsupport/" + this.dbSupport.getDbName() + "/updateMetaDataTable.sql";
      ClassPathResource classPathResource = new ClassPathResource(resourceName, getClass().getClassLoader());
      if (classPathResource.exists())
      {
        String source = classPathResource.loadAsString("UTF-8");
        Map<String, String> placeholders = new HashMap();
        
        placeholders.put("schema", this.table.getSchema().getName());
        placeholders.put("table", this.table.getName());
        
        placeholders.put("installed_rank_val", String.valueOf(calculateInstalledRank()));
        placeholders.put("version_val", versionStr);
        placeholders.put("description_val", appliedMigration.getDescription());
        placeholders.put("type_val", appliedMigration.getType().name());
        placeholders.put("script_val", appliedMigration.getScript());
        placeholders.put("checksum_val", String.valueOf(appliedMigration.getChecksum()));
        placeholders.put("installed_by_val", this.dbSupport.getCurrentUserFunction());
        placeholders.put("execution_time_val", String.valueOf(appliedMigration.getExecutionTime() * 1000L));
        placeholders.put("success_val", String.valueOf(appliedMigration.isSuccess()));
        
        String sourceNoPlaceholders = new PlaceholderReplacer(placeholders, "${", "}").replacePlaceholders(source);
        
        SqlScript sqlScript = new SqlScript(sourceNoPlaceholders, this.dbSupport);
        
        sqlScript.execute(this.jdbcTemplate);
      }
      else
      {
        this.jdbcTemplate.update("INSERT INTO " + this.table + " (" + this.dbSupport
          .quote(new String[] { "installed_rank" }) + "," + this.dbSupport
          .quote(new String[] { "version" }) + "," + this.dbSupport
          .quote(new String[] { "description" }) + "," + this.dbSupport
          .quote(new String[] { "type" }) + "," + this.dbSupport
          .quote(new String[] { "script" }) + "," + this.dbSupport
          .quote(new String[] { "checksum" }) + "," + this.dbSupport
          .quote(new String[] { "installed_by" }) + "," + this.dbSupport
          .quote(new String[] { "execution_time" }) + "," + this.dbSupport
          .quote(new String[] { "success" }) + ")" + " VALUES (?, ?, ?, ?, ?, ?, " + this.dbSupport
          
          .getCurrentUserFunction() + ", ?, ?)", new Object[] {
          Integer.valueOf(calculateInstalledRank()), versionStr, appliedMigration
          
          .getDescription(), appliedMigration
          .getType().name(), appliedMigration
          .getScript(), appliedMigration
          .getChecksum(), 
          Integer.valueOf(appliedMigration.getExecutionTime()), 
          Boolean.valueOf(appliedMigration.isSuccess()) });
      }
      LOG.debug("MetaData table " + this.table + " successfully updated to reflect changes");
    }
    catch (SQLException e)
    {
      throw new FlywayException("Unable to insert row for version '" + version + "' in metadata table " + this.table, e);
    }
  }
  
  private int calculateInstalledRank()
    throws SQLException
  {
    int currentMax = this.jdbcTemplate.queryForInt("SELECT MAX(" + this.dbSupport.quote(new String[] { "installed_rank" }) + ")" + " FROM " + this.table, new String[0]);
    
    return currentMax + 1;
  }
  
  public List<AppliedMigration> allAppliedMigrations()
  {
    return findAppliedMigrations(new MigrationType[0]);
  }
  
  private List<AppliedMigration> findAppliedMigrations(MigrationType... migrationTypes)
  {
    if (!this.table.exists()) {
      return new ArrayList();
    }
    createIfNotExists();
    
    String query = "SELECT " + this.dbSupport.quote(new String[] { "installed_rank" }) + "," + this.dbSupport.quote(new String[] { "version" }) + "," + this.dbSupport.quote(new String[] { "description" }) + "," + this.dbSupport.quote(new String[] { "type" }) + "," + this.dbSupport.quote(new String[] { "script" }) + "," + this.dbSupport.quote(new String[] { "checksum" }) + "," + this.dbSupport.quote(new String[] { "installed_on" }) + "," + this.dbSupport.quote(new String[] { "installed_by" }) + "," + this.dbSupport.quote(new String[] { "execution_time" }) + "," + this.dbSupport.quote(new String[] { "success" }) + " FROM " + this.table;
    if (migrationTypes.length > 0)
    {
      query = query + " WHERE " + this.dbSupport.quote(new String[] { "type" }) + " IN (";
      for (int i = 0; i < migrationTypes.length; i++)
      {
        if (i > 0) {
          query = query + ",";
        }
        query = query + "'" + migrationTypes[i] + "'";
      }
      query = query + ")";
    }
    query = query + " ORDER BY " + this.dbSupport.quote(new String[] { "installed_rank" });
    try
    {
      this.jdbcTemplate.query(query, new RowMapper()
      {
        public AppliedMigration mapRow(ResultSet rs)
          throws SQLException
        {
          Integer checksum = Integer.valueOf(rs.getInt("checksum"));
          if (rs.wasNull()) {
            checksum = null;
          }
          return new AppliedMigration(rs.getInt("installed_rank"), rs.getString("version") != null ? MigrationVersion.fromVersion(rs.getString("version")) : null, rs.getString("description"), MigrationType.valueOf(rs.getString("type")), rs.getString("script"), checksum, rs.getTimestamp("installed_on"), rs.getString("installed_by"), rs.getInt("execution_time"), rs.getBoolean("success"));
        }
      });
    }
    catch (SQLException e)
    {
      throw new FlywayException("Error while retrieving the list of applied migrations from metadata table " + this.table, e);
    }
  }
  
  public void addBaselineMarker(MigrationVersion baselineVersion, String baselineDescription)
  {
    addAppliedMigration(new AppliedMigration(baselineVersion, baselineDescription, MigrationType.BASELINE, baselineDescription, null, 0, true));
  }
  
  public void removeFailedMigrations()
  {
    if (!this.table.exists())
    {
      LOG.info("Repair of failed migration in metadata table " + this.table + " not necessary. No failed migration detected.");
      return;
    }
    createIfNotExists();
    try
    {
      int failedCount = this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + this.table + " WHERE " + this.dbSupport
        .quote(new String[] { "success" }) + "=" + this.dbSupport.getBooleanFalse(), new String[0]);
      if (failedCount == 0)
      {
        LOG.info("Repair of failed migration in metadata table " + this.table + " not necessary. No failed migration detected.");
        return;
      }
    }
    catch (SQLException e)
    {
      throw new FlywayException("Unable to check the metadata table " + this.table + " for failed migrations", e);
    }
    try
    {
      this.jdbcTemplate.execute("DELETE FROM " + this.table + " WHERE " + this.dbSupport
        .quote(new String[] { "success" }) + " = " + this.dbSupport.getBooleanFalse(), new Object[0]);
    }
    catch (SQLException e)
    {
      throw new FlywayException("Unable to repair metadata table " + this.table, e);
    }
  }
  
  public void addSchemasMarker(Schema[] schemas)
  {
    createIfNotExists();
    
    addAppliedMigration(new AppliedMigration(MigrationVersion.fromVersion("0"), "<< Flyway Schema Creation >>", MigrationType.SCHEMA, 
      StringUtils.arrayToCommaDelimitedString(schemas), null, 0, true));
  }
  
  public boolean hasSchemasMarker()
  {
    if (!this.table.exists()) {
      return false;
    }
    createIfNotExists();
    try
    {
      int count = this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + this.table + " WHERE " + this.dbSupport
        .quote(new String[] { "type" }) + "='SCHEMA'", new String[0]);
      return count > 0;
    }
    catch (SQLException e)
    {
      throw new FlywayException("Unable to check whether the metadata table " + this.table + " has a schema marker migration", e);
    }
  }
  
  public boolean hasBaselineMarker()
  {
    if (!this.table.exists()) {
      return false;
    }
    createIfNotExists();
    try
    {
      int count = this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + this.table + " WHERE " + this.dbSupport
        .quote(new String[] { "type" }) + "='INIT' OR " + this.dbSupport.quote(new String[] { "type" }) + "='BASELINE'", new String[0]);
      return count > 0;
    }
    catch (SQLException e)
    {
      throw new FlywayException("Unable to check whether the metadata table " + this.table + " has an baseline marker migration", e);
    }
  }
  
  public AppliedMigration getBaselineMarker()
  {
    List<AppliedMigration> appliedMigrations = findAppliedMigrations(new MigrationType[] { MigrationType.BASELINE });
    return appliedMigrations.isEmpty() ? null : (AppliedMigration)appliedMigrations.get(0);
  }
  
  public boolean hasAppliedMigrations()
  {
    if (!this.table.exists()) {
      return false;
    }
    createIfNotExists();
    try
    {
      int count = this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + this.table + " WHERE " + this.dbSupport
        .quote(new String[] { "type" }) + " NOT IN ('SCHEMA', 'INIT', 'BASELINE')", new String[0]);
      return count > 0;
    }
    catch (SQLException e)
    {
      throw new FlywayException("Unable to check whether the metadata table " + this.table + " has applied migrations", e);
    }
  }
  
  public void updateChecksum(MigrationVersion version, Integer checksum)
  {
    LOG.info("Updating checksum of " + version + " to " + checksum + " ...");
    try
    {
      String resourceName = "org/flywaydb/core/internal/dbsupport/" + this.dbSupport.getDbName() + "/updateChecksum.sql";
      String source = new ClassPathResource(resourceName, getClass().getClassLoader()).loadAsString("UTF-8");
      Map<String, String> placeholders = new HashMap();
      
      placeholders.put("schema", this.table.getSchema().getName());
      placeholders.put("table", this.table.getName());
      
      placeholders.put("version_val", version.toString());
      placeholders.put("checksum_val", String.valueOf(checksum));
      
      String sourceNoPlaceholders = new PlaceholderReplacer(placeholders, "${", "}").replacePlaceholders(source);
      
      SqlScript sqlScript = new SqlScript(sourceNoPlaceholders, this.dbSupport);
      
      sqlScript.execute(this.jdbcTemplate);
    }
    catch (FlywayException fe)
    {
      try
      {
        this.jdbcTemplate.update("UPDATE " + this.table + " SET " + this.dbSupport.quote(new String[] { "checksum" }) + "=" + checksum + " WHERE " + this.dbSupport
          .quote(new String[] { "version" }) + "='" + version + "'", new Object[0]);
      }
      catch (SQLException e)
      {
        throw new FlywayException("Unable to update checksum in metadata table " + this.table + " for version " + version + " to " + checksum, e);
      }
    }
  }
  
  public String toString()
  {
    return this.table.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\metadatatable\MetaDataTableImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */