package net.indiespot.dependencies;

import java.nio.FloatBuffer;

public class Vec2 {
	public float x, y;

	//

	public Vec2() {
		load(0.0f);
	}

	public Vec2(float xy) {
		load(xy);
	}

	public Vec2(float x, float y) {
		load(x, y);
	}

	public Vec2(float[] v, int pos) {
		load(v, pos);
	}

	public Vec2(Vec2 v) {
		load(v);
	}

	/**
	 * LOAD
	 */

	public Vec2 load(Vec2 v) {
		x = v.x;
		y = v.y;

		return this;
	}

	public Vec2 load(float val) {
		x = val;
		y = val;

		return this;
	}

	public Vec2 load(float x, float y) {
		this.x = x;
		this.y = y;

		return this;
	}

	public Vec2 load(float[] arr, int pos) {
		x = arr[pos + 0];
		y = arr[pos + 1];

		return this;
	}

	public Vec2 load(FloatBuffer buf) {
		x = buf.get();
		y = buf.get();

		return this;
	}

	public Vec2 load(FloatBuffer buf, int pos) {
		x = buf.get(pos + 0);
		y = buf.get(pos + 1);

		return this;
	}

	/**
	 * STORE
	 */

	public final void store(float[] arr, int off) {
		arr[off + 0] = x;
		arr[off + 1] = y;
	}

	public final void store(FloatBuffer buf) {
		buf.put(x);
		buf.put(y);
	}

	public final void store(FloatBuffer buf, int pos) {
		buf.put(pos + 0, x);
		buf.put(pos + 1, y);
	}

	/**
	 * CALC
	 */

	public float squaredLength() {
		return (x * x + y * y);
	}

	public float length() {
		return (float) Math.sqrt(this.squaredLength());
	}

	public Vec2 length(float val) {
		float li = val / (float) Math.sqrt(this.squaredLength());

		x = (x * li);
		y = (y * li);

		return this;
	}

	public Vec2 normalize() {
		float li = 1.0f / (float) Math.sqrt(this.squaredLength());

		x = (x * li);
		y = (y * li);

		return this;
	}

	public Vec2 inv() {
		x = (-x);
		y = (-y);

		return this;
	}

	public Vec2 abs() {
		if(x < 0.0F)
			x = (-x);
		if(y < 0.0F)
			y = (-y);

		return this;
	}

	//

	public Vec2 add(float x, float y) {
		this.x += x;
		this.y += y;

		return this;
	}

	public Vec2 sub(float x, float y) {
		this.x -= x;
		this.y -= y;

		return this;
	}

	public Vec2 mul(float x, float y) {
		this.x *= x;
		this.y *= y;

		return this;
	}

	public Vec2 div(float x, float y) {
		this.x /= x;
		this.y /= y;

		return this;
	}

	//

	public Vec2 add(float xy) {
		return add(xy, xy);
	}

	public Vec2 sub(float xy) {
		return sub(xy, xy);
	}

	public Vec2 mul(float xy) {
		return mul(xy, xy);
	}

	public Vec2 div(float xy) {
		return div(xy, xy);
	}

	//

	public Vec2 add(Vec2 vec) {
		return add(vec.x, vec.y);
	}

	public Vec2 sub(Vec2 vec) {
		return sub(vec.x, vec.y);
	}

	public Vec2 mul(Vec2 vec) {
		return mul(vec.x, vec.y);
	}

	public Vec2 div(Vec2 vec) {
		return div(vec.x, vec.y);
	}

	//

	public Vec2 min(Vec2 vec) {
		if(vec.x < x)
			x = (vec.x);
		if(vec.y < y)
			y = (vec.y);

		return this;
	}

	public Vec2 max(Vec2 vec) {
		if(vec.x > x)
			x = (vec.x);
		if(vec.y > y)
			y = (vec.y);

		return this;
	}

	public int hashCode() {
		int xi = (int) (this.x * 1000);
		int yi = (int) (this.y * 1000);

		return xi ^ yi;
	}

	public static float distanceSquared(Vec2 a, Vec2 b) {
		float dx = a.x - b.x;
		float dy = a.y - b.y;
		return dx * dx + dy * dy;
	}

	public static float distance(Vec2 a, Vec2 b) {
		return (float) Math.sqrt(distanceSquared(a, b));
	}
}