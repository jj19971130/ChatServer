package groupwork.server;

import java.io.*;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainService {

    private static int port;
    static DatabaseManagement db;
    static PrintStream log;
    private static ExecutorService threadPool;

    static int createNewTCPSocketThread() throws IOException, ClassNotFoundException {
        Thread network = new TCPSocketManagement();
        network.start();
        return MainService.port;
    }

    static int createNewUDPSocketThread() throws SocketException {
        Thread network = new UDPSocketManagement();
        network.start();
        return MainService.port;
    }

    static void setPort(int port) {
        MainService.port = port;
    }

    public static void main(String args[]) {
        try {
            db = new DatabaseManagement();
            File file = new File("./log.txt");
            log = new PrintStream(new FileOutputStream(file, true), true); //MainService.log.println(new Date() + "[" + Thread.currentThread().getName() + "]:");
            file = new File("./File");
            if (!file.exists() || !file.isDirectory())
                file.mkdir();
        } catch (ClassNotFoundException | SQLException | IOException e) {
            e.printStackTrace();
        }
        Thread portRequestService = new PortRequestService();
        portRequestService.start();
        Scanner input = new Scanner(System.in);
        while (input.hasNext()) {
            String command = input.next();
            switch (command) {
                case "connectNum":
                    System.out.println(TCPSocketManagement.socketNum);
            }
        }
    }
}
