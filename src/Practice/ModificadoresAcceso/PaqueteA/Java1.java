package Practice.ModificadoresAcceso.PaqueteA;

public class Java1 {
    String nombre;
    String apellido;

    public Java1(String nombre,String apellido){
        this.nombre = nombre;
        this.apellido = apellido;
    }

    public void MostrarInfo(){
        System.out.println("Nombre: " + nombre);
        System.out.println("Apellido: " + apellido);
    }

}
