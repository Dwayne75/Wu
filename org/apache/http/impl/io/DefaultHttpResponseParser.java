package org.apache.http.impl.io;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.NoHttpResponseException;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.message.LineParser;
import org.apache.http.message.ParserCursor;
import org.apache.http.params.HttpParams;
import org.apache.http.util.CharArrayBuffer;

@NotThreadSafe
public class DefaultHttpResponseParser
  extends AbstractMessageParser<HttpResponse>
{
  private final HttpResponseFactory responseFactory;
  private final CharArrayBuffer lineBuf;
  
  public DefaultHttpResponseParser(SessionInputBuffer buffer, LineParser parser, HttpResponseFactory responseFactory, HttpParams params)
  {
    super(buffer, parser, params);
    if (responseFactory == null) {
      throw new IllegalArgumentException("Response factory may not be null");
    }
    this.responseFactory = responseFactory;
    this.lineBuf = new CharArrayBuffer(128);
  }
  
  protected HttpResponse parseHead(SessionInputBuffer sessionBuffer)
    throws IOException, HttpException, ParseException
  {
    this.lineBuf.clear();
    int i = sessionBuffer.readLine(this.lineBuf);
    if (i == -1) {
      throw new NoHttpResponseException("The target server failed to respond");
    }
    ParserCursor cursor = new ParserCursor(0, this.lineBuf.length());
    StatusLine statusline = this.lineParser.parseStatusLine(this.lineBuf, cursor);
    return this.responseFactory.newHttpResponse(statusline, null);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\io\DefaultHttpResponseParser.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */