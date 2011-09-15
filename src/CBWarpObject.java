
public class CBWarpObject
{
	public final Location LOCATION;
	public final String TITLE;
	public final String MESSAGE;
	public final String PASSWORD;
	
	public CBWarpObject(Location location, String title, String message)
	{
		this(location, title, message, null);
	}
	
	public CBWarpObject(Location location, String title, String message, String password)
	{
		LOCATION = location;
		title = convertString(title);
		if(title.length() > 40)
			title = title.substring(0, 40);
		TITLE = title;
		MESSAGE = convertString(message);
		PASSWORD = password;
	}
	
	private String convertString(String value)
	{
		if(value == null || value.isEmpty())
			return "";
		
		return value.replace("&#59;", ":");
	}
	
	private String convertToSafeString(String value)
	{
		if(value == null || value.isEmpty())
			return "";
		
		return value.replace(":", "&#59;");
	}
	
	public String[] getMessage()
	{
		if(MESSAGE == null || MESSAGE.isEmpty())
			return new String[]{""};
		
		return MESSAGE.split("<br>", 5);
	}
	
	public String toSaveString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(Util.locationToString(LOCATION)).append(":");
		
		if(PASSWORD != null && !PASSWORD.isEmpty())
			sb.append(PASSWORD).append(":");
		
		if(TITLE != null)
			sb.append(convertToSafeString(TITLE));
		sb.append(":");
		if(MESSAGE != null)
			sb.append(convertToSafeString(MESSAGE));
		
		return sb.toString();
	}
}
