package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.WhiteSpaceProcessor;
import com.sun.xml.fastinfoset.stax.StAXDocumentParser;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

final class FastInfosetConnector
  extends StAXConnector
{
  private final StAXDocumentParser fastInfosetStreamReader;
  private boolean textReported;
  private final Base64Data base64Data = new Base64Data();
  private final StringBuilder buffer = new StringBuilder();
  
  public FastInfosetConnector(StAXDocumentParser fastInfosetStreamReader, XmlVisitor visitor)
  {
    super(visitor);
    fastInfosetStreamReader.setStringInterning(true);
    this.fastInfosetStreamReader = fastInfosetStreamReader;
  }
  
  public void bridge()
    throws XMLStreamException
  {
    try
    {
      int depth = 0;
      
      int event = this.fastInfosetStreamReader.getEventType();
      if (event == 7) {
        while (!this.fastInfosetStreamReader.isStartElement()) {
          event = this.fastInfosetStreamReader.next();
        }
      }
      if (event != 1) {
        throw new IllegalStateException("The current event is not START_ELEMENT\n but " + event);
      }
      handleStartDocument(this.fastInfosetStreamReader.getNamespaceContext());
      for (;;)
      {
        switch (event)
        {
        case 1: 
          handleStartElement();
          depth++;
          break;
        case 2: 
          depth--;
          handleEndElement();
          if (depth != 0) {
            break;
          }
          break;
        case 4: 
        case 6: 
        case 12: 
          if (this.predictor.expectText())
          {
            event = this.fastInfosetStreamReader.peekNext();
            if (event == 2) {
              processNonIgnorableText();
            } else if (event == 1) {
              processIgnorableText();
            } else {
              handleFragmentedCharacters();
            }
          }
          break;
        }
        event = this.fastInfosetStreamReader.next();
      }
      this.fastInfosetStreamReader.next();
      
      handleEndDocument();
    }
    catch (SAXException e)
    {
      throw new XMLStreamException(e);
    }
  }
  
  protected Location getCurrentLocation()
  {
    return this.fastInfosetStreamReader.getLocation();
  }
  
  protected String getCurrentQName()
  {
    return this.fastInfosetStreamReader.getNameString();
  }
  
  private void handleStartElement()
    throws SAXException
  {
    processUnreportedText();
    for (int i = 0; i < this.fastInfosetStreamReader.accessNamespaceCount(); i++) {
      this.visitor.startPrefixMapping(this.fastInfosetStreamReader.getNamespacePrefix(i), this.fastInfosetStreamReader.getNamespaceURI(i));
    }
    this.tagName.uri = this.fastInfosetStreamReader.accessNamespaceURI();
    this.tagName.local = this.fastInfosetStreamReader.accessLocalName();
    this.tagName.atts = this.fastInfosetStreamReader.getAttributesHolder();
    
    this.visitor.startElement(this.tagName);
  }
  
  private void handleFragmentedCharacters()
    throws XMLStreamException, SAXException
  {
    this.buffer.setLength(0);
    
    this.buffer.append(this.fastInfosetStreamReader.accessTextCharacters(), this.fastInfosetStreamReader.accessTextStart(), this.fastInfosetStreamReader.accessTextLength());
    for (;;)
    {
      switch (this.fastInfosetStreamReader.peekNext())
      {
      case 1: 
        processBufferedText(true);
        return;
      case 2: 
        processBufferedText(false);
        return;
      case 4: 
      case 6: 
      case 12: 
        this.fastInfosetStreamReader.next();
        this.buffer.append(this.fastInfosetStreamReader.accessTextCharacters(), this.fastInfosetStreamReader.accessTextStart(), this.fastInfosetStreamReader.accessTextLength());
        
        break;
      case 3: 
      case 5: 
      case 7: 
      case 8: 
      case 9: 
      case 10: 
      case 11: 
      default: 
        this.fastInfosetStreamReader.next();
      }
    }
  }
  
  private void handleEndElement()
    throws SAXException
  {
    processUnreportedText();
    
    this.tagName.uri = this.fastInfosetStreamReader.accessNamespaceURI();
    this.tagName.local = this.fastInfosetStreamReader.accessLocalName();
    
    this.visitor.endElement(this.tagName);
    for (int i = this.fastInfosetStreamReader.accessNamespaceCount() - 1; i >= 0; i--) {
      this.visitor.endPrefixMapping(this.fastInfosetStreamReader.getNamespacePrefix(i));
    }
  }
  
  private final class CharSequenceImpl
    implements CharSequence
  {
    char[] ch;
    int start;
    int length;
    
    CharSequenceImpl() {}
    
    CharSequenceImpl(char[] ch, int start, int length)
    {
      this.ch = ch;
      this.start = start;
      this.length = length;
    }
    
    public void set()
    {
      this.ch = FastInfosetConnector.this.fastInfosetStreamReader.accessTextCharacters();
      this.start = FastInfosetConnector.this.fastInfosetStreamReader.accessTextStart();
      this.length = FastInfosetConnector.this.fastInfosetStreamReader.accessTextLength();
    }
    
    public final int length()
    {
      return this.length;
    }
    
    public final char charAt(int index)
    {
      return this.ch[(this.start + index)];
    }
    
    public final CharSequence subSequence(int start, int end)
    {
      return new CharSequenceImpl(FastInfosetConnector.this, this.ch, this.start + start, end - start);
    }
    
    public String toString()
    {
      return new String(this.ch, this.start, this.length);
    }
  }
  
  private final CharSequenceImpl charArray = new CharSequenceImpl();
  
  private void processNonIgnorableText()
    throws SAXException
  {
    this.textReported = true;
    boolean isTextAlgorithmAplied = this.fastInfosetStreamReader.getTextAlgorithmBytes() != null;
    if ((isTextAlgorithmAplied) && (this.fastInfosetStreamReader.getTextAlgorithmIndex() == 1))
    {
      this.base64Data.set(this.fastInfosetStreamReader.getTextAlgorithmBytesClone(), null);
      this.visitor.text(this.base64Data);
    }
    else
    {
      if (isTextAlgorithmAplied) {
        this.fastInfosetStreamReader.getText();
      }
      this.charArray.set();
      this.visitor.text(this.charArray);
    }
  }
  
  private void processIgnorableText()
    throws SAXException
  {
    boolean isTextAlgorithmAplied = this.fastInfosetStreamReader.getTextAlgorithmBytes() != null;
    if ((isTextAlgorithmAplied) && (this.fastInfosetStreamReader.getTextAlgorithmIndex() == 1))
    {
      this.base64Data.set(this.fastInfosetStreamReader.getTextAlgorithmBytesClone(), null);
      this.visitor.text(this.base64Data);
      this.textReported = true;
    }
    else
    {
      if (isTextAlgorithmAplied) {
        this.fastInfosetStreamReader.getText();
      }
      this.charArray.set();
      if (!WhiteSpaceProcessor.isWhiteSpace(this.charArray))
      {
        this.visitor.text(this.charArray);
        this.textReported = true;
      }
    }
  }
  
  private void processBufferedText(boolean ignorable)
    throws SAXException
  {
    if ((!ignorable) || (!WhiteSpaceProcessor.isWhiteSpace(this.buffer)))
    {
      this.visitor.text(this.buffer);
      this.textReported = true;
    }
  }
  
  private void processUnreportedText()
    throws SAXException
  {
    if ((!this.textReported) && (this.predictor.expectText())) {
      this.visitor.text("");
    }
    this.textReported = false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\FastInfosetConnector.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */