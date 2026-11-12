package Practice.ModificadoresAcceso.PaqueteA;
import Practice.ModificadoresAcceso.PaqueteB.Java2;

public class Main {
    public static void main(String[] args) {
        Java1 obj1 = new Java1("Juan", "Perez");
        Java2 obj2 = new Java2("Maria", "Gomez");

        obj1.MostrarInfo();
    }
}
