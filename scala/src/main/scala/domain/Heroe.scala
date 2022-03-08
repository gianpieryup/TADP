package domain

case class Heroe(val atributos : Atributos, val nivel : Int, val saludActual : Int ,
                 val trabajo : Trabajo,val compatibilidad : Compatibilidad, val criterioEleccion : CriterioEleccion){
  def getFuerza() : Int = {
    val adicional : Int =
      trabajo match {
        case Guerrero => (atributos.fuerzaBase * 0.2 * nivel).toInt
        case _ => 0
      }
    atributos.fuerzaBase + adicional
  }
  def vidaResultante (vidaAPerder : Int) : Int = math.max(0, saludActual - vidaAPerder);
  def bajarVida (vidaPerdida : Int ) : Heroe = this.copy(saludActual = saludActual - vidaPerdida);
  def subirNivel(nivelesGanados: Int) : Heroe = this.copy(nivel = nivel + nivelesGanados)

  def elegirPuerta(grupo : Grupo): Option[Puerta] = criterioEleccion.criterio.apply(grupo)
  def estoyVivo() : Boolean = saludActual > 0
  def getVelocidad() : Int = atributos.velocidadBase;
  def perderVida(vidaAPerder : Int) : Heroe = {
    if (vidaResultante(vidaAPerder) > 0){
      bajarVida(vidaAPerder);
    } else {
      bajarVida(saludActual)
    }
  }
  def seLlevaBien(grupo : Grupo) : Boolean = compatibilidad.criterio.apply(grupo)
}

case class Atributos(val fuerzaBase : Int, val velocidadBase : Int)

trait Trabajo

case object Guerrero extends Trabajo
case class Ladrón(val habilidadBase : Int) extends Trabajo{
  def habilidadEnSusManos(nivelLadron : Int):Int = habilidadBase + 3 * nivelLadron  // nunca se usa
}
case class Mago(val hechizosAprendibles : List[HechizoAprendible]) extends Trabajo{
  def conoceElHechizo(hechizo : Hechizo, nivelMago : Int) : Boolean = {
    hechizosAprendibles.exists(h => h.hechizo == hechizo && h.conoceElHechizo(nivelMago))
  }
}

case class HechizoAprendible(val nivelRequerido : Int, val hechizo : Hechizo){
  def conoceElHechizo(nivel : Int) : Boolean = {
    nivel >= nivelRequerido
  }
}

trait Hechizo
case object Vislumbrar extends Hechizo
case object AprobarElTP extends Hechizo

abstract class CriterioEleccion(val criterio : Grupo => Option[Puerta])
case class Heroico(_criterio : Grupo => Option[Puerta] = _.filtrarPuertasAbribles.lastOption) extends CriterioEleccion(_criterio)
case class Ordenado(_criterio : Grupo => Option[Puerta] = _.filtrarPuertasAbribles.headOption)extends CriterioEleccion(_criterio)
case class Vidente(_criterio : Grupo => Option[Puerta]= (grupo) => grupo.filtrarPuertasAbribles.sortBy(unaPuerta => unaPuerta.habitacion.recorrerHabitacion(grupo).puntaje()).lastOption) extends CriterioEleccion(_criterio)

trait Compatibilidad {
  type Personalidad = Grupo => Boolean
  val criterio: Personalidad
}
case object Introvertido extends Compatibilidad {
  override val criterio: Personalidad = _._heroes.length <= 3
}
case object Bigotes extends Compatibilidad{
  override val criterio: Personalidad = ! _._heroes.exists(h => h.trabajo match {
    case Ladrón(a) => true
    case _ => false
  })
}
case class Interesados(objParticular: Item) extends Compatibilidad{
  override val criterio: Personalidad = _._cofre.contieneItem(objParticular)
}
case object Loquitos extends Compatibilidad{
  override val criterio: Personalidad = _ => false
}