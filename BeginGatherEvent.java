package pumpkins;
/**
 * The BeginGatherEvent occurs when Jack starts the process of picking pumpkins
 * in the patch. It is followed by a CompleteGatherEvent after the number of 
 * pumpkins * Jack.PICK_TIME.
 */
public class BeginGatherEvent extends PumpkinEvent {
	private final Jack jack;
	private final Patch patch;
	/**
	 * Initiailize an event
	 */	
	public BeginGatherEvent(Jack jack, Patch patch){
		this.jack = jack;
		this.patch = patch;
		this.silent = true;
	}
	/**
	 * Execute event
	 */
	public void run(){
		jack.gather(this.patch);
	}
}
