package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;

final class DowngradedSeekableByteChannel
  implements SeekableByteChannel
{
  private final FileChannel channel;
  
  DowngradedSeekableByteChannel(FileChannel channel)
  {
    this.channel = ((FileChannel)Preconditions.checkNotNull(channel));
  }
  
  public int read(ByteBuffer dst)
    throws IOException
  {
    return this.channel.read(dst);
  }
  
  public int write(ByteBuffer src)
    throws IOException
  {
    return this.channel.write(src);
  }
  
  public long position()
    throws IOException
  {
    return this.channel.position();
  }
  
  public SeekableByteChannel position(long newPosition)
    throws IOException
  {
    this.channel.position(newPosition);
    return this;
  }
  
  public long size()
    throws IOException
  {
    return this.channel.size();
  }
  
  public SeekableByteChannel truncate(long size)
    throws IOException
  {
    this.channel.truncate(size);
    return this;
  }
  
  public boolean isOpen()
  {
    return this.channel.isOpen();
  }
  
  public void close()
    throws IOException
  {
    this.channel.close();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\DowngradedSeekableByteChannel.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */