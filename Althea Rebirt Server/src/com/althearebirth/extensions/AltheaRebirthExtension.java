package com.althearebirth.extensions;

import com.althearebirth.extensions.handlers.UserVariablesHandler;
import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.api.ISFSMMOApi;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class AltheaRebirthExtension extends SFSExtension {

	/**
	 * User Variables Handler
	 */
	private UserVariablesHandler userVariablesHandler;
	/**
	 * SFS MMO Api
	 */
	private ISFSMMOApi mmoApi;
	
	@Override
	public void init() {
		mmoApi = SmartFoxServer.getInstance().getAPIManager().getMMOApi();
		userVariablesHandler = new UserVariablesHandler(mmoApi, this);
		
		addEventHandler(SFSEventType.USER_VARIABLES_UPDATE, userVariablesHandler);
	}

}
