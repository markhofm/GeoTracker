package at.jku.se.tracking.messages;

import java.util.Map;

import at.jku.se.tracking.messages.serialization.AMessage;

public class MsgLogout extends AMessage {

	private static final String FIELD_SESSION_ID = "session-id";

	// ------------------------------------------------------------------------

	public MsgLogout(Map<?, ?> map) {
		setMap(map);
	}
	public MsgLogout(String session_id) {
		setType(MessageType.LOGOUT);
		setValue(FIELD_SESSION_ID, session_id);
	}

	// ------------------------------------------------------------------------

	public String getSessionId() {
		return (String) getValue(FIELD_SESSION_ID);
	}
}
