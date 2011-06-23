
public class CBHookFunc implements PluginInterface
{
	public final static int GATE_IS_OPEN = 0;
	public final static int BRIDGE_IS_OPEN = 1;
	public final static int DOOR_IS_OPEN = 2;
	
	@Override
	public String checkParameters(Object[] args)
	{
		if(args == null || args.length < 1
		   || args[0] == null || !(args[0] instanceof Integer) )
			return "CBRequest - Invalid Parameters";
		return null;
	}

	@Override
	public String getName()
	{
		return "CBRequest";
	}

	@Override
	public int getNumParameters()
	{
		return 1;
	}

	@Override
	public Object run(Object[] args)
	{
		switch((Integer)args[0])
		{
			case GATE_IS_OPEN:
				if(args.length > 1 && args[1] != null && args[1] instanceof Sign)
					return GateSwitch.isOpen((Sign)args[1]);
				return null;
			case BRIDGE_IS_OPEN:
				if(args.length > 1 && args[1] != null && args[1] instanceof Sign)
					return Bridge.isOpen((Sign)args[1]);
				return null;
			case DOOR_IS_OPEN:
				if(args.length > 1 && args[1] != null && args[1] instanceof Sign)
					return Door.isOpen((Sign)args[1]);
				return null;
		}
		return null;
	}
}
