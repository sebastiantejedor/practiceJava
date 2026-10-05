package POO1;

public class Animal {
    String nombre;
    int edad;

    Animal(String nombre, int edad) {
        this.nombre = nombre;
        this.edad = edad;
    }

    public static void main(String[] args) {

        Gato gato1 = new Gato("Michi", 3);
    }
}
