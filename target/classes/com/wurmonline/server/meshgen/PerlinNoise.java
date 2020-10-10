package com.wurmonline.server.meshgen;

import java.util.Random;

final class PerlinNoise
{
  static float[] f_lut = new float['က'];
  float[][] noise;
  private float[][] noiseValues;
  private int level;
  private int width;
  private Random random;
  
  static
  {
    for (int i = 0; i < f_lut.length; i++)
    {
      double ft = i / f_lut.length * 3.141592653589793D;
      f_lut[i] = ((float)((1.0D - Math.cos(ft)) * 0.5D));
    }
  }
  
  PerlinNoise(Random aRandom, int aLevel)
  {
    this.random = aRandom;
    this.width = (2 << aLevel);
    this.level = aLevel;
    if (this.width > 4096) {
      throw new IllegalArgumentException("Max size is 4096");
    }
    this.noise = new float[this.width][this.width];
    this.noiseValues = new float[this.width][this.width];
  }
  
  float[][] generatePerlinNoise(float persistence, int mode, MeshGenGui.Task task, int progressStart, int progressRange)
  {
    int highnoisesteps = 1;
    int start = 0;
    for (int x = 0; x < this.width; x++) {
      for (int y = 0; y < this.width; y++) {
        this.noiseValues[x][y] = 0.0F;
      }
    }
    for (int i = 0; i < this.level + 2; i++)
    {
      int w = 1 << i;
      
      float perst = (float)Math.pow(0.9990000128746033D, i - 0 + 1) * persistence;
      float amplitude = (float)Math.pow(perst, i - 0 + 1);
      if (i <= 1) {
        amplitude *= i * i / 1.0F;
      }
      PerlinNoise.NoiseMap noiseMap = new PerlinNoise.NoiseMap(this, this.random, w, mode, null);
      for (int x = 0; x < this.width; x++)
      {
        task.setNote(progressStart + (x + (i - 0) * this.width) / (this.level - 0 + 2));
        int xx = x * w / this.width;
        int xx2 = x * w % this.width * 4096 / this.width;
        for (int y = 0; y < this.width; y++)
        {
          int yy = y * w / this.width;
          
          int yy2 = y * w % this.width * 4096 / this.width;
          
          this.noiseValues[x][y] += PerlinNoise.NoiseMap.access$100(noiseMap, xx, yy, xx2, yy2) * amplitude;
        }
      }
    }
    return this.noiseValues;
  }
  
  void setRandom(Random aRandom)
  {
    this.random = aRandom;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\meshgen\PerlinNoise.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */