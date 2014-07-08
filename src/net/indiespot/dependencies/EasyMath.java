package net.indiespot.dependencies;

import java.util.Random;

public class EasyMath {

	/**
	 * POWER OF TWO
	 */

	public static final boolean isPowerOfTwo(int n) {
		for(int i = 0; i < 31; i++)
			if(n == (1 << i))
				return true;

		return false;
	}

	public static final int fitInPowerOfTwo(int n) {
		if(n < 0)
			throw new IllegalArgumentException("n must be positive");

		for(int i = 1; i < 31; i++) // 2,4,8,16,...
		{
			int pow = (1 << i);
			if(n <= pow)
				return pow;
		}

		// never happens
		return -1;
	}

	public static final int widthInBits(int n) {
		if(n == 0)
			return 0;
		if(n < 0)
			return 32;

		for(int i = 30; i >= 0; i--)
			if(n >= (1 << i))
				return i + 1;

		return 0;
	}

	/**
	 * CURVE
	 */

	public static final double curve(double distance, double topDistance, double margin, double power) {
		// Shape: __________________/\_____________________ ( /\ == sin)

		double min = topDistance - margin;
		double max = topDistance;
		double ratio = (distance - min) / (max - min);
		double angle = ratio * 0.5 * Math.PI;
		if(angle < 0.0 || angle > Math.PI)
			return 0.0;
		double sinCurve = Math.sin(angle);
		return Math.pow(sinCurve, power);
	}

	/**
	 * LERP
	 */

	public static final float lerp(float a, float b, float t) {
		return a + t * (b - a);
	}

	public static final double lerp(double t, double a, double b) {
		return a + t * (b - a);
	}

	public static final float lerpWithCap(float cur, float min, float max) {
		if(cur < 0.0f)
			return min;
		if(cur > 1.0f)
			return max;

		return (cur - min) / (max - min);
	}

	/**
	 * INV LERP
	 */

	public static final float invLerp(float cur, float min, float max) {
		return (cur - min) / (max - min);
	}

	public static final double invLerp(double cur, double min, double max) {
		return (cur - min) / (max - min);
	}

	public static final float invLerpWithCap(float cur, float min, float max) {
		if(cur < min)
			return 0.0f;
		if(cur > max)
			return 1.0f;
		return invLerp(cur, min, max);
	}

	//

	public static final float invLerp(long cur, long min, long max) {
		return (float) (cur - min) / (max - min);
	}

	public static final float invLerpWithCap(long cur, long min, long max) {
		if(cur < min)
			return 0.0f;
		if(cur > max)
			return 1.0f;
		return invLerp(cur, min, max);
	}

	/**
	 * INTERPOLATE
	 */

	public static final float interpolate(float cur, float min, float max, float startValue, float endValue) {
		return lerp(startValue, endValue, invLerp(cur, min, max));
	}

	public static final float interpolateWithCap(float cur, float min, float max, float startValue, float endValue) {
		if(cur < min)
			return startValue;
		if(cur > max)
			return endValue;
		return interpolate(cur, min, max, startValue, endValue);
	}

	public static final float interpolate2d(float x, float z, float nw, float ne, float se, float sw) {
		// n -= n % dim -> n = 0..dim (local offset)
		x = x - (int) x;
		z = z - (int) z;

		// Which triangle of quad (left | right)
		if(x > z)
			sw = nw + se - ne;
		else
			ne = se + nw - sw;

		// calculate interpolation of selected triangle
		float n = lerp(nw, ne, x);
		float s = lerp(sw, se, x);
		return lerp(n, s, z);
	}

	/**
	 * CLAMP
	 */

	public static final double clamp(double val, double min, double max) {
		return (val < min) ? min : (val > max) ? max : val;
	}

	public static final float clamp(float val, float min, float max) {
		return (val < min) ? min : (val > max) ? max : val;
	}

	public static final long clamp(long val, long min, long max) {
		return (val < min) ? min : (val > max) ? max : val;
	}

	public static final int clamp(int val, int min, int max) {
		return (val < min) ? min : (val > max) ? max : val;
	}

	/**
	 * BETWEEN
	 */

	public static final boolean isBetween(double val, double min, double max) {
		return min <= val && val <= max;
	}

	public static final boolean isBetween(float val, float min, float max) {
		return min <= val && val <= max;
	}

	public static final boolean isBetween(long val, long min, long max) {
		return min <= val && val <= max;
	}

	public static final boolean isBetween(int val, int min, int max) {
		return min <= val && val <= max;
	}

	public static final boolean areBetween(int[] val, int min, int max) {
		for(int i = 0; i < val.length; i++)
			if(!(min <= val[i] && val[i] <= max))
				return false;

		return true;
	}

	/**
	 * EQUALS
	 */

	public static final boolean equals(float a, float b, float error) {
		if(a == b)
			return true;

		float diff = a - b;
		return diff * diff < error * error;
	}

	public static final float bounce(float val, float min, float max) {
		float range = max - min;
		float range2 = range * 2.0f;
		float steps = (val - min) % range2;
		if(steps > range)
			steps = range2 - steps;
		return steps + min;
	}

	/**
	 * MODULO ABS
	 */

	public static final float moduloAbs(float val, float max) {
		return ((val % max) + max) % max;
	}

	public static final int moduloAbs(int val, int max) {
		return ((val % max) + max) % max;
	}

	public static final long moduloAbs(long val, long max) {
		return ((val % max) + max) % max;
	}

	/**
	 * MODULO RANGE
	 */

	public static final float moduloInRange(float val, float min, float max) {
		float range = max - min;
		float adjust = (val - min) % range;

		if(adjust >= 0.0F)
			return adjust + min;

		return adjust + min + range;
	}

	public static final int moduloInRange(int val, int min, int max) {
		int range = max - min;
		int adjust = (val - min) % range;

		if(adjust >= 0)
			return adjust + min;

		return adjust + min + range;
	}

	public static final long moduloInRange(long val, long min, long max) {
		long range = max - min;
		long adjust = (val - min) % range;

		if(adjust >= 0)
			return adjust + min;

		return adjust + min + range;
	}

	/**
	 * RANDOM
	 */

	public static final float random(Random r) {
		return (r.nextFloat() - 0.5F) * 2.0F;
	}

	public static final float random(Random r, float max) {
		return r.nextFloat() * max;
	}

	public static final float random(Random r, float min, float max) {
		return r.nextFloat() * (max - min) + min;
	}

	public static final float randomRange(Random r, float base, float range) {
		return base + EasyMath.random(r) * range;
	}

	public static boolean intersects(int aMin, int aMax, int bMin, int bMax) {
		if(isBetween(aMin, bMin, bMax))
			return true;
		if(isBetween(aMax, bMin, bMax))
			return true;
		if(isBetween(bMin, aMin, aMax))
			return true;
		if(isBetween(bMax, aMin, aMax))
			return true;
		return false;
	}

	public static boolean intersects(int[] min, int[] max, int off[], int dim) {
		for(int i = 0; i < min.length; i++)
			if(!intersects(off[i], off[i] + dim, min[i], max[i]))
				return false;
		return true;
	}

	private static final int BIG_ENOUGH_INT = 16 * 1024;
	private static final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT + 0.0000;
	private static final double BIG_ENOUGH_ROUND = BIG_ENOUGH_INT + 0.5000;
	private static final double BIG_ENOUGH_CEIL = BIG_ENOUGH_INT + 0.9999;

	public static int fastFloor(float x) {
		return (int) (x + BIG_ENOUGH_FLOOR) - BIG_ENOUGH_INT;
	}

	public static int fastRound(float x) {
		return (int) (x + BIG_ENOUGH_ROUND) - BIG_ENOUGH_INT;
	}

	public static int fastCeil(float x) {
		return (int) (x + BIG_ENOUGH_CEIL) - BIG_ENOUGH_INT;
	}

	public static void main(String[] args) {
		System.out.println(fastFloor(+13.4f));
		System.out.println(fastFloor(-13.4f));
		System.out.println(fastFloor(+13.6f));
		System.out.println(fastFloor(-13.6f));
		System.out.println();
		System.out.println(fastRound(+13.4f));
		System.out.println(fastRound(-13.4f));
		System.out.println(fastRound(+13.6f));
		System.out.println(fastRound(-13.6f));
		System.out.println();
		System.out.println(fastCeil(+13.4f));
		System.out.println(fastCeil(-13.4f));
		System.out.println(fastCeil(+13.6f));
		System.out.println(fastCeil(-13.6f));
	}
}