package pumpkins;
import java.util.*;
import java.util.concurrent.*;
/**
 * The Clock class increments a counter and keeps a list of timed events
 * which it gives to the program to execute.
 */
public class Clock implements Runnable {
	private final int SLEEP_DURATION = 1;
	private final Program program;
	private volatile boolean active;
	private volatile int timestamp;
	private final List<Timer> timers;
	/**
	 * Initialize a new Clock object
	 */
	public Clock(Program program){
		this.program = program;
		this.active = true;
		this.timestamp = 0;
		this.timers = new LinkedList<Timer>();
	}
	/**
	 * Begin to increment counter, decrementing timers at each tick
	 */
	public void run(){
		for (; this.active; this.increment()){
			try {
				Thread.sleep(this.SLEEP_DURATION);
			} catch (InterruptedException ie){
				ie.printStackTrace();
			}
			decrementTimers();
		}
		//Undo last increment that occurs before Clock sees active == false
		synchronized(this){
			--this.timestamp;
		}
	}
	/**
	 * Stop the clock 
	 */
	public void stop(){
		this.active = false;
	}
	/**
	 * Get the clock's current timestamp
	 */
	public int getTimestamp(){
		return this.timestamp;
	}
	/** 
	 * Check if the timer has expired (timestamp exceeds Program.TIME_UNITS)
	 */
	public boolean expired(){
		return this.timestamp >= Program.TIME_UNITS;
	}
	/**
	 * Accepts an event and a duration, after which the clock will pass the
	 * event to the program to execute
	 */
	public void setTimer(PumpkinEvent event, int duration){
		if (duration <= 0)
			this.program.processEvent(event);
		else {
			Timer t = new Timer(event, duration);
			synchronized(this.timers){
				this.timers.add(t);
			}
		}
	}
	// Get the meaning of life
	private synchronized void increment(){
		++this.timestamp;
	}
	// Iterates through registered timers and decrements them at each clock tick
	private void decrementTimers(){
		List<Timer> expired = new LinkedList<Timer>();
		synchronized(this.timers){
			Iterator<Timer> i = this.timers.iterator();
			while (i.hasNext()){
				Timer t = i.next();
				if (--t.duration == 0){
					expired.add(t);
					i.remove();
				}
			}
		}
		//Sends events from expired timers to program to be executed
		for (Timer t : expired){
			this.program.processEvent(t.getEvent());
		}
	}
	// Simple timer class which stores a runnable event and a countdown value 
	class Timer {
		private PumpkinEvent event;
		private volatile int duration;
		Timer(PumpkinEvent event, int duration){
			this.event = event;
			this.duration = duration;
		}
		PumpkinEvent getEvent(){
			return this.event;
		}
	}
}

