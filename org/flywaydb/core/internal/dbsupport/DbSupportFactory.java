package org.flywaydb.core.internal.dbsupport;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.dbsupport.db2.DB2DbSupport;
import org.flywaydb.core.internal.dbsupport.db2zos.DB2zosDbSupport;
import org.flywaydb.core.internal.dbsupport.derby.DerbyDbSupport;
import org.flywaydb.core.internal.dbsupport.h2.H2DbSupport;
import org.flywaydb.core.internal.dbsupport.hsql.HsqlDbSupport;
import org.flywaydb.core.internal.dbsupport.mysql.MySQLDbSupport;
import org.flywaydb.core.internal.dbsupport.oracle.OracleDbSupport;
import org.flywaydb.core.internal.dbsupport.phoenix.PhoenixDbSupport;
import org.flywaydb.core.internal.dbsupport.postgresql.PostgreSQLDbSupport;
import org.flywaydb.core.internal.dbsupport.redshift.RedshfitDbSupportViaPostgreSQLDriver;
import org.flywaydb.core.internal.dbsupport.redshift.RedshfitDbSupportViaRedshiftDriver;
import org.flywaydb.core.internal.dbsupport.redshift.RedshiftDbSupport;
import org.flywaydb.core.internal.dbsupport.saphana.SapHanaDbSupport;
import org.flywaydb.core.internal.dbsupport.solid.SolidDbSupport;
import org.flywaydb.core.internal.dbsupport.sqlite.SQLiteDbSupport;
import org.flywaydb.core.internal.dbsupport.sqlserver.SQLServerDbSupport;
import org.flywaydb.core.internal.dbsupport.sybase.ase.SybaseASEDbSupport;
import org.flywaydb.core.internal.dbsupport.vertica.VerticaDbSupport;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public class DbSupportFactory
{
  private static final Log LOG = LogFactory.getLog(DbSupportFactory.class);
  
  public static DbSupport createDbSupport(Connection connection, boolean printInfo)
  {
    String databaseProductName = getDatabaseProductName(connection);
    if (printInfo) {
      LOG.info("Database: " + getJdbcUrl(connection) + " (" + databaseProductName + ")");
    }
    if (databaseProductName.startsWith("Apache Derby")) {
      return new DerbyDbSupport(connection);
    }
    if (databaseProductName.startsWith("SQLite")) {
      return new SQLiteDbSupport(connection);
    }
    if (databaseProductName.startsWith("H2")) {
      return new H2DbSupport(connection);
    }
    if (databaseProductName.contains("HSQL Database Engine")) {
      return new HsqlDbSupport(connection);
    }
    if (databaseProductName.startsWith("Microsoft SQL Server")) {
      return new SQLServerDbSupport(connection);
    }
    if (databaseProductName.contains("MySQL")) {
      return new MySQLDbSupport(connection);
    }
    if (databaseProductName.startsWith("Oracle")) {
      return new OracleDbSupport(connection);
    }
    if (databaseProductName.startsWith("PostgreSQL 8"))
    {
      RedshiftDbSupport redshift;
      RedshiftDbSupport redshift;
      if ("RedshiftJDBC".equals(getDriverName(connection))) {
        redshift = new RedshfitDbSupportViaRedshiftDriver(connection);
      } else {
        redshift = new RedshfitDbSupportViaPostgreSQLDriver(connection);
      }
      if (redshift.detect()) {
        return redshift;
      }
    }
    if (databaseProductName.startsWith("PostgreSQL")) {
      return new PostgreSQLDbSupport(connection);
    }
    if (databaseProductName.startsWith("DB2"))
    {
      if (getDatabaseProductVersion(connection).startsWith("DSN")) {
        return new DB2zosDbSupport(connection);
      }
      return new DB2DbSupport(connection);
    }
    if (databaseProductName.startsWith("Vertica")) {
      return new VerticaDbSupport(connection);
    }
    if (databaseProductName.contains("solidDB")) {
      return new SolidDbSupport(connection);
    }
    if (databaseProductName.startsWith("Phoenix")) {
      return new PhoenixDbSupport(connection);
    }
    if ((databaseProductName.startsWith("ASE")) || (databaseProductName.startsWith("Adaptive"))) {
      return new SybaseASEDbSupport(connection);
    }
    if (databaseProductName.startsWith("HDB")) {
      return new SapHanaDbSupport(connection);
    }
    throw new FlywayException("Unsupported Database: " + databaseProductName);
  }
  
  private static String getJdbcUrl(Connection connection)
  {
    try
    {
      return connection.getMetaData().getURL();
    }
    catch (SQLException e)
    {
      throw new FlywayException("Unable to retrieve the Jdbc connection Url!", e);
    }
  }
  
  private static String getDatabaseProductName(Connection connection)
  {
    try
    {
      DatabaseMetaData databaseMetaData = connection.getMetaData();
      if (databaseMetaData == null) {
        throw new FlywayException("Unable to read database metadata while it is null!");
      }
      String databaseProductName = databaseMetaData.getDatabaseProductName();
      if (databaseProductName == null) {
        throw new FlywayException("Unable to determine database. Product name is null.");
      }
      int databaseMajorVersion = databaseMetaData.getDatabaseMajorVersion();
      int databaseMinorVersion = databaseMetaData.getDatabaseMinorVersion();
      
      return databaseProductName + " " + databaseMajorVersion + "." + databaseMinorVersion;
    }
    catch (SQLException e)
    {
      throw new FlywayException("Error while determining database product name", e);
    }
  }
  
  private static String getDatabaseProductVersion(Connection connection)
  {
    try
    {
      DatabaseMetaData databaseMetaData = connection.getMetaData();
      if (databaseMetaData == null) {
        throw new FlywayException("Unable to read database metadata while it is null!");
      }
      String databaseProductVersion = databaseMetaData.getDatabaseProductVersion();
      if (databaseProductVersion == null) {
        throw new FlywayException("Unable to determine database. Product version is null.");
      }
      return databaseProductVersion;
    }
    catch (SQLException e)
    {
      throw new FlywayException("Error while determining database product version", e);
    }
  }
  
  private static String getDriverName(Connection connection)
  {
    try
    {
      DatabaseMetaData databaseMetaData = connection.getMetaData();
      if (databaseMetaData == null) {
        throw new FlywayException("Unable to read database metadata while it is null!");
      }
      String driverName = databaseMetaData.getDriverName();
      if (driverName == null) {
        throw new FlywayException("Unable to determine JDBC  driver name. JDBC driver name is null.");
      }
      return driverName;
    }
    catch (SQLException e)
    {
      throw new FlywayException("Error while determining JDBC driver name", e);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\DbSupportFactory.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */