package pumpkins;
import java.util.*;
/**
 * The OrderGenerator class creates pumpkin orders for Jack to fulfill.
 */
public class OrderGenerator {
	private final static int MEAN_ORDER_INTERVAL = 120;
	private final static int DELAY = 100000;
	private final Program program;
	private final Jack jack;
	/**
	 * Create an instance of OrderGenerator for the given Program and Jack
	 * objects
	 */
	public OrderGenerator(Program program, Jack jack){
		this.program = program;
		this.jack = jack;
		
		//Set first timer to begin creating orders
		int duration = this.program.randomUniform(OrderGenerator.MEAN_ORDER_INTERVAL);
		duration += OrderGenerator.DELAY;
		PumpkinEvent event = new OrderEvent(this);
		this.program.setTimer(event, duration);
	}
	// Place a new pumpkin order for Jack
	public boolean placeOrder(){
		if (this.program.timerExpired()) 
			return false; // STOP GENERATING ORDERS;

		this.jack.receiveOrder();
		this.setNextTimer(); 
		return true;
	}
	// Set timer for next order
	private void setNextTimer(){
		int duration = this.program.randomUniform(OrderGenerator.MEAN_ORDER_INTERVAL);
		PumpkinEvent event = new OrderEvent(this);
		this.program.setTimer(event, duration);
	}
}
