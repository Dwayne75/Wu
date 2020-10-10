package org.flywaydb.core.internal.util.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.internal.util.logging.Log;
import org.flywaydb.core.internal.util.logging.LogFactory;

public class TransactionTemplate
{
  private static final Log LOG = LogFactory.getLog(TransactionTemplate.class);
  private final Connection connection;
  private final boolean rollbackOnException;
  
  public TransactionTemplate(Connection connection)
  {
    this(connection, true);
  }
  
  public TransactionTemplate(Connection connection, boolean rollbackOnException)
  {
    this.connection = connection;
    this.rollbackOnException = rollbackOnException;
  }
  
  public <T> T execute(TransactionCallback<T> transactionCallback)
  {
    boolean oldAutocommit = true;
    try
    {
      oldAutocommit = this.connection.getAutoCommit();
      this.connection.setAutoCommit(false);
      T result = transactionCallback.doInTransaction();
      this.connection.commit();
      return result;
    }
    catch (SQLException e)
    {
      throw new FlywayException("Unable to commit transaction", e);
    }
    catch (RuntimeException e)
    {
      if (this.rollbackOnException) {
        try
        {
          LOG.debug("Rolling back transaction...");
          this.connection.rollback();
          LOG.debug("Transaction rolled back");
        }
        catch (SQLException se)
        {
          LOG.error("Unable to rollback transaction", se);
        }
      } else {
        try
        {
          this.connection.commit();
        }
        catch (SQLException se)
        {
          LOG.error("Unable to commit transaction", se);
        }
      }
      throw e;
    }
    finally
    {
      try
      {
        this.connection.setAutoCommit(oldAutocommit);
      }
      catch (SQLException e)
      {
        LOG.error("Unable to restore autocommit to original value for connection", e);
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\jdbc\TransactionTemplate.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */