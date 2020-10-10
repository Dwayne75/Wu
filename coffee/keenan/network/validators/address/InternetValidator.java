package coffee.keenan.network.validators.address;

import coffee.keenan.network.config.IConfiguration;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

public class InternetValidator
  implements IAddressValidator
{
  private Exception exception;
  
  public boolean validate(InetAddress address, IConfiguration configuration)
  {
    try
    {
      SocketChannel socket = SocketChannel.open();Throwable localThrowable3 = null;
      try
      {
        socket.socket().setSoTimeout(configuration.getTimeout());
        socket.bind(new InetSocketAddress(address, 0));
        socket.connect(new InetSocketAddress(configuration.getTestUrl(), configuration.getTestPort()));
      }
      catch (Throwable localThrowable1)
      {
        localThrowable3 = localThrowable1;throw localThrowable1;
      }
      finally
      {
        if (socket != null) {
          if (localThrowable3 != null) {
            try
            {
              socket.close();
            }
            catch (Throwable localThrowable2)
            {
              localThrowable3.addSuppressed(localThrowable2);
            }
          } else {
            socket.close();
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\coffee\keenan\network\validators\address\InternetValidator.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */