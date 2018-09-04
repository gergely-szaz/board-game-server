package hu.gergelyszaz.bgs.server;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hu.gergelyszaz.bgs.manager.GameManager;

@ServerEndpoint(value = "/game")
public class BGSServer {
	private static final String VIEW = "game";
	private static final String PARAMETER = "parameter";
	private static final String ACTION = "action";
	private static final String STATUS = "status";

	public static GameManager GAME_MANAGER;
	private static Logger log = Logger.getLogger(BGSServer.class.getName());

	private static Set<Session> sessions = new HashSet<>();

	@OnOpen
	public void onOpen(Session session) {
		sessions.add(session);
		log.info("Connected ... " + session.getId());
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		sessions.remove(session);

		log.info(String.format("Session %s closed because of %s", session.getId(), closeReason));
	}

	@OnMessage
	public String onMessage(String input, Session session) {
		try {
			log.info("Server received: " + input);

			JSONObject message = new JSONObject(input);
			String param = message.has(PARAMETER) ? message.getString(PARAMETER) : "";

			switch (message.getString(ACTION)) {

			case "join":
				return handleJoinAction(session, param);

			case "info":
				return handleInfoAction();

			case "select":
				return handleSelectAction(session, param);

			default:
				return new JSONObject().put(STATUS, "error").put("message", "Invalid action!").toString();
			}
		} catch (JSONException e) {
			log.warning(e.getMessage());
			return new JSONObject().put(STATUS, "error").put("message", "Invalid JSON message!").toString();
		} catch (Exception e) {
			log.severe(e.getMessage());
			return new JSONObject().put(STATUS, "error").put("message", "Internal Server Error").toString();
		}
	}

	private String handleSelectAction(Session session, String param) {

		ViewImpl view = (ViewImpl) session.getUserProperties().get(VIEW);
		int selected = Integer.parseInt(PARAMETER);

		if (view.select(selected)) {
			GAME_MANAGER.Wake();
			return new JSONObject().put(STATUS, "ok").toString();
		} else {
			return createErrorMessage("Internal Server Error");
		}
	}

	private String handleInfoAction() {

		JSONArray games = new JSONArray();
		
		for (String g : GAME_MANAGER.modelManager.AvailableModels()) {
			games.put(new JSONObject().put("name", g));
		}

		return new JSONObject().put(STATUS, "ok").put("games", games).toString();
	}

	private String handleJoinAction(Session session, String param) throws Exception {
		if (isAlreadyJoined(session)) {
			return createErrorMessage("Already joined a game");
		}

		JSONObject ret = new JSONObject();

		ViewImpl view = new ViewImpl(session);

		GAME_MANAGER.JoinGame(view, param);

		session.getUserProperties().put(VIEW, view);

		return ret.put(STATUS, "ok").put("message", "Joined").toString();
	}

	private String createErrorMessage(String message) {
		return new JSONObject().put(STATUS, "error").put("message", message).toString();
	}

	private boolean isAlreadyJoined(Session session) {
		return session.getUserProperties().containsKey(VIEW);
	}

	public static void pingClients() {
		for (Session session : sessions) {
			try {
				session.getAsyncRemote().sendPing(null);
			} catch (IllegalArgumentException | IOException e) {
				log.severe(e.getMessage());
			}
		}
	}
}
