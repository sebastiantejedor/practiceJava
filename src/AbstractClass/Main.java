package AbstractClass;

public class Main {
    public static void main(String[] args) {
        Auto auto = new Auto("Toyota", "Corolla", 4);
        Moto moto = new Moto("Honda", "CBR500R", 2);

        auto.acelerar();
        moto.acelerar();

    }
}
