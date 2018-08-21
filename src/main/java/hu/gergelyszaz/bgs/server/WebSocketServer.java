package hu.gergelyszaz.bgs.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.tyrus.server.Server;

import hu.gergelyszaz.bgs.manager.GameFactory;
import hu.gergelyszaz.bgs.manager.GameManager;
import hu.gergelyszaz.bgs.manager.ModelManager;

/**
 * @author Gergely Száz
 */
public class WebSocketServer {
	private static Logger logger = Logger.getLogger(WebSocketServer.class.getName());
	private static volatile boolean running = false;
	private static Server server = null;

	public static void stopServer() {
		if (running) {
			server.stop();
			server = null;
			running = false;
		}
	}

	public static void main(String[] args) throws Exception {
		logger.log(Level.INFO, "Starting server");

		int port = Configuration.getPort();
		String hostName = Configuration.getHostname();

		WebSocketServer.runServer(hostName, port, null);

		while (WebSocketServer.isRunning()) {
			BGSServer.pingClients();
			Thread.sleep(Configuration.getPingIntervalInSeconds() * 1000);
		}
	}

	public static boolean runServer(String hostName, int port, String rootpath) {
		running = true;
		try {
			BGSServer.gm = new GameManager(new GameFactory(), new ModelManager());
			server = new Server(hostName, port, rootpath, null, BGSServer.class);

			new Thread(BGSServer.gm).start();
			server.start();
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage());
			running = false;
		}
		logger.log(Level.INFO, "Server Started");
		return running;
	}

	public static boolean isRunning() {
		return running;
	}
}
