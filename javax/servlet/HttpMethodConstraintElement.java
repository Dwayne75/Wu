package javax.servlet;

public class HttpMethodConstraintElement
  extends HttpConstraintElement
{
  private String methodName;
  
  public HttpMethodConstraintElement(String methodName)
  {
    if ((methodName == null) || (methodName.length() == 0)) {
      throw new IllegalArgumentException("invalid HTTP method name");
    }
    this.methodName = methodName;
  }
  
  public HttpMethodConstraintElement(String methodName, HttpConstraintElement constraint)
  {
    super(constraint.getEmptyRoleSemantic(), constraint.getTransportGuarantee(), constraint.getRolesAllowed());
    if ((methodName == null) || (methodName.length() == 0)) {
      throw new IllegalArgumentException("invalid HTTP method name");
    }
    this.methodName = methodName;
  }
  
  public String getMethodName()
  {
    return this.methodName;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\HttpMethodConstraintElement.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */