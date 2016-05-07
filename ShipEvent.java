package pumpkins;
/**
 * The ShipEvent occurs when Jack starts the process of delivering pumpkins
 * to the shipping facility. It is followed by one or more CompleteOrderEvents.
 */
public class ShipEvent extends PumpkinEvent {
	private final Jack jack;
	/**
	 * Initialize an event
	 */
	public ShipEvent(Jack jack){
		this.jack = jack;
		this.silent = true;
	}
	/**
	 * Execute event
	 */
	public void run(){
		this.jack.deliverShipment();
	}
}
