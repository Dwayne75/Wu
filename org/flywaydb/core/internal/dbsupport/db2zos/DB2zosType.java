package org.flywaydb.core.internal.dbsupport.db2zos;

import java.sql.SQLException;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.JdbcTemplate;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.Type;

public class DB2zosType
  extends Type
{
  public DB2zosType(JdbcTemplate jdbcTemplate, DbSupport dbSupport, Schema schema, String name)
  {
    super(jdbcTemplate, dbSupport, schema, name);
  }
  
  protected void doDrop()
    throws SQLException
  {
    this.jdbcTemplate.execute("DROP TYPE " + this.dbSupport.quote(new String[] { this.schema.getName(), this.name }), new Object[0]);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\dbsupport\db2zos\DB2zosType.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */