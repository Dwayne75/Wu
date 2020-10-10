package com.sun.xml.xsom.impl.parser.state;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public abstract class NGCCInterleaveFilter
  implements NGCCEventSource, NGCCEventReceiver
{
  protected NGCCEventReceiver[] _receivers;
  private final NGCCHandler _parent;
  private final int _cookie;
  private int lockedReceiver;
  
  protected NGCCInterleaveFilter(NGCCHandler parent, int cookie)
  {
    this._parent = parent;
    this._cookie = cookie;
  }
  
  protected void setHandlers(NGCCEventReceiver[] receivers)
  {
    this._receivers = receivers;
  }
  
  public int replace(NGCCEventReceiver oldHandler, NGCCEventReceiver newHandler)
  {
    for (int i = 0; i < this._receivers.length; i++) {
      if (this._receivers[i] == oldHandler)
      {
        this._receivers[i] = newHandler;
        return i;
      }
    }
    throw new InternalError();
  }
  
  private int lockCount = 0;
  
  public void enterElement(String uri, String localName, String qname, Attributes atts)
    throws SAXException
  {
    if (this.isJoining) {
      return;
    }
    if (this.lockCount++ == 0)
    {
      this.lockedReceiver = findReceiverOfElement(uri, localName);
      if (this.lockedReceiver == -1)
      {
        joinByEnterElement(null, uri, localName, qname, atts);
        return;
      }
    }
    this._receivers[this.lockedReceiver].enterElement(uri, localName, qname, atts);
  }
  
  public void leaveElement(String uri, String localName, String qname)
    throws SAXException
  {
    if (this.isJoining) {
      return;
    }
    if (this.lockCount-- == 0) {
      joinByLeaveElement(null, uri, localName, qname);
    } else {
      this._receivers[this.lockedReceiver].leaveElement(uri, localName, qname);
    }
  }
  
  public void enterAttribute(String uri, String localName, String qname)
    throws SAXException
  {
    if (this.isJoining) {
      return;
    }
    if (this.lockCount++ == 0)
    {
      this.lockedReceiver = findReceiverOfAttribute(uri, localName);
      if (this.lockedReceiver == -1)
      {
        joinByEnterAttribute(null, uri, localName, qname);
        return;
      }
    }
    this._receivers[this.lockedReceiver].enterAttribute(uri, localName, qname);
  }
  
  public void leaveAttribute(String uri, String localName, String qname)
    throws SAXException
  {
    if (this.isJoining) {
      return;
    }
    if (this.lockCount-- == 0) {
      joinByLeaveAttribute(null, uri, localName, qname);
    } else {
      this._receivers[this.lockedReceiver].leaveAttribute(uri, localName, qname);
    }
  }
  
  public void text(String value)
    throws SAXException
  {
    if (this.isJoining) {
      return;
    }
    if (this.lockCount != 0)
    {
      this._receivers[this.lockedReceiver].text(value);
    }
    else
    {
      int receiver = findReceiverOfText();
      if (receiver != -1) {
        this._receivers[receiver].text(value);
      } else {
        joinByText(null, value);
      }
    }
  }
  
  private boolean isJoining = false;
  
  protected abstract int findReceiverOfElement(String paramString1, String paramString2);
  
  protected abstract int findReceiverOfAttribute(String paramString1, String paramString2);
  
  protected abstract int findReceiverOfText();
  
  public void joinByEnterElement(NGCCEventReceiver source, String uri, String local, String qname, Attributes atts)
    throws SAXException
  {
    if (this.isJoining) {
      return;
    }
    this.isJoining = true;
    for (int i = 0; i < this._receivers.length; i++) {
      if (this._receivers[i] != source) {
        this._receivers[i].enterElement(uri, local, qname, atts);
      }
    }
    this._parent._source.replace(this, this._parent);
    this._parent.onChildCompleted(null, this._cookie, true);
    
    this._parent.enterElement(uri, local, qname, atts);
  }
  
  public void joinByLeaveElement(NGCCEventReceiver source, String uri, String local, String qname)
    throws SAXException
  {
    if (this.isJoining) {
      return;
    }
    this.isJoining = true;
    for (int i = 0; i < this._receivers.length; i++) {
      if (this._receivers[i] != source) {
        this._receivers[i].leaveElement(uri, local, qname);
      }
    }
    this._parent._source.replace(this, this._parent);
    this._parent.onChildCompleted(null, this._cookie, true);
    
    this._parent.leaveElement(uri, local, qname);
  }
  
  public void joinByEnterAttribute(NGCCEventReceiver source, String uri, String local, String qname)
    throws SAXException
  {
    if (this.isJoining) {
      return;
    }
    this.isJoining = true;
    for (int i = 0; i < this._receivers.length; i++) {
      if (this._receivers[i] != source) {
        this._receivers[i].enterAttribute(uri, local, qname);
      }
    }
    this._parent._source.replace(this, this._parent);
    this._parent.onChildCompleted(null, this._cookie, true);
    
    this._parent.enterAttribute(uri, local, qname);
  }
  
  public void joinByLeaveAttribute(NGCCEventReceiver source, String uri, String local, String qname)
    throws SAXException
  {
    if (this.isJoining) {
      return;
    }
    this.isJoining = true;
    for (int i = 0; i < this._receivers.length; i++) {
      if (this._receivers[i] != source) {
        this._receivers[i].leaveAttribute(uri, local, qname);
      }
    }
    this._parent._source.replace(this, this._parent);
    this._parent.onChildCompleted(null, this._cookie, true);
    
    this._parent.leaveAttribute(uri, local, qname);
  }
  
  public void joinByText(NGCCEventReceiver source, String value)
    throws SAXException
  {
    if (this.isJoining) {
      return;
    }
    this.isJoining = true;
    for (int i = 0; i < this._receivers.length; i++) {
      if (this._receivers[i] != source) {
        this._receivers[i].text(value);
      }
    }
    this._parent._source.replace(this, this._parent);
    this._parent.onChildCompleted(null, this._cookie, true);
    
    this._parent.text(value);
  }
  
  public void sendEnterAttribute(int threadId, String uri, String local, String qname)
    throws SAXException
  {
    this._receivers[threadId].enterAttribute(uri, local, qname);
  }
  
  public void sendEnterElement(int threadId, String uri, String local, String qname, Attributes atts)
    throws SAXException
  {
    this._receivers[threadId].enterElement(uri, local, qname, atts);
  }
  
  public void sendLeaveAttribute(int threadId, String uri, String local, String qname)
    throws SAXException
  {
    this._receivers[threadId].leaveAttribute(uri, local, qname);
  }
  
  public void sendLeaveElement(int threadId, String uri, String local, String qname)
    throws SAXException
  {
    this._receivers[threadId].leaveElement(uri, local, qname);
  }
  
  public void sendText(int threadId, String value)
    throws SAXException
  {
    this._receivers[threadId].text(value);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\parser\state\NGCCInterleaveFilter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */