package com.sun.codemodel;

import com.sun.codemodel.util.EncoderFactory;
import com.sun.codemodel.util.UnicodeEscapeWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.CharsetEncoder;

public abstract class CodeWriter
{
  public abstract OutputStream openBinary(JPackage paramJPackage, String paramString)
    throws IOException;
  
  public Writer openSource(JPackage pkg, String fileName)
    throws IOException
  {
    final OutputStreamWriter bw = new OutputStreamWriter(openBinary(pkg, fileName));
    try
    {
      new UnicodeEscapeWriter(bw)
      {
        private final CharsetEncoder encoder = EncoderFactory.createEncoder(bw.getEncoding());
        
        protected boolean requireEscaping(int ch)
        {
          if ((ch < 32) && (" \t\r\n".indexOf(ch) == -1)) {
            return true;
          }
          if (ch < 128) {
            return false;
          }
          return !this.encoder.canEncode((char)ch);
        }
      };
    }
    catch (Throwable t) {}
    return new UnicodeEscapeWriter(bw);
  }
  
  public abstract void close()
    throws IOException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\CodeWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */