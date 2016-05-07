package pumpkins;
import java.util.*;
/**
 * The Patch class maintains collections of ripe and unripe pumpkin plants.
 * Ripe pumpkins may be picked, and plants may be pulled and used for compost.
 */
public class Patch {
	public final static int SEEDS_PER_PLANT = 4;
	private final static int PLANTS_BEGIN = 1000;
	private final static int PLANTS_TO_COMPOST = 5;
	private final Program program;
	private final Jack jack;
	private final List<Plant> unripe;
	private final List<Plant> ripe;

	/**
	 * Create a new pumpkin patch
	 */
	public Patch(Program program, Jack jack){
		this.program = program;
		this.jack = jack;
		this.unripe = new ArrayList<Plant>();
		this.ripe = new ArrayList<Plant>();
	}
	/**
	 * Add initial quantity of plants to the patch
	 */
	public void setup(){
		for (int i = 0; i < Patch.PLANTS_BEGIN; i++)
			this.program.processEvent(new NewPlantEvent(this.program, this, this.jack));
	}
	/**
	 * Add new plant to patch
	 */
	public void add(Plant plant){
		synchronized(this.unripe){
			this.unripe.add(plant);
		}
	}
	/**
	 * Get number of ripe plants in patch
	 */
	public int getPumpkinCount(){
		synchronized(this.ripe){
			return this.ripe.size();
		}
	}
	/**
	 * Pick a number of ripe plants from the patch
	 */
	public void pick(int quantity){
		List<Plant> picked = new ArrayList<Plant>(quantity);
		synchronized(this.ripe){
			for (int i = 0; i < quantity; i++)
				picked.add(this.ripe.remove(0));
		}
		synchronized(this.unripe){
			for(Plant p : picked){ 
				p.pick();
				this.unripe.add(p);
			}
		}
	}
	/**
	 * Choose some plants from the patch to use for compost starting
	 * with the unripe plants
	 */
	public void compost(){
		int count = Patch.PLANTS_TO_COMPOST;
		synchronized(this.unripe){
			while (this.unripe.size() > 0 && count > 0){
				this.unripe.remove(0);
				count--;
			}
		}
		if (count == 0)
			return;
		synchronized(this.ripe){
			while (this.ripe.size() > 0 && count > 0){
				this.ripe.remove(0);
				count--;
			}
		}
	}
	// Move plants from the unripe collection to the ripe collection
	void ripen(Plant plant){
		boolean found = false;
		synchronized(this.unripe){
			found = this.unripe.remove(plant);
		}
		if (found)
			synchronized(this.ripe){
				this.ripe.add(plant);
			}
	}
}
