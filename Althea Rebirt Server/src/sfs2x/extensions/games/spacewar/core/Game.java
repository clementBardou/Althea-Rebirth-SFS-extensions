package sfs2x.extensions.games.spacewar.core;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import sfs2x.extensions.games.spacewar.SpaceWarRoomExtension;
import sfs2x.extensions.games.spacewar.entities.Velocity;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.exceptions.ExceptionMessageComposer;

public class Game implements Runnable
{
	private SpaceWarRoomExtension ext;
	private Map<Integer, Starship> starships;
	private Map<Integer, WeaponShot> weaponShots;

	public Game(SpaceWarRoomExtension ext)
	{
		this.ext = ext;

		this.starships = new ConcurrentHashMap<Integer, Starship>();

		this.weaponShots = new ConcurrentHashMap<Integer, WeaponShot>();
	}

	public void createStarship(int ownerId, ISFSObject settings)
	{
		Starship ship = new Starship(ownerId, settings);

		ship.x = (Math.random() * 1000.0D);
		ship.y = (Math.random() * 800.0D);
		ship.rotation = ((int) Math.round(Math.random() * 360.0D * 3.141592653589793D / 180.0D));

		ship.lastRenderTime = System.currentTimeMillis();

		this.starships.put(Integer.valueOf(ownerId), ship);

		saveStarshipPosition(ship, true);
	}

	public void removeStarship(int ownerId)
	{
		this.starships.remove(Integer.valueOf(ownerId));
	}

	public void rotateStarship(int ownerId, int direction)
	{
		Starship ship = (Starship) this.starships.get(Integer.valueOf(ownerId));

		ship.rotatingDir = direction;
		if (direction != 0)
		{
			this.ext.setStarshipRotating(ownerId, direction);
		} else
		{
			saveStarshipPosition(ship, true);
		}
	}

	public void thrustStarship(int ownerId, boolean activate)
	{
		Starship ship = (Starship) this.starships.get(Integer.valueOf(ownerId));

		ship.thrust = activate;

		saveStarshipPosition(ship, true);
	}

	public void createWeaponShot(int ownerId, ISFSObject settings)
	{
		WeaponShot shot = new WeaponShot(ownerId, settings);

		Starship ship = (Starship) this.starships.get(Integer.valueOf(ownerId));

		shot.x = (ship.x + 15.0D * Math.cos(ship.rotation));
		shot.y = (ship.y + 15.0D * Math.sin(ship.rotation));

		double vx = Math.cos(ship.rotation) * shot.getSpeed();
		double vy = Math.sin(ship.rotation) * shot.getSpeed();
		Velocity v = new Velocity(vx + ship.getVX(), vy + ship.getVY());
		shot.velocity = v;

		shot.lastRenderTime = System.currentTimeMillis();

		int id = this.ext.addWeaponShot(shot.getModel(), shot.x, shot.y, shot.getVX(), shot.getVY());
		shot.setMMOItemId(id);

		this.weaponShots.put(Integer.valueOf(id), shot);
	}

	public void run()
	{
		try
		{
			WeaponShot shot;
			for (Iterator<Map.Entry<Integer, WeaponShot>> it = this.weaponShots.entrySet().iterator(); it.hasNext();)
			{
				shot = (WeaponShot) it.next().getValue();
				if (shot.isSelfDestruct())
				{
					it.remove();

					removeWeaponShot(shot);
				} else
				{
					renderWeaponShot(shot);

					saveWeaponShotPosition(shot);
				}
			}
			for (Starship ship : this.starships.values())
			{
				renderStarship(ship);

				List<Integer> shotIDs = this.ext.getWeaponShotsList(ship.x, ship.y);

				boolean hit = false;
				for (int i = 0; i < shotIDs.size(); i++)
				{
					int shotID = ((Integer) shotIDs.get(i)).intValue();
					shot = (WeaponShot) this.weaponShots.get(Integer.valueOf(shotID));
					if (getDistance(ship, shot) <= shot.getHitRadius())
					{
						this.weaponShots.remove(Integer.valueOf(shotID));

						removeWeaponShot(shot);

						int dirX = ship.x > shot.x ? 1 : -1;
						int dirY = ship.y > shot.y ? 1 : -1;
						ship.velocity.vx += dirX * shot.getHitForce();
						ship.velocity.vy += dirY * shot.getHitForce();

						hit = true;
					}
				}
				saveStarshipPosition(ship, hit);
			}
		} catch (Exception e)
		{
			ExceptionMessageComposer emc = new ExceptionMessageComposer(e);
			this.ext.trace(new Object[]
			{ emc.toString() });
		}
	}

	private void saveStarshipPosition(Starship ss, boolean doUpdateClients)
	{
		this.ext.setStarshipState(ss.getOwnerId(), ss.x, ss.y, ss.getVX(), ss.getVY(), ss.rotation, ss.thrust,
				ss.rotatingDir, doUpdateClients);
	}

	private void saveWeaponShotPosition(WeaponShot ws)
	{
		this.ext.setWeaponShotPosition(ws.getMMOItemId(), ws.x, ws.y, ws.getVX(), ws.getVY());
	}

	private void removeWeaponShot(WeaponShot shot)
	{
		this.ext.removeWeaponShot(shot.getMMOItemId());

		this.ext.notifyWeaponShotExplosion(shot.getMMOItemId(), shot.x, shot.y);
	}

	private void renderStarship(Starship ship)
	{
		long now = System.currentTimeMillis();
		long elapsed = now - ship.lastRenderTime;
		for (long i = 0L; i < elapsed; i += 1L)
		{
			ship.rotation += ship.rotatingDir * ship.getRotationSpeed();
			if (ship.thrust)
			{
				ship.velocity.vx += Math.cos(ship.rotation) * ship.getThrustAcceleration();
				ship.velocity.vy += Math.sin(ship.rotation) * ship.getThrustAcceleration();
			}
			ship.velocity.limitSpeed(ship.getMaxSpeed());

			ship.x += ship.velocity.vx;
			ship.y += ship.velocity.vy;
		}
		ship.lastRenderTime = now;
	}

	private void renderWeaponShot(WeaponShot shot)
	{
		long now = System.currentTimeMillis();
		long elapsed = now - shot.lastRenderTime;
		for (long i = 0L; i < elapsed; i += 1L)
		{
			shot.x += shot.velocity.vx;
			shot.y += shot.velocity.vy;
		}
		shot.lastRenderTime = now;
	}

	private double getDistance(GameItem simItem1, GameItem simItem2)
	{
		double dist_x = simItem1.x - simItem2.x;
		double dist_y = simItem1.y - simItem2.y;

		return Math.sqrt(Math.pow(dist_x, 2.0D) + Math.pow(dist_y, 2.0D));
	}
}
