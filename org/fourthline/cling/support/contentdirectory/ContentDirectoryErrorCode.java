package org.fourthline.cling.support.contentdirectory;

public enum ContentDirectoryErrorCode
{
  NO_SUCH_OBJECT(701, "The specified ObjectID is invalid"),  UNSUPPORTED_SORT_CRITERIA(709, "Unsupported or invalid sort criteria"),  CANNOT_PROCESS(720, "Cannot process the request");
  
  private int code;
  private String description;
  
  private ContentDirectoryErrorCode(int code, String description)
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
  
  public static ContentDirectoryErrorCode getByCode(int code)
  {
    for (ContentDirectoryErrorCode errorCode : ) {
      if (errorCode.getCode() == code) {
        return errorCode;
      }
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\contentdirectory\ContentDirectoryErrorCode.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */