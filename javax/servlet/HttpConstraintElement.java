package javax.servlet;

import javax.servlet.annotation.ServletSecurity.EmptyRoleSemantic;
import javax.servlet.annotation.ServletSecurity.TransportGuarantee;

public class HttpConstraintElement
{
  private ServletSecurity.EmptyRoleSemantic emptyRoleSemantic;
  private ServletSecurity.TransportGuarantee transportGuarantee;
  private String[] rolesAllowed;
  
  public HttpConstraintElement()
  {
    this(ServletSecurity.EmptyRoleSemantic.PERMIT);
  }
  
  public HttpConstraintElement(ServletSecurity.EmptyRoleSemantic semantic)
  {
    this(semantic, ServletSecurity.TransportGuarantee.NONE, new String[0]);
  }
  
  public HttpConstraintElement(ServletSecurity.TransportGuarantee guarantee, String... roleNames)
  {
    this(ServletSecurity.EmptyRoleSemantic.PERMIT, guarantee, roleNames);
  }
  
  public HttpConstraintElement(ServletSecurity.EmptyRoleSemantic semantic, ServletSecurity.TransportGuarantee guarantee, String... roleNames)
  {
    if ((semantic == ServletSecurity.EmptyRoleSemantic.DENY) && (roleNames.length > 0)) {
      throw new IllegalArgumentException("Deny semantic with rolesAllowed");
    }
    this.emptyRoleSemantic = semantic;
    this.transportGuarantee = guarantee;
    this.rolesAllowed = roleNames;
  }
  
  public ServletSecurity.EmptyRoleSemantic getEmptyRoleSemantic()
  {
    return this.emptyRoleSemantic;
  }
  
  public ServletSecurity.TransportGuarantee getTransportGuarantee()
  {
    return this.transportGuarantee;
  }
  
  public String[] getRolesAllowed()
  {
    return this.rolesAllowed;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\HttpConstraintElement.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */