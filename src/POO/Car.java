package POO;

public class Car {
    String make;
    String model;
    int year;
    int price;

    public Car(String make, String model, int year, int price) {
        this.make = make;
        this.model = model;
        this.year = year;
        this.price = price;
    }

    public void displayInfo() {
        System.out.println("POO.Car 1:");
        System.out.println(year + " " + make + " " + model + " $" + price);
    }
}
