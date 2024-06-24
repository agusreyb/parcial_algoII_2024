import java.time.LocalDate

// Falta mejorar

// punto 1: no implementás template method, repetís el código en cada subclase de Lugar, nombres poco representativos
// punto 2: el Alternado repite lógica
// punto 3: originalmente pensé que no lo habías resuelto, está dentro de lo que marcaste como punto 4,
//          falta un paso antes (quién conoce a todas las personas para elegirle un tour),
//          falta validar que el tour no esté completo,
//          la implementación es rara y hay mucho acoplamiento innecesario entre los objetos que participan del proceso
// punto 4: falta el observer que cambia el alternante, la interfaz con AFIP no es correcta, hay una clase intermedia innecesaria (MiddleMan), se crean los observers en el momento de confirmar y eso es raro, hay una validación innecesaria
// en el diagrama de clases, el Combinado no tiene asociación con el EstadoPersona

/////////////////// PUNTO 1 ////////////////
//me pareció implementar template method ya que
// defino una estructura general para evaluar si un lugar
// es divertido o tranquilo
// pero los detalles específicos varían según el tipo de lugar.
abstract class Lugar {
    var nombre: String = "El bayado"
    var codigoLugar: Int = 1234
    fun esDivertido(): Boolean{
        return tieneLetrasPares() && criterioDeDiversion()
    }

    abstract fun criterioDeDiversion(): Boolean

    abstract fun esTranquilo(): Boolean
    fun tieneLetrasPares() = nombre.length % 2 == 0
}

class Ciudad() : Lugar() {
    var cantidadHabitantes: Int = 1000000
    var atraccionesTuristicas: MutableList<String> = mutableListOf()
    var decibeles: Int = 1000

    override fun criterioDeDiversion(): Boolean {
        return ((atraccionesTuristicas.size > 3) && (cantidadHabitantes > 100000))
    }

    override fun esTranquilo(): Boolean {
        return decibeles < 20
    }
}

class Pueblo : Lugar() {
    var km: Int = 50
    var añoFundacion: Int = 1950
    var provincia: String = "Chubut"
    var listasLitoral: MutableList<String> = mutableListOf("Entre Rios", "Corrientes", "Misiones")

    override fun criterioDeDiversion(): Boolean {
        val esDelLitoral: Boolean = listasLitoral.contains(provincia)
        return ((añoFundacion < 1800) || (esDelLitoral))
    }

    override fun esTranquilo(): Boolean {
        return provincia == "La Pampa"
    }
}

class Balneario : Lugar() {
    var metrosPlaya: Int = 4500
    var esPeligroso: Boolean = false
    var tienePeatonal: Boolean = false

    override fun criterioDeDiversion(): Boolean {
        return ((metrosPlaya > 300) && esPeligroso)
    }

    override fun esTranquilo(): Boolean {
        return !tienePeatonal
    }
}

/////////////// PUNTO 2 //////////////////
//utlicé stratergy ya que las preferencias de una persona
// para los lugares que visita pueden variar

interface Preferencia {
    fun esAdecuado(lugar: Lugar): Boolean
    fun cambiarPreferencia(){}
}

class Tranquilo() : Preferencia {
    override fun esAdecuado(lugar: Lugar): Boolean {
        return lugar.esTranquilo()
    }
}

class Divertido() : Preferencia {
    override fun esAdecuado(lugar: Lugar): Boolean {
        return lugar.esDivertido()
    }
}

class Alternado() : Preferencia {
    private var alternar: Boolean = false
    override fun esAdecuado(lugar: Lugar): Boolean {
        return (if (alternar) Divertido() else Tranquilo()).esAdecuado(lugar)
    }
    override fun cambiarPreferencia() {
        alternar = !alternar
    }
}

class Combinado(val listaCristerios: MutableList<Preferencia>) : Preferencia {
    override fun esAdecuado(lugar: Lugar): Boolean {
        return listaCristerios.any { it.esAdecuado(lugar) }
    }
}

class Persona() {
    var nombre: String = "Canto"
    var dni: String = "41106994"
    var preferenciaDeVacaciones: Preferencia = Divertido()
    var presupuestoMaximo: Int = 56000
    var email: String = "a.bla@gmail.com"
    fun cambiarPreferencia() {
        preferenciaDeVacaciones.cambiarPreferencia()
    }

    fun esLugarAdecuado(lugar: Lugar): Boolean = preferenciaDeVacaciones.esAdecuado(lugar)
}

///////////////// PUNTO 3 ///////////////
class Tour() {
    val fechaSalida: LocalDate = LocalDate.of(2024, 5, 23)
    val cantRequerida: Int = 35
    val lugaresARecorrer: MutableList<Lugar> = mutableListOf()
    val montoPorPersona: Double = 2800.0
    val personas: MutableList<Persona> = mutableListOf()

    fun estaCompleto() = personas.size >= cantRequerida
    fun puedeAgregarPersona(persona: Persona): Boolean{
        return !estaCompleto() && persona.presupuestoMaximo>=montoPorPersona && lugaresARecorrer.all{persona.esLugarAdecuado(it)}
    }
    fun agregarPersona(persona: Persona) {
        personas.add(persona)
    }

    fun eliminarPersona(persona: Persona) {
        personas.remove(persona)
    }
    fun codigoLugares(): List<Int> {
        return lugaresARecorrer.map{it.codigoLugar}
    }

    fun dniParticipantes(): List<String> {
        return personas.map{it.dni}
    }

    fun cambiarPreferencia() {
        personas.forEach{it.cambiarPreferencia()}
    }

}
class ArmadorDeTours {
    val pendientes: MutableList<Persona> = mutableListOf()
    val personas = mutableListOf<Persona>()
    val tours = mutableListOf<Tour>()

    fun agregarTour(tour: Tour) {
        tours.add(tour)
    }

    fun ordenarTourPorMasBarato(): List<Tour> {
       return tours.sortedBy { it.montoPorPersona }
    }
    fun buscarTourAdecuado(persona: Persona): List<Tour>{
        return ordenarTourPorMasBarato().filter{it.puedeAgregarPersona(persona)}
    }

    fun armarTour(){
        personas.forEach{asignarTour(it,buscarTourAdecuado(it))}
    }

    fun asignarTour(persona: Persona, buscarTourAdecuado: List<Tour>) {
        if(buscarTourAdecuado.isEmpty()){
            pendientes.add(persona)
        }else{
            buscarTourAdecuado.first().agregarPersona(persona)
        }
    }

}
class AdministradorDeTour{
    val accionNotifiacion = mutableListOf<Notificacion>()

    fun agregarAccion(accion:Notificacion){
        accionNotifiacion.add(accion)
    }
    fun eliminarAccion(accion:Notificacion){
        accionNotifiacion.remove(accion)
    }
    fun agregarPersonaAlTour(persona:Persona, tour: Tour){
        tour.agregarPersona(persona)
    }
    fun eliminarPersonaDelTour(persona:Persona, tour: Tour){
        tour.eliminarPersona(persona)
    }
    fun confirmarTour(tour: Tour){
        accionNotifiacion.forEach{it.confirmar(tour)}
    }
}
/////////////// PUNTO 4 ///////////////
// Utilizo patrón Observer para manejar las notificaciones
// La clase AdministradorTours notifica a sus obvservadores cuando
//se confirma tour
interface Notificacion {
    fun confirmar(tour: Tour)
}

class EnviarEmailConfirmacion : Notificacion {
    lateinit var mailSender: MailSender
    override fun confirmar(tour: Tour) {
        val mailParticipantes: List<String> = tour.personas.map { it.email }
        val fechaLimitePago = if (LocalDate.now().isAfter(tour.fechaSalida.minusDays(30))) {
            LocalDate.now()
        } else {
            tour.fechaSalida.minusDays(30)
        }
        tour.personas.forEach { persona ->
            mailSender.sendMail(
                Mail(
                    from = "turismo@unsam.edu.ar",
                    to = persona.email,
                    subject = "Confirmación de Tour",
                    content = "Estimado/a ${persona.dni},\n\n" +
                            "Su tour ha sido confirmado. Aquí están los detalles:\n" +
                            "- Fecha de salida: ${tour.fechaSalida}\n" +
                            "- Fecha límite de pago: $fechaLimitePago\n" +
                            "- Lugares a visitar: ${tour.lugaresARecorrer.joinToString { it.nombre }}\n\n" +
                            "¡Gracias por confiar en nosotros!\n\n" +
                            "Saludos cordiales,\n" +
                            "Turismo UNSAM"
                )
            )
        }

    }
}

class InformarAFIP(val agencia: AFIP) : Notificacion {
    override fun confirmar(tour: Tour) {
        if(tour.montoPorPersona > 1000000){
            agencia.notificarMontoGrande(
                Mensaje(
                    codigosLugares = tour.codigoLugares().toMutableList(),
                    dniParticipantes = tour.dniParticipantes().toMutableList()
                )
            )
        }
    }
}

class ModificarPreferencia: Notificacion{
    override fun confirmar(tour: Tour) {
        tour.cambiarPreferencia()
    }
}
interface AFIP {
    fun notificarMontoGrande(mensaje: Mensaje)
}

data class Mensaje(
    val codigosLugares: MutableList<Int>,
    val dniParticipantes: MutableList<String>
)

interface MailSender {
    fun sendMail(mail: Mail)
}

data class Mail(val from: String, val to: String, val subject: String, val content: String)


