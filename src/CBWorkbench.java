import java.util.List;


public class CBWorkbench extends OBlockWorkbench
{

	protected CBWorkbench(int arg0)
	{
		super(arg0);
		c(2.5F);
		a(OBlock.e);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean a(OWorld oworld, int x, int y, int z, OEntityPlayer eplayer)
	{
		boolean output = super.a(oworld, x, y, z, eplayer);
		
		if(!(eplayer.m instanceof OContainerWorkbench))
			return output;
		
		OContainerWorkbench ocontainerwb = (OContainerWorkbench)eplayer.m;
		
		@SuppressWarnings("rawtypes")
		List inventorySlots = ocontainerwb.e;
		
		if(inventorySlots != null)
		{
			for(int i = 0; i < inventorySlots.size(); i++)
			{
				if(inventorySlots.get(i) instanceof OSlotCrafting)
				{
					CBSlotCrafting cbslot = new CBSlotCrafting(eplayer, ocontainerwb.a, ocontainerwb.b, 0, 124, 35);
					inventorySlots.set(i, cbslot);
					
					return output;
				}
			}
		}
		
		return output;
	}
}
