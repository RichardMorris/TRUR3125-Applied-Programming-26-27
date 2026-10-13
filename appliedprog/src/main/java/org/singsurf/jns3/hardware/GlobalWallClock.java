package org.singsurf.jns3.hardware;

import java.util.ArrayList;
import java.util.List;

public class GlobalWallClock {
	static GlobalWallClock instance = null;
	int tickCount = 0;
	List<TickListener> listeners = new ArrayList<>();
	
	public interface TickListener {
		void onTick();
	}
	
	/**
	 * A private constructor to prevent instantiation of this class.
	 */
	private GlobalWallClock() {
	}

	public void advanceTime() {
		tickCount++;
		for (TickListener listener : listeners) {
			listener.onTick();
		}
	}
	
	public void addTickListener(TickListener listener) {
		listeners.add(listener);
	}
	
	public void removeTickListener(TickListener listener) {
		listeners.remove(listener);
	}
	
	public int getTickCount() {
		return tickCount;
	}
	
	
	/**
	 * Returns the singleton instance of GlobalWallClock.
	 * If the instance does not exist, it creates a new one.
	 *
	 * @return the singleton instance of GlobalWallClock
	 */
	public static GlobalWallClock getInstance() {
		if (instance == null) {
			instance = new GlobalWallClock();
		}
		return instance;
	}

}

