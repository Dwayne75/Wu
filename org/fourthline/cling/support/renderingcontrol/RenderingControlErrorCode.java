package org.fourthline.cling.support.renderingcontrol;

public enum RenderingControlErrorCode
{
  INVALID_PRESET_NAME(701, "The specified name is not a valid preset name"),  INVALID_INSTANCE_ID(702, "The specified instanceID is invalid for this RenderingControl");
  
  private int code;
  private String description;
  
  private RenderingControlErrorCode(int code, String description)
  {
    this.code = code;
    this.description = description;
  }
  
  public int getCode()
  {
    return this.code;
  }
  
  public String getDescription()
  {
    return this.description;
  }
  
  public static RenderingControlErrorCode getByCode(int code)
  {
    for (RenderingControlErrorCode errorCode : ) {
      if (errorCode.getCode() == code) {
        return errorCode;
      }
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\renderingcontrol\RenderingControlErrorCode.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */