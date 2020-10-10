package com.wurmonline.server.meshgen;

import java.util.Random;

public final class ImprovedNoise
{
  private final int[] p = new int['Ȁ'];
  
  public ImprovedNoise(long seed)
  {
    shuffle(seed);
  }
  
  public double noise(double x, double y, double z)
  {
    int X = (int)Math.floor(x) & 0xFF;
    int Y = (int)Math.floor(y) & 0xFF;
    int Z = (int)Math.floor(z) & 0xFF;
    
    x -= Math.floor(x);
    y -= Math.floor(y);
    z -= Math.floor(z);
    
    double u = fade(x);
    double v = fade(y);
    double w = fade(z);
    
    int A = this.p[X] + Y;
    int AA = this.p[A] + Z;
    int AB = this.p[(A + 1)] + Z;
    int B = this.p[(X + 1)] + Y;
    int BA = this.p[B] + Z;
    int BB = this.p[(B + 1)] + Z;
    
    return lerp(w, lerp(v, lerp(u, grad(this.p[AA], x, y, z), 
      grad(this.p[BA], x - 1.0D, y, z)), 
      lerp(u, grad(this.p[AB], x, y - 1.0D, z), 
      grad(this.p[BB], x - 1.0D, y - 1.0D, z))), 
      lerp(v, lerp(u, grad(this.p[(AA + 1)], x, y, z - 1.0D), 
      grad(this.p[(BA + 1)], x - 1.0D, y, z - 1.0D)), 
      lerp(u, grad(this.p[(AB + 1)], x, y - 1.0D, z - 1.0D), grad(this.p[(BB + 1)], x - 1.0D, y - 1.0D, z - 1.0D))));
  }
  
  double fade(double t)
  {
    return t * t * t * (t * (t * 6.0D - 15.0D) + 10.0D);
  }
  
  double lerp(double t, double a, double b)
  {
    return a + t * (b - a);
  }
  
  double grad(int hash, double x, double y, double z)
  {
    int h = hash & 0xF;
    double u = h < 8 ? x : y;
    double v = (h == 12) || (h == 14) ? x : h < 4 ? y : z;
    return ((h & 0x1) == 0 ? u : -u) + ((h & 0x2) == 0 ? v : -v);
  }
  
  public double perlinNoise(double x, double y)
  {
    double n = 0.0D;
    for (int i = 0; i < 8; i++)
    {
      double stepSize = 64.0D / (1 << i);
      n += noise(x / stepSize, y / stepSize, 128.0D) * 1.0D / (1 << i);
    }
    return n;
  }
  
  public void shuffle(long seed)
  {
    Random random = new Random(seed);
    int[] permutation = new int['Ā'];
    for (int i = 0; i < 256; i++) {
      permutation[i] = i;
    }
    for (int i = 0; i < 256; i++)
    {
      int j = random.nextInt(256 - i) + i;
      int tmp = permutation[i];
      permutation[i] = permutation[j];
      permutation[j] = tmp;
      this.p[i] = permutation[i];
      this.p[(i + 256)] = permutation[i];
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\meshgen\ImprovedNoise.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */