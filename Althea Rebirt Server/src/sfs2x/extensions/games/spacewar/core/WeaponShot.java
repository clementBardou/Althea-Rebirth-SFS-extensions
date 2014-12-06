package sfs2x.extensions.games.spacewar.core;

import com.smartfoxserver.v2.entities.data.ISFSObject;

public class WeaponShot extends GameItem
{
	private long selfDestructTime = 0L;
	private int mmoItemId = -1;

	public WeaponShot(int ownerId, ISFSObject settings)
	{
		super(ownerId, settings);
		if (getDuration() > 0)
		{
			this.selfDestructTime = (System.currentTimeMillis() + getDuration() * 1000);
		}
	}

	public void setMMOItemId(int mmoItemId)
	{
		if (this.mmoItemId == -1)
		{
			this.mmoItemId = mmoItemId;
		}
	}

	public int getMMOItemId()
	{
		return this.mmoItemId;
	}

	public double getSpeed()
	{
		double setting = this.settings.getInt("speed").intValue();

		return setting / 1000.0D;
	}

	public int getHitRadius()
	{
		return this.settings.getInt("hitRadius").intValue();
	}

	public double getHitForce()
	{
		double setting = this.settings.getInt("hitForce").intValue();

		return setting / 1000.0D;
	}

	public boolean isSelfDestruct()
	{
		if (this.selfDestructTime > 0L) { return System.currentTimeMillis() >= this.selfDestructTime; }
		return false;
	}

	private int getDuration()
	{
		return this.settings.getInt("duration").intValue();
	}
}
