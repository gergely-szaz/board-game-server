package hu.gergelyszaz.bgs.server;

import javax.websocket.Session;

import com.google.gson.Gson;

import hu.gergelyszaz.bgs.state.GameState;
import hu.gergelyszaz.bgs.view.Controller;
import hu.gergelyszaz.bgs.view.View;

public class ViewImpl implements View {

	private Session session;
	private Controller controller;

	public ViewImpl(Session session) {
		this.session = session;
	}
	
	public void setController(Controller controller) {
		this.controller = controller;
	}

	@Override
	public void Refresh() {
		GameState currentState = controller.getCurrentState(getId());
		session.getAsyncRemote().sendText(new Gson().toJson(currentState));
	}

	@Override
	public String getId() {
		return session.getId();
	}

	public boolean select(int selected) {
		return controller.setSelected(getId(), selected);
	}

}
