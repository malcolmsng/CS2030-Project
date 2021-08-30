import java.util.Scanner;
import cs2030.simulator.EventManager;

class Main5 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int numServers = sc.nextInt();
        int maxQueue = sc.nextInt();
        int numCusts = sc.nextInt();
        int seed = sc.nextInt();
        double lambda = sc.nextDouble();
        double mu = sc.nextDouble();
        double rho = sc.nextDouble();
        double restChance = sc.nextDouble();
        double greedChance = sc.nextDouble();
        EventManager.run(numServers, maxQueue, numCusts, seed, lambda, mu, rho, 
            restChance, greedChance);
    }
}
