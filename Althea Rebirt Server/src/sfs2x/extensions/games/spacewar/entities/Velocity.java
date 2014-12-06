/*  1:   */ package sfs2x.extensions.games.spacewar.entities;
/*  2:   */ 
/*  3:   */ public class Velocity
/*  4:   */ {
/*  5: 8 */   public double vx = 0.0D;
/*  6: 9 */   public double vy = 0.0D;
/*  7:   */   
/*  8:   */   public Velocity(double x, double y)
/*  9:   */   {
/* 10:13 */     this.vx = x;
/* 11:14 */     this.vy = y;
/* 12:   */   }
/* 13:   */   
/* 14:   */   public double getSpeed()
/* 15:   */   {
/* 16:19 */     return Math.sqrt(Math.pow(this.vx, 2.0D) + Math.pow(this.vy, 2.0D));
/* 17:   */   }
/* 18:   */    
/* 19:   */   public double getDirection()
/* 20:   */   {
/* 21:24 */     return Math.atan2(this.vy, this.vx);
/* 22:   */   }
/* 23:   */   
/* 24:   */   public void limitSpeed(double maxSpeed)
/* 25:   */   {
/* 26:29 */     if (getSpeed() > maxSpeed)
/* 27:   */     {
/* 28:31 */       double dir = getDirection();
/* 29:   */       
/* 30:33 */       this.vx = (Math.cos(dir) * maxSpeed);
/* 31:34 */       this.vy = (Math.sin(dir) * maxSpeed);
/* 32:   */     }
/* 33:   */   }
/* 34:   */   
/* 35:   */   public String toComponentsString()
/* 36:   */   {
/* 37:40 */     return "(" + this.vx + "," + this.vy + ")";
/* 38:   */   }
/* 39:   */   
/* 40:   */   public String toVectorString()
/* 41:   */   {
/* 42:45 */     return "[" + getSpeed() + "," + getDirection() + " rad]";
/* 43:   */   }
/* 44:   */ }


/* Location:           C:\Users\Clément\SmartFoxServer_2X\SFS2X\extensions\SpaceWar\SpaceWarExtension.jar
 * Qualified Name:     sfs2x.extensions.games.spacewar.entities.Velocity
 * JD-Core Version:    0.7.0.1
 */