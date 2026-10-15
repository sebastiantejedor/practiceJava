package AbstractClass;

public class Moto extends Vehiculo {
    private int numRuedas;

    public Moto(String marca, String modelo, int numRuedas) {
        super(marca, modelo);
        this.numRuedas = numRuedas;
    }

    @Override
    public void acelerar() {
        System.out.println("La moto " + marca + " " + modelo + " está acelerando.");
    }
}
