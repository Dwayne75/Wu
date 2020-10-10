package org.fourthline.cling.support.model.dlna.types;

public class CodedDataBuffer
{
  private Long size;
  private TransferMechanism tranfer;
  
  public static enum TransferMechanism
  {
    IMMEDIATELY,  TIMESTAMP,  OTHER;
    
    private TransferMechanism() {}
  }
  
  public CodedDataBuffer(Long size, TransferMechanism transfer)
  {
    this.size = size;
    this.tranfer = transfer;
  }
  
  public Long getSize()
  {
    return this.size;
  }
  
  public TransferMechanism getTranfer()
  {
    return this.tranfer;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\types\CodedDataBuffer.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */