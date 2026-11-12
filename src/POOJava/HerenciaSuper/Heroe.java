package POOJava.HerenciaSuper;

    public class Heroe extends Persona {
    String Poder;

    Heroe(String name, String poder, int edad) {
        super(name, edad);
        this.Poder = poder;
    }
}
