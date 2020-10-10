package javax.mail.internet;

import com.sun.mail.util.LineOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import javax.activation.DataHandler;
import javax.mail.MessagingException;

public class PreencodedMimeBodyPart
  extends MimeBodyPart
{
  private String encoding;
  
  public PreencodedMimeBodyPart(String encoding)
  {
    this.encoding = encoding;
  }
  
  public String getEncoding()
    throws MessagingException
  {
    return this.encoding;
  }
  
  public void writeTo(OutputStream os)
    throws IOException, MessagingException
  {
    LineOutputStream los = null;
    if ((os instanceof LineOutputStream)) {
      los = (LineOutputStream)os;
    } else {
      los = new LineOutputStream(os);
    }
    Enumeration hdrLines = getAllHeaderLines();
    while (hdrLines.hasMoreElements()) {
      los.writeln((String)hdrLines.nextElement());
    }
    los.writeln();
    
    getDataHandler().writeTo(os);
    os.flush();
  }
  
  protected void updateHeaders()
    throws MessagingException
  {
    super.updateHeaders();
    MimeBodyPart.setEncoding(this, this.encoding);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\internet\PreencodedMimeBodyPart.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */