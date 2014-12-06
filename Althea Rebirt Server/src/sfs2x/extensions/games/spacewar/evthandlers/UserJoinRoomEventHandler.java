package sfs2x.extensions.games.spacewar.evthandlers;

import sfs2x.extensions.games.spacewar.SpaceWarRoomExtension;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

public class UserJoinRoomEventHandler extends BaseServerEventHandler
{
	public void handleServerEvent(ISFSEvent event) throws SFSException
	{
		User user = (User) event.getParameter(SFSEventParam.USER);

		((SpaceWarRoomExtension) getParentExtension()).addStarship(user);
	}
}