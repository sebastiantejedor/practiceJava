import java.util.Scanner;
public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome Back!!");
        Motorcycle myMotorcycle = new Motorcycle("Suzuki", "GIXXER SF FI 150 ABS", 2027, 12690000);
        myMotorcycle.displayInfo();

        bucleWhile x = new bucleWhile();
        forPractice y = new forPractice();
        bucleAnidado z = new bucleAnidado();
        bucleWhile.prueba();
        forPractice.prueba();
        bucleAnidado.prueba();

    }
    static class condicional {
        static void prueba() {
            int age;
            boolean invited;
            Scanner input = new Scanner(System.in);
            System.out.print("Enter your age: ");
            age = input.nextInt();

            System.out.println("Are you invited to the party? (true/false): ");
            invited = input.nextBoolean();

            if (age >= 18 && invited == true) {
                System.out.println("You can pass.");
            } else {
                System.out.println("You can´t pass, sorry.");
            }
        }
    }

    static class bucleWhile {
        static void prueba() {
            Scanner input = new Scanner(System.in);
            String name = "";

            while (name.isBlank()) {
                System.out.print("Enter your name: ");
                name = input.nextLine();
            }
            System.out.println("Welcome back, " + name + "!");
        }
    }

    static class hello{
        static void prueba() {
            System.out.println("Hello World!");
        }
    }

    static class forPractice {
        static void prueba() {
            for (int i = 1; i <= 10;) {
                System.out.println("Hello World! " + i);
                i++;
            }
        }
    }

    static class bucleAnidado{
        static void prueba() {
            Scanner input = new Scanner(System.in);
            int filas;
            System.out.print("Enter the number of rows: ");
            filas = input.nextInt();
            int columnas;
            System.out.print("Enter the number of columns: ");
            columnas = input.nextInt();
            String Simbolo;
            System.out.print("Enter the symbol to use: ");
            Simbolo = input.next();

            for (int i = 1; i <= filas; i++) {
                for (int j = 1; j <= columnas; j++) {
                    System.out.print(Simbolo);
                }
                System.out.println();
            }
        }
    }
}
