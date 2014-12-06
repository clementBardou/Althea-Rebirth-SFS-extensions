package sfs2x.extensions.games.spacewar;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import sfs2x.extensions.games.spacewar.evthandlers.LoginEventHandler;

import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class SpaceWarZoneExtension extends SFSExtension {
	private static final String CFG_STARSHIPS_KEY = "starships";
	private static final String CFG_WEAPONS_KEY = "weapons";
	private ISFSObject configuration;

	public void init() {
		try {
			setupGame();

			addEventHandler(SFSEventType.USER_LOGIN, LoginEventHandler.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Object handleInternalMessage(String cmdName, Object params) {
		if (cmdName.equals("getStarshipCfg")) {
			String starshipModel = (String) params;
			return getStarshipsCfg().getSFSObject(starshipModel);
		}
		if (cmdName.equals("getWeaponCfg")) {
			ISFSObject data = (ISFSObject) params;

			String starshipModel = data.getUtfString("shipModel");
			int weaponNum = data.getInt("weaponNum").intValue();
			String weaponModel = getStarshipsCfg().getSFSObject(starshipModel)
					.getUtfString("weapon" + weaponNum);

			return getWeaponsCfg().getSFSObject(weaponModel);
		}
		return null;
	}

	public ISFSObject getStarshipsCfg() {
		return this.configuration.getSFSObject("starships");
	}

	public ISFSObject getWeaponsCfg() {
		return this.configuration.getSFSObject("weapons");
	}

	private void setupGame() throws IOException {
		String cfgData = FileUtils.readFileToString(new File(getCurrentFolder()
				+ "SpaceWar.cfg"));

		ISFSObject tempCfg = SFSObject.newFromJsonData(cfgData);

		ISFSArray starshipsCfg = tempCfg.getSFSArray("starships");
		ISFSObject starships = new SFSObject();
		for (int i = 0; i < starshipsCfg.size(); i++) {
			ISFSObject starship = starshipsCfg.getSFSObject(i);
			String model = starship.getUtfString("model");

			starships.putSFSObject(model, starship);
		}
		ISFSArray weaponsCfg = tempCfg.getSFSArray("weapons");
		ISFSObject weapons = new SFSObject();
		for (int i = 0; i < weaponsCfg.size(); i++) {
			ISFSObject weapon = weaponsCfg.getSFSObject(i);
			String model = weapon.getUtfString("model");

			weapons.putSFSObject(model, weapon);
		}
		this.configuration = new SFSObject();
		this.configuration.putSFSObject("starships", starships);
		this.configuration.putSFSObject("weapons", weapons);
	}
}
