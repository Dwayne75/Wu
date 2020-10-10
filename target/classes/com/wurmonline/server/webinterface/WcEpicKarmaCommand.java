package com.wurmonline.server.webinterface;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WcEpicKarmaCommand
  extends WebCommand
{
  private static final Logger logger = Logger.getLogger(WcEpicKarmaCommand.class.getName());
  private long[] pids;
  private int[] karmas;
  private int deity;
  private static final String CLEAR_KARMA = "DELETE FROM HELPERS";
  
  public WcEpicKarmaCommand(long _id, long[] playerids, int[] karmaValues, int _deity)
  {
    super(_id, (short)16);
    this.pids = playerids;
    this.karmas = karmaValues;
    this.deity = _deity;
  }
  
  public WcEpicKarmaCommand(long _id, byte[] _data)
  {
    super(_id, (short)16, _data);
  }
  
  public boolean autoForward()
  {
    return false;
  }
  
  byte[] encode()
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    DataOutputStream dos = null;
    byte[] barr = null;
    try
    {
      dos = new DataOutputStream(bos);
      dos.writeInt(this.pids.length);
      dos.writeInt(this.deity);
      for (int x = 0; x < this.pids.length; x++)
      {
        dos.writeLong(this.pids[x]);
        dos.writeInt(this.karmas[x]);
      }
      dos.flush();
      dos.close();
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, "Problem encoding for Deity " + this.deity + " - " + ex.getMessage(), ex);
    }
    finally
    {
      StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
      barr = bos.toByteArray();
      StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
      setData(barr);
    }
    return barr;
  }
  
  public void execute()
  {
    new WcEpicKarmaCommand.1(this).start();
  }
  
  public static void clearKarma()
  {
    for (Deity deity : ) {
      deity.clearKarma();
    }
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getDeityDbCon();
      ps = dbcon.prepareStatement("DELETE FROM HELPERS");
      ps.executeUpdate();
      ps.close();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public static void loadAllKarmaHelpers()
  {
    for (Deity deity : ) {
      deity.loadAllKarmaHelpers();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\webinterface\WcEpicKarmaCommand.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */