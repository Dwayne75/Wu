package coffee.keenan.network.validators.port;

import coffee.keenan.network.config.IConfiguration;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

public class UDPValidator
  implements IPortValidator
{
  private Exception exception;
  
  public boolean validate(InetAddress address, IConfiguration configuration, int port)
  {
    try
    {
      DatagramChannel datagram = DatagramChannel.open();Throwable localThrowable3 = null;
      try
      {
        datagram.socket().setSoTimeout(configuration.getTimeout());
        datagram.bind(new InetSocketAddress(address, port));
      }
      catch (Throwable localThrowable1)
      {
        localThrowable3 = localThrowable1;throw localThrowable1;
      }
      finally
      {
        if (datagram != null) {
          if (localThrowable3 != null) {
            try
            {
              datagram.close();
            }
            catch (Throwable localThrowable2)
            {
              localThrowable3.addSuppressed(localThrowable2);
            }
          } else {
            datagram.close();
          }
        }
      }
    }
    catch (Exception e)
    {
      this.exception = e;
      return false;
    }
    return true;
  }
  
  public Exception getException()
  {
    return this.exception;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\coffee\keenan\network\validators\port\UDPValidator.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */