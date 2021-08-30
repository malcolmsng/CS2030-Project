package cs2030.simulator;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Collections;
import java.util.stream.IntStream;
import cs2030.simulator.RandomGenerator;

public class EventManager {
    private final List<Server> servers;
    private final List<Customer> custs;
    private final List<Double> restTimes;

    public EventManager(List<Server> servers, List<Customer> custs) {
        this.servers = servers;
        this.custs = custs;
        this.restTimes = new ArrayList<Double>(this.custs.size());
        for (int i = 0; i < this.custs.size() + 1; i++) {
            this.restTimes.add(0.0);
        }
    }

    public EventManager(List<Server> servers, List<Customer> custs, List<Double> restTimes) {
        this.servers = servers;
        this.custs = custs;
        this.restTimes = restTimes;
    }


    public void run() {
        //sequence of events - ASAWSDAL 
        //how many customers triggered leave event - will be incremented
        int custsLeft = 0;

        //initialising recorder
        Recorder recorder = new Recorder();
        // creating arrival events and adding to events queue
        PriorityQueue<Event> events = new PriorityQueue<>(new EventComparator());
        custs.stream().forEach(x -> events.add(new Event(x, x.getArrivalTime())));
        
        int restCount = 0;
        while (events.peek() != null) {

            Event e = events.poll();
            Customer c = e.getCust();
            //printing out arrival events first

            for (Server s : servers) {
            
                if (e.getEventTime() >= s.getNext()) {
                    s.setRest(false);
                }
            }

            if (c.getState() == State.SERVES) {
                Server s = e.getServer();

                double totalTime = c.getServiceTime();

                for (Customer cust : s.getQueue()) {
                    if (cust.getDoneTime() <= s.getNext() && cust.getId() > c.getId() 
                        && cust.getDoneTime() != 0.0) {
                        totalTime += cust.getServiceTime();
                        
                    }
                }

                if ((Math.abs(s.getNext() - totalTime - e.getEventTime()) > 0) && 
                    (s.getNext() > e.getEventTime())) {
                    double correctTime = s.getNext() - totalTime + c.getServiceTime();
                    c.get("serves");
                    //correctTime);
                    Event serving = new Event(s, c, correctTime);
                    c.done(correctTime);
                    s.serve(serving.getEventTime(), c);
                    events.add(serving); 
                    continue;
                }
            }
                    
            System.out.println(e);

            if (c.getState() == State.ARRIVES) {
                boolean served = false;

                //finding idle servers

                for (Server s : servers) {
                    if (s.canServe(c) && (s.isResting() == false)) {
                        //creating served customer
                        c.get("serves");
                        // creating a served event
                        Event serving = new Event(s, c, c.getArrivalTime());
                        served = true;
                        // adding to events queue
                        events.add(serving);
                        break;

                    }
                }

                //if served is false -> wait event or leave event
                if (!served && (c.getGreed() == false)) {
                    //finding an empty queue
                    for (Server s : servers) {
                        if (s.canQueue()) {
                            //create waiting customer if there is empty queue
                            c.get("waits");
                            //create wait event
                            Event waiting = new Event(s, c, c.getArrivalTime());
                            served = true;
                            //adding waiting to events
                            events.add(waiting);
                            break;
                        }
                    }
                }

                if (!served && c.getGreed() == true) {
                
                    //finding an empty queue
                    List<Server> tempServers = new ArrayList<>(servers);
                    tempServers.sort(new ServerComparator());
                    for (Server s : tempServers) {
                        if (s.canQueue()) {
                            //create waiting customer if there is empty queue
                            c.get("waits");

                            //create wait event
                            Event waiting = new Event(s, c, c.getArrivalTime());
                            served = true;

                            //adding waiting to events
                            events.add(waiting);
                            break;
                        }
                    }
                }

                //no empty queue
                if (!served) {
                    //creating leaving customer
                    c.get("leaves");

                    //creating leaving event
                    Event leaving = new Event(c, c.getArrivalTime());

                    //adding leaving to queue
                    events.add(leaving);

                    //tallying left customers
                    custsLeft++;
                }


                //if next event in the queue is wait event
            } else if (c.getState() == State.WAITS) {
                //adding customer to queue
                Server s = e.getServer();
                double nextTime = s.getNext();
                c.get("serves");
                s.add(c); 
                //serve event 
                Event serving = new Event(s, c, s.getNext());
                c.done(serving.getEventTime());
                events.add(serving);
            
            } else if (c.getState() == State.SERVES) {
                Server s = e.getServer();
                //updating next available time for server
                double eventTime = e.getEventTime();
                c.get("done");
                s.setCurrent(c);    
                recorder.add(eventTime - c.getArrivalTime());
                if (!s.getQueue().isEmpty()) {
                    if (s.getWaiting().getId() == c.getId()) {
                        s.dequeue();
                    }
                }
                //updating servers list

                Event done = new Event(s, c, eventTime + c.getServiceTime());
                events.add(done);
                s.update(done.getEventTime());
            
            } else if (c.getState() == State.DONE) {
                Server s = e.getServer();
                double rest = restTimes.get(restCount);
                if (rest > 0.0) {
                    s.setRest(true);
                } else { 
                    s.setRest(false);
                }
                s.update(e.getEventTime() +  rest);
                restCount++;
            }
        }

        int custsServed = custs.size() - custsLeft;
        System.out.println(recorder.of(custsServed, custsLeft)); 

    }
    
    public static void run(int numServers, int maxQueue, int numCusts, int seed, 
        double lambda, double mu, double rho, double restChance, double greedChance) {
        RandomGenerator random = new RandomGenerator(seed, lambda, mu, rho);
        Recorder recorder = new Recorder(); 
        // initialising events, customers, and servers

        PriorityQueue<Event> events = new PriorityQueue<>(new EventComparator());

        List<Server> servers = new ArrayList<>();
        IntStream.range(0, numServers).forEach(x -> servers.add(new Server(x + 1, 0.0, maxQueue)));

        List<Customer> custs = new ArrayList<>();
        int count = 0;            
        double arrivalTime = 0.0;
        double serviceTime = 0.0;

        while (count < numCusts) {
            count++;                
            double greed = random.genCustomerType();
            Customer temp = new Customer(count, arrivalTime, serviceTime, greed < greedChance);
            custs.add(temp);
            arrivalTime += random.genInterArrivalTime();
        }

        List<Double> restTimes = new ArrayList<Double>();
            
        custs.stream().forEach(x -> events.add(new Event(x, x.getArrivalTime())));
        
        int custsLeft = 0;

        int restCount = 0;
        while (events.peek() != null) {

            Event e = events.poll();
            Customer c = e.getCust();
            //printing out arrival events first

            for (Server s : servers) {
                if (e.getEventTime() >= s.getNext()) {
                    s.setRest(false);
                }
            }

            if (c.getState() == State.SERVES) {
                Server s = e.getServer();
                double totalTime = c.getServiceTime();

                for (Customer cust : s.getQueue()) {
                    if (cust.getDoneTime() <= s.getNext() && cust.getId() > c.getId() 
                        && cust.getDoneTime() != 0.0) {
                        totalTime += cust.getServiceTime();
                        
                    }
                }

                if ((Math.abs(s.getNext() - totalTime - e.getEventTime()) > 0) && 
                    (s.getNext() > e.getEventTime())) {
                    double correctTime = s.getNext() - totalTime + c.getServiceTime();
                    c.get("serves");
                    Event serving = new Event(s, c, correctTime);
                    c.done(serving.getEventTime());
                    s.serve(serving.getEventTime(), c);
                    events.add(serving); 
                    continue;
                }
            }
                    
            System.out.println(e);

            if (c.getState() == State.ARRIVES) {
                boolean served = false;

                //finding idle servers

                for (Server s : servers) {
                    if (s.canServe(c) && (s.isResting() == false)) {
                        //creating served customer
                        c.get("serves"); 
                        // creating a served event
                        Event serving = new Event(s, c, c.getArrivalTime());
                        served = true;
                        // adding to events queue
                        events.add(serving);
                        break;

                    }
                }

                //if served is false -> wait event or leave event
                if (!served && (c.getGreed() == false)) {
                    //finding an empty queue
                    for (Server s : servers) {
                        if (s.canQueue()) {
                            //create waiting customer if there is empty queue
                            c.get("waits");
                            //create wait event
                            Event waiting = new Event(s, c, c.getArrivalTime());
                            served = true;
                            //adding waiting to events
                            events.add(waiting);
                            break;
                        }
                    }
                }

                if (!served && c.getGreed() == true) {
                    List<Server> temp = new ArrayList<>(servers);
                    temp.sort(new ServerComparator());
                    //finding an empty queue
                    for (Server s : temp) {
                        if (s.canQueue()) {
                            //create waiting customer if there is empty queue
                            c.get("waits");

                            //create wait event
                            Event waiting = new Event(s, c, c.getArrivalTime());
                            served = true;

                            //adding waiting to events
                            events.add(waiting);
                            break;
                        }
                    }
                }

                //no empty queue
                if (!served) {
                    //creating leaving customer
                    c.get("leaves");

                    //creating leaving event
                    Event leaving = new Event(c, c.getArrivalTime());

                    //adding leaving to queue
                    events.add(leaving);

                    //tallying left customers
                    custsLeft++;
                }


                //if next event in the queue is wait event
            } else if (c.getState() == State.WAITS) {
                //adding customer to queue
                Server s = e.getServer();
                c.get("serves"); 
                s.add(c); 
                //serve event 
                Event serving = new Event(s, c, s.getNext());
                events.add(serving);
                //updating wait times
            
            } else if (c.getState() == State.SERVES) {
                Server s = e.getServer();
                //updating next available time for server
                double eventTime = e.getEventTime();
                c.get("done");
                c.serving(random.genServiceTime());
                c.done(e.getEventTime());
                s.serve(e.getEventTime(), c);
                s.setCurrent(c);
                recorder.add(eventTime - c.getArrivalTime());
                if (!s.getQueue().isEmpty()) {
                    if (s.getWaiting().getId() == c.getId()) {
                        s.dequeue();
                    }
                }
                //updating server q

                Event done = new Event(s, c, eventTime + c.getServiceTime());
                s.update(done.getEventTime());
                events.add(done);
            
            } else if (c.getState() == State.DONE) {
                Server s = e.getServer();
                double restChanceActual = random.genRandomRest();
                if (restChanceActual < restChance) {
                    double restTime = random.genRestPeriod();
                    restTimes.add(restTime);
                } else {
                    restTimes.add(0.0);
                }

                double rest = restTimes.get(restCount);
                if (rest > 0.0) {
                    s.setRest(true);
                } else { 
                    s.setRest(false);
                }
                s.updateCurrent();
                s.update(e.getEventTime() +  rest);
                restCount++; 
            }
        }
        int custsServed = custs.size() - custsLeft;
        System.out.println(recorder.of(custsServed, custsLeft)); 
    }


}
