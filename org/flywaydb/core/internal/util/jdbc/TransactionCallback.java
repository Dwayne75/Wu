package org.flywaydb.core.internal.util.jdbc;

import java.sql.SQLException;

public abstract interface TransactionCallback<T>
{
  public abstract T doInTransaction()
    throws SQLException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\jdbc\TransactionCallback.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */