package com.mysql.jdbc;

import com.mysql.jdbc.log.Log;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

class CompressedInputStream
  extends InputStream
{
  private byte[] buffer;
  private Connection connection;
  private InputStream in;
  private Inflater inflater;
  private byte[] packetHeaderBuffer = new byte[7];
  private int pos = 0;
  
  public CompressedInputStream(Connection conn, InputStream streamFromServer)
  {
    this.connection = conn;
    this.in = streamFromServer;
    this.inflater = new Inflater();
  }
  
  public int available()
    throws IOException
  {
    if (this.buffer == null) {
      return this.in.available();
    }
    return this.buffer.length - this.pos + this.in.available();
  }
  
  public void close()
    throws IOException
  {
    this.in.close();
    this.buffer = null;
    this.inflater = null;
  }
  
  private void getNextPacketFromServer()
    throws IOException
  {
    byte[] uncompressedData = null;
    
    int lengthRead = readFully(this.packetHeaderBuffer, 0, 7);
    if (lengthRead < 7) {
      throw new IOException("Unexpected end of input stream");
    }
    int compressedPacketLength = (this.packetHeaderBuffer[0] & 0xFF) + ((this.packetHeaderBuffer[1] & 0xFF) << 8) + ((this.packetHeaderBuffer[2] & 0xFF) << 16);
    
    int uncompressedLength = (this.packetHeaderBuffer[4] & 0xFF) + ((this.packetHeaderBuffer[5] & 0xFF) << 8) + ((this.packetHeaderBuffer[6] & 0xFF) << 16);
    if (this.connection.getTraceProtocol()) {
      try
      {
        this.connection.getLog().logTrace("Reading compressed packet of length " + compressedPacketLength + " uncompressed to " + uncompressedLength);
      }
      catch (SQLException sqlEx)
      {
        throw new IOException(sqlEx.toString());
      }
    }
    if (uncompressedLength > 0)
    {
      uncompressedData = new byte[uncompressedLength];
      
      byte[] compressedBuffer = new byte[compressedPacketLength];
      
      readFully(compressedBuffer, 0, compressedPacketLength);
      try
      {
        this.inflater.reset();
      }
      catch (NullPointerException npe)
      {
        this.inflater = new Inflater();
      }
      this.inflater.setInput(compressedBuffer);
      try
      {
        this.inflater.inflate(uncompressedData);
      }
      catch (DataFormatException dfe)
      {
        throw new IOException("Error while uncompressing packet from server.");
      }
      this.inflater.end();
    }
    else
    {
      if (this.connection.getTraceProtocol()) {
        try
        {
          this.connection.getLog().logTrace("Packet didn't meet compression threshold, not uncompressing...");
        }
        catch (SQLException sqlEx)
        {
          throw new IOException(sqlEx.toString());
        }
      }
      uncompressedData = new byte[compressedPacketLength];
      readFully(uncompressedData, 0, compressedPacketLength);
    }
    if (this.connection.getTraceProtocol()) {
      try
      {
        this.connection.getLog().logTrace("Uncompressed packet: \n" + StringUtils.dumpAsHex(uncompressedData, compressedPacketLength));
      }
      catch (SQLException sqlEx)
      {
        throw new IOException(sqlEx.toString());
      }
    }
    if ((this.buffer != null) && (this.pos < this.buffer.length))
    {
      if (this.connection.getTraceProtocol()) {
        try
        {
          this.connection.getLog().logTrace("Combining remaining packet with new: ");
        }
        catch (SQLException sqlEx)
        {
          throw new IOException(sqlEx.toString());
        }
      }
      int remaining = this.buffer.length - this.pos;
      byte[] newBuffer = new byte[remaining + uncompressedData.length];
      
      int newIndex = 0;
      for (int i = this.pos; i < this.buffer.length; i++) {
        newBuffer[(newIndex++)] = this.buffer[i];
      }
      System.arraycopy(uncompressedData, 0, newBuffer, newIndex, uncompressedData.length);
      
      uncompressedData = newBuffer;
    }
    this.pos = 0;
    this.buffer = uncompressedData;
  }
  
  private void getNextPacketIfRequired(int numBytes)
    throws IOException
  {
    if ((this.buffer == null) || (this.pos + numBytes > this.buffer.length)) {
      getNextPacketFromServer();
    }
  }
  
  public int read()
    throws IOException
  {
    try
    {
      getNextPacketIfRequired(1);
    }
    catch (IOException ioEx)
    {
      return -1;
    }
    return this.buffer[(this.pos++)] & 0xFF;
  }
  
  public int read(byte[] b)
    throws IOException
  {
    return read(b, 0, b.length);
  }
  
  public int read(byte[] b, int off, int len)
    throws IOException
  {
    if (b == null) {
      throw new NullPointerException();
    }
    if ((off < 0) || (off > b.length) || (len < 0) || (off + len > b.length) || (off + len < 0)) {
      throw new IndexOutOfBoundsException();
    }
    if (len <= 0) {
      return 0;
    }
    try
    {
      getNextPacketIfRequired(len);
    }
    catch (IOException ioEx)
    {
      return -1;
    }
    System.arraycopy(this.buffer, this.pos, b, off, len);
    this.pos += len;
    
    return len;
  }
  
  private final int readFully(byte[] b, int off, int len)
    throws IOException
  {
    if (len < 0) {
      throw new IndexOutOfBoundsException();
    }
    int n = 0;
    while (n < len)
    {
      int count = this.in.read(b, off + n, len - n);
      if (count < 0) {
        throw new EOFException();
      }
      n += count;
    }
    return n;
  }
  
  public long skip(long n)
    throws IOException
  {
    long count = 0L;
    for (long i = 0L; i < n; i += 1L)
    {
      int bytesRead = read();
      if (bytesRead == -1) {
        break;
      }
      count += 1L;
    }
    return count;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\CompressedInputStream.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */