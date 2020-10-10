package com.wurmonline.math;

public final class Matrix
{
  private float[] matrix = new float[16];
  
  public Matrix() {}
  
  public Matrix(Matrix matrix)
  {
    this.matrix = matrix.matrix;
  }
  
  public float[] inverseRotateVect(float[] pVect)
  {
    float[] vec = new float[3];
    
    vec[0] = (pVect[0] * this.matrix[0] + pVect[1] * this.matrix[1] + pVect[2] * this.matrix[2]);
    vec[1] = (pVect[0] * this.matrix[4] + pVect[1] * this.matrix[5] + pVect[2] * this.matrix[6]);
    vec[2] = (pVect[0] * this.matrix[8] + pVect[1] * this.matrix[9] + pVect[2] * this.matrix[10]);
    
    return vec;
  }
  
  public float[] inverseTranslateVect(float[] pVect)
  {
    pVect[0] -= this.matrix[12];
    pVect[1] -= this.matrix[13];
    pVect[2] -= this.matrix[14];
    
    return pVect;
  }
  
  public void postMultiply(Matrix m2)
  {
    float[] newMatrix = new float[16];
    
    newMatrix[0] = (this.matrix[0] * m2.matrix[0] + this.matrix[4] * m2.matrix[1] + this.matrix[8] * m2.matrix[2]);
    newMatrix[1] = (this.matrix[1] * m2.matrix[0] + this.matrix[5] * m2.matrix[1] + this.matrix[9] * m2.matrix[2]);
    newMatrix[2] = (this.matrix[2] * m2.matrix[0] + this.matrix[6] * m2.matrix[1] + this.matrix[10] * m2.matrix[2]);
    newMatrix[3] = 0.0F;
    
    newMatrix[4] = (this.matrix[0] * m2.matrix[4] + this.matrix[4] * m2.matrix[5] + this.matrix[8] * m2.matrix[6]);
    newMatrix[5] = (this.matrix[1] * m2.matrix[4] + this.matrix[5] * m2.matrix[5] + this.matrix[9] * m2.matrix[6]);
    newMatrix[6] = (this.matrix[2] * m2.matrix[4] + this.matrix[6] * m2.matrix[5] + this.matrix[10] * m2.matrix[6]);
    newMatrix[7] = 0.0F;
    
    newMatrix[8] = (this.matrix[0] * m2.matrix[8] + this.matrix[4] * m2.matrix[9] + this.matrix[8] * m2.matrix[10]);
    newMatrix[9] = (this.matrix[1] * m2.matrix[8] + this.matrix[5] * m2.matrix[9] + this.matrix[9] * m2.matrix[10]);
    newMatrix[10] = (this.matrix[2] * m2.matrix[8] + this.matrix[6] * m2.matrix[9] + this.matrix[10] * m2.matrix[10]);
    newMatrix[11] = 0.0F;
    
    newMatrix[12] = (this.matrix[0] * m2.matrix[12] + this.matrix[4] * m2.matrix[13] + this.matrix[8] * m2.matrix[14] + this.matrix[12]);
    newMatrix[13] = (this.matrix[1] * m2.matrix[12] + this.matrix[5] * m2.matrix[13] + this.matrix[9] * m2.matrix[14] + this.matrix[13]);
    newMatrix[14] = (this.matrix[2] * m2.matrix[12] + this.matrix[6] * m2.matrix[13] + this.matrix[10] * m2.matrix[14] + this.matrix[14]);
    newMatrix[15] = 1.0F;
    
    set(newMatrix);
  }
  
  public void postMultiplyFull(Matrix m2)
  {
    float[] newMatrix = new float[16];
    
    newMatrix[0] = (this.matrix[0] * m2.matrix[0] + this.matrix[4] * m2.matrix[1] + this.matrix[8] * m2.matrix[2] + this.matrix[12] * m2.matrix[3]);
    newMatrix[1] = (this.matrix[1] * m2.matrix[0] + this.matrix[5] * m2.matrix[1] + this.matrix[9] * m2.matrix[2] + this.matrix[13] * m2.matrix[3]);
    newMatrix[2] = (this.matrix[2] * m2.matrix[0] + this.matrix[6] * m2.matrix[1] + this.matrix[10] * m2.matrix[2] + this.matrix[14] * m2.matrix[3]);
    newMatrix[3] = (this.matrix[3] * m2.matrix[0] + this.matrix[7] * m2.matrix[1] + this.matrix[11] * m2.matrix[2] + this.matrix[15] * m2.matrix[3]);
    
    newMatrix[4] = (this.matrix[0] * m2.matrix[4] + this.matrix[4] * m2.matrix[5] + this.matrix[8] * m2.matrix[6] + this.matrix[12] * m2.matrix[7]);
    newMatrix[5] = (this.matrix[1] * m2.matrix[4] + this.matrix[5] * m2.matrix[5] + this.matrix[9] * m2.matrix[6] + this.matrix[13] * m2.matrix[7]);
    newMatrix[6] = (this.matrix[2] * m2.matrix[4] + this.matrix[6] * m2.matrix[5] + this.matrix[10] * m2.matrix[6] + this.matrix[14] * m2.matrix[7]);
    newMatrix[7] = (this.matrix[3] * m2.matrix[4] + this.matrix[7] * m2.matrix[5] + this.matrix[11] * m2.matrix[6] + this.matrix[15] * m2.matrix[7]);
    
    newMatrix[8] = (this.matrix[0] * m2.matrix[8] + this.matrix[4] * m2.matrix[9] + this.matrix[8] * m2.matrix[10] + this.matrix[12] * m2.matrix[11]);
    newMatrix[9] = (this.matrix[1] * m2.matrix[8] + this.matrix[5] * m2.matrix[9] + this.matrix[9] * m2.matrix[10] + this.matrix[13] * m2.matrix[11]);
    newMatrix[10] = (this.matrix[2] * m2.matrix[8] + this.matrix[6] * m2.matrix[9] + this.matrix[10] * m2.matrix[10] + this.matrix[14] * m2.matrix[11]);
    newMatrix[11] = (this.matrix[3] * m2.matrix[8] + this.matrix[7] * m2.matrix[9] + this.matrix[11] * m2.matrix[10] + this.matrix[15] * m2.matrix[11]);
    
    newMatrix[12] = (this.matrix[0] * m2.matrix[12] + this.matrix[4] * m2.matrix[13] + this.matrix[8] * m2.matrix[14] + this.matrix[12] * m2.matrix[15]);
    newMatrix[13] = (this.matrix[1] * m2.matrix[12] + this.matrix[5] * m2.matrix[13] + this.matrix[9] * m2.matrix[14] + this.matrix[13] * m2.matrix[15]);
    newMatrix[14] = (this.matrix[2] * m2.matrix[12] + this.matrix[6] * m2.matrix[13] + this.matrix[10] * m2.matrix[14] + this.matrix[14] * m2.matrix[15]);
    newMatrix[15] = (this.matrix[3] * m2.matrix[12] + this.matrix[7] * m2.matrix[13] + this.matrix[11] * m2.matrix[14] + this.matrix[15] * m2.matrix[15]);
    
    set(newMatrix);
  }
  
  public final Matrix setTranslation(float[] translation)
  {
    this.matrix[12] = translation[0];
    this.matrix[13] = translation[1];
    this.matrix[14] = translation[2];
    return this;
  }
  
  public final Matrix setTranslation(float x, float y, float z)
  {
    this.matrix[12] = x;
    this.matrix[13] = y;
    this.matrix[14] = z;
    return this;
  }
  
  public final Matrix setTranslation(Vector translation)
  {
    this.matrix[12] = translation.x();
    this.matrix[13] = translation.y();
    this.matrix[14] = translation.z();
    return this;
  }
  
  public void setInverseTranslation(float[] translation)
  {
    this.matrix[12] = (-translation[0]);
    this.matrix[13] = (-translation[1]);
    this.matrix[14] = (-translation[2]);
  }
  
  public void setRotationDegrees(float[] angles)
  {
    float[] vec = new float[3];
    vec[0] = ((float)(angles[0] * 180.0D / 3.141592653589793D));
    vec[1] = ((float)(angles[1] * 180.0D / 3.141592653589793D));
    vec[2] = ((float)(angles[2] * 180.0D / 3.141592653589793D));
    setRotationRadians(vec);
  }
  
  public void setInverseRotationDegrees(float[] angles)
  {
    float[] vec = new float[3];
    vec[0] = ((float)(angles[0] * 180.0D / 3.141592653589793D));
    vec[1] = ((float)(angles[1] * 180.0D / 3.141592653589793D));
    vec[2] = ((float)(angles[2] * 180.0D / 3.141592653589793D));
    setInverseRotationRadians(vec);
  }
  
  public void setRotationRadians(float[] angles)
  {
    float cr = (float)Math.cos(angles[0]);
    float sr = (float)Math.sin(angles[0]);
    float cp = (float)Math.cos(angles[1]);
    float sp = (float)Math.sin(angles[1]);
    float cy = (float)Math.cos(angles[2]);
    float sy = (float)Math.sin(angles[2]);
    
    this.matrix[0] = (cp * cy);
    this.matrix[1] = (cp * sy);
    this.matrix[2] = (-sp);
    
    float srsp = sr * sp;
    float crsp = cr * sp;
    
    this.matrix[4] = (srsp * cy - cr * sy);
    this.matrix[5] = (srsp * sy + cr * cy);
    this.matrix[6] = (sr * cp);
    
    this.matrix[8] = (crsp * cy + sr * sy);
    this.matrix[9] = (crsp * sy - sr * cy);
    this.matrix[10] = (cr * cp);
  }
  
  public void setInverseRotationRadians(float[] angles)
  {
    float cr = (float)Math.cos(angles[0]);
    float sr = (float)Math.sin(angles[0]);
    float cp = (float)Math.cos(angles[1]);
    float sp = (float)Math.sin(angles[1]);
    float cy = (float)Math.cos(angles[2]);
    float sy = (float)Math.sin(angles[2]);
    
    this.matrix[0] = (cp * cy);
    this.matrix[4] = (cp * sy);
    this.matrix[8] = (-sp);
    
    float srsp = sr * sp;
    float crsp = cr * sp;
    
    this.matrix[1] = (srsp * cy - cr * sy);
    this.matrix[5] = (srsp * sy + cr * cy);
    this.matrix[9] = (sr * cp);
    
    this.matrix[2] = (crsp * cy + sr * sy);
    this.matrix[6] = (crsp * sy - sr * cy);
    this.matrix[10] = (cr * cp);
  }
  
  public final Matrix setRotationQuaternion(Quaternion quaternion)
  {
    float[] quat = quaternion.getQuat();
    this.matrix[0] = ((float)(1.0D - 2.0D * quat[1] * quat[1] - 2.0D * quat[2] * quat[2]));
    this.matrix[1] = ((float)(2.0D * quat[0] * quat[1] + 2.0D * quat[3] * quat[2]));
    this.matrix[2] = ((float)(2.0D * quat[0] * quat[2] - 2.0D * quat[3] * quat[1]));
    
    this.matrix[4] = ((float)(2.0D * quat[0] * quat[1] - 2.0D * quat[3] * quat[2]));
    this.matrix[5] = ((float)(1.0D - 2.0D * quat[0] * quat[0] - 2.0D * quat[2] * quat[2]));
    this.matrix[6] = ((float)(2.0D * quat[1] * quat[2] + 2.0D * quat[3] * quat[0]));
    
    this.matrix[8] = ((float)(2.0D * quat[0] * quat[2] + 2.0D * quat[3] * quat[1]));
    this.matrix[9] = ((float)(2.0D * quat[1] * quat[2] - 2.0D * quat[3] * quat[0]));
    this.matrix[10] = ((float)(1.0D - 2.0D * quat[0] * quat[0] - 2.0D * quat[1] * quat[1]));
    
    this.matrix[3] = (this.matrix[7] = this.matrix[11] = this.matrix[12] = this.matrix[13] = this.matrix[14] = 0.0F);
    this.matrix[15] = 1.0F;
    
    return this;
  }
  
  public void set(float[] matrix)
  {
    this.matrix = matrix;
  }
  
  public float get(int i, int j)
  {
    return this.matrix[(4 * i + j)];
  }
  
  public void set(int i, int j, float val)
  {
    this.matrix[(4 * i + j)] = val;
  }
  
  public final Matrix loadIdentity()
  {
    for (int i = 0; i < 16; i++) {
      this.matrix[i] = 0.0F;
    }
    this.matrix[0] = (this.matrix[5] = this.matrix[10] = this.matrix[15] = 1.0F);
    
    return this;
  }
  
  public void setScale(float scalX, float scalY, float scalZ)
  {
    this.matrix[0] *= scalX;
    this.matrix[5] *= scalY;
    this.matrix[10] *= scalZ;
  }
  
  public Matrix inverse()
  {
    this.matrix[0] = (-this.matrix[0]);
    this.matrix[1] = (-this.matrix[1]);
    this.matrix[2] = (-this.matrix[2]);
    this.matrix[4] = (-this.matrix[4]);
    this.matrix[5] = (-this.matrix[5]);
    this.matrix[6] = (-this.matrix[6]);
    this.matrix[8] = (-this.matrix[8]);
    this.matrix[9] = (-this.matrix[9]);
    this.matrix[10] = (-this.matrix[10]);
    
    return this;
  }
  
  public final float[] getMatrix()
  {
    return this.matrix;
  }
  
  public final void setMatrix(float[] matrix)
  {
    this.matrix = matrix;
  }
  
  public final Matrix setAxisX(float x, float y, float z)
  {
    this.matrix[0] = x;
    this.matrix[1] = y;
    this.matrix[2] = z;
    return this;
  }
  
  public final Matrix setAxisY(float x, float y, float z)
  {
    this.matrix[4] = x;
    this.matrix[5] = y;
    this.matrix[6] = z;
    return this;
  }
  
  public final Matrix setAxisZ(float x, float y, float z)
  {
    this.matrix[8] = x;
    this.matrix[9] = y;
    this.matrix[10] = z;
    return this;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\math\Matrix.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */