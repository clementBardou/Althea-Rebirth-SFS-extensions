package sfs2x.extensions.games.spacewar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import sfs2x.extensions.games.spacewar.core.Game;
import sfs2x.extensions.games.spacewar.evthandlers.UserJoinRoomEventHandler;
import sfs2x.extensions.games.spacewar.evthandlers.UserLeaveRoomEventHandler;
import sfs2x.extensions.games.spacewar.reqhandlers.ControlRequestHandler;

import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.api.ISFSMMOApi;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.SFSUserVariable;
import com.smartfoxserver.v2.entities.variables.UserVariable;
import com.smartfoxserver.v2.extensions.SFSExtension;
import com.smartfoxserver.v2.mmo.BaseMMOItem;
import com.smartfoxserver.v2.mmo.IMMOItemVariable;
import com.smartfoxserver.v2.mmo.MMOItem;
import com.smartfoxserver.v2.mmo.MMOItemVariable;
import com.smartfoxserver.v2.mmo.MMORoom;
import com.smartfoxserver.v2.mmo.Vec3D;

public class SpaceWarRoomExtension extends SFSExtension
{
	private static final String UV_MODEL = "sModel";
	private static final String UV_X = "x";
	private static final String UV_Y = "y";
	private static final String UV_VX = "vx";
	private static final String UV_VY = "vy";
	private static final String UV_DIR = "d";
	private static final String UV_THRUST = "t";
	private static final String UV_ROTATE = "r";
	private static final String IV_TYPE = "iType";
	private static final String IV_MODEL = "iModel";
	private static final String IV_X = "x";
	private static final String IV_Y = "y";
	private static final String IV_VX = "vx";
	private static final String IV_VY = "vy";
	private static final String ITYPE_WEAPON = "weapon";
	private static final String REQ_CONTROL = "control";
	private static final String RES_SHOT_XPLODE = "shot_xplode";
	private SmartFoxServer sfs;
	private ISFSMMOApi mmoApi;
	private MMORoom room;
	private Game game;
	private ScheduledFuture<?> gameTask;

	public void init()
	{
		this.room = ((MMORoom) getParentRoom());

		this.sfs = SmartFoxServer.getInstance();

		this.mmoApi = this.sfs.getAPIManager().getMMOApi();

		addEventHandler(SFSEventType.USER_JOIN_ROOM, UserJoinRoomEventHandler.class);
		addEventHandler(SFSEventType.USER_LEAVE_ROOM, UserLeaveRoomEventHandler.class);
		addEventHandler(SFSEventType.USER_DISCONNECT, UserLeaveRoomEventHandler.class);

		addRequestHandler("control", ControlRequestHandler.class);

		this.game = new Game(this);

		this.gameTask = this.sfs.getTaskScheduler().scheduleAtFixedRate(this.game, 0, 40, TimeUnit.MILLISECONDS);
	}

	public void destroy()
	{
		this.gameTask.cancel(true);
	}

	public Game getGame()
	{
		return this.game;
	}

	public void addStarship(User user)
	{
		String shipModel = user.getVariable("sModel").getStringValue();

		ISFSObject settings = (ISFSObject) getParentZone().getExtension().handleInternalMessage("getStarshipCfg",
				shipModel);

		this.game.createStarship(user.getId(), settings);
	}

	public void removeStarship(int userId)
	{
		this.game.removeStarship(userId);
	}

	public void fireWeapon(User user, int weaponNum)
	{
		String shipModel = user.getVariable("sModel").getStringValue();

		ISFSObject data = new SFSObject();
		data.putUtfString("shipModel", shipModel);
		data.putInt("weaponNum", weaponNum);

		ISFSObject settings = (ISFSObject) getParentZone().getExtension().handleInternalMessage("getWeaponCfg", data);

		this.game.createWeaponShot(user.getId(), settings);
	}

	public void setStarshipState(int userId, double x, double y, double vx, double vy, double direction,
			boolean thrust, int rotation, boolean fireClientEvent)
	{
		User user = this.room.getUserById(userId);
		if (user != null)
		{
			List<UserVariable> vars = new ArrayList<UserVariable>();
			vars.add(new SFSUserVariable("x", Double.valueOf(x)));
			vars.add(new SFSUserVariable("y", Double.valueOf(y)));
			vars.add(new SFSUserVariable("vx", Double.valueOf(vx)));
			vars.add(new SFSUserVariable("vy", Double.valueOf(vy)));
			vars.add(new SFSUserVariable("d", Double.valueOf(direction)));
			vars.add(new SFSUserVariable("t", Boolean.valueOf(thrust)));
			vars.add(new SFSUserVariable("r", Integer.valueOf(rotation)));

			getApi().setUserVariables(user, vars, fireClientEvent, false);

			int intX = (int) Math.round(x);
			int intY = (int) Math.round(y);
			Vec3D pos = new Vec3D(intX, intY, 0);
			this.mmoApi.setUserPosition(user, pos, getParentRoom());
		}
	}

	public void setStarshipRotating(int userId, int direction)
	{
		User user = this.room.getUserById(userId);
		if (user != null)
		{
			List<UserVariable> vars = new ArrayList<UserVariable>();
			vars.add(new SFSUserVariable("r", Integer.valueOf(direction)));

			getApi().setUserVariables(user, vars, true, false);
		}
	}

	public int addWeaponShot(String model, double x, double y, double vx, double vy)
	{
		List<IMMOItemVariable> vars = buildWeaponShotMMOItemVars(x, y, vx, vy);
		vars.add(new MMOItemVariable("iModel", model));
		vars.add(new MMOItemVariable("iType", "weapon"));

		MMOItem item = new MMOItem(vars);

		setMMOItemPosition(item, x, y);

		return item.getId();
	}

	public void removeWeaponShot(int mmoItemId)
	{
		BaseMMOItem item = this.room.getMMOItemById(mmoItemId);

		this.mmoApi.removeMMOItem(item);
	}

	public void setWeaponShotPosition(int mmoItemId, double x, double y, double vx, double vy)
	{
		BaseMMOItem item = this.room.getMMOItemById(mmoItemId);

		List<IMMOItemVariable> vars = buildWeaponShotMMOItemVars(x, y, vx, vy);
		this.mmoApi.setMMOItemVariables(item, vars, false);

		setMMOItemPosition(item, x, y);
	}

	public void notifyWeaponShotExplosion(int mmoItemId, double x, double y)
	{
		int intX = (int) Math.round(x);
		int intY = (int) Math.round(y);
		Vec3D pos = new Vec3D(intX, intY, 0);

		List<User> users = this.room.getProximityList(pos);

		ISFSObject params = new SFSObject();
		params.putInt("id", mmoItemId);
		params.putInt("x", intX);
		params.putInt("y", intY);

		send("shot_xplode", params, users);
	}

	public List<Integer> getWeaponShotsList(double x, double y)
	{
		List<Integer> shots = new ArrayList<Integer>();

		int intX = (int) Math.round(x);
		int intY = (int) Math.round(y);
		Vec3D pos = new Vec3D(intX, intY, 0);

		List<BaseMMOItem> items = this.room.getProximityItems(pos);
		for (BaseMMOItem item : items)
		{
			boolean isWeapon = item.getVariable("iType").getStringValue().equals("weapon");
			if (isWeapon)
			{
				shots.add(Integer.valueOf(item.getId()));
			}
		}
		return shots;
	}

	private List<IMMOItemVariable> buildWeaponShotMMOItemVars(double x, double y, double vx, double vy)
	{
		List<IMMOItemVariable> vars = new ArrayList<IMMOItemVariable>();

		vars.add(new MMOItemVariable("x", Double.valueOf(x)));
		vars.add(new MMOItemVariable("y", Double.valueOf(y)));
		vars.add(new MMOItemVariable("vx", Double.valueOf(vx)));
		vars.add(new MMOItemVariable("vy", Double.valueOf(vy)));

		return vars;
	}

	private void setMMOItemPosition(BaseMMOItem item, double x, double y)
	{
		int intX = (int) Math.round(x);
		int intY = (int) Math.round(y);
		Vec3D pos = new Vec3D(intX, intY, 0);
		this.mmoApi.setMMOItemPosition(item, pos, getParentRoom());
	}
}
