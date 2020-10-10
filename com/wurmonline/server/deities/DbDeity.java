package com.wurmonline.server.deities;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DbUtilities;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

final class DbDeity
  extends Deity
{
  private static final Logger logger = Logger.getLogger(DbDeity.class.getName());
  private static final String CREATE_DEITY = "INSERT INTO DEITIES (ID,NAME,FAITH,ALIGNMENT,POWER,SEX,HOLYITEM,ATTACK,VITALITY) VALUES(?,?,?,?,?,?,?,?,?)";
  private static final String SET_FAVOR = "UPDATE DEITIES SET FAVOR=? WHERE ID=?";
  private static final String SET_POWER = "UPDATE DEITIES SET POWER=? WHERE ID=?";
  
  DbDeity(int num, String nam, byte align, byte aSex, byte pow, double aFaith, int aHolyItem, int _favor, float _attack, float _vitality, boolean create)
  {
    super(num, nam, align, aSex, pow, aFaith, aHolyItem, _favor, _attack, _vitality, create);
  }
  
  public void save()
    throws IOException
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getDeityDbCon();
      ps = dbcon.prepareStatement("INSERT INTO DEITIES (ID,NAME,FAITH,ALIGNMENT,POWER,SEX,HOLYITEM,ATTACK,VITALITY) VALUES(?,?,?,?,?,?,?,?,?)");
      ps.setByte(1, (byte)this.number);
      ps.setString(2, this.name);
      ps.setDouble(3, this.faith);
      ps.setByte(4, (byte)this.alignment);
      ps.setByte(5, this.power);
      ps.setByte(6, this.sex);
      ps.setInt(7, this.holyItem);
      
      ps.setFloat(8, this.attack);
      
      ps.setFloat(9, this.vitality);
      ps.executeUpdate();
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, sqx.getMessage(), sqx);
      throw new IOException(sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  void setFaith(double aFaith)
    throws IOException
  {
    this.faith = aFaith;
  }
  
  public void setFavor(int newfavor)
  {
    this.favor = newfavor;
    if (this.favor % 20 == 0)
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      try
      {
        dbcon = DbConnector.getDeityDbCon();
        ps = dbcon.prepareStatement("UPDATE DEITIES SET FAVOR=? WHERE ID=?");
        ps.setDouble(1, this.favor);
        ps.setByte(2, (byte)this.number);
        ps.executeUpdate();
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
  }
  
  public final void setPower(byte newPower)
  {
    this.power = newPower;
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getDeityDbCon();
      ps = dbcon.prepareStatement("UPDATE DEITIES SET POWER=? WHERE ID=?");
      logger.log(Level.INFO, "Changing power for deity " + this.name + " " + (byte)this.number + " to power " + this.power);
      ps.setByte(1, this.power);
      ps.setByte(2, (byte)this.number);
      ps.executeUpdate();
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
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\deities\DbDeity.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */