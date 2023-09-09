package defaultpackage;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Customer extends Thread {

	private int customerName;
	private SleepingBarber sb;
	private SleepingBarber sb2;
	private Lock customerLock;
	private Condition gettingHaircutCondition;
	public Customer(int customerName, SleepingBarber sb, SleepingBarber sb2) {
		this.customerName = customerName;
		this.sb = sb;
		this.sb2 = sb2;
		customerLock = new ReentrantLock();
		gettingHaircutCondition = customerLock.newCondition();
	}
	
	public int getCustomerName() {
		return customerName;
	}
	public void startingHaircut() {
		Util.printMessage("Customer " + customerName + " is getting hair cut.");
	}
	public void finishingHaircut() {
		Util.printMessage("Customer " + customerName + " is done getting hair cut.");
		try {
			customerLock.lock();
			gettingHaircutCondition.signal();
		} finally {
			customerLock.unlock();
		}
	}
	public void run() {
		boolean seatsAvailable = SleepingBarber.addCustomerToWaiting(this);
		if (!seatsAvailable) {
			Util.printMessage("Customer " + customerName + " leaving...no seats available.");
			return;
		}
		if (sb.isSleeping()) //will awake barber 1 regardless of whether both are sleeping or just barber 1
		{
			sb.wakeUpBarber();
		}
		else //otherwise barber one is awake
		{
			sb2.wakeUpBarber();
		}
		try {
			customerLock.lock();
			gettingHaircutCondition.await();
		} catch (InterruptedException ie) {
			System.out.println("ie getting haircut: " + ie.getMessage());
		} finally {
			customerLock.unlock();
		}
		Util.printMessage("Customer " + customerName + " is leaving.");
	}
}
