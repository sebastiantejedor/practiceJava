import java.util.Scanner;
import java.util.ArrayList;
public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome Back!!");

        Motorcycle myMotorcycle = new Motorcycle("Suzuki", "GIXXER SF FI 150 ABS", 2027, 12690000);
        myMotorcycle.displayInfo();

        // Call the overloaded methods
        int result = suma(5, 10, 15, 5);
        
        Car myCar = new Car("Toyota", "Camry", 202, 20000);

        Student myStudent = new Student();
        myStudent.name = "Sebastian";
        myStudent.code = 7502610039L;
        myStudent.age = 17;
        myStudent.semester = "2th";
        myStudent.displayInfo();
        myCar.displayInfo();


        Humano myHumano = new Humano("Alvaro", 40);
        Humano myHumano2 = new Humano("Dayana", 39);
        myHumano.comer();
        myHumano.mostrarInformacion();
        myHumano.edadIncrementada();
        myHumano2.comer();
        myHumano2.mostrarInformacion();
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
                System.out.println("You can't pass, sorry.");
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

    // ArrayList practice
    static class ArrayListPractice{
        static void prueba() {

        ArrayList<String> names = new ArrayList<String>();

        names.add("Dayana");
        names.add("Alvaro");
        names.add("Sebastian");
        names.add("Santiago");

            for (String i : names) {
                System.out.println(i);
            }

        }
    }

    static void hola(String name) {
        System.out.println("Hola, " + name + "!");
    }

    // Overloaded methods for summation
    static int suma(int a, int b) {
        System.out.printf("The sum of %d and %d is: %d\n", a, b, a + b);
        return a + b;
    }
    static int suma(int a, int b, int c) {
        System.out.printf("The sum of %d, %d, and %d is: %d\n", a, b, c, a + b + c);
        return a + b + c;
    }
    static int suma(int a, int b, int c, int d) {
        System.out.printf("The sum of %d, %d, %d, and %d is: %d\n", a, b, c, d, a + b + c + d);
        return a + b + c + d;
    }
} 
