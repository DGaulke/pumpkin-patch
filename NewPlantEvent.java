package pumpkins;
/**
 * The NewPlantEvent occurs when a Plant is instantiated and goes to live
 * in its Patch's unripe collection.
 */
public class NewPlantEvent extends PumpkinEvent {
	private final static String EVENT_DESCRIPTION = "New plant";
	private Program program;
	private Patch patch;
	private Jack jack;
	/**
	 * Initialize an event
	 */
	public NewPlantEvent(Program program, Patch patch, Jack jack){
		this.program = program;
		this.patch = patch;
		this.jack = jack;
		this.silent = false;
	}
	/**
	 * Execute event
	 */
	public void run(){
		Plant plant = new Plant(this.program, this.patch, this.jack);	
		this.patch.add(plant); 
	}
	/**
	 * Display event as String for log
	 */
	public String toString(){
		return NewPlantEvent.EVENT_DESCRIPTION;
	}
}
