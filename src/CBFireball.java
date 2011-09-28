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
		
		this.b = new EntityCreatureX(oworld);
        
		b(1.0F, 1.0F);
        
		this.c(x, y, z, 0F, 0F);
		this.c(x, y, z);
        
		this.by = 0.0F;
		this.bi = this.bj = this.bk = 0.0D;
        
		this.c = Math.cos(Math.toRadians(rotation)) * speed;
		this.d = Math.sin(Math.toRadians(pitch)) * speed;
		this.e = Math.sin(Math.toRadians(rotation)) * speed;
	}
	
	@Override
	public void s_()
	{
		aa();
		this.bO = 10;

	    if (this.a > 0) this.a -= 1;

	    if (this.inGround)
	    {
	      int m = this.bb.a(this.xTile, this.yTile, this.zTile);
	      if (m != this.inTile) {
	        this.inGround = false;

	        this.bi *= this.bL.nextFloat() * 0.2F;
	        this.bj *= this.bL.nextFloat() * 0.2F;
	        this.bk *= this.bL.nextFloat() * 0.2F;
	        this.ticksAlive = 0;
	        this.ticksInAir = 0;
	      } else {
	        this.ticksAlive += 1;
	        if (this.ticksAlive == 1200) N();
	        return;
	      }
	    } else {
	      this.ticksInAir += 1;
	    }
	    
	    OVec3D localOVec3D1 = OVec3D.b(this.bf, this.bg, this.bh);
	    OVec3D localOVec3D2 = OVec3D.b(this.bf + this.bi, this.bg + this.bj, this.bh + this.bk);
	    OMovingObjectPosition localOMovingObjectPosition1 = this.bb.a(localOVec3D1, localOVec3D2);

	    localOVec3D1 = OVec3D.b(this.bf, this.bg, this.bh);
	    localOVec3D2 = OVec3D.b(this.bf + this.bi, this.bg + this.bj, this.bh + this.bk);
	    if (localOMovingObjectPosition1 != null) {
	      localOVec3D2 = OVec3D.b(localOMovingObjectPosition1.f.a, localOMovingObjectPosition1.f.b, localOMovingObjectPosition1.f.c);
	    }
	    OEntity oentity = null;
	    @SuppressWarnings("rawtypes")
	    List localList = this.bb.b(this, this.bp.a(this.bi, this.bj, this.bk).b(1.0D, 1.0D, 1.0D));
	    double d1 = 0.0D;
	    for (int n = 0; n < localList.size(); n++) {
	      OEntity localOEntity = (OEntity)localList.get(n);
	      if ((!localOEntity.r_()) || ((localOEntity == this.b) && (this.ticksInAir < 25)))
	        continue;
	      float f3 = 0.3F;
	      OAxisAlignedBB localOAxisAlignedBB = localOEntity.bp.b(f3, f3, f3);
	      OMovingObjectPosition localOMovingObjectPosition2 = localOAxisAlignedBB.a(localOVec3D1, localOVec3D2);
	      if (localOMovingObjectPosition2 != null) {
	        double d2 = localOVec3D1.b(localOMovingObjectPosition2.f);
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
	      if (!this.bb.I) {
	        if ((localOMovingObjectPosition1.g != null) && 
	          (localOMovingObjectPosition1.g.a(ODamageSource.a(this, this.b), 0)));
	        this.bb.a(null, this.bf, this.bg, this.bh, this.power, true);
	      }
	      N();
	    }
	    this.bf += this.bi;
	    this.bg += this.bj;
	    this.bh += this.bk;

	    float f1 = OMathHelper.a(this.bi * this.bi + this.bk * this.bk);
	    this.bl = (float)(Math.atan2(this.bi, this.bk) * 180.0D / 3.141592741012573D);
	    this.bm = (float)(Math.atan2(this.bj, f1) * 180.0D / 3.141592741012573D);

	    while (this.bm - this.bo < -180.0F)
	      this.bo -= 360.0F;
	    while (this.bm - this.bo >= 180.0F) {
	      this.bo += 360.0F;
	    }
	    while (this.bl - this.bn < -180.0F)
	      this.bn -= 360.0F;
	    while (this.bl - this.bn >= 180.0F) {
	      this.bn += 360.0F;
	    }
	    this.bm = (this.bo + (this.bm - this.bo) * 0.2F);
	    this.bl = (this.bn + (this.bl - this.bn) * 0.2F);

	    float f2 = 0.95F;
	    if (ao()) {
	      for (int i1 = 0; i1 < 4; i1++) {
	        float f4 = 0.25F;
	        this.bb.a("bubble", this.bf - this.bi * f4, this.bg - this.bj * f4, this.bh - this.bk * f4, this.bi, this.bj, this.bk);
	      }
	      f2 = 0.8F;
	    }

	    this.bi += this.c;
	    this.bj += this.d;
	    this.bk += this.e;
	    this.bi *= f2;
	    this.bj *= f2;
	    this.bk *= f2;

	    this.bb.a("smoke", this.bf, this.bg + 0.5D, this.bh, 0.0D, 0.0D, 0.0D);

	    c(this.bf, this.bg, this.bh);
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

class EntityCreatureX extends OEntityCreature
{
	public EntityCreatureX(OWorld arg0)
	{
		super(arg0);
	}
}
