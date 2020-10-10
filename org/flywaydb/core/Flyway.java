package org.flywaydb.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import javax.sql.DataSource;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.internal.callback.SqlScriptFlywayCallback;
import org.flywaydb.core.internal.command.DbBaseline;
import org.flywaydb.core.internal.command.DbClean;
import org.flywaydb.core.internal.command.DbMigrate;
import org.flywaydb.core.internal.command.DbRepair;
import org.flywaydb.core.internal.command.DbSchemas;
import org.flywaydb.core.internal.command.DbValidate;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.DbSupportFactory;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.info.MigrationInfoServiceImpl;
import org.flywaydb.core.internal.metadatatable.MetaDataTable;
import org.flywaydb.core.internal.metadatatable.MetaDataTableImpl;
import org.flywaydb.core.internal.resolver.CompositeMigrationResolver;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.ConfigurationInjectionUtils;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.Locations;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.VersionPrinter;
import org.flywaydb.core.internal.util.jdbc.DriverDataSource;
import org.flywaydb.core.internal.util.jdbc.JdbcUtils;
import org.flywaydb.core.internal.util.jdbc.TransactionCallback;
import org.flywaydb.core.internal.util.jdbc.TransactionTemplate;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;
import org.flywaydb.core.internal.util.scanner.Scanner;

public class Flyway
  implements FlywayConfiguration
{
  private static final Log LOG = LogFactory.getLog(Flyway.class);
  private static final String PLACEHOLDERS_PROPERTY_PREFIX = "flyway.placeholders.";
  private Locations locations = new Locations(new String[] { "db/migration" });
  private String encoding = "UTF-8";
  private String[] schemaNames = new String[0];
  private String table = "schema_version";
  private MigrationVersion target = MigrationVersion.LATEST;
  private boolean placeholderReplacement = true;
  private Map<String, String> placeholders = new HashMap();
  private String placeholderPrefix = "${";
  private String placeholderSuffix = "}";
  private String sqlMigrationPrefix = "V";
  private String repeatableSqlMigrationPrefix = "R";
  private String sqlMigrationSeparator = "__";
  private String sqlMigrationSuffix = ".sql";
  private boolean ignoreFutureMigrations = true;
  @Deprecated
  private boolean ignoreFailedFutureMigration;
  private boolean validateOnMigrate = true;
  private boolean cleanOnValidationError;
  private boolean cleanDisabled;
  private MigrationVersion baselineVersion = MigrationVersion.fromVersion("1");
  private String baselineDescription = "<< Flyway Baseline >>";
  private boolean baselineOnMigrate;
  private boolean outOfOrder;
  private FlywayCallback[] callbacks = new FlywayCallback[0];
  private boolean skipDefaultCallbacks;
  private MigrationResolver[] resolvers = new MigrationResolver[0];
  private boolean skipDefaultResolvers;
  private boolean createdDataSource;
  private DataSource dataSource;
  private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
  private boolean dbConnectionInfoPrinted;
  
  public String[] getLocations()
  {
    String[] result = new String[this.locations.getLocations().size()];
    for (int i = 0; i < this.locations.getLocations().size(); i++) {
      result[i] = ((Location)this.locations.getLocations().get(i)).toString();
    }
    return result;
  }
  
  public String getEncoding()
  {
    return this.encoding;
  }
  
  public String[] getSchemas()
  {
    return this.schemaNames;
  }
  
  public String getTable()
  {
    return this.table;
  }
  
  public MigrationVersion getTarget()
  {
    return this.target;
  }
  
  public boolean isPlaceholderReplacement()
  {
    return this.placeholderReplacement;
  }
  
  public Map<String, String> getPlaceholders()
  {
    return this.placeholders;
  }
  
  public String getPlaceholderPrefix()
  {
    return this.placeholderPrefix;
  }
  
  public String getPlaceholderSuffix()
  {
    return this.placeholderSuffix;
  }
  
  public String getSqlMigrationPrefix()
  {
    return this.sqlMigrationPrefix;
  }
  
  public String getRepeatableSqlMigrationPrefix()
  {
    return this.repeatableSqlMigrationPrefix;
  }
  
  public String getSqlMigrationSeparator()
  {
    return this.sqlMigrationSeparator;
  }
  
  public String getSqlMigrationSuffix()
  {
    return this.sqlMigrationSuffix;
  }
  
  public boolean isIgnoreFutureMigrations()
  {
    return this.ignoreFutureMigrations;
  }
  
  @Deprecated
  public boolean isIgnoreFailedFutureMigration()
  {
    LOG.warn("ignoreFailedFutureMigration has been deprecated and will be removed in Flyway 5.0. Use the more generic ignoreFutureMigrations instead.");
    return this.ignoreFailedFutureMigration;
  }
  
  public boolean isValidateOnMigrate()
  {
    return this.validateOnMigrate;
  }
  
  public boolean isCleanOnValidationError()
  {
    return this.cleanOnValidationError;
  }
  
  public boolean isCleanDisabled()
  {
    return this.cleanDisabled;
  }
  
  public MigrationVersion getBaselineVersion()
  {
    return this.baselineVersion;
  }
  
  public String getBaselineDescription()
  {
    return this.baselineDescription;
  }
  
  public boolean isBaselineOnMigrate()
  {
    return this.baselineOnMigrate;
  }
  
  public boolean isOutOfOrder()
  {
    return this.outOfOrder;
  }
  
  public MigrationResolver[] getResolvers()
  {
    return this.resolvers;
  }
  
  public boolean isSkipDefaultResolvers()
  {
    return this.skipDefaultResolvers;
  }
  
  public DataSource getDataSource()
  {
    return this.dataSource;
  }
  
  public ClassLoader getClassLoader()
  {
    return this.classLoader;
  }
  
  public void setIgnoreFutureMigrations(boolean ignoreFutureMigrations)
  {
    this.ignoreFutureMigrations = ignoreFutureMigrations;
  }
  
  @Deprecated
  public void setIgnoreFailedFutureMigration(boolean ignoreFailedFutureMigration)
  {
    LOG.warn("ignoreFailedFutureMigration has been deprecated and will be removed in Flyway 5.0. Use the more generic ignoreFutureMigrations instead.");
    this.ignoreFailedFutureMigration = ignoreFailedFutureMigration;
  }
  
  public void setValidateOnMigrate(boolean validateOnMigrate)
  {
    this.validateOnMigrate = validateOnMigrate;
  }
  
  public void setCleanOnValidationError(boolean cleanOnValidationError)
  {
    this.cleanOnValidationError = cleanOnValidationError;
  }
  
  public void setCleanDisabled(boolean cleanDisabled)
  {
    this.cleanDisabled = cleanDisabled;
  }
  
  public void setLocations(String... locations)
  {
    this.locations = new Locations(locations);
  }
  
  public void setEncoding(String encoding)
  {
    this.encoding = encoding;
  }
  
  public void setSchemas(String... schemas)
  {
    this.schemaNames = schemas;
  }
  
  public void setTable(String table)
  {
    this.table = table;
  }
  
  public void setTarget(MigrationVersion target)
  {
    this.target = target;
  }
  
  public void setTargetAsString(String target)
  {
    this.target = MigrationVersion.fromVersion(target);
  }
  
  public void setPlaceholderReplacement(boolean placeholderReplacement)
  {
    this.placeholderReplacement = placeholderReplacement;
  }
  
  public void setPlaceholders(Map<String, String> placeholders)
  {
    this.placeholders = placeholders;
  }
  
  public void setPlaceholderPrefix(String placeholderPrefix)
  {
    if (!StringUtils.hasLength(placeholderPrefix)) {
      throw new FlywayException("placeholderPrefix cannot be empty!");
    }
    this.placeholderPrefix = placeholderPrefix;
  }
  
  public void setPlaceholderSuffix(String placeholderSuffix)
  {
    if (!StringUtils.hasLength(placeholderSuffix)) {
      throw new FlywayException("placeholderSuffix cannot be empty!");
    }
    this.placeholderSuffix = placeholderSuffix;
  }
  
  public void setSqlMigrationPrefix(String sqlMigrationPrefix)
  {
    this.sqlMigrationPrefix = sqlMigrationPrefix;
  }
  
  public void setRepeatableSqlMigrationPrefix(String repeatableSqlMigrationPrefix)
  {
    this.repeatableSqlMigrationPrefix = repeatableSqlMigrationPrefix;
  }
  
  public void setSqlMigrationSeparator(String sqlMigrationSeparator)
  {
    if (!StringUtils.hasLength(sqlMigrationSeparator)) {
      throw new FlywayException("sqlMigrationSeparator cannot be empty!");
    }
    this.sqlMigrationSeparator = sqlMigrationSeparator;
  }
  
  public void setSqlMigrationSuffix(String sqlMigrationSuffix)
  {
    this.sqlMigrationSuffix = sqlMigrationSuffix;
  }
  
  public void setDataSource(DataSource dataSource)
  {
    this.dataSource = dataSource;
    this.createdDataSource = false;
  }
  
  public void setDataSource(String url, String user, String password, String... initSqls)
  {
    this.dataSource = new DriverDataSource(this.classLoader, null, url, user, password, initSqls);
    this.createdDataSource = true;
  }
  
  public void setClassLoader(ClassLoader classLoader)
  {
    this.classLoader = classLoader;
  }
  
  public void setBaselineVersion(MigrationVersion baselineVersion)
  {
    this.baselineVersion = baselineVersion;
  }
  
  public void setBaselineVersionAsString(String baselineVersion)
  {
    this.baselineVersion = MigrationVersion.fromVersion(baselineVersion);
  }
  
  public void setBaselineDescription(String baselineDescription)
  {
    this.baselineDescription = baselineDescription;
  }
  
  public void setBaselineOnMigrate(boolean baselineOnMigrate)
  {
    this.baselineOnMigrate = baselineOnMigrate;
  }
  
  public void setOutOfOrder(boolean outOfOrder)
  {
    this.outOfOrder = outOfOrder;
  }
  
  public FlywayCallback[] getCallbacks()
  {
    return this.callbacks;
  }
  
  public boolean isSkipDefaultCallbacks()
  {
    return this.skipDefaultCallbacks;
  }
  
  public void setCallbacks(FlywayCallback... callbacks)
  {
    this.callbacks = callbacks;
  }
  
  public void setCallbacksAsClassNames(String... callbacks)
  {
    List<FlywayCallback> callbackList = ClassUtils.instantiateAll(callbacks, this.classLoader);
    setCallbacks((FlywayCallback[])callbackList.toArray(new FlywayCallback[callbacks.length]));
  }
  
  public void setSkipDefaultCallbacks(boolean skipDefaultCallbacks)
  {
    this.skipDefaultCallbacks = skipDefaultCallbacks;
  }
  
  public void setResolvers(MigrationResolver... resolvers)
  {
    this.resolvers = resolvers;
  }
  
  public void setResolversAsClassNames(String... resolvers)
  {
    List<MigrationResolver> resolverList = ClassUtils.instantiateAll(resolvers, this.classLoader);
    setResolvers((MigrationResolver[])resolverList.toArray(new MigrationResolver[resolvers.length]));
  }
  
  public void setSkipDefaultResolvers(boolean skipDefaultResolvers)
  {
    this.skipDefaultResolvers = skipDefaultResolvers;
  }
  
  public int migrate()
    throws FlywayException
  {
    ((Integer)execute(new Command()
    {
      public Integer execute(Connection connectionMetaDataTable, Connection connectionUserObjects, MigrationResolver migrationResolver, MetaDataTable metaDataTable, DbSupport dbSupport, Schema[] schemas, FlywayCallback[] flywayCallbacks)
      {
        if (Flyway.this.validateOnMigrate) {
          Flyway.this.doValidate(connectionMetaDataTable, dbSupport, migrationResolver, metaDataTable, schemas, flywayCallbacks, true);
        }
        new DbSchemas(connectionMetaDataTable, schemas, metaDataTable).create();
        if ((!metaDataTable.hasSchemasMarker()) && (!metaDataTable.hasBaselineMarker()) && (!metaDataTable.hasAppliedMigrations()))
        {
          List<Schema> nonEmptySchemas = new ArrayList();
          for (Schema schema : schemas) {
            if (!schema.empty()) {
              nonEmptySchemas.add(schema);
            }
          }
          if ((Flyway.this.baselineOnMigrate) || (nonEmptySchemas.isEmpty()))
          {
            if ((Flyway.this.baselineOnMigrate) && (!nonEmptySchemas.isEmpty())) {
              new DbBaseline(connectionMetaDataTable, dbSupport, metaDataTable, schemas[0], Flyway.this.baselineVersion, Flyway.this.baselineDescription, flywayCallbacks).baseline();
            }
          }
          else if (nonEmptySchemas.size() == 1)
          {
            Schema schema = (Schema)nonEmptySchemas.get(0);
            if ((schema.allTables().length != 1) || (!schema.getTable(Flyway.this.table).exists())) {
              throw new FlywayException("Found non-empty schema " + schema + " without metadata table! Use baseline()" + " or set baselineOnMigrate to true to initialize the metadata table.");
            }
          }
          else
          {
            throw new FlywayException("Found non-empty schemas " + StringUtils.collectionToCommaDelimitedString(nonEmptySchemas) + " without metadata table! Use baseline()" + " or set baselineOnMigrate to true to initialize the metadata table.");
          }
        }
        DbMigrate dbMigrate = new DbMigrate(connectionMetaDataTable, connectionUserObjects, dbSupport, metaDataTable, schemas[0], migrationResolver, Flyway.this.target, Flyway.this.ignoreFutureMigrations, Flyway.this.ignoreFailedFutureMigration, Flyway.this.outOfOrder, flywayCallbacks);
        return Integer.valueOf(dbMigrate.migrate());
      }
    })).intValue();
  }
  
  public void validate()
    throws FlywayException
  {
    execute(new Command()
    {
      public Void execute(Connection connectionMetaDataTable, Connection connectionUserObjects, MigrationResolver migrationResolver, MetaDataTable metaDataTable, DbSupport dbSupport, Schema[] schemas, FlywayCallback[] flywayCallbacks)
      {
        Flyway.this.doValidate(connectionMetaDataTable, dbSupport, migrationResolver, metaDataTable, schemas, flywayCallbacks, false);
        return null;
      }
    });
  }
  
  private void doValidate(Connection connectionMetaDataTable, DbSupport dbSupport, MigrationResolver migrationResolver, MetaDataTable metaDataTable, Schema[] schemas, FlywayCallback[] flywayCallbacks, boolean pending)
  {
    String validationError = new DbValidate(connectionMetaDataTable, dbSupport, metaDataTable, schemas[0], migrationResolver, this.target, this.outOfOrder, pending, this.ignoreFutureMigrations, flywayCallbacks).validate();
    if (validationError != null) {
      if (this.cleanOnValidationError) {
        new DbClean(connectionMetaDataTable, dbSupport, metaDataTable, schemas, flywayCallbacks, this.cleanDisabled).clean();
      } else {
        throw new FlywayException("Validate failed: " + validationError);
      }
    }
  }
  
  public void clean()
  {
    execute(new Command()
    {
      public Void execute(Connection connectionMetaDataTable, Connection connectionUserObjects, MigrationResolver migrationResolver, MetaDataTable metaDataTable, DbSupport dbSupport, Schema[] schemas, FlywayCallback[] flywayCallbacks)
      {
        new DbClean(connectionMetaDataTable, dbSupport, metaDataTable, schemas, flywayCallbacks, Flyway.this.cleanDisabled).clean();
        return null;
      }
    });
  }
  
  public MigrationInfoService info()
  {
    (MigrationInfoService)execute(new Command()
    {
      public MigrationInfoService execute(final Connection connectionMetaDataTable, Connection connectionUserObjects, MigrationResolver migrationResolver, MetaDataTable metaDataTable, final DbSupport dbSupport, final Schema[] schemas, FlywayCallback[] flywayCallbacks)
      {
        try
        {
          FlywayCallback[] arrayOfFlywayCallback = flywayCallbacks;int i = arrayOfFlywayCallback.length;
          for (FlywayCallback localFlywayCallback1 = 0; localFlywayCallback1 < i; localFlywayCallback1++)
          {
            callback = arrayOfFlywayCallback[localFlywayCallback1];
            new TransactionTemplate(connectionMetaDataTable).execute(new TransactionCallback()
            {
              public Object doInTransaction()
                throws SQLException
              {
                dbSupport.changeCurrentSchemaTo(schemas[0]);
                callback.beforeInfo(connectionMetaDataTable);
                return null;
              }
            });
          }
          MigrationInfoServiceImpl migrationInfoService = new MigrationInfoServiceImpl(migrationResolver, metaDataTable, Flyway.this.target, Flyway.this.outOfOrder, true, true);
          migrationInfoService.refresh();
          
          Object localObject1 = flywayCallbacks;localFlywayCallback1 = localObject1.length;
          for (final FlywayCallback callback = 0; callback < localFlywayCallback1; callback++)
          {
            final FlywayCallback callback = localObject1[callback];
            new TransactionTemplate(connectionMetaDataTable).execute(new TransactionCallback()
            {
              public Object doInTransaction()
                throws SQLException
              {
                dbSupport.changeCurrentSchemaTo(schemas[0]);
                callback.afterInfo(connectionMetaDataTable);
                return null;
              }
            });
          }
          return migrationInfoService;
        }
        finally
        {
          dbSupport.restoreCurrentSchema();
        }
      }
    });
  }
  
  public void baseline()
    throws FlywayException
  {
    execute(new Command()
    {
      public Void execute(Connection connectionMetaDataTable, Connection connectionUserObjects, MigrationResolver migrationResolver, MetaDataTable metaDataTable, DbSupport dbSupport, Schema[] schemas, FlywayCallback[] flywayCallbacks)
      {
        new DbSchemas(connectionMetaDataTable, schemas, metaDataTable).create();
        new DbBaseline(connectionMetaDataTable, dbSupport, metaDataTable, schemas[0], Flyway.this.baselineVersion, Flyway.this.baselineDescription, flywayCallbacks).baseline();
        return null;
      }
    });
  }
  
  public void repair()
    throws FlywayException
  {
    execute(new Command()
    {
      public Void execute(Connection connectionMetaDataTable, Connection connectionUserObjects, MigrationResolver migrationResolver, MetaDataTable metaDataTable, DbSupport dbSupport, Schema[] schemas, FlywayCallback[] flywayCallbacks)
      {
        new DbRepair(dbSupport, connectionMetaDataTable, schemas[0], migrationResolver, metaDataTable, flywayCallbacks).repair();
        return null;
      }
    });
  }
  
  private MigrationResolver createMigrationResolver(DbSupport dbSupport, Scanner scanner)
  {
    for (MigrationResolver resolver : this.resolvers) {
      ConfigurationInjectionUtils.injectFlywayConfiguration(resolver, this);
    }
    return new CompositeMigrationResolver(dbSupport, scanner, this, this.locations, this.encoding, this.sqlMigrationPrefix, this.repeatableSqlMigrationPrefix, this.sqlMigrationSeparator, this.sqlMigrationSuffix, createPlaceholderReplacer(), this.resolvers);
  }
  
  private PlaceholderReplacer createPlaceholderReplacer()
  {
    if (this.placeholderReplacement) {
      return new PlaceholderReplacer(this.placeholders, this.placeholderPrefix, this.placeholderSuffix);
    }
    return PlaceholderReplacer.NO_PLACEHOLDERS;
  }
  
  public void configure(Properties properties)
  {
    Map<String, String> props = new HashMap();
    for (Map.Entry<Object, Object> entry : properties.entrySet()) {
      props.put(entry.getKey().toString(), entry.getValue().toString());
    }
    String driverProp = getValueAndRemoveEntry(props, "flyway.driver");
    String urlProp = getValueAndRemoveEntry(props, "flyway.url");
    String userProp = getValueAndRemoveEntry(props, "flyway.user");
    String passwordProp = getValueAndRemoveEntry(props, "flyway.password");
    if (StringUtils.hasText(urlProp)) {
      setDataSource(new DriverDataSource(this.classLoader, driverProp, urlProp, userProp, passwordProp, new String[0]));
    } else if ((!StringUtils.hasText(urlProp)) && (
      (StringUtils.hasText(driverProp)) || (StringUtils.hasText(userProp)) || (StringUtils.hasText(passwordProp)))) {
      LOG.warn("Discarding INCOMPLETE dataSource configuration! flyway.url must be set.");
    }
    String locationsProp = getValueAndRemoveEntry(props, "flyway.locations");
    if (locationsProp != null) {
      setLocations(StringUtils.tokenizeToStringArray(locationsProp, ","));
    }
    String placeholderReplacementProp = getValueAndRemoveEntry(props, "flyway.placeholderReplacement");
    if (placeholderReplacementProp != null) {
      setPlaceholderReplacement(Boolean.parseBoolean(placeholderReplacementProp));
    }
    String placeholderPrefixProp = getValueAndRemoveEntry(props, "flyway.placeholderPrefix");
    if (placeholderPrefixProp != null) {
      setPlaceholderPrefix(placeholderPrefixProp);
    }
    String placeholderSuffixProp = getValueAndRemoveEntry(props, "flyway.placeholderSuffix");
    if (placeholderSuffixProp != null) {
      setPlaceholderSuffix(placeholderSuffixProp);
    }
    String sqlMigrationPrefixProp = getValueAndRemoveEntry(props, "flyway.sqlMigrationPrefix");
    if (sqlMigrationPrefixProp != null) {
      setSqlMigrationPrefix(sqlMigrationPrefixProp);
    }
    String repeatableSqlMigrationPrefixProp = getValueAndRemoveEntry(props, "flyway.repeatableSqlMigrationPrefix");
    if (repeatableSqlMigrationPrefixProp != null) {
      setRepeatableSqlMigrationPrefix(repeatableSqlMigrationPrefixProp);
    }
    String sqlMigrationSeparatorProp = getValueAndRemoveEntry(props, "flyway.sqlMigrationSeparator");
    if (sqlMigrationSeparatorProp != null) {
      setSqlMigrationSeparator(sqlMigrationSeparatorProp);
    }
    String sqlMigrationSuffixProp = getValueAndRemoveEntry(props, "flyway.sqlMigrationSuffix");
    if (sqlMigrationSuffixProp != null) {
      setSqlMigrationSuffix(sqlMigrationSuffixProp);
    }
    String encodingProp = getValueAndRemoveEntry(props, "flyway.encoding");
    if (encodingProp != null) {
      setEncoding(encodingProp);
    }
    String schemasProp = getValueAndRemoveEntry(props, "flyway.schemas");
    if (schemasProp != null) {
      setSchemas(StringUtils.tokenizeToStringArray(schemasProp, ","));
    }
    String tableProp = getValueAndRemoveEntry(props, "flyway.table");
    if (tableProp != null) {
      setTable(tableProp);
    }
    String cleanOnValidationErrorProp = getValueAndRemoveEntry(props, "flyway.cleanOnValidationError");
    if (cleanOnValidationErrorProp != null) {
      setCleanOnValidationError(Boolean.parseBoolean(cleanOnValidationErrorProp));
    }
    String cleanDisabledProp = getValueAndRemoveEntry(props, "flyway.cleanDisabled");
    if (cleanDisabledProp != null) {
      setCleanDisabled(Boolean.parseBoolean(cleanDisabledProp));
    }
    String validateOnMigrateProp = getValueAndRemoveEntry(props, "flyway.validateOnMigrate");
    if (validateOnMigrateProp != null) {
      setValidateOnMigrate(Boolean.parseBoolean(validateOnMigrateProp));
    }
    String baselineVersionProp = getValueAndRemoveEntry(props, "flyway.baselineVersion");
    if (baselineVersionProp != null) {
      setBaselineVersion(MigrationVersion.fromVersion(baselineVersionProp));
    }
    String baselineDescriptionProp = getValueAndRemoveEntry(props, "flyway.baselineDescription");
    if (baselineDescriptionProp != null) {
      setBaselineDescription(baselineDescriptionProp);
    }
    String baselineOnMigrateProp = getValueAndRemoveEntry(props, "flyway.baselineOnMigrate");
    if (baselineOnMigrateProp != null) {
      setBaselineOnMigrate(Boolean.parseBoolean(baselineOnMigrateProp));
    }
    String ignoreFutureMigrationsProp = getValueAndRemoveEntry(props, "flyway.ignoreFutureMigrations");
    if (ignoreFutureMigrationsProp != null) {
      setIgnoreFutureMigrations(Boolean.parseBoolean(ignoreFutureMigrationsProp));
    }
    String ignoreFailedFutureMigrationProp = getValueAndRemoveEntry(props, "flyway.ignoreFailedFutureMigration");
    if (ignoreFailedFutureMigrationProp != null) {
      setIgnoreFailedFutureMigration(Boolean.parseBoolean(ignoreFailedFutureMigrationProp));
    }
    String targetProp = getValueAndRemoveEntry(props, "flyway.target");
    if (targetProp != null) {
      setTarget(MigrationVersion.fromVersion(targetProp));
    }
    String outOfOrderProp = getValueAndRemoveEntry(props, "flyway.outOfOrder");
    if (outOfOrderProp != null) {
      setOutOfOrder(Boolean.parseBoolean(outOfOrderProp));
    }
    String resolversProp = getValueAndRemoveEntry(props, "flyway.resolvers");
    if (StringUtils.hasLength(resolversProp)) {
      setResolversAsClassNames(StringUtils.tokenizeToStringArray(resolversProp, ","));
    }
    String skipDefaultResolversProp = getValueAndRemoveEntry(props, "flyway.skipDefaultResolvers");
    if (skipDefaultResolversProp != null) {
      setSkipDefaultResolvers(Boolean.parseBoolean(skipDefaultResolversProp));
    }
    String callbacksProp = getValueAndRemoveEntry(props, "flyway.callbacks");
    if (StringUtils.hasLength(callbacksProp)) {
      setCallbacksAsClassNames(StringUtils.tokenizeToStringArray(callbacksProp, ","));
    }
    String skipDefaultCallbacksProp = getValueAndRemoveEntry(props, "flyway.skipDefaultCallbacks");
    if (skipDefaultCallbacksProp != null) {
      setSkipDefaultCallbacks(Boolean.parseBoolean(skipDefaultCallbacksProp));
    }
    Map<String, String> placeholdersFromProps = new HashMap(this.placeholders);
    Iterator<Map.Entry<String, String>> iterator = props.entrySet().iterator();
    Map.Entry<String, String> entry;
    while (iterator.hasNext())
    {
      entry = (Map.Entry)iterator.next();
      String propertyName = (String)entry.getKey();
      if ((propertyName.startsWith("flyway.placeholders.")) && 
        (propertyName.length() > "flyway.placeholders.".length()))
      {
        String placeholderName = propertyName.substring("flyway.placeholders.".length());
        String placeholderValue = (String)entry.getValue();
        placeholdersFromProps.put(placeholderName, placeholderValue);
        iterator.remove();
      }
    }
    setPlaceholders(placeholdersFromProps);
    for (String key : props.keySet()) {
      if (key.startsWith("flyway.")) {
        LOG.warn("Unknown configuration property: " + key);
      }
    }
  }
  
  private String getValueAndRemoveEntry(Map<String, String> map, String key)
  {
    String value = (String)map.get(key);
    map.remove(key);
    return value;
  }
  
  <T> T execute(Command<T> command)
  {
    VersionPrinter.printVersion();
    
    Connection connectionMetaDataTable = null;
    Connection connectionUserObjects = null;
    try
    {
      if (this.dataSource == null) {
        throw new FlywayException("Unable to connect to the database. Configure the url, user and password!");
      }
      connectionMetaDataTable = JdbcUtils.openConnection(this.dataSource);
      connectionUserObjects = JdbcUtils.openConnection(this.dataSource);
      
      DbSupport dbSupport = DbSupportFactory.createDbSupport(connectionMetaDataTable, !this.dbConnectionInfoPrinted);
      this.dbConnectionInfoPrinted = true;
      LOG.debug("DDL Transactions Supported: " + dbSupport.supportsDdlTransactions());
      if (this.schemaNames.length == 0)
      {
        Schema currentSchema = dbSupport.getOriginalSchema();
        if (currentSchema == null) {
          throw new FlywayException("Unable to determine schema for the metadata table. Set a default schema for the connection or specify one using the schemas property!");
        }
        setSchemas(new String[] { currentSchema.getName() });
      }
      if (this.schemaNames.length == 1) {
        LOG.debug("Schema: " + this.schemaNames[0]);
      } else {
        LOG.debug("Schemas: " + StringUtils.arrayToCommaDelimitedString(this.schemaNames));
      }
      Schema[] schemas = new Schema[this.schemaNames.length];
      for (int i = 0; i < this.schemaNames.length; i++) {
        schemas[i] = dbSupport.getSchema(this.schemaNames[i]);
      }
      Scanner scanner = new Scanner(this.classLoader);
      MigrationResolver migrationResolver = createMigrationResolver(dbSupport, scanner);
      
      Set<FlywayCallback> flywayCallbacks = new LinkedHashSet(Arrays.asList(this.callbacks));
      if (!this.skipDefaultCallbacks) {
        flywayCallbacks.add(new SqlScriptFlywayCallback(dbSupport, scanner, this.locations, createPlaceholderReplacer(), this.encoding, this.sqlMigrationSuffix));
      }
      for (FlywayCallback callback : flywayCallbacks) {
        ConfigurationInjectionUtils.injectFlywayConfiguration(callback, this);
      }
      FlywayCallback[] flywayCallbacksArray = (FlywayCallback[])flywayCallbacks.toArray(new FlywayCallback[flywayCallbacks.size()]);
      MetaDataTable metaDataTable = new MetaDataTableImpl(dbSupport, schemas[0].getTable(this.table));
      if (metaDataTable.upgradeIfNecessary())
      {
        new DbRepair(dbSupport, connectionMetaDataTable, schemas[0], migrationResolver, metaDataTable, flywayCallbacksArray).repairChecksums();
        LOG.info("Metadata table " + this.table + " successfully upgraded to the Flyway 4.0 format.");
      }
      result = command.execute(connectionMetaDataTable, connectionUserObjects, migrationResolver, metaDataTable, dbSupport, schemas, flywayCallbacksArray);
    }
    finally
    {
      T result;
      JdbcUtils.closeConnection(connectionUserObjects);
      JdbcUtils.closeConnection(connectionMetaDataTable);
      if (((this.dataSource instanceof DriverDataSource)) && (this.createdDataSource)) {
        ((DriverDataSource)this.dataSource).close();
      }
    }
    T result;
    return result;
  }
  
  static abstract interface Command<T>
  {
    public abstract T execute(Connection paramConnection1, Connection paramConnection2, MigrationResolver paramMigrationResolver, MetaDataTable paramMetaDataTable, DbSupport paramDbSupport, Schema[] paramArrayOfSchema, FlywayCallback[] paramArrayOfFlywayCallback);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\Flyway.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */