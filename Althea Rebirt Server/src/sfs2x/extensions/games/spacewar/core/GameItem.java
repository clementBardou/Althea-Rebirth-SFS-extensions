package sfs2x.extensions.games.spacewar.core;

import sfs2x.extensions.games.spacewar.entities.Velocity;

import com.smartfoxserver.v2.entities.data.ISFSObject;

public class GameItem
{
	protected int ownerId;
	protected ISFSObject settings;
	public double x;
	public double y;
	public Velocity velocity;
	public long lastRenderTime;

	public GameItem(int ownerId, ISFSObject settings)
	{
		this.ownerId = ownerId;
		this.settings = settings;
		this.velocity = new Velocity(0.0D, 0.0D);
	}

	public int getOwnerId()
	{
		return this.ownerId;
	}

	public String getModel()
	{
		return this.settings.getUtfString("model");
	}

	public double getVX()
	{
		return this.velocity.vx;
	}

	public double getVY()
	{
		return this.velocity.vy;
	}
}
