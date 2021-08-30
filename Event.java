package cs2030.simulator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Event {
    private final Customer cust;
    private final Double time;
    private final Server server;

    public Event(Server server, Customer cust, Double time)  {
        this.server = server;
        this.cust = cust;
        this.time = time;
        
    }
    
    public Event(Customer cust, Double time) {
        this.cust = cust;
        this.time = time;
        this.server = null;
    }

    public Customer getCust() {
        return this.cust;
    }

    public Double getEventTime() {
        return this.time;
    }

    public State getState() {
        return this.cust.getState();
    }
    
    public Server getServer() {
        return this.server;
    }


    public String toString() {
        String temp = String.format("%.3f %s", this.time, this.cust.toString());
        if (cust.getState() == State.WAITS) {
            return temp + " at server " + server.getId();
        } else if (cust.getState() == State.SERVES) {
            return temp + " by server " + server.getId();
        } else if (cust.getState() == State.DONE) {
            return temp + " serving by server " + server.getId();
        } 
        
        return temp;
    }
}

