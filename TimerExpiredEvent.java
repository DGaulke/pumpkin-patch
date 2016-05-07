package pumpkins;
/**
 * The TimerExpiredEvent occurs when the pumpkin program's clock runs out.
 * It notifies Jack, who is responsible for the program shutdown conditions.
 */
public class TimerExpiredEvent extends PumpkinEvent {
	private final Jack jack;
	/**
	 * Create an event
	 */
	public TimerExpiredEvent(Jack jack){
		this.jack = jack;
		this.silent = true;
	}
	/*
	 * Execute event
	 */
	public void run(){
		this.jack.timerExpired();
	}
}
