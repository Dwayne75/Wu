package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl;
import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;

public final class BridgeContextImpl
  extends BridgeContext
{
  public final UnmarshallerImpl unmarshaller;
  public final MarshallerImpl marshaller;
  
  BridgeContextImpl(JAXBContextImpl context)
  {
    this.unmarshaller = context.createUnmarshaller();
    this.marshaller = context.createMarshaller();
  }
  
  public void setErrorHandler(ValidationEventHandler handler)
  {
    try
    {
      this.unmarshaller.setEventHandler(handler);
      this.marshaller.setEventHandler(handler);
    }
    catch (JAXBException e)
    {
      throw new Error(e);
    }
  }
  
  public void setAttachmentMarshaller(AttachmentMarshaller m)
  {
    this.marshaller.setAttachmentMarshaller(m);
  }
  
  public void setAttachmentUnmarshaller(AttachmentUnmarshaller u)
  {
    this.unmarshaller.setAttachmentUnmarshaller(u);
  }
  
  public AttachmentMarshaller getAttachmentMarshaller()
  {
    return this.marshaller.getAttachmentMarshaller();
  }
  
  public AttachmentUnmarshaller getAttachmentUnmarshaller()
  {
    return this.unmarshaller.getAttachmentUnmarshaller();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\BridgeContextImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */