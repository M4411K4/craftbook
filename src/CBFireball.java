


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
        
		this.bC = 0.0F;
		this.bm = this.bn = this.bo = 0.0D;
        
		this.b = Math.cos(Math.toRadians(rotation)) * speed;
		this.c = Math.sin(Math.toRadians(pitch)) * speed;
		this.d = Math.sin(Math.toRadians(rotation)) * speed;
	}
	
	@Override
	protected void a(OMovingObjectPosition paramOMovingObjectPosition)
	{
		if (!this.bf.I) {
	      if ((paramOMovingObjectPosition.g != null) && 
	        (paramOMovingObjectPosition.g.a(ODamageSource.a(this, this.a), 4)));
	      this.bf.a(null, this.bj, this.bk, this.bl, this.power, true);
	      S();
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
	public int c()
	{
		return 1;
	}
}
