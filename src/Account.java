import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Account {

    private double balance = 0;

    public Account(double balance) {
        this.balance = balance;
    }

    public double getBalance() {
        return balance;
    }

    public synchronized void deposit(double amount) throws InterruptedException {
        double bal = getBalance();
        if (amount <= 0) {
            wait();
            throw new IllegalArgumentException("Can't deposit!");
        }

        bal += amount;
        this.balance = bal;
        System.out.println("Deposit " + amount + "... new balance: " + bal);
    }

    public synchronized void withdraw(double amount) throws InterruptedException {
        double bal = getBalance();
        if (amount > bal) {
            wait();
            throw new IllegalArgumentException("Wrong amount!");
        }

        bal -= amount;
        this.balance = bal;
        notifyAll();
        System.out.println("Withdraw " + amount + "... new balance: " + bal);
    }

    public void setBalance(double bal) {
        balance = bal;
    }
}

class Transaction implements Runnable {
    private final String transaction;
    private final Account account;
    private final double amount;

    public Transaction(String transaction, Account account, double amount) {
        // Set the account & balance
        this.transaction = transaction;
        this.account = account;
        this.amount = amount;
    }


    public synchronized void run() {
        // make a deposit
        if (Objects.equals(transaction, "deposit")) {
            try {
                account.deposit(amount);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (Objects.equals(transaction, "withdraw")) {
            // make a withdrawal
            try {
                account.withdraw(amount);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}

class AccountTest {

    public static void main(String[] args) {
        Account account1 = new Account(100);
        List<Transaction> task = Collections.synchronizedList(new ArrayList<>());

        task.add(new Transaction("deposit", account1, 30));
        task.add(new Transaction("deposit", account1, 20));
        task.add(new Transaction("withdraw", account1, 10));
        task.add(new Transaction("withdraw", account1, 30));
        task.add(new Transaction("deposit", account1, 50));
        task.add(new Transaction("withdraw", account1, 20));

        // execute Thread Pool
        ExecutorService executorService = Executors.newCachedThreadPool();

        System.out.println("Current balance is " + account1.getBalance());

        // start transactions
        for (Transaction transaction : task) {
            executorService.execute(transaction);
        }
        // shut down ExecutorService
        executorService.shutdown();

    } // end main()
}

