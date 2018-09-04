package hu.gergelyszaz.bgs.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.DeploymentException;

import org.glassfish.tyrus.server.Server;

import hu.gergelyszaz.bgs.manager.GameFactory;
import hu.gergelyszaz.bgs.manager.GameManager;
import hu.gergelyszaz.bgs.manager.ModelManager;

/**
 * @author Gergely Száz
 */
public class WebSocketServer {
	private static Logger log = Logger.getLogger(WebSocketServer.class.getName());
	private static volatile boolean running = false;
	private static Server server = null;

	public static void stopServer() {
		if (running) {
			server.stop();
			running = false;
		}
	}

	public static void main(String[] args) throws Exception {
		log.log(Level.INFO, "Starting server");

		int port = Configuration.getPort();
		String hostName = Configuration.getHostname();

		WebSocketServer.startServer(hostName, port, null);
		log.log(Level.INFO, "Server Started");

		while (WebSocketServer.isRunning()) {
			BGSServer.pingClients();
			Thread.sleep(Configuration.getPingIntervalInSeconds() * 1000);
		}
	}

	public static void startServer(String hostName, int port, String rootpath) throws DeploymentException {
		BGSServer.GAME_MANAGER = new GameManager(new GameFactory(), new ModelManager());
		server = new Server(hostName, port, rootpath, null, BGSServer.class);
		new Thread(BGSServer.GAME_MANAGER).start();
		server.start();
		running = true;
	}

	public static boolean isRunning() {
		return running;
	}
}
