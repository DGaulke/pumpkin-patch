package pumpkins;
/**
 * The OrderEvent class is created by an OrderGenerator and is registered
 * with the clock to be returned after a period of time units, so the 
 * OrderGenerator can place orders.
 */
public class OrderEvent extends PumpkinEvent {
	private final static String EVENT_DESCRIPTION = "Order placed"; 
	private final OrderGenerator generator;
	/**
	 * Initialize the event
	 */	
	public OrderEvent(OrderGenerator generator){
		this.generator = generator;
		this.silent = false;
	}
	/**
	 * Execute event
	 */
	public void run(){
		boolean completed = this.generator.placeOrder();
		if (!completed)
			this.silent = true;

	}
	/**
	 * Represent event for log
	 */
	public String toString(){
		return OrderEvent.EVENT_DESCRIPTION;
	}
}
