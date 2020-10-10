package winterwell.jtwitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import winterwell.json.JSONObject;

final class StreamGobbler
  extends Thread
{
  Exception ex;
  int forgotten;
  private ArrayList<String> jsons = new ArrayList();
  volatile boolean stopFlag;
  final AStream stream;
  
  public StreamGobbler(AStream stream)
  {
    setDaemon(true);
    this.stream = stream;
  }
  
  protected void finalize()
    throws Throwable
  {
    if (this.stream != null) {
      InternalUtils.close(this.stream.stream);
    }
  }
  
  public void pleaseStop()
  {
    if (this.stream != null) {
      InternalUtils.close(this.stream.stream);
    }
    this.stopFlag = true;
  }
  
  public synchronized String[] popJsons()
  {
    String[] arr = (String[])this.jsons.toArray(new String[this.jsons.size()]);
    
    this.jsons = new ArrayList();
    return arr;
  }
  
  private void readJson(BufferedReader br, int len)
    throws IOException
  {
    assert (len > 0);
    char[] sb = new char[len];
    int cnt = 0;
    while (len > 0)
    {
      int rd = br.read(sb, cnt, len);
      if (rd == -1) {
        throw new IOException("end of stream");
      }
      cnt += rd;
      len -= rd;
    }
    String json = new String(sb);
    if (!this.stream.listenersOnly) {
      synchronized (this)
      {
        this.jsons.add(json);
        
        this.forgotten += AStream.forgetIfFull(this.jsons);
      }
    }
    readJson2_notifyListeners(json);
  }
  
  private void readJson2_notifyListeners(String json)
  {
    if (this.stream.listeners.size() == 0) {
      return;
    }
    synchronized (this.stream.listeners)
    {
      try
      {
        JSONObject jo = new JSONObject(json);
        Object obj = AStream.read3_parse(jo, this.stream.jtwit);
        for (AStream.IListen listener : this.stream.listeners)
        {
          boolean carryOn;
          boolean carryOn;
          if ((obj instanceof Twitter.ITweet))
          {
            carryOn = listener.processTweet((Twitter.ITweet)obj);
          }
          else
          {
            boolean carryOn;
            if ((obj instanceof TwitterEvent)) {
              carryOn = listener.processEvent((TwitterEvent)obj);
            } else {
              carryOn = listener.processSystemEvent((Object[])obj);
            }
          }
          if (!carryOn) {
            break;
          }
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }
  
  private int readLength(BufferedReader br)
    throws IOException
  {
    StringBuilder numSb = new StringBuilder();
    for (;;)
    {
      int ich = br.read();
      if (ich == -1) {
        throw new IOException("end of stream " + this);
      }
      char ch = (char)ich;
      if ((ch == '\n') || (ch == '\r'))
      {
        if (numSb.length() != 0) {
          break;
        }
      }
      else
      {
        assert (Character.isDigit(ch)) : ch;
        assert (numSb.length() < 10) : numSb;
        numSb.append(ch);
      }
    }
    return Integer.valueOf(numSb.toString()).intValue();
  }
  
  public void run()
  {
    while (!this.stopFlag)
    {
      assert (this.stream.stream != null) : this.stream;
      try
      {
        InputStreamReader isr = new InputStreamReader(this.stream.stream);
        BufferedReader br = new BufferedReader(isr);
        while (!this.stopFlag)
        {
          int len = readLength(br);
          readJson(br, len);
        }
      }
      catch (Exception ioe)
      {
        if (this.stopFlag) {
          return;
        }
        this.ex = ioe;
        
        this.stream.addSysEvent(new Object[] { "exception", this.ex });
        if (!this.stream.autoReconnect) {
          return;
        }
        try
        {
          this.stream.reconnectFromGobblerThread();
          if ((!$assertionsDisabled) && (this.stream.stream == null)) {
            throw new AssertionError(this.stream);
          }
        }
        catch (Exception e)
        {
          this.ex = e;
          return;
        }
      }
    }
  }
  
  public String toString()
  {
    return getName() + "[" + this.jsons.size() + "]";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\StreamGobbler.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */