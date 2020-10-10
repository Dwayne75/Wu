package com.sun.xml.bind.v2.runtime.output;

import com.sun.xml.bind.v2.runtime.unmarshaller.Base64Data;
import javax.xml.stream.XMLStreamException;
import org.jvnet.staxex.XMLStreamWriterEx;

public final class StAXExStreamWriterOutput
  extends XMLStreamWriterOutput
{
  private final XMLStreamWriterEx out;
  
  public StAXExStreamWriterOutput(XMLStreamWriterEx out)
  {
    super(out);
    this.out = out;
  }
  
  public void text(Pcdata value, boolean needsSeparatingWhitespace)
    throws XMLStreamException
  {
    if (needsSeparatingWhitespace) {
      this.out.writeCharacters(" ");
    }
    if (!(value instanceof Base64Data))
    {
      this.out.writeCharacters(value.toString());
    }
    else
    {
      Base64Data v = (Base64Data)value;
      this.out.writeBinary(v.getDataHandler());
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\output\StAXExStreamWriterOutput.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */