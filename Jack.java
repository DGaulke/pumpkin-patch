package pumpkins;
import java.util.*;
/**
 * Jack is a complicated fellow in the Pumpkin program. He receives notice from
 * ripening pumpkins, upon which he gathers all available. He also receives
 * orders, upon which he brings all outstanding orders to be shipped off.
 * Both activites take time, and cannot be performed concurrently.
 */
public class Jack {
	private final static int MEAN_SHIP_INTERVAL = 60;
	private final static int MEAN_SHIP_DEVIATION = 20;
	private final static int PICK_TIME = 2;
	private final static int STASH_MAX = 10000;
	private final static int REFILL_LEVEL = 9000;
	private final static int STASH_MIN = 1000;
	private final Program program;
	private final Random random;
	private Patch patch;
	private final List<Order> openOrders;
	private final List<Order> completedOrders;
	private volatile int stashCount;
	private volatile int inTransit;
	private volatile boolean stashFull;
	private volatile boolean stashLow;
	private volatile boolean active;
	private volatile boolean shipFlag;
	private volatile boolean gatherFlag;
	/**
	 * Create an instance of Jack and freshen up his attributes
	 */
	public Jack(Program program){
		this.program = program;
		this.random = new Random();
		this.openOrders = new ArrayList<Order>();
		this.completedOrders = new ArrayList<Order>();
		this.stashCount = 0;
		this.inTransit = 0;
		this.stashFull = false;
		this.stashLow = false;
		this.active = false;
		this.shipFlag = false;
		this.gatherFlag = false;
	}
	/**
	 * Assign a Patch to Jack where he can pick pumpkins
	 */
	public void setPatch(Patch patch){
		this.patch = patch;
	}
	/**
	 * Receive a single order and track it. If Jack is busy, tell him to
	 * ship when he can. Otherwise he's now busy and goes off to ship the
	 * order (if his stash is not empty)
	 */	
	public void receiveOrder(){
		Order o = new Order();
		synchronized(this.openOrders){
			openOrders.add(o);
		}
		synchronized(this){
			if (this.active){
				this.shipFlag = true;
			} else { 
				this.active = true;
				PumpkinEvent event = new ShipEvent(this);
				this.program.processEvent(event);
			}
		}	
	}
	/**
	 * Get the number of pumpkins in Jack's stash
	 */
	public int getStashCount(){
		return this.stashCount;
	}
	/**
	 * Send Jack to the patch to pick pumpkins. After a duration of 
	 * Jack.PICK_TIME * (# available pumpkins), they will be added to his 
	 * stash in a CompleteGatherEvent
	 */
	public void gather(Patch patch){
		int quantity = patch.getPumpkinCount();	
		this.gatherFlag = false;
		if (quantity == 0)
			return;

		PumpkinEvent event = new CompleteGatherEvent(this, patch, quantity);
		this.program.setTimer(event, quantity * Jack.PICK_TIME); 
	}
	/**
	 * Jack picks as many pumpkins at the patch as he can fit in his stash.
	 * If it becomes full, he stops gathering for awhile. If his stash gets
	 * too low, he takes a ripe pumpkin from the patch and plants more 
	 * pumpkins. Once complete, he checks his messages to see if he needs to
	 * deliver any orders or go back to the patch for more pumpkins. If not,
	 * he rests.
	 */
	protected void completeGather(Patch patch, int quantity){

		if (!this.stashFull && // Check if stash will become full now
				(Jack.STASH_MAX - this.stashCount) <= quantity){
			this.stashFull = true;	
			this.compost(patch); // Producing too fast - compost some plants
			int available = this.patch.getPumpkinCount(); // Recalculate
			quantity = Math.min(available, Jack.STASH_MAX - this.stashCount);
		}
		if (this.stashLow){ // If stash is running low, plant more pumpkins
			sow(patch);
			--quantity;
			this.stashLow = false;
		}

		// Replenish pumpkin stash
		patch.pick(quantity);
		this.incrementStash(quantity);

		// Determine what to do next
		if (this.shipFlag){
			PumpkinEvent event = new ShipEvent(this);
			this.program.processEvent(event);
		} else if (this.gatherFlag && !this.stashFull){
			PumpkinEvent event = new BeginGatherEvent(this, this.patch);
			this.program.processEvent(event);
		} else {
			this.active = false;
		}

	}
	/**
	 * Begin the process of completing orders. Completes as many open orders
	 * as allowed by stashCount
	 */
	public void deliverShipment(){
		int available, actual;
		List<Order> fulfilled = new ArrayList<Order>();

		// Quit if stash is empty
		if ((available = this.stashCount) == 0){
			this.shipFlag = false;
			this.active = false;
			return;
		}
		
		// Take orders out of "open" collection until amount == stash	
		synchronized(this.openOrders){
			Iterator<Order> i = this.openOrders.iterator();
			while (i.hasNext() && (fulfilled.size() < available)){
				fulfilled.add(i.next());
				i.remove();
			}
			this.shipFlag = false;
		}
		
		// Quit if no orders to fulfill
		if ((actual = fulfilled.size()) == 0){
			this.active = false;
			return;
		}
		
		// Track number shipped so final shipment can identify itself
		synchronized(this){
			this.inTransit += actual;
		}
		
		// Register timers for order completions after delivery
		int duration = program.randomGaussian(60, 20);
		for (Order o : fulfilled){
			this.decrementStash();
			PumpkinEvent event = new CompleteOrderEvent(this, o);
			program.setTimer(event, duration); 
		}

	}
	/**
	 * Jack completes a pumpkin order when he arrives at the shipping
	 * facility. The order is moved to the "completed" collection" and
	 * Jack recalculates his stash strategy. If the timer has expired and
	 * this is the final order, Jack signals the end of the program. Otherwise
	 * he checks whether he needs to go collect pumpkins again or fulfull
	 * more orders. If not, he rests.
	 */
	public void completeOrder(Order order){
		// Complete the actual order object and move it to the completed pile
		order.complete();
		synchronized(this.completedOrders){
			this.completedOrders.add(order);
		}

		// Stash was full, but now it's time to start collecting again
		if (this.stashFull && this.stashCount <= Jack.REFILL_LEVEL)
			this.stashFull = false;

		// Stash is getting low - Jack remembers to plant seeds next time he
		// gathers pumpkins
		if (this.stashCount <= Jack.STASH_MIN)
			this.stashLow = true;

		// Determine if this is the last or only order in transit	
		boolean allOrdersComplete = false;
		synchronized(this){
			if (--this.inTransit == 0)
				allOrdersComplete = true;
		}

		// Check for program shutdown conditions: Timer has expired and this
		// is the last order in transit
		boolean shutdown = false;
		if (this.program.timerExpired() && allOrdersComplete)
			synchronized(this.openOrders){
				if (this.openOrders.size() == 0)
					shutdown = true;
			}
		// Tell program OK to stop
		if (shutdown)
			this.program.stop();

		// If this is the last order in transit, determine what to do next
		if (allOrdersComplete){
			if (this.gatherFlag && !this.stashFull){
				PumpkinEvent event = new BeginGatherEvent(this, this.patch);
				this.program.processEvent(event);
			} else if (this.shipFlag){
				PumpkinEvent event = new ShipEvent(this);
				this.program.processEvent(event);
			} else { this.active = false; }
		}
	} // Jack receives a signal from the patch that pumpkins have ripened
	// If he's busy, he gets a message. If not, he becomes busy and goes
	// to the patch to gather pumpkins.
	synchronized void patchReady(Patch patch){
		if (this.active){
			this.gatherFlag = true;
		} else if (!this.stashFull) {
			this.active = true;
			PumpkinEvent event = new BeginGatherEvent(this, patch);
			this.program.processEvent(event);
		}
	}
	// Jack's stash has become full as he is producing them faster than
	// necessary, so he composts some of the plants to slow production
	void compost(Patch patch){
		patch.compost();
	}
	// Jack's stash has become low so he takes a plant from the patch
	// and plants more pumpkins
	void sow(Patch patch){
		patch.pick(1);
		for (int i = 0; i < Patch.SEEDS_PER_PLANT; i++)
			this.program.processEvent(new NewPlantEvent(this.program, patch, this));
	}
	private synchronized void incrementStash(int quantity){
		this.stashCount += quantity;
		//Cannot exceed STASH_MAX
		if (stashCount > Jack.STASH_MAX)
			stashCount = Jack.STASH_MAX;
	}
	private synchronized void decrementStash(){
		--this.stashCount;
	}
	// Jack is notified when the program's time has run out
	void timerExpired(){
		// If he still has open orders, ignore
		synchronized(this.openOrders){
			if (this.openOrders.size() > 0)
				return;
		}
		// If he has orders in transit, ignore
		if (this.inTransit > 0)
			return;

		// Tell program OK to stop
		this.program.stop();
		
	}
	// Calculate mean order completion time and standard deviation
	protected String completedOrderReport(){
		int count, sum;
		double mean, variance, deviationSum, stdDeviation;
		synchronized(this.completedOrders){
			Iterator<Order> i = this.completedOrders.iterator();
			sum = 0;
			count = 0;
			while (i.hasNext()){
				Order o = i.next();
				sum += o.getDuration();
				++count;
			}
			mean = (double)sum / count;

			deviationSum = 0;
			i = this.completedOrders.iterator();
			while (i.hasNext()){
				Order o = i.next();
				double deviation = o.getDuration() - mean;
				deviationSum += Math.pow(deviation, 2); 
			}
		}
		variance = deviationSum / count;
		stdDeviation = Math.sqrt(variance);
		return "Mean order completion time = " + String.format("%.2f", mean) + 
				"; standard deviation = " + String.format("%.2f", stdDeviation);

	}
	// Simple Order class to track when orders were placed and completed
	class Order {
		private final int timestamp;
		private int duration;
		Order(){
			this.timestamp = Jack.this.program.getTimestamp();
		}
		void complete(){
			this.duration = Jack.this.program.getTimestamp() - this.timestamp;
		}
		int getDuration(){
			return this.duration;
		}
	}

}
