package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.support.Ticket;
import com.wurmonline.server.support.TicketAction;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcTicket
  extends WebCommand
  implements MiscConstants
{
  private static final Logger logger = Logger.getLogger(WcTicket.class.getName());
  public static final byte DO_NOTHING = 0;
  public static final byte GET_BATCHNOS = 1;
  public static final byte THE_BATCHNOS = 2;
  public static final byte SEND_TICKET = 3;
  public static final byte CHECK_FOR_UPDATES = 4;
  private byte type = 0;
  private int noBatchNos = 1;
  private int firstBatchNos = 0;
  private int secondBatchNos = 0;
  private Ticket ticket = null;
  private boolean sendActions = false;
  private TicketAction ticketAction = null;
  private long checkDate = 0L;
  
  public WcTicket(int aNoBatchNos)
  {
    super(WurmId.getNextWCCommandId(), (short)18);
    this.type = 1;
    this.noBatchNos = aNoBatchNos;
  }
  
  public WcTicket(int aFirstBatchNos, int aSecondBatchNos)
  {
    super(WurmId.getNextWCCommandId(), (short)18);
    this.type = 2;
    this.firstBatchNos = aFirstBatchNos;
    this.secondBatchNos = aSecondBatchNos;
  }
  
  public WcTicket(Ticket aTicket)
  {
    super(WurmId.getNextWCCommandId(), (short)18);
    this.type = 3;
    this.ticket = aTicket;
    this.ticketAction = null;
    this.sendActions = true;
  }
  
  public WcTicket(long aId, Ticket aTicket, int aNumbActions, TicketAction aTicketAction)
  {
    super(aId, (short)18);
    this.type = 3;
    this.ticket = aTicket;
    if (aNumbActions > 1) {
      this.ticketAction = null;
    } else {
      this.ticketAction = aTicketAction;
    }
    this.sendActions = (aNumbActions > 0);
  }
  
  public WcTicket(Ticket aTicket, TicketAction aTicketAction)
  {
    super(WurmId.getNextWCCommandId(), (short)18);
    this.type = 3;
    this.ticket = aTicket;
    this.ticketAction = aTicketAction;
    this.sendActions = true;
  }
  
  public WcTicket(long aDate)
  {
    super(WurmId.getNextWCCommandId(), (short)18);
    this.type = 4;
    this.checkDate = aDate;
  }
  
  public WcTicket(long aId, byte[] aData)
  {
    super(aId, (short)18, aData);
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
      dos.writeByte(this.type);
      switch (this.type)
      {
      case 1: 
        dos.writeInt(this.noBatchNos);
        break;
      case 2: 
        dos.writeInt(this.firstBatchNos);
        dos.writeInt(this.secondBatchNos);
        break;
      case 3: 
        dos.writeInt(this.ticket.getTicketId());
        dos.writeLong(this.ticket.getTicketDate());
        dos.writeLong(this.ticket.getPlayerId());
        dos.writeUTF(this.ticket.getPlayerName());
        dos.writeByte(this.ticket.getCategoryCode());
        dos.writeInt(this.ticket.getServerId());
        dos.writeBoolean(this.ticket.isGlobal());
        dos.writeLong(this.ticket.getClosedDate());
        dos.writeByte(this.ticket.getStateCode());
        dos.writeByte(this.ticket.getLevelCode());
        dos.writeUTF(this.ticket.getResponderName());
        dos.writeUTF(this.ticket.getDescription());
        dos.writeShort(this.ticket.getRefFeedback());
        dos.writeBoolean(this.ticket.getAcknowledged());
        if (this.sendActions)
        {
          if (this.ticketAction == null)
          {
            TicketAction[] ticketActions = this.ticket.getTicketActions();
            dos.writeByte(ticketActions.length);
            for (TicketAction ta : ticketActions) {
              addTicketAction(dos, ta);
            }
          }
          else
          {
            dos.writeByte(1);
            addTicketAction(dos, this.ticketAction);
          }
        }
        else {
          dos.writeByte(0);
        }
        break;
      case 4: 
        dos.writeLong(this.checkDate);
        break;
      }
      dos.flush();
      dos.close();
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, ex.getMessage(), ex);
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
  
  private void addTicketAction(DataOutputStream dos, TicketAction ta)
    throws IOException
  {
    dos.writeShort(ta.getActionNo());
    dos.writeByte(ta.getAction());
    dos.writeLong(ta.getDate());
    dos.writeUTF(ta.getByWhom());
    dos.writeUTF(ta.getNote());
    dos.writeByte(ta.getVisibilityLevel());
    if (ta.getAction() == 14)
    {
      dos.writeByte(ta.getQualityOfServiceCode());
      dos.writeByte(ta.getCourteousCode());
      dos.writeByte(ta.getKnowledgeableCode());
      dos.writeByte(ta.getGeneralFlags());
      dos.writeByte(ta.getQualitiesFlags());
      dos.writeByte(ta.getIrkedFlags());
    }
  }
  
  public void execute()
  {
    new WcTicket.1(this).start();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\webinterface\WcTicket.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */