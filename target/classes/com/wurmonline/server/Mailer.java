package com.wurmonline.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetAddress;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;

public final class Mailer
{
  private static final String pwfileName = "passwordmail.html";
  private static final String regmailfileName1 = "registrationphase1.html";
  private static final String regmailfileName2 = "registrationphase2.html";
  private static final String premexpiryw = "premiumexpirywarning.html";
  private static final String accountdelw = "accountdeletionwarning.html";
  private static final String accountdels = "accountdeletionsilvers.html";
  private static String phaseOneMail = ;
  private static String phaseTwoMail = loadConfirmationMail2();
  private static String passwordMail = loadPasswordMail();
  private static String accountDelMail = loadAccountDelMail();
  private static String accountDelPreventionMail = loadAccountDelPreventionMail();
  private static String premExpiryMail = loadPremExpiryMail();
  private static final Logger logger = Logger.getLogger(Mailer.class.getName());
  public static String smtpserver = "localhost";
  private static String smtpuser = "";
  private static String smtppw = "";
  private static final String amaserver = "";
  
  public static void sendMail(String sender, String receiver, String subject, String text)
    throws AddressException, MessagingException
  {
    new Mailer.1(sender, text, subject, receiver).start();
  }
  
  private static final String getLocalHost(Session session)
  {
    String localHostName = null;
    String name = "smtp";
    try
    {
      if ((localHostName == null) || (localHostName.length() <= 0)) {
        localHostName = InetAddress.getLocalHost().getHostName();
      }
      if ((localHostName == null) || (localHostName.length() <= 0)) {
        localHostName = session.getProperty("mail.smtp.localhost");
      }
    }
    catch (Exception uhex)
    {
      return "localhost";
    }
    return localHostName;
  }
  
  public static final String getPhaseOneMail()
  {
    if (phaseOneMail == null) {
      phaseOneMail = loadConfirmationMail1();
    }
    return phaseOneMail;
  }
  
  public static final String getPhaseTwoMail()
  {
    if (phaseTwoMail == null) {
      phaseTwoMail = loadConfirmationMail2();
    }
    return phaseTwoMail;
  }
  
  public static final String getPasswordMail()
  {
    if (passwordMail == null) {
      passwordMail = loadPasswordMail();
    }
    return passwordMail;
  }
  
  public static final String getAccountDelPreventionMail()
  {
    if (accountDelPreventionMail == null) {
      accountDelPreventionMail = loadAccountDelPreventionMail();
    }
    return accountDelPreventionMail;
  }
  
  public static final String getAccountDelMail()
  {
    if (accountDelMail == null) {
      accountDelMail = loadAccountDelMail();
    }
    return accountDelMail;
  }
  
  public static final String getPremExpiryMail()
  {
    if (premExpiryMail == null) {
      premExpiryMail = loadPremExpiryMail();
    }
    return premExpiryMail;
  }
  
  private static final String loadConfirmationMail1()
  {
    try
    {
      BufferedReader in = new BufferedReader(new FileReader("registrationphase1.html"));Throwable localThrowable3 = null;
      try
      {
        StringBuilder buf = new StringBuilder();
        String str;
        while ((str = in.readLine()) != null) {
          buf.append(str);
        }
        in.close();
        return buf.toString();
      }
      catch (Throwable localThrowable1)
      {
        localThrowable3 = localThrowable1;throw localThrowable1;
      }
      finally
      {
        if (in != null) {
          if (localThrowable3 != null) {
            try
            {
              in.close();
            }
            catch (Throwable localThrowable2)
            {
              localThrowable3.addSuppressed(localThrowable2);
            }
          } else {
            in.close();
          }
        }
      }
      return "";
    }
    catch (Exception localException) {}
  }
  
  private static final String loadConfirmationMail2()
  {
    try
    {
      BufferedReader in = new BufferedReader(new FileReader("registrationphase2.html"));Throwable localThrowable3 = null;
      try
      {
        StringBuilder buf = new StringBuilder();
        String str;
        while ((str = in.readLine()) != null) {
          buf.append(str);
        }
        in.close();
        return buf.toString();
      }
      catch (Throwable localThrowable1)
      {
        localThrowable3 = localThrowable1;throw localThrowable1;
      }
      finally
      {
        if (in != null) {
          if (localThrowable3 != null) {
            try
            {
              in.close();
            }
            catch (Throwable localThrowable2)
            {
              localThrowable3.addSuppressed(localThrowable2);
            }
          } else {
            in.close();
          }
        }
      }
      return "";
    }
    catch (Exception localException) {}
  }
  
  private static final String loadPasswordMail()
  {
    try
    {
      BufferedReader in = new BufferedReader(new FileReader("passwordmail.html"));Throwable localThrowable3 = null;
      try
      {
        StringBuilder buf = new StringBuilder();
        String str;
        while ((str = in.readLine()) != null) {
          buf.append(str);
        }
        in.close();
        return buf.toString();
      }
      catch (Throwable localThrowable1)
      {
        localThrowable3 = localThrowable1;throw localThrowable1;
      }
      finally
      {
        if (in != null) {
          if (localThrowable3 != null) {
            try
            {
              in.close();
            }
            catch (Throwable localThrowable2)
            {
              localThrowable3.addSuppressed(localThrowable2);
            }
          } else {
            in.close();
          }
        }
      }
      return "";
    }
    catch (Exception localException) {}
  }
  
  private static final String loadAccountDelMail()
  {
    try
    {
      BufferedReader in = new BufferedReader(new FileReader("accountdeletionwarning.html"));Throwable localThrowable3 = null;
      try
      {
        StringBuilder buf = new StringBuilder();
        String str;
        while ((str = in.readLine()) != null) {
          buf.append(str);
        }
        in.close();
        return buf.toString();
      }
      catch (Throwable localThrowable1)
      {
        localThrowable3 = localThrowable1;throw localThrowable1;
      }
      finally
      {
        if (in != null) {
          if (localThrowable3 != null) {
            try
            {
              in.close();
            }
            catch (Throwable localThrowable2)
            {
              localThrowable3.addSuppressed(localThrowable2);
            }
          } else {
            in.close();
          }
        }
      }
      return "";
    }
    catch (Exception localException) {}
  }
  
  private static final String loadAccountDelPreventionMail()
  {
    try
    {
      BufferedReader in = new BufferedReader(new FileReader("accountdeletionsilvers.html"));Throwable localThrowable3 = null;
      try
      {
        StringBuilder buf = new StringBuilder();
        String str;
        while ((str = in.readLine()) != null) {
          buf.append(str);
        }
        in.close();
        return buf.toString();
      }
      catch (Throwable localThrowable1)
      {
        localThrowable3 = localThrowable1;throw localThrowable1;
      }
      finally
      {
        if (in != null) {
          if (localThrowable3 != null) {
            try
            {
              in.close();
            }
            catch (Throwable localThrowable2)
            {
              localThrowable3.addSuppressed(localThrowable2);
            }
          } else {
            in.close();
          }
        }
      }
      return "";
    }
    catch (Exception localException) {}
  }
  
  private static final String loadPremExpiryMail()
  {
    try
    {
      BufferedReader in = new BufferedReader(new FileReader("premiumexpirywarning.html"));Throwable localThrowable3 = null;
      try
      {
        StringBuilder buf = new StringBuilder();
        String str;
        while ((str = in.readLine()) != null) {
          buf.append(str);
        }
        in.close();
        return buf.toString();
      }
      catch (Throwable localThrowable1)
      {
        localThrowable3 = localThrowable1;throw localThrowable1;
      }
      finally
      {
        if (in != null) {
          if (localThrowable3 != null) {
            try
            {
              in.close();
            }
            catch (Throwable localThrowable2)
            {
              localThrowable3.addSuppressed(localThrowable2);
            }
          } else {
            in.close();
          }
        }
      }
      return "";
    }
    catch (Exception localException) {}
  }
  
  public static void main(String[] args) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\Mailer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */