package cs2030.simulator;

import java.util.PriorityQueue;
import java.util.Comparator;

public class Server {
    private final int id;
    private final PriorityQueue<Customer> queue;
    private final int maxQueue; 
    private final double[] nextAvailable;
    private final boolean[] isResting = new boolean[]{false};
    private final Customer[] current;
    
    public  Server(int id) {
        this.id = id;
        this.nextAvailable = new double[]{0.0};
        this.maxQueue = 1;
        this.queue = new PriorityQueue<Customer>(new CustomerComparator());
        this.current = new Customer[]{null};
    }

    public Server(int id, double nextAvailable) {
        this.id = id;
        this.nextAvailable = new double[]{nextAvailable};
        this.maxQueue = 1;
        this.queue = new PriorityQueue<Customer>(new CustomerComparator());
        this.current = new Customer[]{null};
    }


    public Server(int id, double nextAvailable, int maxQueue) {
        this.id = id;
        this.nextAvailable = new double[]{nextAvailable};
        this.maxQueue = maxQueue;
        this.queue = new PriorityQueue<Customer>(new CustomerComparator());
        this.current = new Customer[]{null};
    }
    
    
    Server(int id, double nextAvailable, int maxQueue, PriorityQueue<Customer> queue,
        Customer cust) {
        this.id = id;
        this.nextAvailable = new double[]{nextAvailable};
        this.maxQueue = maxQueue;
        this.queue = queue;
        this.current = new Customer[]{null};
    }

    public void update(double time) {
        nextAvailable[0] = time;
    }

    public void serve(double eventTime, Customer c) {
        double temp = c.getServiceTime() + eventTime;
        this.nextAvailable[0] = temp;
    }

    public boolean canServe(Customer c) {
        return (nextAvailable[0] <= c.getArrivalTime() && canQueue());
    }

    public void add(Customer c) {
        this.queue.add(c);
    }
  
    public void setCurrent(Customer c) {
        this.current[0] = c;
    }

    public void updateCurrent() {
        if (this.current[0] != null) {
            this.current[0] = null;
        }
        if (this.canQueue()) {
            this.current[0] = this.queue.peek();
        }
    }

    public int getId() {
        return this.id;
    }

    public boolean canQueue() {
        return (queue.size() < maxQueue);
    }
    
    public double getNext() {
        return this.nextAvailable[0];
    }

    public void dequeueAll() {
        if (!queue.isEmpty()) {
            for (Customer c : queue) {
                queue.poll();
            }
        }
    }

    public void dequeue() {
        if (!queue.isEmpty()) {
            queue.poll();
        }
    }

    public int inQueue() {
        return this.queue.size();
    }
    
    public Customer getWaiting() {
        return this.queue.peek();
    }

    public int max() {
        return this.maxQueue;
    }
    
    public boolean isResting() {
        return isResting[0];
    }

    public void setRest(boolean bool) {
        isResting[0] = bool;
    }

    public PriorityQueue<Customer> getQueue() {
        return this.queue;
    }
}

