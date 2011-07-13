import java.util.List;


public class CBFireball extends OEntityFireball
{
	private float power = 1.0F;
	private int xTile = -1;
	private int yTile = -1;
	private int zTile = -1;
	private int inTile = 0;
	private boolean inGround = false;
	private int ticksAlive = 0;
	private int ticksInAir = 0;
	
	public CBFireball(OWorld oworld, double x, double y, double z, float rotation, float pitch, float power, double speed)
	{
		super(oworld);
		
		this.power = power;
		
		this.b = new OEntityCreature(oworld);
        
		this.bj = 1.0F;
		this.bk = 1.0F;
        
		this.c(x, y, z, 0F, 0F);
		this.c(x, y, z);
        
		this.bi = 0.0F;
		this.aS = this.aT = this.aU = 0.0D;
        
		this.c = Math.cos(Math.toRadians(rotation)) * speed;
		this.d = Math.sin(Math.toRadians(pitch)) * speed;
		this.e = Math.sin(Math.toRadians(rotation)) * speed;
	}
	
	@Override
	public void m_()
	{
		R();
		this.by = 10;

	    if (this.a > 0) this.a -= 1;

	    if (this.inGround)
	    {
	      int m = this.aL.a(this.xTile, this.yTile, this.zTile);
	      if (m != this.inTile) {
	        this.inGround = false;

	        this.aS *= this.bv.nextFloat() * 0.2F;
	        this.aT *= this.bv.nextFloat() * 0.2F;
	        this.aU *= this.bv.nextFloat() * 0.2F;
	        this.ticksAlive = 0;
	        this.ticksInAir = 0;
	      } else {
	        this.ticksAlive += 1;
	        if (this.ticksAlive == 1200) J();
	        return;
	      }
	    } else {
	      this.ticksInAir += 1;
	    }

	    OVec3D localOVec3D1 = OVec3D.b(this.aP, this.aQ, this.aR);
	    OVec3D localOVec3D2 = OVec3D.b(this.aP + this.aS, this.aQ + this.aT, this.aR + this.aU);
	    OMovingObjectPosition localOMovingObjectPosition1 = this.aL.a(localOVec3D1, localOVec3D2);

	    localOVec3D1 = OVec3D.b(this.aP, this.aQ, this.aR);
	    localOVec3D2 = OVec3D.b(this.aP + this.aS, this.aQ + this.aT, this.aR + this.aU);
	    if (localOMovingObjectPosition1 != null) {
	      localOVec3D2 = OVec3D.b(localOMovingObjectPosition1.f.a, localOMovingObjectPosition1.f.b, localOMovingObjectPosition1.f.c);
	    }
	    OEntity oentity = null;
	    @SuppressWarnings("rawtypes")
		List localList = this.aL.b(this, this.aZ.a(this.aS, this.aT, this.aU).b(1.0D, 1.0D, 1.0D));
	    double d1 = 0.0D;
	    for (int n = 0; n < localList.size(); n++) {
	      OEntity localOEntity = (OEntity)localList.get(n);
	      if ((!localOEntity.l_()) || ((localOEntity == this.b) && (this.ticksInAir < 25)))
	        continue;
	      float f3 = 0.3F;
	      OAxisAlignedBB localOAxisAlignedBB = localOEntity.aZ.b(f3, f3, f3);
	      OMovingObjectPosition localOMovingObjectPosition2 = localOAxisAlignedBB.a(localOVec3D1, localOVec3D2);
	      if (localOMovingObjectPosition2 != null) {
	        double d2 = localOVec3D1.a(localOMovingObjectPosition2.f);
	        if ((d2 < d1) || (d1 == 0.0D)) {
	        	oentity = localOEntity;
	          d1 = d2;
	        }
	      }
	    }

	    if (oentity != null) {
	      localOMovingObjectPosition1 = new OMovingObjectPosition(oentity);
	    }

	    if (localOMovingObjectPosition1 != null) {
	      if (!this.aL.B) {
	        if ((localOMovingObjectPosition1.g != null) && 
	          (localOMovingObjectPosition1.g.a(this.b, 0)));
	        this.aL.a(null, this.aP, this.aQ, this.aR, this.power, true);
	      }
	      J();
	    }
	    this.aP += this.aS;
	    this.aQ += this.aT;
	    this.aR += this.aU;

	    float f1 = OMathHelper.a(this.aS * this.aS + this.aU * this.aU);
	    this.aV = (float)(Math.atan2(this.aS, this.aU) * 180.0D / 3.141592741012573D);
	    this.aW = (float)(Math.atan2(this.aT, f1) * 180.0D / 3.141592741012573D);

	    while (this.aW - this.aY < -180.0F)
	      this.aY -= 360.0F;
	    while (this.aW - this.aY >= 180.0F) {
	      this.aY += 360.0F;
	    }
	    while (this.aV - this.aX < -180.0F)
	      this.aX -= 360.0F;
	    while (this.aV - this.aX >= 180.0F) {
	      this.aX += 360.0F;
	    }
	    this.aW = (this.aY + (this.aW - this.aY) * 0.2F);
	    this.aV = (this.aX + (this.aV - this.aX) * 0.2F);

	    float f2 = 0.95F;
	    if (ad()) {
	      for (int i1 = 0; i1 < 4; i1++) {
	        float f4 = 0.25F;
	        this.aL.a("bubble", this.aP - this.aS * f4, this.aQ - this.aT * f4, this.aR - this.aU * f4, this.aS, this.aT, this.aU);
	      }
	      f2 = 0.8F;
	    }

	    this.aS += this.c;
	    this.aT += this.d;
	    this.aU += this.e;
	    this.aS *= f2;
	    this.aT *= f2;
	    this.aU *= f2;

	    this.aL.a("smoke", this.aP, this.aQ + 0.5D, this.aR, 0.0D, 0.0D, 0.0D);

	    c(this.aP, this.aQ, this.aR);
	}
	
	@Override
	public void b(ONBTTagCompound paramONBTTagCompound)
	{
		super.b(paramONBTTagCompound);
		paramONBTTagCompound.a("xTile", (short)this.xTile);
		paramONBTTagCompound.a("yTile", (short)this.yTile);
		paramONBTTagCompound.a("zTile", (short)this.zTile);
		paramONBTTagCompound.a("inTile", (byte)this.inTile);
		paramONBTTagCompound.a("inGround", (byte)(this.inGround ? 1 : 0));
	}
	
	@Override
	public void a(ONBTTagCompound paramONBTTagCompound)
	{
		super.a(paramONBTTagCompound);
		this.xTile = paramONBTTagCompound.d("xTile");
		this.yTile = paramONBTTagCompound.d("yTile");
		this.zTile = paramONBTTagCompound.d("zTile");
		this.inTile = (paramONBTTagCompound.c("inTile") & 0xFF);
		this.inGround = (paramONBTTagCompound.c("inGround") == 1);
	}
}
