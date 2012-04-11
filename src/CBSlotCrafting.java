import java.util.List;


public class CBSlotCrafting extends OSlotCrafting
{
	private final OIInventory craftMatrix;
	private OEntityPlayer thePlayer;
	
	public CBSlotCrafting(OEntityPlayer eplayer, OIInventory craftMatrix,
							OIInventory arg2, int arg3, int arg4, int arg5)
	{
		super(eplayer, craftMatrix, arg2, arg3, arg4, arg5);
		
		this.craftMatrix = craftMatrix;
		this.thePlayer = eplayer;
	}
	
	@Override
	public void c(OItemStack paramOItemStack)
	{
		if(!(this.craftMatrix instanceof OInventoryCrafting))
		{
			super.c(paramOItemStack);
			return;
		}
		
		OInventoryCrafting oinvcrafting = (OInventoryCrafting)this.craftMatrix;
		
		@SuppressWarnings("rawtypes")
		List recipes = OCraftingManager.a().b();
		
		CBEnchantRecipe cbrecipe = null;
		for(int i = 0; i < recipes.size(); i++)
		{
			OIRecipe oirecipe = (OIRecipe)recipes.get(i);
			if(oirecipe.a(oinvcrafting))
			{
				if(!(oirecipe instanceof CBEnchantRecipe))
				{
					super.c(paramOItemStack);
					return;
				}
				
				cbrecipe = (CBEnchantRecipe)oirecipe;
				break;
			}
		}
		
		if(cbrecipe == null)
		{
			super.c(paramOItemStack);
			return;
		}
		
		b(paramOItemStack);
		
		cbrecipe.decrStackSizes((OEntityPlayerMP)thePlayer, oinvcrafting);
		
		for(int i = 0; i < this.craftMatrix.c(); i++)
		{
			OItemStack oitemstack = this.craftMatrix.g_(i);
			
			if (oitemstack == null)
            {
                continue;
            }

            if (!oitemstack.a().k())
            {
                continue;
            }

            OItemStack oitemstack1 = new OItemStack(oitemstack.a().j());

            if (oitemstack.a().e(oitemstack) && thePlayer.k.a(oitemstack1))
            {
                continue;
            }

            if (craftMatrix.g_(i) == null)
            {
                craftMatrix.a(i, oitemstack1);
            }
            else
            {
                thePlayer.b(oitemstack1);
            }
		}
	}
}
