package com.wurmonline.server.webinterface;

import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WcGlobalIgnore
  extends WebCommand
  implements MiscConstants
{
  private static final Logger logger = Logger.getLogger(WcGlobalIgnore.class.getName());
  private long senderWurmId;
  private String ignorerName;
  private long targetWurmId;
  private String ignoreTarget;
  private boolean response = false;
  private boolean cant = false;
  private boolean triggerMute = false;
  private boolean startIgnore = true;
  private boolean startUnIgnore = false;
  private byte ignorerKingdom = 0;
  
  public WcGlobalIgnore(long _id, long senderId, String ignorer, long targetId, String ignored, boolean isResponseCommand, boolean cannot, boolean muting, boolean ignoring, boolean unIgnoring, byte kingdomId)
  {
    super(_id, (short)15);
    this.ignorerName = LoginHandler.raiseFirstLetter(ignorer);
    this.ignoreTarget = LoginHandler.raiseFirstLetter(ignored);
    this.senderWurmId = senderId;
    this.targetWurmId = targetId;
    this.response = isResponseCommand;
    this.cant = cannot;
    this.triggerMute = muting;
    this.startIgnore = ignoring;
    this.startUnIgnore = unIgnoring;
    this.ignorerKingdom = kingdomId;
  }
  
  public WcGlobalIgnore(long _id, byte[] _data)
  {
    super(_id, (short)15, _data);
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
      dos.writeBoolean(this.response);
      dos.writeBoolean(this.cant);
      dos.writeLong(this.senderWurmId);
      dos.writeUTF(this.ignorerName);
      dos.writeLong(this.targetWurmId);
      dos.writeUTF(this.ignoreTarget);
      dos.writeBoolean(this.triggerMute);
      dos.writeBoolean(this.startIgnore);
      dos.writeBoolean(this.startUnIgnore);
      dos.writeByte(this.ignorerKingdom);
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
  
  public void execute()
  {
    new WcGlobalIgnore.1(this).start();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\webinterface\WcGlobalIgnore.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */