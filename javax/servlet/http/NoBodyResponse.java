package javax.servlet.http;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ResourceBundle;
import javax.servlet.ServletOutputStream;

class NoBodyResponse
  extends HttpServletResponseWrapper
{
  private static final ResourceBundle lStrings = ResourceBundle.getBundle("javax.servlet.http.LocalStrings");
  private NoBodyOutputStream noBody;
  private PrintWriter writer;
  private boolean didSetContentLength;
  private boolean usingOutputStream;
  
  NoBodyResponse(HttpServletResponse r)
  {
    super(r);
    this.noBody = new NoBodyOutputStream();
  }
  
  void setContentLength()
  {
    if (!this.didSetContentLength)
    {
      if (this.writer != null) {
        this.writer.flush();
      }
      setContentLength(this.noBody.getContentLength());
    }
  }
  
  public void setContentLength(int len)
  {
    super.setContentLength(len);
    this.didSetContentLength = true;
  }
  
  public ServletOutputStream getOutputStream()
    throws IOException
  {
    if (this.writer != null) {
      throw new IllegalStateException(lStrings.getString("err.ise.getOutputStream"));
    }
    this.usingOutputStream = true;
    
    return this.noBody;
  }
  
  public PrintWriter getWriter()
    throws UnsupportedEncodingException
  {
    if (this.usingOutputStream) {
      throw new IllegalStateException(lStrings.getString("err.ise.getWriter"));
    }
    if (this.writer == null)
    {
      OutputStreamWriter w = new OutputStreamWriter(this.noBody, getCharacterEncoding());
      
      this.writer = new PrintWriter(w);
    }
    return this.writer;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\http\NoBodyResponse.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */