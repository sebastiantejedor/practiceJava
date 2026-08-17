package POO;
import POO.Car;
import POO.Humano;
import POO.Motorcycle;
import POO.Student;

public class Main {
    public static void main(String[] args) {

        Motorcycle myMotorcycle = new Motorcycle("Suzuki", "GIXXER SF FI 150 ABS", 2027, 12690000);
        myMotorcycle.displayInfo();

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
}