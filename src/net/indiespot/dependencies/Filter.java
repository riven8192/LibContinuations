package net.indiespot.dependencies;

public interface Filter<T> {
	public boolean accept(T t);
}
