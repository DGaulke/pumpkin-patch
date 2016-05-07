package pumpkins;
/**
 * The RipenEvent occurs to a Plant object when its timer expires. A plant 
 * becomes pickable, and in the patch it moves from unripe to ripe, and
 * it notifies Jack.
 */
public class RipenEvent extends PumpkinEvent {
	private final static String EVENT_DESCRIPTION = "Pumpkin ripens"; 
	private final Program program;
	private final Plant plant;
	private final Patch patch;
	private final Jack jack;
	/**
	 * Create a new RipenEvent
	 */
	public RipenEvent(Program program, Plant plant, Patch patch, Jack jack){
		this.program = program;
		this.plant = plant;
		this.patch = patch;
		this.jack = jack;
		this.silent = false;
	}
	/**
	 * Execute event
	 */
	public void run(){
		this.plant.ripen();
		this.patch.ripen(this.plant);
		this.plant.notifyJack();
	}
	/**
	 * Represent as string for log
	 */
	public String toString(){
		return RipenEvent.EVENT_DESCRIPTION;
	}

}
