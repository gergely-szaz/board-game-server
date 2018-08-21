package hu.gergelyszaz.bgs.server;

/**
 * @author Gergely Száz
 */
public class Configuration {
	public static int getPingIntervalInSeconds() {
		return Integer.parseInt(getEnv("bgs.server.ping.interval", "60"));
	}
	
	public static String getHostname() {
		return getEnv("HOSTNAME","0.0.0.0");
	}
	
	public static int getPort() {
		return Integer.parseInt(getEnv("PORT","8025"));
	}
	
	public static String getEnv(String key, String defaultValue) {
		String env = System.getenv(key);
		return env != null ? env : defaultValue;
	}
}
