import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import cs2030.simulator.EventManager;
import cs2030.simulator.Event;
import cs2030.simulator.Customer;
import cs2030.simulator.Server;
import cs2030.simulator.State;

class Main4 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int numOfServers = sc.nextInt();
        int maxQueue = sc.nextInt();
        int numOfCustomers = sc.nextInt();
        int id = 0;


        List<Customer> custs = new ArrayList<>();
        while (id < numOfCustomers) {
            double arrivalTime = sc.nextDouble();
            double serviceTime = sc.nextDouble();
            id++;
            custs.add(new Customer(id, arrivalTime, serviceTime, State.ARRIVES));
        }

        List<Server> servers = new ArrayList<>();
        int count = 0;
        while (count < numOfServers) {
            count++;
            servers.add(new Server(count, 0.0, maxQueue));
        }

        count = 0;
        List<Double> restTimes = new ArrayList<Double>();
        while (count < numOfCustomers) {
            double restTime = sc.nextDouble();
            restTimes.add(restTime);
            count++;
        }



        EventManager simulator = new EventManager(servers, custs, restTimes);
        simulator.run();
    }   
}

