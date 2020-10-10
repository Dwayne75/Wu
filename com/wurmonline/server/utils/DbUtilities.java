package com.wurmonline.server.utils;

import com.wurmonline.server.Constants;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.WillClose;

public final class DbUtilities
{
  private static Logger logger = Logger.getLogger(DbUtilities.class.getName());
  
  public static enum DbAdminAction
  {
    ANALYZE,  CHECK_QUICK,  CHECK_FAST,  CHECK_CHANGED,  CHECK_MEDIUM,  CHECK_EXTENDED,  OPTIMIZE;
    
    private DbAdminAction() {}
  }
  
  public static void closeDatabaseObjects(@Nullable @WillClose Statement aStatementToClose, @Nullable @WillClose ResultSet aResultSetToClose)
  {
    if (aResultSetToClose != null)
    {
      try
      {
        aResultSetToClose.close();
      }
      catch (Exception localException) {}
      aResultSetToClose = null;
    }
    if (aStatementToClose != null)
    {
      try
      {
        aStatementToClose.close();
      }
      catch (Exception localException1) {}
      aStatementToClose = null;
    }
  }
  
  public static void performAdminOnAllTables(Connection aConnection, DbAdminAction aAction)
  {
    logger.info("Performing " + aAction + " on all Wurm database tables");
    
    long start = System.nanoTime();
    
    ResultSet rsCatalogs = null;
    ResultSet rsTables = null;
    ResultSet rsOperationStatus = null;
    Statement lStmt = null;
    
    boolean problemsEncountered = false;
    try
    {
      DatabaseMetaData dbmd = aConnection.getMetaData();
      rsCatalogs = dbmd.getCatalogs();
      while (rsCatalogs.next())
      {
        String lCatalogName = rsCatalogs.getString("TABLE_CAT");
        boolean proceed = true;
        if (aAction == DbAdminAction.CHECK_MEDIUM) {
          if (lCatalogName.toUpperCase().startsWith("WURMLOGS")) {
            proceed = Constants.checkWurmLogs;
          }
        }
        if ((lCatalogName.toUpperCase().startsWith("WURM")) && (proceed))
        {
          logger.info("Performing " + aAction + " on CatalogName: " + lCatalogName);
          lStmt = aConnection.createStatement();
          rsTables = dbmd.getTables(lCatalogName, null, null, null);
          while (rsTables.next())
          {
            String lTableName = rsTables.getString("TABLE_NAME");
            String lAdminQuery = null;
            switch (aAction)
            {
            case ANALYZE: 
              lAdminQuery = "ANALYZE LOCAL TABLE " + lCatalogName + '.' + lTableName;
              break;
            case CHECK_CHANGED: 
              lAdminQuery = "CHECK TABLE " + lCatalogName + '.' + lTableName + " CHANGED";
              break;
            case CHECK_EXTENDED: 
              lAdminQuery = "CHECK TABLE " + lCatalogName + '.' + lTableName + " EXTENDED";
              break;
            case CHECK_FAST: 
              lAdminQuery = "CHECK TABLE " + lCatalogName + '.' + lTableName + " FAST";
              break;
            case CHECK_MEDIUM: 
              lAdminQuery = "CHECK TABLE " + lCatalogName + '.' + lTableName + " MEDIUM";
              break;
            case CHECK_QUICK: 
              lAdminQuery = "CHECK TABLE " + lCatalogName + '.' + lTableName + " QUICK";
              break;
            case OPTIMIZE: 
              lAdminQuery = "OPTIMIZE LOCAL TABLE " + lCatalogName + '.' + lTableName;
            }
            lStmt = aConnection.createStatement();
            if (lStmt.execute(lAdminQuery))
            {
              rsOperationStatus = lStmt.getResultSet();
              if (rsOperationStatus.next())
              {
                String lMsgType = rsOperationStatus.getString("Msg_type");
                String lMsgText = rsOperationStatus.getString("Msg_text");
                if (("OK".equals(lMsgText)) && ("status".equals(lMsgType)))
                {
                  if (logger.isLoggable(Level.FINE)) {
                    logger.fine("TableName: " + lAdminQuery + " - OK");
                  }
                }
                else
                {
                  logger.warning("TableName: " + lAdminQuery + " - " + lMsgType + ": " + lMsgText);
                  if ((!"status".equals(lMsgType)) || (lMsgText == null) || (lMsgText.contains("is not BASE TABLE"))) {}
                }
              }
              rsOperationStatus.close();
            }
            else if (logger.isLoggable(Level.FINE))
            {
              logger.fine("TableName: " + lAdminQuery);
            }
            lStmt.close();
          }
          rsTables.close();
        }
        else if (logger.isLoggable(Level.FINE))
        {
          logger.fine("Not performing " + aAction + " on non-Wurm CatalogName: " + lCatalogName);
        }
      }
      rsCatalogs.close();
    }
    catch (SQLException e)
    {
      float lElapsedTime;
      logger.log(Level.WARNING, e.getMessage(), e);
    }
    finally
    {
      float lElapsedTime;
      closeDatabaseObjects(lStmt, rsCatalogs);
      closeDatabaseObjects(null, rsTables);
      closeDatabaseObjects(null, rsOperationStatus);
      
      float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
      logger.info("Finished performing " + aAction + " on all database tables, which took " + lElapsedTime + " millis.");
      if (problemsEncountered) {
        logger.severe("\n\n**** At least one problem was encountered while performing admin actions ***********\n\n");
      }
    }
  }
  
  public static Timestamp getTimestampOrNull(String timestampString)
  {
    if (timestampString.contains(":")) {
      try
      {
        return new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timestampString).getTime());
      }
      catch (ParseException e)
      {
        logger.warning("Unable to convert '" + timestampString + "' into a timestamp, expected format: yyyy-MM-dd HH:mm:ss");
        return null;
      }
    }
    try
    {
      return new Timestamp(Long.parseLong(timestampString));
    }
    catch (NumberFormatException e)
    {
      logger.warning("Unable to convert '" + timestampString + "' into a timestamp, value is not valid for type 'long'");
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\utils\DbUtilities.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */