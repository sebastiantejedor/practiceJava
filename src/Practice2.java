public class Practice2 {
    String diaSemana = "Viernes";

    public void main() {
        if (diaSemana.equals("Lunes") || diaSemana.equals("Martes") || diaSemana.equals("Miércoles") || diaSemana.equals("Jueves")) {
            System.out.println("Es un día de semana.");
        } else if (diaSemana.equals("Viernes")) {
            System.out.println("Es viernes, el fin de semana está cerca.");
        } else if (diaSemana.equals("Sábado") || diaSemana.equals("Domingo")) {
            System.out.println("Es fin de semana, disfruta tu descanso.");
        } else {
            System.out.println("Día no válido.");
        }
    }
}
