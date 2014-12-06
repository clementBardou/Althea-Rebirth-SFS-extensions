package sfs2x.extensions.games.spacewar.reqhandlers;

import sfs2x.extensions.games.spacewar.SpaceWarRoomExtension;
import sfs2x.extensions.games.spacewar.core.Game;

import com.smartfoxserver.v2.annotations.Instantiation;
import com.smartfoxserver.v2.annotations.MultiHandler;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

@Instantiation
@MultiHandler
public class ControlRequestHandler extends BaseClientRequestHandler {
	private static final String REQ_ROTATE = "rotate";
	private static final String REQ_THRUST = "thrust";
	private static final String REQ_FIRE = "fire";

	public void handleClientRequest(User sender, ISFSObject params) {
		String requestId = params.getUtfString("__[[REQUEST_ID]]__");

		Game game = ((SpaceWarRoomExtension) getParentExtension()).getGame();
		if (requestId.equals(REQ_ROTATE)) {
			game.rotateStarship(sender.getId(), params.getInt("dir").intValue());
		} else if (requestId.equals(REQ_THRUST)) {
			game.thrustStarship(sender.getId(), params.getBool("go")
					.booleanValue());
		} else if (requestId.equals(REQ_FIRE)) {
			int weaponNum = params.getInt("wnum").intValue();
			((SpaceWarRoomExtension) getParentExtension()).fireWeapon(sender,
					weaponNum);
		}
	}
}
