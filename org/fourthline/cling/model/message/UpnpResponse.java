package org.fourthline.cling.model.message;

public class UpnpResponse
  extends UpnpOperation
{
  private int statusCode;
  private String statusMessage;
  
  public static enum Status
  {
    OK(200, "OK"),  BAD_REQUEST(400, "Bad Request"),  NOT_FOUND(404, "Not Found"),  METHOD_NOT_SUPPORTED(405, "Method Not Supported"),  PRECONDITION_FAILED(412, "Precondition Failed"),  UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),  INTERNAL_SERVER_ERROR(500, "Internal Server Error"),  NOT_IMPLEMENTED(501, "Not Implemented");
    
    private int statusCode;
    private String statusMsg;
    
    private Status(int statusCode, String statusMsg)
    {
      this.statusCode = statusCode;
      this.statusMsg = statusMsg;
    }
    
    public int getStatusCode()
    {
      return this.statusCode;
    }
    
    public String getStatusMsg()
    {
      return this.statusMsg;
    }
    
    public static Status getByStatusCode(int statusCode)
    {
      for (Status status : ) {
        if (status.getStatusCode() == statusCode) {
          return status;
        }
      }
      return null;
    }
  }
  
  public UpnpResponse(int statusCode, String statusMessage)
  {
    this.statusCode = statusCode;
    this.statusMessage = statusMessage;
  }
  
  public UpnpResponse(Status status)
  {
    this.statusCode = status.getStatusCode();
    this.statusMessage = status.getStatusMsg();
  }
  
  public int getStatusCode()
  {
    return this.statusCode;
  }
  
  public String getStatusMessage()
  {
    return this.statusMessage;
  }
  
  public boolean isFailed()
  {
    return this.statusCode >= 300;
  }
  
  public String getResponseDetails()
  {
    return getStatusCode() + " " + getStatusMessage();
  }
  
  public String toString()
  {
    return getResponseDetails();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\UpnpResponse.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */