package org.apache.http.impl.conn.tsccm;

@Deprecated
public class WaitingThreadAborter
{
  private WaitingThread waitingThread;
  private boolean aborted;
  
  public void abort()
  {
    this.aborted = true;
    if (this.waitingThread != null) {
      this.waitingThread.interrupt();
    }
  }
  
  public void setWaitingThread(WaitingThread waitingThread)
  {
    this.waitingThread = waitingThread;
    if (this.aborted) {
      waitingThread.interrupt();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\conn\tsccm\WaitingThreadAborter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */