package AbstractClass;

public class Auto extends Vehiculo {
    private int numPuertas;

    public Auto(String marca, String modelo, int numPuertas) {
        super(marca, modelo);
        this.numPuertas = numPuertas;
    }

    @Override
    public void acelerar() {
        System.out.println("El auto " + marca + " " + modelo + " está acelerando.");
    }
}
