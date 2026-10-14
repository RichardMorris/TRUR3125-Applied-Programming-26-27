package org.singsurf.jns3.hardware;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GlobalWallClock {
	public static final int INIT = 0;
	public static final int RELEASE = 8;
	
	static GlobalWallClock instance = null;
	int tickCount = 0;
	Map<Integer,List<TickListener>> listnersByPriority = new TreeMap<>();
	List<TickListener> initListners = new ArrayList<>();
	List<TickListener> releaseListners = new ArrayList<>();
	
	public interface TickListener {
		void onTick();
	}
	
	/**
	 * A private constructor to prevent instantiation of this class.
	 */
	private GlobalWallClock() {
		listnersByPriority.put(INIT, initListners);
		listnersByPriority.put(RELEASE, releaseListners);		
	}

	public void advanceTime() {
		tickCount++;
		for(var lst : listnersByPriority.entrySet()) {
			for (TickListener listener : lst.getValue()) {
				listener.onTick();
			}
			
		}
	}
	
	public void addTickListener(TickListener listener) {
		releaseListners.add(listener);
	}
	
	public void addTickListener(int priority, TickListener listener) {
		listnersByPriority.computeIfAbsent(priority, k -> new ArrayList<>()).add(listener);
	}
	public void removeTickListener(TickListener listener) {
		for(var lst : listnersByPriority.entrySet()) {
			lst.getValue().remove(listener);
		}
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

