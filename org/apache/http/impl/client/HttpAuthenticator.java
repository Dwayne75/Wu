package org.apache.http.impl.client;

import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthOption;
import org.apache.http.auth.AuthProtocolState;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.protocol.HttpContext;

public class HttpAuthenticator
{
  private final Log log;
  
  public HttpAuthenticator(Log log)
  {
    this.log = (log != null ? log : LogFactory.getLog(getClass()));
  }
  
  public HttpAuthenticator()
  {
    this(null);
  }
  
  public boolean isAuthenticationRequested(HttpHost host, HttpResponse response, AuthenticationStrategy authStrategy, AuthState authState, HttpContext context)
  {
    if (authStrategy.isAuthenticationRequested(host, response, context))
    {
      if (authState.getState() == AuthProtocolState.SUCCESS) {
        authStrategy.authFailed(host, authState.getAuthScheme(), context);
      }
      this.log.debug("Authentication required");
      return true;
    }
    switch (authState.getState())
    {
    case CHALLENGED: 
    case HANDSHAKE: 
      this.log.debug("Authentication succeeded");
      authState.setState(AuthProtocolState.SUCCESS);
      authStrategy.authSucceeded(host, authState.getAuthScheme(), context);
      break;
    case SUCCESS: 
      break;
    default: 
      authState.setState(AuthProtocolState.UNCHALLENGED);
    }
    return false;
  }
  
  public boolean authenticate(HttpHost host, HttpResponse response, AuthenticationStrategy authStrategy, AuthState authState, HttpContext context)
  {
    try
    {
      if (this.log.isDebugEnabled()) {
        this.log.debug(host.toHostString() + " requested authentication");
      }
      Map<String, Header> challenges = authStrategy.getChallenges(host, response, context);
      if (challenges.isEmpty())
      {
        this.log.debug("Response contains no authentication challenges");
        return false;
      }
      AuthScheme authScheme = authState.getAuthScheme();
      switch (authState.getState())
      {
      case FAILURE: 
        return false;
      case SUCCESS: 
        authState.reset();
        break;
      case CHALLENGED: 
      case HANDSHAKE: 
        if (authScheme == null)
        {
          this.log.debug("Auth scheme is null");
          authStrategy.authFailed(host, null, context);
          authState.reset();
          authState.setState(AuthProtocolState.FAILURE);
          return false;
        }
      case UNCHALLENGED: 
        if (authScheme != null)
        {
          String id = authScheme.getSchemeName();
          Header challenge = (Header)challenges.get(id.toLowerCase(Locale.US));
          if (challenge != null)
          {
            this.log.debug("Authorization challenge processed");
            authScheme.processChallenge(challenge);
            if (authScheme.isComplete())
            {
              this.log.debug("Authentication failed");
              authStrategy.authFailed(host, authState.getAuthScheme(), context);
              authState.reset();
              authState.setState(AuthProtocolState.FAILURE);
              return false;
            }
            authState.setState(AuthProtocolState.HANDSHAKE);
            return true;
          }
          authState.reset();
        }
        break;
      }
      Queue<AuthOption> authOptions = authStrategy.select(challenges, host, response, context);
      if ((authOptions != null) && (!authOptions.isEmpty()))
      {
        if (this.log.isDebugEnabled()) {
          this.log.debug("Selected authentication options: " + authOptions);
        }
        authState.setState(AuthProtocolState.CHALLENGED);
        authState.update(authOptions);
        return true;
      }
      return false;
    }
    catch (MalformedChallengeException ex)
    {
      if (this.log.isWarnEnabled()) {
        this.log.warn("Malformed challenge: " + ex.getMessage());
      }
      authState.reset();
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\client\HttpAuthenticator.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */