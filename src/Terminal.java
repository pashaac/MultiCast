import java.util.Scanner;

/**
 * Created by Asadchiy Pavel
 * on 19.12.15.
 */
public class Terminal implements Runnable {
    private boolean wasQuite;
    private String address;

    @Override
    public void run() {
        Scanner in = new Scanner(System.in);
        System.out.println("Quite (Q) :");
        System.out.println("Otherwise, enter address like ***.*.*.* : ");
        while (!Thread.interrupted()) {
            final String next = in.next();
            if (next.startsWith("Q") || next.startsWith("q")) {
                wasQuite = true;
                break;
            }
            final String[] split = next.split("\\.");
            if (split.length == 4) {
                address = next;
                System.out.println("Quite (Q) :");
                System.out.println("Otherwise, enter address like ***.*.*.* : ");
            }
        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isWasQuite() {
        return wasQuite;
    }

}
