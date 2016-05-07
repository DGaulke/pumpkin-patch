package pumpkins;
/**
 * The Plant class represents a plant in a pumpkin patch
 */
public class Plant {
	private final static int MEAN_RIPEN_INTERVAL = 50000;
	private final static int MEAN_RIPEN_DEVIATION = 10000;
	private final Program program;
	private final Patch patch;
	private final Jack jack;
	private volatile boolean ripe;
	/**
	 * Create a new plant in the given patch, with a Jack to notify
	 * when it ripens
	 */
	public Plant(Program program, Patch patch, Jack jack){
		this.program = program;
		this.patch = patch;
		this.jack = jack;
		this.pick();
	}	
	/**
	 * Change from unripe to ripe
	 */
	public void ripen(){
		this.ripe = true;
	}
	/**
	 * notify Jack when ripe (via Patch)
	 */	
	void notifyJack(){
		this.jack.patchReady(patch);
	}
	// Pick ripe pumpkin from patch and set new timer to ripen again
	void pick(){
		this.ripe = false;
		if (this.program.timerExpired())
			return;
		int duration = this.program.randomGaussian(
				Plant.MEAN_RIPEN_INTERVAL, Plant.MEAN_RIPEN_DEVIATION);
		PumpkinEvent event = 
				new RipenEvent(this.program, this, this.patch, this.jack);
		this.program.setTimer(event, duration);
	}
}
