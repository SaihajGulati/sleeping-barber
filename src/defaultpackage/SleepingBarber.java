package defaultpackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SleepingBarber extends Thread {

	private static int maxSeats;
	private static int totalCustomers;
	private static List<Customer> customersWaiting;
	private Lock barberLock;
	private Condition sleepingCondition;
	private boolean moreCustomers;
	private String name;
	private boolean sleeping;
	
	public boolean isSleeping()
	{
		return sleeping;
	}
	
	public SleepingBarber(String n) {
		maxSeats = 3;
		totalCustomers = 10;
		moreCustomers = true;
		customersWaiting = Collections.synchronizedList(new ArrayList<Customer>()); 
		//could also use vector and not need collections, but vector is deprecated bc it's stupid to use bc synchs each individual operation which makes no sense instead of sequence of operations (look up for more)
		barberLock = new ReentrantLock();
		sleepingCondition = barberLock.newCondition();
		name = n;
		this.start();
		sleeping = false;
	}
	public static synchronized boolean addCustomerToWaiting(Customer customer) {
		if (customersWaiting.size() == maxSeats) {
			return false;
		}
		Util.printMessage("Customer " + customer.getCustomerName() + " is waiting");
		customersWaiting.add(customer);
		String customersString = "";
		for (int i=0; i < customersWaiting.size(); i++) {
			customersString += customersWaiting.get(i).getCustomerName();
			if (i < customersWaiting.size() - 1) {
				customersString += ",";
			}
		}
		Util.printMessage("Customers currently waiting: " + customersString);
		return true;
	}
	public void wakeUpBarber() {
		try {
			sleeping = false;
			barberLock.lock();
			sleepingCondition.signal();
		} finally {
			barberLock.unlock();
		}
	}
	public void run() {
		while(moreCustomers) {
			while(!customersWaiting.isEmpty()) {
				Customer customer = null;
				synchronized(this) {
					customer = customersWaiting.remove(0);
				}
				customer.startingHaircut();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
					System.out.println(name + " ie cutting customer's hair" + ie.getMessage());
				}
				customer.finishingHaircut();
				Util.printMessage(name + " checking for more customers...");		
			}
			try {
				barberLock.lock();
				sleeping = true;
				Util.printMessage(name + " has no customers, so time to sleep...");
				sleepingCondition.await();
				sleeping = false;
				Util.printMessage("Someone woke " + name + " up!");
			} catch (InterruptedException ie) {
				System.out.println(name + " ie while sleeping: " + ie.getMessage());
			} finally {
				barberLock.unlock();
			}
		}
		Util.printMessage(name + " all done for today!  Time to go home!");
		
	}
	public static void main(String [] args) {
		SleepingBarber sb = new SleepingBarber("Barber 1");
		SleepingBarber sb2 = new SleepingBarber("Barber 2");
		
		ExecutorService executors = Executors.newCachedThreadPool();
		for (int i=0; i < SleepingBarber.totalCustomers; i++) {
			Customer customer = new Customer(i, sb, sb2);
			executors.execute(customer);
			try {
				Random rand = new Random();
				int timeBetweenCustomers = rand.nextInt(2000);
				Thread.sleep(timeBetweenCustomers);
			} catch (InterruptedException ie) {
				System.out.println("ie in customers entering: " + ie.getMessage());
			}
		}
		executors.shutdown();
		while(!executors.isTerminated()) {
			Thread.yield();
		}
		Util.printMessage("No more customers coming today...");
		sb.moreCustomers = false;
		sb2.moreCustomers = false;
		sb.wakeUpBarber();
		sb2.wakeUpBarber();
	}
}
