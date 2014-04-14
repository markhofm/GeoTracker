package at.jku.se.tracking;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.UUID;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import at.jku.se.tracking.database.DatabaseService;
import at.jku.se.tracking.database.UserObject;
import at.jku.se.tracking.messages.MsgLocationUpdate;
import at.jku.se.tracking.messages.MsgLogin;
import at.jku.se.tracking.messages.MsgLoginError;
import at.jku.se.tracking.messages.MsgLoginOk;
import at.jku.se.tracking.messages.MsgLogout;
import at.jku.se.tracking.messages.MsgRegister;
import at.jku.se.tracking.messages.MsgRegisterOk;
import at.jku.se.tracking.messages.MsgRegistrationError;
import at.jku.se.tracking.messages.serialization.AMessage;
import at.jku.se.tracking.messages.serialization.InvalidMessageException;
import at.jku.se.tracking.messages.serialization.MarshallingService;
import at.jku.se.tracking.utils.PasswordEncryptionService;

import com.json.exceptions.JSONParsingException;

@WebSocket
public class WebSocketSession {
	private RemoteEndpoint remote;
	private double userId = -1;
	private String sessionId = null;

	// ------------------------------------------------------------------------

	@OnWebSocketConnect
	public void onConnect(Session session) {
		System.out.println("WebSocket Opened");
		this.remote = session.getRemote();
	}

	// ------------------------------------------------------------------------

	@OnWebSocketMessage
	public void onMessage(String message) {
		// TODO: define messages for actions
		// 1) Register
		// 2) Authenticate
		// 3) List observable users
		// 4) Request observation
		// 5) Stop observation
		// 6) Show tracking sessions
		// ------------------------------
		// Store authenticated users remote session for push messages
		System.out.println("Message from Client: " + message);
		// --
		/*
		 * try { remote.sendString(message); } catch (IOException e) {
		 * e.printStackTrace(); }
		 */
		// --
		try {
			AMessage m = MarshallingService.fromJSON(message);
			switch (m.getType()) {
			case REGISTRATION:
				MsgRegister register = (MsgRegister) m;
				handleRegistration(register);
				break;
			case LOGIN:
				MsgLogin login = (MsgLogin) m;
				handleLogin(login);
				break;
			case LOGOUT:
				MsgLogout logout = (MsgLogout) m;
				handleLogout(logout);
				break;
			case LOCATION_UPDATE:
				MsgLocationUpdate locUpdate = (MsgLocationUpdate) m;
				handleLocationUpdate(locUpdate);
				break;
			default:
				break;
			}
		} catch (InvalidMessageException e) {
			e.printStackTrace();
			// for now do nothing
		} catch (JSONParsingException e) {
			e.printStackTrace();
			// for now do nothing
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// ------------------------------------------------------------------------

	@OnWebSocketClose
	public void onClose(int statusCode, String reason) {
		SessionObserver.unregisterSession(userId);
		System.out.println("WebSocket Closed. Code:" + statusCode);
	}

	// ------------------------------------------------------------------------

	public void sendMessage(AMessage message) throws IOException {
		remote.sendString(MarshallingService.toJSON(message));
	}

	// ------------------------------------------------------------------------

	private void handleRegistration(MsgRegister registration)
			throws IOException {
		try {
			UserObject user = DatabaseService.queryUser(registration
					.getUsername());
			if (user != null) {
				sendMessage(new MsgRegistrationError("user already exists"));
			} else {
				// Encrypt password
				byte[] salt = PasswordEncryptionService.generateSalt();
				byte[] encryptedPassword = PasswordEncryptionService
						.getEncryptedPassword(registration.getPassword(), salt);
				// Instantiate User Object
				user = new UserObject(registration.getUsername(),
						encryptedPassword, salt, registration.isObservable());
				// Store user
				boolean success = DatabaseService.insertUser(user);
				// TODO: send reply
				if (success) {
					sendMessage(new MsgRegisterOk());
				} else {
					sendMessage(new MsgRegistrationError(
							"failed to finish registration"));
				}
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sendMessage(new MsgRegistrationError(e));
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sendMessage(new MsgRegistrationError(e));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sendMessage(new MsgRegistrationError(e));
		}
	}

	private void handleLogin(MsgLogin login) throws IOException {
		try {
			if (DatabaseService.loginUser(login.getUsername(),
					login.getPassword())) {
				this.sessionId = UUID.randomUUID().toString();
				sendMessage(new MsgLoginOk(this.sessionId));
			} else {
				sendMessage(new MsgLoginError(
						"User/Password comination not found"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			sendMessage(new MsgLoginError(e));
		}

	}

	private void handleLogout(MsgLogout logout) throws IOException {
		this.sessionId = null;
	}
	
	private void handleLocationUpdate(MsgLocationUpdate location) throws IOException {
		//todo
	}
}