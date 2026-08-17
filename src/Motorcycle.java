public class Motorcycle {

    String make;
    String model;
    int year;
    int price;

    public Motorcycle(String make, String model, int year, int price) {
        this.make = make;
        this.model = model;
        this.year = year;
        this.price = price;
    }


    public void displayInfo() {
        System.out.println("Motorcycle 1:");
        System.out.println(year + " " + make + " " + model + " $" + price);
        IO.println("Finishing method...");
    }
}