public class Humano {
    String nombre;
    int edad;

    public Humano(String nombre, int edad) {
        this.nombre = nombre;
        this.edad = edad;
    }

    void comer () {
        System.out.println(nombre + " está comiendo.");
    }

    void mostrarInformacion() {
        System.out.println("Nombre: " + nombre);
        System.out.println("Edad: " + edad);
        System.out.println("La edad incrementada de " + nombre + " es: " + (edadIncrementada()));
    }

    int edadIncrementada() {
        edad+=10;
        return edad;
    }
}
