/**
 * From iConomy
 */
public class CBHooked
{
  public static void silent(String listener, Object[] args)
  {
    etc.getLoader().callCustomHook(listener, args);
  }

  public static Object call(String listener, Object[] args)
  {
    return etc.getLoader().callCustomHook(listener, args);
  }

  public static int getInt(String listener, Object[] args)
  {
    Object result = call(listener, args);
    return ((Integer)result).intValue();
  }

  public static String getString(String listener, Object[] args)
  {
    Object result = call(listener, args);
    return (String)result;
  }

  public static boolean getBoolean(String listener, Object[] args)
  {
    Object result = call(listener, args);
    return ((Boolean)result).booleanValue();
  }

  public static double getDouble(String listener, Object[] args)
  {
    Object result = call(listener, args);
    return ((Double)result).doubleValue();
  }

  public static long getLong(String listener, Object[] args)
  {
    Object result = call(listener, args);
    return ((Long)result).longValue();
  }
}
