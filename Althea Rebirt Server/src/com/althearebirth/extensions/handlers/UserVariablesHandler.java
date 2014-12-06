package com.althearebirth.extensions.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.althearebirth.extensions.AltheaRebirthExtension;
import com.smartfoxserver.v2.api.ISFSMMOApi;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import com.smartfoxserver.v2.mmo.Vec3D;

public class UserVariablesHandler extends BaseServerEventHandler {

	/**
	 * SFS MMO Api
	 */
	private ISFSMMOApi mmoApi;
	/**
	 * Althea Rebirth Extension
	 */
	private AltheaRebirthExtension extension;
	
	private static final float MAX_POSITION_CHANGE = 10;
	
	public UserVariablesHandler(ISFSMMOApi mmoApi, AltheaRebirthExtension extension) {
		this.mmoApi = mmoApi;
		this.extension = extension;
	}
	
	@Override
	public void handleServerEvent(ISFSEvent event) throws SFSException {
		
		@SuppressWarnings("unchecked")
		final List<UserVariable> variables = (List<UserVariable>) event.getParameter(SFSEventParam.VARIABLES);
		final User user = (User) event.getParameter(SFSEventParam.USER);
		
		// Make a map of the variables list
		Map<String, UserVariable> varMap = new HashMap<String, UserVariable>();
		for (UserVariable var : variables) {
			varMap.put(var.getName(), var);
		}
		
		if (varMap.containsKey("x") && varMap.containsKey("y") && varMap.containsKey("z")) {
			
			final Vec3D posCurrent = new Vec3D
			(
				user.getVariable("x").getDoubleValue().floatValue(), 
				user.getVariable("y").getDoubleValue().floatValue(), 
				user.getVariable("z").getDoubleValue().floatValue()
			);
			
			final Vec3D posRequested = new Vec3D
			(
				varMap.get("x").getDoubleValue().floatValue(),
				varMap.get("y").getDoubleValue().floatValue(),
				varMap.get("z").getDoubleValue().floatValue()
			);
			
			mmoApi.setUserPosition(user, posRequested, extension.getParentRoom());
//			if (checkUpdatedPos(posCurrent, posRequested)) {
//				extension.trace("[USER MOVE] -- HandleServerEvent.checkUpdatedPos() -- USER : " + user.getName());
//				mmoApi.setUserPosition(user, posRequested, extension.getParentRoom());
//			} else {
//				extension.trace("[CHEATING] -- HandleServerEvent.checkUpdatedPos() -- USER : " + user.getName());
//			}
//			
		}

	}
	
	/**
	 * This return {@link Boolean}.<code>true</code> if the posRequester is not too far than the posCurrent otherwise it return {@link Boolean}.<code>false</code>.
	 * @param posCurrent
	 * @param posRequested
	 * @return {@link Boolean}
	 */
	private Boolean checkUpdatedPos(Vec3D posCurrent, Vec3D posRequested) {
		
		// X Change
		final Integer xChange = Math.abs(posRequested.intX() - posCurrent.intX());
		// Y Change
		final Integer yChange = Math.abs(posRequested.intY() - posCurrent.intY());
		// Z Change
		final Integer zChange = Math.abs(posRequested.intZ() - posCurrent.intZ());
		if (xChange + yChange + zChange > MAX_POSITION_CHANGE) {
			return Boolean.FALSE;
		} else {
			return Boolean.TRUE;
		}
	}

}
