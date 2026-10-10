package POO3;

public class Main {
    public static void main(String[] args) {
        Heroe heroe1 = new Heroe("Rodger", "Fuerza sobrehumana", 30);
        Heroe heroe2 = new Heroe("Luffy", "Goma Goma", 19);

        System.out.println("Poder del héroe 1: " + heroe1.Poder);
        System.out.println("Nombre del héroe 1: " + heroe1.name);
        System.out.println("Edad del héroe 1: " + heroe1.edad);
        System.out.println();
        System.out.println("Poder del héroe 2: " + heroe2.Poder);
        System.out.println("Nombre del héroe 2: " + heroe2.name);
        System.out.println("Edad del héroe 2: " + heroe2.edad);
    }
}
