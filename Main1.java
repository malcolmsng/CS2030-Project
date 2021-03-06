import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import cs2030.simulator.EventManager;
import cs2030.simulator.Event;
import cs2030.simulator.Customer;
import cs2030.simulator.Server;
import cs2030.simulator.State;

class Main1 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int numOfServers = sc.nextInt();
        int id = 0;
        List<Customer> custs = new ArrayList<>();
        while (sc.hasNextDouble()) {
            double arrivalTime = sc.nextDouble();
            id++;
            custs.add(new Customer(id, arrivalTime));
        }
        List<Server> servers = new ArrayList<>();
        int count = 0;
        while (count < numOfServers) {
            count++;
            servers.add(new Server(count, 0.0));
        }
        EventManager simulator = new EventManager(servers, custs);
        simulator.run();
    }   
}

