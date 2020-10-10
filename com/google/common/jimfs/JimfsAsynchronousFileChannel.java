package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLock;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import javax.annotation.Nullable;

final class JimfsAsynchronousFileChannel
  extends AsynchronousFileChannel
{
  private final JimfsFileChannel channel;
  private final ListeningExecutorService executor;
  
  public JimfsAsynchronousFileChannel(JimfsFileChannel channel, ExecutorService executor)
  {
    this.channel = ((JimfsFileChannel)Preconditions.checkNotNull(channel));
    this.executor = MoreExecutors.listeningDecorator(executor);
  }
  
  public long size()
    throws IOException
  {
    return this.channel.size();
  }
  
  private <R, A> void addCallback(ListenableFuture<R> future, CompletionHandler<R, ? super A> handler, @Nullable A attachment)
  {
    future.addListener(new CompletionHandlerCallback(future, handler, attachment, null), this.executor);
  }
  
  public AsynchronousFileChannel truncate(long size)
    throws IOException
  {
    this.channel.truncate(size);
    return this;
  }
  
  public void force(boolean metaData)
    throws IOException
  {
    this.channel.force(metaData);
  }
  
  public <A> void lock(long position, long size, boolean shared, @Nullable A attachment, CompletionHandler<FileLock, ? super A> handler)
  {
    Preconditions.checkNotNull(handler);
    addCallback(lock(position, size, shared), handler, attachment);
  }
  
  public ListenableFuture<FileLock> lock(final long position, long size, final boolean shared)
  {
    Util.checkNotNegative(position, "position");
    Util.checkNotNegative(size, "size");
    if (!isOpen()) {
      return closedChannelFuture();
    }
    if (shared) {
      this.channel.checkReadable();
    } else {
      this.channel.checkWritable();
    }
    this.executor.submit(new Callable()
    {
      public FileLock call()
        throws IOException
      {
        return JimfsAsynchronousFileChannel.this.tryLock(position, shared, this.val$shared);
      }
    });
  }
  
  public FileLock tryLock(long position, long size, boolean shared)
    throws IOException
  {
    Util.checkNotNegative(position, "position");
    Util.checkNotNegative(size, "size");
    this.channel.checkOpen();
    if (shared) {
      this.channel.checkReadable();
    } else {
      this.channel.checkWritable();
    }
    return new JimfsFileChannel.FakeFileLock(this, position, size, shared);
  }
  
  public <A> void read(ByteBuffer dst, long position, @Nullable A attachment, CompletionHandler<Integer, ? super A> handler)
  {
    addCallback(read(dst, position), handler, attachment);
  }
  
  public ListenableFuture<Integer> read(final ByteBuffer dst, final long position)
  {
    Preconditions.checkArgument(!dst.isReadOnly(), "dst may not be read-only");
    Util.checkNotNegative(position, "position");
    if (!isOpen()) {
      return closedChannelFuture();
    }
    this.channel.checkReadable();
    this.executor.submit(new Callable()
    {
      public Integer call()
        throws IOException
      {
        return Integer.valueOf(JimfsAsynchronousFileChannel.this.channel.read(dst, position));
      }
    });
  }
  
  public <A> void write(ByteBuffer src, long position, @Nullable A attachment, CompletionHandler<Integer, ? super A> handler)
  {
    addCallback(write(src, position), handler, attachment);
  }
  
  public ListenableFuture<Integer> write(final ByteBuffer src, final long position)
  {
    Util.checkNotNegative(position, "position");
    if (!isOpen()) {
      return closedChannelFuture();
    }
    this.channel.checkWritable();
    this.executor.submit(new Callable()
    {
      public Integer call()
        throws IOException
      {
        return Integer.valueOf(JimfsAsynchronousFileChannel.this.channel.write(src, position));
      }
    });
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
  
  private static <V> ListenableFuture<V> closedChannelFuture()
  {
    SettableFuture<V> future = SettableFuture.create();
    future.setException(new ClosedChannelException());
    return future;
  }
  
  private static final class CompletionHandlerCallback<R, A>
    implements Runnable
  {
    private final ListenableFuture<R> future;
    private final CompletionHandler<R, ? super A> completionHandler;
    @Nullable
    private final A attachment;
    
    private CompletionHandlerCallback(ListenableFuture<R> future, CompletionHandler<R, ? super A> completionHandler, @Nullable A attachment)
    {
      this.future = ((ListenableFuture)Preconditions.checkNotNull(future));
      this.completionHandler = ((CompletionHandler)Preconditions.checkNotNull(completionHandler));
      this.attachment = attachment;
    }
    
    public void run()
    {
      R result;
      try
      {
        result = this.future.get();
      }
      catch (ExecutionException e)
      {
        onFailure(e.getCause());
        return;
      }
      catch (InterruptedException|RuntimeException|Error e)
      {
        onFailure(e);
        return;
      }
      onSuccess(result);
    }
    
    private void onSuccess(R result)
    {
      this.completionHandler.completed(result, this.attachment);
    }
    
    private void onFailure(Throwable t)
    {
      this.completionHandler.failed(t, this.attachment);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\JimfsAsynchronousFileChannel.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */