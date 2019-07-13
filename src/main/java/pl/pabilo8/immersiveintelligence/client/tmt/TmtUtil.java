package pl.pabilo8.immersiveintelligence.client.tmt;

/**
 * Created by Pabilo8 on 2019-06-01.
 */
public class TmtUtil
{
	public static float AngleToTMT(float angle)
	{
		return angle/57.29578f;
	}

	public static float TMTToAngle(float angle)
	{
		return angle*57.29578f;
	}
}
