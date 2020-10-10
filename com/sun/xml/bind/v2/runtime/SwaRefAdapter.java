package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;
import javax.activation.DataHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;

public final class SwaRefAdapter
  extends XmlAdapter<String, DataHandler>
{
  public DataHandler unmarshal(String cid)
  {
    AttachmentUnmarshaller au = UnmarshallingContext.getInstance().parent.getAttachmentUnmarshaller();
    
    return au.getAttachmentAsDataHandler(cid);
  }
  
  public String marshal(DataHandler data)
  {
    if (data == null) {
      return null;
    }
    AttachmentMarshaller am = XMLSerializer.getInstance().attachmentMarshaller;
    
    return am.addSwaRefAttachment(data);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\SwaRefAdapter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */