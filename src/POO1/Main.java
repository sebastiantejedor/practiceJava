package POO1;

public class Main {

    Auto auto1 = new Auto("Toyota", "Corolla", 2020);
    Garaje garaje = new Garaje();

    public static void main(String[] args) {
        Main main = new Main();
        main.garaje.estacionar(main.auto1);
    }
}
