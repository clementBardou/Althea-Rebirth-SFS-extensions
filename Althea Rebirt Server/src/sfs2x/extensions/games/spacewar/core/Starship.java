package sfs2x.extensions.games.spacewar.core;

import com.smartfoxserver.v2.entities.data.ISFSObject;

public class Starship extends GameItem
{
	public double rotation;
	public int rotatingDir;
	public boolean thrust;

	public Starship(int ownerId, ISFSObject settings)
	{
		super(ownerId, settings);
	}

	public double getMaxSpeed()
	{
		double setting = this.settings.getInt("maxSpeed").intValue();

		return setting / 1000.0D;
	}

	public double getRotationSpeed()
	{
		double setting = this.settings.getInt("rotationSpeed").intValue();

		return setting * 3.141592653589793D / 180.0D / 1000.0D;
	}

	public double getThrustAcceleration()
	{
		double setting = this.settings.getInt("thrustAccel").intValue();

		return setting / 1000000.0D;
	}
}
