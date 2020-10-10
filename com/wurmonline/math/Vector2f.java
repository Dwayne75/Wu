package com.wurmonline.math;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.logging.Logger;

public final class Vector2f
  implements Externalizable, Cloneable
{
  private static final Logger logger = Logger.getLogger(Vector2f.class.getName());
  private static final long serialVersionUID = 1L;
  public float x;
  public float y;
  
  public Vector2f(float x, float y)
  {
    this.x = x;
    this.y = y;
  }
  
  public Vector2f()
  {
    this.x = (this.y = 0.0F);
  }
  
  public Vector2f(Vector2f vector2f)
  {
    this.x = vector2f.x;
    this.y = vector2f.y;
  }
  
  public Vector2f set(float x, float y)
  {
    this.x = x;
    this.y = y;
    return this;
  }
  
  public Vector2f set(Vector2f vec)
  {
    this.x = vec.x;
    this.y = vec.y;
    return this;
  }
  
  public Vector2f add(Vector2f vec)
  {
    if (null == vec)
    {
      logger.warning("Provided vector is null, null returned.");
      return null;
    }
    return new Vector2f(this.x + vec.x, this.y + vec.y);
  }
  
  public Vector2f add(float dx, float dy)
  {
    return new Vector2f(this.x + dx, this.y + dy);
  }
  
  public Vector2f addLocal(Vector2f vec)
  {
    if (null == vec)
    {
      logger.warning("Provided vector is null, null returned.");
      return null;
    }
    this.x += vec.x;
    this.y += vec.y;
    return this;
  }
  
  public Vector2f addLocal(float addX, float addY)
  {
    this.x += addX;
    this.y += addY;
    return this;
  }
  
  public Vector2f add(Vector2f vec, Vector2f result)
  {
    if (null == vec)
    {
      logger.warning("Provided vector is null, null returned.");
      return null;
    }
    if (result == null) {
      result = new Vector2f();
    }
    this.x += vec.x;
    this.y += vec.y;
    return result;
  }
  
  public float dot(Vector2f vec)
  {
    if (null == vec)
    {
      logger.warning("Provided vector is null, 0 returned.");
      return 0.0F;
    }
    return this.x * vec.x + this.y * vec.y;
  }
  
  public Vector3f cross(Vector2f v)
  {
    return new Vector3f(0.0F, 0.0F, determinant(v));
  }
  
  public float determinant(Vector2f v)
  {
    return this.x * v.y - this.y * v.x;
  }
  
  public void interpolate(Vector2f finalVec, float changeAmnt)
  {
    this.x = ((1.0F - changeAmnt) * this.x + changeAmnt * finalVec.x);
    this.y = ((1.0F - changeAmnt) * this.y + changeAmnt * finalVec.y);
  }
  
  public void interpolate(Vector2f beginVec, Vector2f finalVec, float changeAmnt)
  {
    this.x = ((1.0F - changeAmnt) * beginVec.x + changeAmnt * finalVec.x);
    this.y = ((1.0F - changeAmnt) * beginVec.y + changeAmnt * finalVec.y);
  }
  
  public static boolean isValidVector(Vector2f vector)
  {
    if (vector == null) {
      return false;
    }
    if ((Float.isNaN(vector.x)) || (Float.isNaN(vector.y))) {
      return false;
    }
    if ((Float.isInfinite(vector.x)) || (Float.isInfinite(vector.y))) {
      return false;
    }
    return true;
  }
  
  public float length()
  {
    return FastMath.sqrt(lengthSquared());
  }
  
  public float lengthSquared()
  {
    return this.x * this.x + this.y * this.y;
  }
  
  public float distanceSquared(Vector2f v)
  {
    double dx = this.x - v.x;
    double dy = this.y - v.y;
    return (float)(dx * dx + dy * dy);
  }
  
  public float distanceSquared(float otherX, float otherY)
  {
    double dx = this.x - otherX;
    double dy = this.y - otherY;
    return (float)(dx * dx + dy * dy);
  }
  
  public float distance(Vector2f v)
  {
    return FastMath.sqrt(distanceSquared(v));
  }
  
  public Vector2f mult(float scalar)
  {
    return new Vector2f(this.x * scalar, this.y * scalar);
  }
  
  public Vector2f multLocal(float scalar)
  {
    this.x *= scalar;
    this.y *= scalar;
    return this;
  }
  
  public Vector2f multLocal(Vector2f vec)
  {
    if (null == vec)
    {
      logger.warning("Provided vector is null, null returned.");
      return null;
    }
    this.x *= vec.x;
    this.y *= vec.y;
    return this;
  }
  
  public Vector2f mult(float scalar, Vector2f product)
  {
    if (null == product) {
      product = new Vector2f();
    }
    this.x *= scalar;
    this.y *= scalar;
    return product;
  }
  
  public Vector2f divide(float scalar)
  {
    return new Vector2f(this.x / scalar, this.y / scalar);
  }
  
  public Vector2f divideLocal(float scalar)
  {
    this.x /= scalar;
    this.y /= scalar;
    return this;
  }
  
  public Vector2f negate()
  {
    return new Vector2f(-this.x, -this.y);
  }
  
  public Vector2f negateLocal()
  {
    this.x = (-this.x);
    this.y = (-this.y);
    return this;
  }
  
  public Vector2f subtract(Vector2f vec)
  {
    return subtract(vec, null);
  }
  
  public Vector2f subtract(Vector2f vec, Vector2f store)
  {
    if (store == null) {
      store = new Vector2f();
    }
    this.x -= vec.x;
    this.y -= vec.y;
    return store;
  }
  
  public Vector2f subtract(float valX, float valY)
  {
    return new Vector2f(this.x - valX, this.y - valY);
  }
  
  public Vector2f subtractLocal(Vector2f vec)
  {
    if (null == vec)
    {
      logger.warning("Provided vector is null, null returned.");
      return null;
    }
    this.x -= vec.x;
    this.y -= vec.y;
    return this;
  }
  
  public Vector2f subtractLocal(float valX, float valY)
  {
    this.x -= valX;
    this.y -= valY;
    return this;
  }
  
  public Vector2f normalize()
  {
    float length = length();
    if (length != 0.0F) {
      return divide(length);
    }
    return divide(1.0F);
  }
  
  public Vector2f normalizeLocal()
  {
    float length = length();
    if (length != 0.0F) {
      return divideLocal(length);
    }
    return divideLocal(1.0F);
  }
  
  public float smallestAngleBetween(Vector2f otherVector)
  {
    float dotProduct = dot(otherVector);
    float angle = FastMath.acos(dotProduct);
    return angle;
  }
  
  public float angleBetween(Vector2f otherVector)
  {
    float angle = FastMath.atan2(otherVector.y, otherVector.x) - FastMath.atan2(this.y, this.x);
    return angle;
  }
  
  public float getX()
  {
    return this.x;
  }
  
  public void setX(float x)
  {
    this.x = x;
  }
  
  public float getY()
  {
    return this.y;
  }
  
  public void setY(float y)
  {
    this.y = y;
  }
  
  public float getAngle()
  {
    return -FastMath.atan2(this.y, this.x);
  }
  
  public void zero()
  {
    this.x = (this.y = 0.0F);
  }
  
  public int hashCode()
  {
    int hash = 37;
    hash += 37 * hash + Float.floatToIntBits(this.x);
    hash += 37 * hash + Float.floatToIntBits(this.y);
    return hash;
  }
  
  public Vector2f clone()
  {
    try
    {
      return (Vector2f)super.clone();
    }
    catch (CloneNotSupportedException e)
    {
      throw new AssertionError();
    }
  }
  
  public float[] toArray(float[] floats)
  {
    if (floats == null) {
      floats = new float[2];
    }
    floats[0] = this.x;
    floats[1] = this.y;
    return floats;
  }
  
  public boolean equals(Object o)
  {
    if (!(o instanceof Vector2f)) {
      return false;
    }
    if (this == o) {
      return true;
    }
    Vector2f comp = (Vector2f)o;
    if (Float.compare(this.x, comp.x) != 0) {
      return false;
    }
    if (Float.compare(this.y, comp.y) != 0) {
      return false;
    }
    return true;
  }
  
  public String toString()
  {
    return "(" + this.x + ", " + this.y + ')';
  }
  
  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException
  {
    this.x = in.readFloat();
    this.y = in.readFloat();
  }
  
  public void writeExternal(ObjectOutput out)
    throws IOException
  {
    out.writeFloat(this.x);
    out.writeFloat(this.y);
  }
  
  public Class<? extends Vector2f> getClassTag()
  {
    return getClass();
  }
  
  public void rotateAroundOrigin(float angle, boolean cw)
  {
    if (cw) {
      angle = -angle;
    }
    float newX = FastMath.cos(angle) * this.x - FastMath.sin(angle) * this.y;
    float newY = FastMath.sin(angle) * this.x + FastMath.cos(angle) * this.y;
    this.x = newX;
    this.y = newY;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\math\Vector2f.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */