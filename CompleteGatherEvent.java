package pumpkins;
/**
 * The CompleteGatherEvent occurs when Jack completes the process of picking
 * pumpkins in the patch. It is preceded by a CompleteGatherEvent and a pause 
 * while Jack is picking pumpkins = number of pumpkins * Jack.PICK_TIME.
 */
public class CompleteGatherEvent extends PumpkinEvent {
	public final static String EVENT_DESCRIPTION = "Jack gathers pumpkins";
	private final Jack jack;
	private final Patch patch;
	private final int quantity;
	/**
	 * Initialize an event
	 */	
	public CompleteGatherEvent(Jack jack, Patch patch, int quantity){
		this.jack = jack;
		this.patch = patch;
		this.quantity = quantity;
		this.silent = false;
	}
	/**
	 * Execute event
	 */
	public void run(){
		jack.completeGather(this.patch, this.quantity);
	}
	/**
	 * Describes the event for external display
	 */
	public String toString(){
		return CompleteGatherEvent.EVENT_DESCRIPTION;
	}

}
