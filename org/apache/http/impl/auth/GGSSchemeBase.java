package org.apache.http.impl.auth;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.InvalidCredentialsException;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.message.BufferedHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.CharArrayBuffer;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

public abstract class GGSSchemeBase
  extends AuthSchemeBase
{
  static enum State
  {
    UNINITIATED,  CHALLENGE_RECEIVED,  TOKEN_GENERATED,  FAILED;
    
    private State() {}
  }
  
  private final Log log = LogFactory.getLog(getClass());
  private final Base64 base64codec;
  private final boolean stripPort;
  private State state;
  private byte[] token;
  
  GGSSchemeBase(boolean stripPort)
  {
    this.base64codec = new Base64(0);
    this.stripPort = stripPort;
    this.state = State.UNINITIATED;
  }
  
  GGSSchemeBase()
  {
    this(false);
  }
  
  protected GSSManager getManager()
  {
    return GSSManager.getInstance();
  }
  
  protected byte[] generateGSSToken(byte[] input, Oid oid, String authServer)
    throws GSSException
  {
    byte[] token = input;
    if (token == null) {
      token = new byte[0];
    }
    GSSManager manager = getManager();
    GSSName serverName = manager.createName("HTTP@" + authServer, GSSName.NT_HOSTBASED_SERVICE);
    GSSContext gssContext = manager.createContext(serverName.canonicalize(oid), oid, null, 0);
    
    gssContext.requestMutualAuth(true);
    gssContext.requestCredDeleg(true);
    return gssContext.initSecContext(token, 0, token.length);
  }
  
  protected abstract byte[] generateToken(byte[] paramArrayOfByte, String paramString)
    throws GSSException;
  
  public boolean isComplete()
  {
    return (this.state == State.TOKEN_GENERATED) || (this.state == State.FAILED);
  }
  
  @Deprecated
  public Header authenticate(Credentials credentials, HttpRequest request)
    throws AuthenticationException
  {
    return authenticate(credentials, request, null);
  }
  
  public Header authenticate(Credentials credentials, HttpRequest request, HttpContext context)
    throws AuthenticationException
  {
    if (request == null) {
      throw new IllegalArgumentException("HTTP request may not be null");
    }
    switch (this.state)
    {
    case UNINITIATED: 
      throw new AuthenticationException(getSchemeName() + " authentication has not been initiated");
    case FAILED: 
      throw new AuthenticationException(getSchemeName() + " authentication has failed");
    case CHALLENGE_RECEIVED: 
      try
      {
        String key = null;
        if (isProxy()) {
          key = "http.proxy_host";
        } else {
          key = "http.target_host";
        }
        HttpHost host = (HttpHost)context.getAttribute(key);
        if (host == null) {
          throw new AuthenticationException("Authentication host is not set in the execution context");
        }
        String authServer;
        String authServer;
        if ((!this.stripPort) && (host.getPort() > 0)) {
          authServer = host.toHostString();
        } else {
          authServer = host.getHostName();
        }
        if (this.log.isDebugEnabled()) {
          this.log.debug("init " + authServer);
        }
        this.token = generateToken(this.token, authServer);
        this.state = State.TOKEN_GENERATED;
      }
      catch (GSSException gsse)
      {
        this.state = State.FAILED;
        if ((gsse.getMajor() == 9) || (gsse.getMajor() == 8)) {
          throw new InvalidCredentialsException(gsse.getMessage(), gsse);
        }
        if (gsse.getMajor() == 13) {
          throw new InvalidCredentialsException(gsse.getMessage(), gsse);
        }
        if ((gsse.getMajor() == 10) || (gsse.getMajor() == 19) || (gsse.getMajor() == 20)) {
          throw new AuthenticationException(gsse.getMessage(), gsse);
        }
        throw new AuthenticationException(gsse.getMessage());
      }
    case TOKEN_GENERATED: 
      String tokenstr = new String(this.base64codec.encode(this.token));
      if (this.log.isDebugEnabled()) {
        this.log.debug("Sending response '" + tokenstr + "' back to the auth server");
      }
      CharArrayBuffer buffer = new CharArrayBuffer(32);
      if (isProxy()) {
        buffer.append("Proxy-Authorization");
      } else {
        buffer.append("Authorization");
      }
      buffer.append(": Negotiate ");
      buffer.append(tokenstr);
      return new BufferedHeader(buffer);
    }
    throw new IllegalStateException("Illegal state: " + this.state);
  }
  
  protected void parseChallenge(CharArrayBuffer buffer, int beginIndex, int endIndex)
    throws MalformedChallengeException
  {
    String challenge = buffer.substringTrimmed(beginIndex, endIndex);
    if (this.log.isDebugEnabled()) {
      this.log.debug("Received challenge '" + challenge + "' from the auth server");
    }
    if (this.state == State.UNINITIATED)
    {
      this.token = Base64.decodeBase64(challenge.getBytes());
      this.state = State.CHALLENGE_RECEIVED;
    }
    else
    {
      this.log.debug("Authentication already attempted");
      this.state = State.FAILED;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\auth\GGSSchemeBase.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */