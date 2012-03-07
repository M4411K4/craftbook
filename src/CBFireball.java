


public class CBFireball extends OEntityFireball
{
	private float power = 1.0F;
	
	public CBFireball(OWorld oworld, double x, double y, double z, float rotation, float pitch, float power, double speed)
	{
		super(oworld);
		
		this.power = power;
		
		this.a = new EntityCreatureX(oworld);
        
		b(1.0F, 1.0F);
        
		this.c(x, y, z, 0F, 0F);
		this.c(x, y, z);
        
		this.bF = 0.0F;
		this.bp = this.bq = this.br = 0.0D;
        
		this.b = Math.cos(Math.toRadians(rotation)) * speed;
		this.c = Math.sin(Math.toRadians(pitch)) * speed;
		this.d = Math.sin(Math.toRadians(rotation)) * speed;
	}
	
	@Override
	protected void a(OMovingObjectPosition paramOMovingObjectPosition)
	{
		if (!this.bi.F) {
	      if ((paramOMovingObjectPosition.g != null) && 
	        (paramOMovingObjectPosition.g.a(ODamageSource.a(this, this.a), 4)));
	      this.bi.a(null, this.bm, this.bn, this.bo, this.power, true);
	      W();
	    }
	}
}

class EntityCreatureX extends OEntityCreature
{
	public EntityCreatureX(OWorld arg0)
	{
		super(arg0);
	}

	@Override
	public int d()
	{
		return 1;
	}
}
