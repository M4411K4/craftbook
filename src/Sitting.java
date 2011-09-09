
public class Sitting
{
	protected static boolean enabled = true;
	protected static boolean requireRightClickPermission = true;
	protected static boolean requiresChairFormats = false;
	
	protected static void sit(OEntityPlayerMP eplayer, World world, double x, double y, double z, float rotation, double offsety)
	{
		eplayer.aP = x;
		eplayer.aQ = y;
		eplayer.aR = z;
		eplayer.aV = rotation;
		
		OWorldServer oworld = world.getWorld();
		EntitySitting esitting = new EntitySitting(oworld, eplayer.aP, eplayer.aQ, eplayer.aR, offsety);
		oworld.b(esitting);
		eplayer.b(esitting);
	}
	
	protected static void stand(OEntityPlayerMP eplayer, double offsetx, double offsety, double offsetz)
	{
		if(!(eplayer.aK instanceof EntitySitting))
			return;
		
		OEntity nullEnt = null;
		eplayer.b(nullEnt);
		eplayer.a.a(eplayer.aP+offsetx, eplayer.aQ+offsety, eplayer.aR+offsetz, eplayer.aV, eplayer.aW);
	}
	
	protected static boolean isChair(Block block)
	{
		int data = block.getWorld().getBlockData(block.getX(), block.getY(), block.getZ());
		Block[] sides = ChairFormatUtil.getStairSideBlocks(block, data);
		if(sides == null)
			return false;
		
		return ChairFormatUtil.isChair(block, data, sides[0], sides[1]);
	}
}
