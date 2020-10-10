package com.sun.xml.txw2.output;

import com.sun.xml.txw2.TxwException;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

public class StreamSerializer
  implements XmlSerializer
{
  private final SaxSerializer serializer;
  private final XMLWriter writer;
  
  public StreamSerializer(OutputStream out)
  {
    this(createWriter(out));
  }
  
  public StreamSerializer(OutputStream out, String encoding)
    throws UnsupportedEncodingException
  {
    this(createWriter(out, encoding));
  }
  
  public StreamSerializer(Writer out)
  {
    this(new StreamResult(out));
  }
  
  public StreamSerializer(StreamResult streamResult)
  {
    final OutputStream[] autoClose = new OutputStream[1];
    if (streamResult.getWriter() != null)
    {
      this.writer = createWriter(streamResult.getWriter());
    }
    else if (streamResult.getOutputStream() != null)
    {
      this.writer = createWriter(streamResult.getOutputStream());
    }
    else if (streamResult.getSystemId() != null)
    {
      String fileURL = streamResult.getSystemId();
      
      fileURL = convertURL(fileURL);
      try
      {
        FileOutputStream fos = new FileOutputStream(fileURL);
        autoClose[0] = fos;
        this.writer = createWriter(fos);
      }
      catch (IOException e)
      {
        throw new TxwException(e);
      }
    }
    else
    {
      throw new IllegalArgumentException();
    }
    this.serializer = new SaxSerializer(this.writer, this.writer, false)
    {
      public void endDocument()
      {
        super.endDocument();
        if (autoClose[0] != null)
        {
          try
          {
            autoClose[0].close();
          }
          catch (IOException e)
          {
            throw new TxwException(e);
          }
          autoClose[0] = null;
        }
      }
    };
  }
  
  private StreamSerializer(XMLWriter writer)
  {
    this.writer = writer;
    
    this.serializer = new SaxSerializer(writer, writer, false);
  }
  
  private String convertURL(String url)
  {
    url = url.replace('\\', '/');
    url = url.replaceAll("//", "/");
    url = url.replaceAll("//", "/");
    if (url.startsWith("file:/")) {
      if (url.substring(6).indexOf(":") > 0) {
        url = url.substring(6);
      } else {
        url = url.substring(5);
      }
    }
    return url;
  }
  
  public void startDocument()
  {
    this.serializer.startDocument();
  }
  
  public void beginStartTag(String uri, String localName, String prefix)
  {
    this.serializer.beginStartTag(uri, localName, prefix);
  }
  
  public void writeAttribute(String uri, String localName, String prefix, StringBuilder value)
  {
    this.serializer.writeAttribute(uri, localName, prefix, value);
  }
  
  public void writeXmlns(String prefix, String uri)
  {
    this.serializer.writeXmlns(prefix, uri);
  }
  
  public void endStartTag(String uri, String localName, String prefix)
  {
    this.serializer.endStartTag(uri, localName, prefix);
  }
  
  public void endTag()
  {
    this.serializer.endTag();
  }
  
  public void text(StringBuilder text)
  {
    this.serializer.text(text);
  }
  
  public void cdata(StringBuilder text)
  {
    this.serializer.cdata(text);
  }
  
  public void comment(StringBuilder comment)
  {
    this.serializer.comment(comment);
  }
  
  public void endDocument()
  {
    this.serializer.endDocument();
  }
  
  public void flush()
  {
    this.serializer.flush();
    try
    {
      this.writer.flush();
    }
    catch (IOException e)
    {
      throw new TxwException(e);
    }
  }
  
  private static XMLWriter createWriter(Writer w)
  {
    DataWriter dw = new DataWriter(new BufferedWriter(w));
    dw.setIndentStep("  ");
    return dw;
  }
  
  private static XMLWriter createWriter(OutputStream os, String encoding)
    throws UnsupportedEncodingException
  {
    XMLWriter writer = createWriter(new OutputStreamWriter(os, encoding));
    writer.setEncoding(encoding);
    return writer;
  }
  
  private static XMLWriter createWriter(OutputStream os)
  {
    try
    {
      return createWriter(os, "UTF-8");
    }
    catch (UnsupportedEncodingException e)
    {
      throw new Error(e);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\txw2\output\StreamSerializer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */