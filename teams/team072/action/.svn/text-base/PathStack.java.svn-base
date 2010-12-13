package team072.action;

import java.util.LinkedList;

import battlecode.common.MapLocation;

public class PathStack {
	
	private LinkedList<MapLocation> stack;
	
	public PathStack() {
		resetPathStack();
	}
	
	public void resetPathStack() {
		stack = new LinkedList<MapLocation>();
	}
	
	public void pushPathStack(MapLocation loc) {
		stack.offer(loc);
	}
	
	public MapLocation popPathStack() {
		return stack.pollLast();
	}
}
