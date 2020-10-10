package com.wurmonline.mesh;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

public class Mesh
{
  public static final float MAX_NOISE = 200.0F;
  private final int width;
  private final int height;
  Node[][] nodes;
  private final int meshWidth;
  private final float textureScale;
  private final int heightMinusOne;
  private final int widthMinusOne;
  private boolean wrap = false;
  
  public Mesh(int width, int height, int meshWidth)
  {
    this.width = width;
    this.height = height;
    this.widthMinusOne = (width - 1);
    this.heightMinusOne = (height - 1);
    this.meshWidth = meshWidth;
    this.textureScale = 1.0F;
    
    this.nodes = new Node[width][height];
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < width; y++) {
        this.nodes[x][y] = new Node();
      }
    }
  }
  
  public float getTextureScale()
  {
    return this.textureScale;
  }
  
  public final int getMeshWidth()
  {
    return this.meshWidth;
  }
  
  public void generateMesh(InputStream in)
    throws IOException, ClassNotFoundException
  {
    ObjectInputStream ois = new ObjectInputStream(in);
    int[][] tileDatas = (int[][])ois.readObject();
    
    ois.close();
    for (int x = 0; x < this.width; x++) {
      for (int y = 0; y < this.width; y++)
      {
        this.nodes[x][y] = new Node();
        
        this.nodes[x][y].setHeight(Tiles.decodeHeightAsFloat(tileDatas[x][y]));
        this.nodes[x][y].setTexture(Tiles.decodeType(tileDatas[x][y]));
      }
    }
    processData();
  }
  
  public void generateEmpty(boolean createObjects)
  {
    for (int x = 0; x < this.width; x++) {
      for (int y = 0; y < this.width; y++)
      {
        if (createObjects) {
          this.nodes[x][y] = new Node();
        }
        this.nodes[x][y].setHeight(-100.0F);
        this.nodes[x][y].setTexture(Tiles.Tile.TILE_SAND.getId());
        if (createObjects) {
          this.nodes[x][y].setNormals(new float[3]);
        }
        this.nodes[x][y].normals[0] = 0.0F;
        this.nodes[x][y].normals[1] = 1.0F;
        this.nodes[x][y].normals[2] = 0.0F;
      }
    }
    processData();
  }
  
  public void processData()
  {
    processData(0, 0, this.width, this.height);
  }
  
  public void processData(int x1, int y1, int x2, int y2)
  {
    for (int x = x1; x < x2; x++) {
      for (int y = y1; y < y2; y++)
      {
        Node node = getNode(x, y);
        float b = node.getHeight();
        float t = node.getHeight();
        if (getNode(x + 1, y).getHeight() < b) {
          b = getNode(x + 1, y).getHeight();
        }
        if (getNode(x + 1, y).getHeight() > t) {
          t = getNode(x + 1, y).getHeight();
        }
        if (getNode(x + 1, y + 1).getHeight() < b) {
          b = getNode(x + 1, y + 1).getHeight();
        }
        if (getNode(x + 1, y + 1).getHeight() > t) {
          t = getNode(x + 1, y + 1).getHeight();
        }
        if (getNode(x, y + 1).getHeight() < b) {
          b = getNode(x, y + 1).getHeight();
        }
        if (getNode(x, y + 1).getHeight() > t) {
          t = getNode(x, y + 1).getHeight();
        }
        float h = t - b;
        
        node.setBbBottom(b);
        node.setBbHeight(h);
      }
    }
  }
  
  public void calculateNormals()
  {
    calculateNormals(0, 0, this.width, this.height);
  }
  
  public void calculateNormals(int x1, int y1, int x2, int y2)
  {
    for (int x = x1; x < x2; x++) {
      for (int y = y1; y < y2; y++)
      {
        Node n1 = getNode(x, y);
        
        float v1x = getMeshWidth();
        float v1y = getNode(x + 1, y).getHeight() - n1.getHeight();
        float v1z = 0.0F;
        
        float v2x = 0.0F;
        float v2y = getNode(x, y + 1).getHeight() - n1.getHeight();
        float v2z = getMeshWidth();
        
        float vx = v1y * v2z - v1z * v2y;
        float vy = v1z * v2x - v1x * v2z;
        float vz = v1x * v2y - v1y * v2x;
        
        v1x = -getMeshWidth();
        v1y = getNode(x - 1, y).getHeight() - n1.getHeight();
        v1z = 0.0F;
        
        v2x = 0.0F;
        v2y = getNode(x, y - 1).getHeight() - n1.getHeight();
        v2z = -getMeshWidth();
        
        vx += v1y * v2z - v1z * v2y;
        vy += v1z * v2x - v1x * v2z;
        vz += v1x * v2y - v1y * v2x;
        
        float dist = (float)Math.sqrt(vx * vx + vy * vy + vz * vz);
        vx /= dist;
        vy /= dist;
        vz /= dist;
        
        n1.normals[0] = (-vx);
        n1.normals[1] = (-vy);
        n1.normals[2] = (-vz);
      }
    }
  }
  
  public FloatBuffer createFloatBuffer(int size)
  {
    ByteBuffer temp = ByteBuffer.allocateDirect(4 * size);
    temp.order(ByteOrder.nativeOrder());
    
    return temp.asFloatBuffer();
  }
  
  public void setWraparound()
  {
    this.wrap = true;
  }
  
  public Node getNode(int x, int y)
  {
    if ((!this.wrap) && ((x < 0) || (y < 0) || (x >= this.width) || (y >= this.height))) {
      return this.nodes[0][0];
    }
    return this.nodes[(x & this.widthMinusOne)][(y & this.heightMinusOne)];
  }
  
  public void generateHills()
  {
    float[][] heights = new float[this.width][this.height];
    for (int x = 0; x < this.width; x++) {
      for (int y = 0; y < this.height; y++)
      {
        Node node = this.nodes[x][y];
        
        heights[x][y] = node.getHeight();
        
        boolean lower = heights[x][y] < -64.0F;
        if (heights[x][y] < 0.0F) {
          heights[x][y] *= 0.1F;
        }
        if (lower) {
          heights[x][y] = (heights[x][y] * 2.0F - 3.0F);
        }
      }
    }
    for (int x = 0; x < this.width; x++) {
      for (int y = 0; y < this.height; y++) {
        this.nodes[x][y].setHeight(heights[x][y]);
      }
    }
  }
  
  public void smooth(float bias)
  {
    float[][] heights = new float[this.width][this.height];
    for (int x = 0; x < this.width; x++) {
      for (int y = 0; y < this.height; y++)
      {
        Node node = this.nodes[x][y];
        
        float surroundingHeight = 0.0F;
        surroundingHeight += getNode(x - 1, y).getHeight();
        surroundingHeight += getNode(x + 1, y).getHeight();
        surroundingHeight += getNode(x, y - 1).getHeight();
        surroundingHeight += getNode(x, y + 1).getHeight();
        
        heights[x][y] = (node.getHeight() * bias + surroundingHeight / 4.0F * (1.0F - bias));
      }
    }
    for (int x = 0; x < this.width; x++) {
      for (int y = 0; y < this.height; y++) {
        this.nodes[x][y].setHeight(heights[x][y]);
      }
    }
  }
  
  public void noise(Random random, float noiseLevel)
  {
    for (int x = 0; x < this.width; x++) {
      for (int y = 0; y < this.height; y++) {
        this.nodes[x][y].setHeight(this.nodes[x][y].getHeight() + (random.nextFloat() - 0.5F) * noiseLevel);
      }
    }
  }
  
  public int getHeight()
  {
    return this.height;
  }
  
  public int getWidth()
  {
    return this.width;
  }
  
  public boolean isTransition(float xp, float yp)
  {
    while (xp < 0.0F) {
      xp += getWidth() * getMeshWidth();
    }
    while (yp < 0.0F) {
      yp += getHeight() * getMeshWidth();
    }
    int xx = (int)(xp / getMeshWidth());
    int yy = (int)(yp / getMeshWidth());
    
    return getNode(xx, yy).getTexture() == Tiles.Tile.TILE_HOLE.getId();
  }
  
  public float getHeight(float xp, float yp)
  {
    while (xp < 0.0F) {
      xp += getWidth() * getMeshWidth();
    }
    while (yp < 0.0F) {
      yp += getHeight() * getMeshWidth();
    }
    int xx = (int)(xp / getMeshWidth());
    int yy = (int)(yp / getMeshWidth());
    if (getNode(xx, yy).getTexture() == Tiles.Tile.TILE_HOLE.getId()) {
      return 1.0F;
    }
    float xa = xp / getMeshWidth() - xx;
    float ya = yp / getMeshWidth() - yy;
    if (xa < 0.0F) {
      xa = -xa;
    }
    if (ya < 0.0F) {
      ya = -ya;
    }
    float height = 0.0F;
    if (xa > ya)
    {
      xa -= ya;
      xa /= (1.0F - ya);
      float xheight1 = getNode(xx, yy).getHeight() * (1.0F - xa) + getNode(xx + 1, yy).getHeight() * xa;
      
      float xheight2 = getNode(xx + 1, yy + 1).getHeight();
      height = xheight1 * (1.0F - ya) + xheight2 * ya;
    }
    else
    {
      if (ya <= 0.001F) {
        ya = 0.001F;
      }
      xa /= ya;
      
      float xheight1 = getNode(xx, yy).getHeight();
      float xheight2 = getNode(xx, yy + 1).getHeight() * (1.0F - xa) + getNode(xx + 1, yy + 1).getHeight() * xa;
      
      height = xheight1 * (1.0F - ya) + xheight2 * ya;
    }
    return height;
  }
  
  public boolean setTiles(int xStart, int yStart, int w, int h, int[][] tiles)
  {
    boolean changed = false;
    for (int x = 0; x < w; x++) {
      for (int y = 0; y < h; y++)
      {
        Node node = getNode(x + xStart, y + yStart);
        node.setHeight(Tiles.decodeHeightAsFloat(tiles[x][y]));
        
        short t = (short)Tiles.decodeType(tiles[x][y]);
        short d = (short)Tiles.decodeData(tiles[x][y]);
        if ((t != node.getTexture()) || (d != node.data))
        {
          node.setTexture((byte)t);
          node.setData((byte)d);
          changed = true;
        }
      }
    }
    return changed;
  }
  
  public void refresh(int xStart, int yStart, int w, int h)
  {
    calculateNormals(xStart - 1, yStart - 1, xStart + w + 2, yStart + h + 2);
    processData(xStart - 1, yStart - 1, xStart + w + 2, yStart + h + 2);
  }
  
  public void reset()
  {
    generateEmpty(false);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\mesh\Mesh.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */