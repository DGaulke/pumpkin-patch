package pumpkins;
import pumpkins.Jack.*;
/**
 * The CompleteOrderEvent occurs when Jack delivers a pumpkin to the shipping
 * facility. It is preceded by a ShipEvent and a pause while Jack makes the
 * delivery.
 */
public class CompleteOrderEvent extends PumpkinEvent {
	private final static String EVENT_DESCRIPTION = "Order completed"; 
	private final Jack jack;
	private final Order order;
	/**
	 * Initialize an event
	 */	
	public CompleteOrderEvent(Jack jack, Order order){
		this.jack = jack;
		this.order = order;
		this.silent = false;
	}
	/**
	 * Execute event
	 */
	public void run(){
		this.jack.completeOrder(this.order);
	}

	/** 
	 * Describes the event for external display 
	 */
	public String toString(){
		return CompleteOrderEvent.EVENT_DESCRIPTION + 
			(this.order == null ? "" : " (" + this.order.getDuration() + ")");
	}
}
