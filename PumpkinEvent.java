package pumpkins;
import java.util.*;
/**
 * The PumpkinEvent is an abstract event that is extended by the various
 * events in the pumpkin system. It has a silent property, if not silent
 * the applcation will log it. Most importantly, it is Runnable.
 */
public abstract class PumpkinEvent implements Runnable {
	protected boolean silent; // Set true to keep event from appearing in log
	public boolean isSilent(){
		return this.silent;
	}
}
