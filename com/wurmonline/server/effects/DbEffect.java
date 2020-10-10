package com.wurmonline.server.effects;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.shared.constants.CounterTypes;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DbEffect
  extends Effect
  implements CounterTypes
{
  private static final Logger logger = Logger.getLogger(DbEffect.class.getName());
  private static final long serialVersionUID = 1839666903728378027L;
  private static final String CREATE_EFFECT_SQL = "insert into EFFECTS(OWNER, TYPE, POSX, POSY, POSZ, STARTTIME) values(?,?,?,?,?,?)";
  private static final String UPDATE_EFFECT_SQL = "update EFFECTS set OWNER=?, TYPE=?, POSX=?, POSY=?, POSZ=? where ID=?";
  private static final String GET_EFFECT_SQL = "select * from EFFECTS where ID=?";
  private static final String DELETE_EFFECT_SQL = "delete from EFFECTS where ID=?";
  
  DbEffect(long aOwner, short aType, float aPosX, float aPosY, float aPosZ, boolean aSurfaced)
  {
    super(aOwner, aType, aPosX, aPosY, aPosZ, aSurfaced);
  }
  
  DbEffect(long aOwner, int aNumber)
    throws IOException
  {
    super(aOwner, aNumber);
  }
  
  DbEffect(int num, long ownerid, short typ, float posx, float posy, float posz, long stime)
  {
    super(num, ownerid, typ, posx, posy, posz, stime);
  }
  
  public void save()
    throws IOException
  {
    if (WurmId.getType(getOwner()) != 6)
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      try
      {
        if ((WurmId.getType(getOwner()) == 2) || 
          (WurmId.getType(getOwner()) == 19) || 
          (WurmId.getType(getOwner()) == 20))
        {
          dbcon = DbConnector.getItemDbCon();
          if (exists(dbcon))
          {
            ps = dbcon.prepareStatement("update EFFECTS set OWNER=?, TYPE=?, POSX=?, POSY=?, POSZ=? where ID=?");
            
            ps.setLong(1, getOwner());
            ps.setShort(2, getType());
            ps.setFloat(3, getPosX());
            ps.setFloat(4, getPosY());
            ps.setFloat(5, getPosZ());
            ps.setInt(6, getId());
            ps.executeUpdate();
          }
          else
          {
            ps = dbcon.prepareStatement("insert into EFFECTS(OWNER, TYPE, POSX, POSY, POSZ, STARTTIME) values(?,?,?,?,?,?)", 1);
            ps.setLong(1, getOwner());
            ps.setShort(2, getType());
            ps.setFloat(3, getPosX());
            ps.setFloat(4, getPosY());
            ps.setFloat(5, getPosZ());
            ps.setLong(6, getStartTime());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
              setId(rs.getInt(1));
            }
          }
        }
        else if (getId() == 0)
        {
          setId(-Math.abs(Server.rand.nextInt()));
        }
      }
      catch (SQLException sqx)
      {
        throw new IOException(sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
      }
    }
    else if (getId() == 0)
    {
      setId(-Math.abs(Server.rand.nextInt()));
    }
  }
  
  void load()
    throws IOException
  {
    if (WurmId.getType(getOwner()) != 6)
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      try
      {
        if ((WurmId.getType(getOwner()) == 2) || 
          (WurmId.getType(getOwner()) == 19) || 
          (WurmId.getType(getOwner()) == 20))
        {
          dbcon = DbConnector.getItemDbCon();
          ps = dbcon.prepareStatement("select * from EFFECTS where ID=?");
          ps.setInt(1, getId());
          rs = ps.executeQuery();
          if (rs.next())
          {
            setPosX(rs.getFloat("POSX"));
            setPosY(rs.getFloat("POSY"));
            setPosZ(rs.getFloat("POSZ"));
            setType(rs.getShort("TYPE"));
            setOwner(rs.getLong("OWNER"));
            setStartTime(rs.getLong("STARTTIME"));
          }
          else
          {
            logger.log(Level.WARNING, "Failed to find effect with number " + getId());
          }
        }
      }
      catch (SQLException sqx)
      {
        throw new IOException(sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  void delete()
  {
    if (WurmId.getType(getOwner()) != 6)
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        if ((WurmId.getType(getOwner()) == 2) || 
          (WurmId.getType(getOwner()) == 19) || 
          (WurmId.getType(getOwner()) == 20))
        {
          dbcon = DbConnector.getItemDbCon();
          ps = dbcon.prepareStatement("delete from EFFECTS where ID=?");
          ps.setInt(1, getId());
          ps.executeUpdate();
        }
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, "Failed to delete effect with id " + getId(), sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  private boolean exists(Connection dbcon)
    throws SQLException
  {
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      ps = dbcon.prepareStatement("select * from EFFECTS where ID=?");
      ps.setInt(1, getId());
      rs = ps.executeQuery();
      return rs.next();
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\effects\DbEffect.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */