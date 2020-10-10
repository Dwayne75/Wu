package com.wurmonline.server.weather;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.shared.constants.WeatherConstants;
import com.wurmonline.shared.util.MovementChecker;
import java.util.Calendar;
import java.util.Random;

public final class Weather
  implements MiscConstants
{
  private float cloudiness = 0.0F;
  private float fog = 0.0F;
  private float rain = 0.0F;
  private int windChange = 0;
  private float windAdd = 0.0F;
  private float fogAdd = 0.0F;
  private float rainAdd = 0.0F;
  
  public float getRainAdd()
  {
    return this.rainAdd;
  }
  
  public void setRainAdd(float aRainAdd)
  {
    this.rainAdd = aRainAdd;
  }
  
  private float fogTarget = 0.0F;
  private float rainTarget = 0.0F;
  
  public float getRainTarget()
  {
    return this.rainTarget;
  }
  
  public void setRainTarget(float aRainTarget)
  {
    this.rainTarget = aRainTarget;
  }
  
  private float cloudTarget = 0.0F;
  
  public float getCloudTarget()
  {
    return this.cloudTarget;
  }
  
  public void setCloudTarget(float aCloudTarget)
  {
    this.cloudTarget = aCloudTarget;
  }
  
  private final Random random = new Random();
  private float windDir = this.random.nextFloat();
  private float windRotation = normalizeAngle(this.windDir * 360.0F);
  private float windPower = this.random.nextFloat() - 0.5F;
  private int rainTicks = 0;
  private static boolean runningMain = false;
  
  public static final float normalizeAngle(float angle)
  {
    return MovementChecker.normalizeAngle(angle);
  }
  
  public final void modifyFogTarget(float modification)
  {
    this.fogTarget += modification;
  }
  
  public final void modifyRainTarget(float modification)
  {
    this.rainTarget += modification;
  }
  
  public final void modifyCloudTarget(float modification)
  {
    this.cloudTarget += modification;
  }
  
  public boolean tick()
  {
    int day = Calendar.getInstance().get(7);
    if ((!runningMain) && (Servers.localServer.LOGINSERVER))
    {
      this.windChange += 1;
      if (this.windChange == 1)
      {
        this.windDir = ((float)(this.windDir + this.random.nextGaussian() * this.random.nextFloat() * this.random.nextFloat() * this.random.nextFloat() * 0.10000000149011612D));
        
        this.windRotation = normalizeAngle(this.windDir * 360.0F);
        
        float p = 0.3F;
        this.windAdd = ((float)(this.windAdd + this.random.nextGaussian() * this.random.nextFloat() * this.random.nextFloat() * this.random.nextFloat() * 0.30000001192092896D));
        this.windPower += this.windAdd;
        this.windAdd *= 0.94F;
        this.windPower *= 0.82F;
        if (this.windPower > 0.5F) {
          this.windPower = 0.5F;
        }
        if (this.windPower < -0.5F) {
          this.windPower = -0.5F;
        }
      }
      if (this.windChange > 20) {
        this.windChange = 0;
      }
    }
    this.rainAdd *= 0.9F;
    if ((this.rainTicks > 15) && (this.rainAdd > 0.0F)) {
      this.rainAdd *= 0.5F;
    }
    this.rainAdd = ((float)(this.rainAdd + this.random.nextGaussian() * this.random.nextFloat() * this.random.nextFloat() * this.random.nextFloat()));
    float precipitation = 0.97F;
    if ((!runningMain) && (WurmCalendar.isSpring())) {
      precipitation = 0.9F;
    }
    if ((!runningMain) && (WurmCalendar.isAutumn())) {
      precipitation = 0.9F;
    }
    if ((!runningMain) && (WurmCalendar.isSummer())) {
      precipitation = 0.99F;
    }
    this.rainTarget *= precipitation;
    this.rainTarget += this.rainAdd;
    if (this.rainTarget > this.cloudiness) {
      this.rainTarget = this.cloudiness;
    }
    if (day == 2)
    {
      if (this.rainTarget < -8.0F) {
        this.rainTarget = -8.0F;
      }
    }
    else if (this.rainTarget < -16.0F) {
      this.rainTarget = -16.0F;
    }
    this.fogAdd *= 0.8F;
    this.fogAdd = ((float)(this.fogAdd + this.random.nextGaussian() * this.random.nextFloat() * this.random.nextFloat() * this.random.nextFloat() * 0.20000000298023224D));
    this.fogTarget *= 0.9F;
    this.fogTarget += this.fogAdd;
    if (this.fogTarget > 1.0F) {
      this.fogTarget = 1.0F;
    }
    if (this.fogTarget < -16.0F) {
      this.fogTarget = -16.0F;
    }
    if (this.rainTarget > 0.0F) {
      this.fogTarget *= (1.0F - this.rainTarget);
    }
    if (this.windPower > 0.2D) {
      this.fogTarget *= (1.0F - this.windPower);
    }
    float stability = 0.8F;
    if ((!runningMain) && (WurmCalendar.isSpring())) {
      stability = 0.8F;
    }
    if ((!runningMain) && (WurmCalendar.isAutumn())) {
      stability = 0.7F;
    }
    if ((!runningMain) && (WurmCalendar.isSummer())) {
      stability = 0.3F;
    }
    this.rain = (this.rain * stability + this.rainTarget * (1.0F - stability));
    if (this.rain < 0.0F)
    {
      this.rain = 0.0F;
      this.rainTicks = 0;
    }
    else if (this.rain > 0.0F)
    {
      this.rainTicks += 1;
    }
    this.fog = (this.fog * 0.9F + this.fogTarget * 0.1F);
    if (this.fog < 0.0F) {
      this.fog = 0.0F;
    }
    if (this.cloudiness < this.rain * 0.33F) {
      this.cloudiness = (this.rain * 0.33F);
    }
    this.cloudTarget += (float)this.random.nextGaussian() * 0.2F * this.random.nextFloat();
    if (day == 2)
    {
      if (this.cloudTarget > 1.0F) {
        this.cloudTarget = 1.0F;
      }
      if (this.cloudTarget < -0.4F) {
        this.cloudTarget = -0.4F;
      }
    }
    else if (day == 6)
    {
      if (this.cloudTarget > 0.2F) {
        this.cloudTarget = 0.2F;
      }
      if (this.cloudTarget < -0.2F) {
        this.cloudTarget = -0.2F;
      }
    }
    else
    {
      if (this.cloudTarget > 1.0F) {
        this.cloudTarget = 1.0F;
      }
      if (this.cloudTarget < -0.1F) {
        this.cloudTarget = -0.1F;
      }
    }
    this.cloudiness = (this.cloudiness * 0.98F + this.cloudTarget * 0.02F);
    return this.windChange == 1;
  }
  
  public int getRainTicks()
  {
    return this.rainTicks;
  }
  
  public void setRainTicks(int aRainTicks)
  {
    this.rainTicks = aRainTicks;
  }
  
  public void setWindOnly(float aWindRotation, float aWindpower, float aWindDir)
  {
    this.windDir = aWindDir;
    this.windPower = aWindpower;
    this.windRotation = aWindRotation;
  }
  
  public float getCloudiness()
  {
    return this.cloudiness;
  }
  
  public float getFog()
  {
    return this.fog;
  }
  
  public float getRain()
  {
    return this.rain;
  }
  
  public float getEvaporationRate()
  {
    return 0.1F;
  }
  
  public float getXWind()
  {
    return WeatherConstants.getWindX(this.windRotation, this.windPower);
  }
  
  public float getYWind()
  {
    return WeatherConstants.getWindY(this.windRotation, this.windPower);
  }
  
  public float getWindRotation()
  {
    return this.windRotation;
  }
  
  public float getWindDir()
  {
    return this.windDir;
  }
  
  public float getWindPower()
  {
    return this.windPower;
  }
  
  public String getWeatherString(boolean addNumbers)
  {
    StringBuilder buf = new StringBuilder();
    
    float absoluteWindPower = Math.abs(this.windPower);
    if (absoluteWindPower > 0.4D) {
      buf.append("A gale ");
    } else if (absoluteWindPower > 0.3D) {
      buf.append("A strong wind ");
    } else if (absoluteWindPower > 0.2D) {
      buf.append("A strong breeze ");
    } else if (absoluteWindPower > 0.1D) {
      buf.append("A breeze ");
    } else {
      buf.append("A light breeze ");
    }
    buf.append("is coming from the ");
    byte dir = 0;
    float degree = 22.5F;
    if ((this.windRotation >= 337.5D) || (this.windRotation < 22.5F)) {
      dir = 0;
    } else {
      for (int x = 0; x < 8; x++) {
        if (this.windRotation < 22.5F + 45 * x)
        {
          dir = (byte)x;
          break;
        }
      }
    }
    if (dir == 0) {
      buf.append("north.");
    } else if (dir == 7) {
      buf.append("northwest.");
    } else if (dir == 6) {
      buf.append("west.");
    } else if (dir == 5) {
      buf.append("southwest.");
    } else if (dir == 4) {
      buf.append("south.");
    } else if (dir == 3) {
      buf.append("southeast.");
    } else if (dir == 2) {
      buf.append("east");
    } else if (dir == 1) {
      buf.append("northeast.");
    }
    if (addNumbers) {
      buf.append("(" + absoluteWindPower + " from " + this.windRotation + ")");
    }
    return buf.toString();
  }
  
  public static void main(String[] args)
  {
    Weather weather = new Weather();
    runningMain = true;
    int nums = 0;
    int ticksRain = 0;
    float maxRain = 0.0F;
    int ticksCloud = 0;
    int ticksSame = 0;
    int ticksAnyRain = 0;
    boolean keepGoing = true;
    int maxTicks = 5000;
    while (keepGoing)
    {
      weather.tick();
      nums++;
      if (weather.getRain() > 0.5D) {
        ticksRain++;
      }
      if (weather.getRain() > 0.0F) {
        ticksAnyRain++;
      }
      if (weather.getRain() > maxRain) {
        maxRain = weather.getRain();
      }
      if (weather.getCloudiness() > 0.5D) {
        ticksCloud++;
      }
      if ((weather.getRain() > 0.5D) && (weather.getCloudiness() > 0.5D)) {
        ticksSame++;
      }
      if (nums > 5000) {
        keepGoing = false;
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\weather\Weather.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */