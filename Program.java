package pumpkins;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;
/**
 * The Program class governs a multithreaded system that models a pumpkin 
 * patch and a business of selling pumpkins as they ripen.
 */
public class Program {
	protected final static int TIME_UNITS = 1000000;
	private final Clock clock;
	private final Patch patch;
	private final Jack jack;
	private final OrderGenerator orderGenerator;
	private final Random random;
	private final ExecutorService threadPool;
	private final File logFile;
	private PrintWriter writer;
	private volatile boolean active;
	/**
	 * Program entry point. Creates and executes a Program object.
	 */
	public static void main(String[] args){
		Program p = new Program();
		p.execute();
	}	
	/**
	 * Initializes and performs setup for a new program. Logs output
	 * to program directory in format pumpkinlog_m_d_yyyy_h_m_s
	 * Begin by calling execute() method.
	 */
	public Program() {
		//Set member variable initial values
		this.active = true;
		this.logFile = Program.getLogFile();
		try {
			this.writer = new PrintWriter(this.logFile);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		this.random = new Random();
		this.threadPool = Executors.newFixedThreadPool(10);
		this.clock = new Clock(this);
		this.jack = new Jack(this);
		this.patch = new Patch(this, this.jack);
		this.jack.setPatch(this.patch);
		this.patch.setup(); //Create plants
		this.orderGenerator = new OrderGenerator(this, jack);
		//Notify Jack when timer expires
		PumpkinEvent event = new TimerExpiredEvent(this.jack);
		this.setTimer(event, Program.TIME_UNITS);
	}
	/**
	 * Begin program execution. Stops automatically after Program.TIME_UNITS
	 * have elapsed and all orders have been filled.
	 */
	public void execute() {
		//Start clock which will drive events
		this.threadPool.execute(this.clock);
		//Wait until program says it's done	
		synchronized(this){
			try {
				while (this.active){
					this.wait();
				}
			} catch (InterruptedException ie){
				ie.printStackTrace();
			}
		}
		this.shutdown();
	}
	// Stop program - called by Jack when timer is expired and all orders
	// are filled
	void stop(){
		this.active = false;
		synchronized(this){
			this.notifyAll();
		}
	}
	// Runs program events asynchronously with thread pool
	void processEvent(PumpkinEvent event){
		this.threadPool.execute(new Runnable(){
			public void run(){
				event.run();
				if (!event.isSilent())
					log(event.toString());
			}
		});
	}
	// Exposes clock timestamp to program objects
	int getTimestamp(){
		return this.clock.getTimestamp();
	}
	// Indicates whether alloted program time has expired
	boolean timerExpired(){
		return this.clock.expired();
	}
	// Registers a timer with the clock to issue event when it expires 
	void setTimer(PumpkinEvent event, int duration){
		this.clock.setTimer(event, duration);
	}
	// Closes resources, stops threads, issues final log message
	private void shutdown(){
		this.clock.stop();
		this.threadPool.shutdown();
		this.log(this.jack.completedOrderReport());
		this.writer.close();
	}	
	// Calculates a random uniform value with given mean
	int randomUniform(int mean){
		return (int)(-1 * mean * Math.log(random.nextDouble()));		
	}
	// Calculates a random Gaussian value with given mean and variance
	int randomGaussian(int mean, int variance){
		return (int)(variance * random.nextGaussian()) + mean;
	}
	// Provides a filename for event log - format pumpkinlog_m_d_yyyy_h_m_s
	private static File getLogFile(){
		StringBuilder fileName = new StringBuilder();
		Calendar c = new GregorianCalendar();
		fileName.append("pumpkinlog_");
		fileName.append((c.get(Calendar.MONTH) + 1) + "_");
		fileName.append(c.get(Calendar.DAY_OF_MONTH) + "_");
		fileName.append(c.get(Calendar.YEAR) + "_");
		fileName.append(c.get(Calendar.HOUR_OF_DAY) + "_");
		fileName.append(c.get(Calendar.MINUTE) + "_");
		fileName.append(c.get(Calendar.SECOND));
		String filePath = System.getProperty("user.dir") + File.separator +
		   	"pumpkins" + File.separator + fileName.toString();
		return new File(filePath);
	}	
	// Sends formatted event message to log file
	void log(String message){
		System.out.printf("%d: %s - stash = %d - field = %d%n",
				this.clock.getTimestamp(), message,
				this.jack.getStashCount(),
				this.patch.getPumpkinCount());
		
		this.writer.printf("%d: %s - stash = %d - field = %d%n",
				this.clock.getTimestamp(), message,
				this.jack.getStashCount(),
				this.patch.getPumpkinCount()); 
	}
}
