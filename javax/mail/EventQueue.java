package javax.mail;

import java.util.Vector;
import javax.mail.event.MailEvent;

class EventQueue
  implements Runnable
{
  static class QueueElement
  {
    QueueElement next = null;
    QueueElement prev = null;
    MailEvent event = null;
    Vector vector = null;
    
    QueueElement(MailEvent event, Vector vector)
    {
      this.event = event;
      this.vector = vector;
    }
  }
  
  private QueueElement head = null;
  private QueueElement tail = null;
  private Thread qThread;
  
  public EventQueue()
  {
    this.qThread = new Thread(this, "JavaMail-EventQueue");
    this.qThread.setDaemon(true);
    this.qThread.start();
  }
  
  public synchronized void enqueue(MailEvent event, Vector vector)
  {
    QueueElement newElt = new QueueElement(event, vector);
    if (this.head == null)
    {
      this.head = newElt;
      this.tail = newElt;
    }
    else
    {
      newElt.next = this.head;
      this.head.prev = newElt;
      this.head = newElt;
    }
    notifyAll();
  }
  
  private synchronized QueueElement dequeue()
    throws InterruptedException
  {
    while (this.tail == null) {
      wait();
    }
    QueueElement elt = this.tail;
    this.tail = elt.prev;
    if (this.tail == null) {
      this.head = null;
    } else {
      this.tail.next = null;
    }
    elt.prev = (elt.next = null);
    return elt;
  }
  
  public void run()
  {
    try
    {
      for (;;)
      {
        QueueElement qe = dequeue();
        MailEvent e = qe.event;
        Vector v = qe.vector;
        for (int i = 0; i < v.size(); i++)
        {
          try
          {
            e.dispatch(v.elementAt(i));
          }
          catch (Throwable t)
          {
            if (!(t instanceof InterruptedException)) {
              continue;
            }
          }
          return;
        }
        qe = null;e = null;v = null;
      }
    }
    catch (InterruptedException e) {}
  }
  
  void stop()
  {
    if (this.qThread != null)
    {
      this.qThread.interrupt();
      this.qThread = null;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\EventQueue.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */