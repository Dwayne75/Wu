package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import java.util.Vector;

public class Namespaces
{
  public Namespace[] personal;
  public Namespace[] otherUsers;
  public Namespace[] shared;
  
  public static class Namespace
  {
    public String prefix;
    public char delimiter;
    
    public Namespace(Response r)
      throws ProtocolException
    {
      if (r.readByte() != 40) {
        throw new ProtocolException("Missing '(' at start of Namespace");
      }
      this.prefix = BASE64MailboxDecoder.decode(r.readString());
      r.skipSpaces();
      if (r.peekByte() == 34)
      {
        r.readByte();
        this.delimiter = ((char)r.readByte());
        if (this.delimiter == '\\') {
          this.delimiter = ((char)r.readByte());
        }
        if (r.readByte() != 34) {
          throw new ProtocolException("Missing '\"' at end of QUOTED_CHAR");
        }
      }
      else
      {
        String s = r.readAtom();
        if (s == null) {
          throw new ProtocolException("Expected NIL, got null");
        }
        if (!s.equalsIgnoreCase("NIL")) {
          throw new ProtocolException("Expected NIL, got " + s);
        }
        this.delimiter = '\000';
      }
      if (r.peekByte() != 41)
      {
        r.skipSpaces();
        r.readString();
        r.skipSpaces();
        r.readStringList();
      }
      if (r.readByte() != 41) {
        throw new ProtocolException("Missing ')' at end of Namespace");
      }
    }
  }
  
  public Namespaces(Response r)
    throws ProtocolException
  {
    this.personal = getNamespaces(r);
    this.otherUsers = getNamespaces(r);
    this.shared = getNamespaces(r);
  }
  
  private Namespace[] getNamespaces(Response r)
    throws ProtocolException
  {
    r.skipSpaces();
    if (r.peekByte() == 40)
    {
      Vector v = new Vector();
      r.readByte();
      do
      {
        Namespace ns = new Namespace(r);
        v.addElement(ns);
      } while (r.peekByte() != 41);
      r.readByte();
      Namespace[] nsa = new Namespace[v.size()];
      v.copyInto(nsa);
      return nsa;
    }
    String s = r.readAtom();
    if (s == null) {
      throw new ProtocolException("Expected NIL, got null");
    }
    if (!s.equalsIgnoreCase("NIL")) {
      throw new ProtocolException("Expected NIL, got " + s);
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\imap\protocol\Namespaces.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */