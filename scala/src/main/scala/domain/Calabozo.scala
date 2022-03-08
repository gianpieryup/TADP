package domain

trait Condicion extends (Grupo => Boolean)
class Calabozo(val puertaPrincipal : Puerta, val puertaSalida : Puerta) {

  def recorrerCalabozo(grupo: Grupo): EstadoRecorrido = {
    val recorrido: EstadoRecorrido = puertaPrincipal.habitacion.recorrerHabitacion(grupo.agregarPuerta(puertaPrincipal)).estadoDelGrupo()
    recorrido.recorrerHastaFallarOEncontrarSalida(puertaSalida)
  }
  def mejorGrupo(grupos: List[Grupo]): Grupo = grupos.sortBy(g => this.recorrerCalabozo(g).grupo.puntaje()).last
  def cuantosNivelesNecesitaElGrupo(grupo: Grupo): Option[Int] = {
    val numeros: List[Int] = (0 to 20).toList
    numeros.find(numero => recorrerCalabozo(grupo.aumentarNiveles(numero)) match {
      case RecorridoExitoso(_) => true
      case _ => false
    })
  }
}

case class Puerta(habitacion: Habitacion, dificultades : List[Dificultad], val nombre : String = null) {
  type Condicion = (Grupo => Boolean)
  val condicionBase: Condicion = grupo => {
    grupo._heroes.exists(heroe => heroe.trabajo match {
      case l : Ladrón  => l.habilidadEnSusManos(heroe.nivel) >= 20
      //case Ladrón(habilidadBase) => (habilidadBase + (heroe.nivel * 3)) >= 20
      case _ => false
    })
  }
  def puedoSerAbierta(grupo: Grupo): Boolean = condicionBase(grupo) || dificultades.forall(unaDificultad => unaDificultad.puedenSuperarDificultad(grupo))
}

trait Dificultad {
  def puedenSuperarDificultad(grupo: Grupo): Boolean = condicionesParaAbrir(grupo).exists(condicion => condicion.apply(grupo))

  def condicionesParaAbrir(grupo: Grupo): List[Condicion]
}

case object Cerrada extends Dificultad {
  override def condicionesParaAbrir(grupo: Grupo) : List[Condicion] = {
    val condicion1 : Condicion = grupo => grupo._cofre.items.contains(Llave)
    val condicion2 : Condicion = grupo => grupo.exists(heroe => heroe.trabajo match {
      case l : Ladrón  => l.habilidadEnSusManos(heroe.nivel) >= 10 || grupo._cofre.items.contains(Ganzúas)
      //case Ladrón(habilidadBase)  => (habilidadBase + (heroe.nivel * 3)) >= 10 || grupo._cofre.items.contains(Ganzúas)
      case _ => false
    })
    List(condicion1,condicion2)
  }
}

case object Escondida extends Dificultad{
  override def condicionesParaAbrir(grupo: Grupo) : List[Condicion] = {
    val condicion1 : Condicion = (grupo) => grupo.exists(heroe => heroe.trabajo match {
      case l : Ladrón  => l.habilidadEnSusManos(heroe.nivel) >= 6
      //case Ladrón(habilidadBase)  => (habilidadBase + (heroe.nivel * 3)) >= 6
      case _ => false
    })
    val condicion2 : Condicion = (grupo) => grupo.exists(heroe => heroe.trabajo match {
      case m : Mago => m.conoceElHechizo(Vislumbrar,heroe.nivel);
      case _ => false
    })
    List(condicion1,condicion2)
  }
}

case class Encantada(hechizoUtilizado: Hechizo) extends Dificultad(){
  override def condicionesParaAbrir(grupo: Grupo) : List[Condicion] = {
    val condicion1 : Condicion = (grupo) => grupo.exists(heroe => heroe.trabajo match {
      case m : Mago  => m.conoceElHechizo(hechizoUtilizado,heroe.nivel);
      case _ => false
    })
    List(condicion1)
  }
}


case class Habitacion(situacion: Situacion, puertas: List[Puerta]){
  def recorrerHabitacion(grupo: Grupo): Grupo = {
    situacion.enfrentaAGrupo(grupo).agregarHabitacion(this)
  }
}

trait Situacion{
  def enfrentaAGrupo(grupo: Grupo) : Grupo
}
case object NoPasaNada extends Situacion {
  def enfrentaAGrupo(grupo: Grupo): Grupo = grupo
}
case class TesoroPerdido(item: Item) extends Situacion {
  def enfrentaAGrupo(grupo: Grupo): Grupo = grupo.agregarABotin(item)
}
case object MuchosMuchosDardos extends  Situacion {
  def enfrentaAGrupo(grupo: Grupo): Grupo = grupo.transformarHeroesVivos(h => h.perderVida(10))
}
case object TrampaDeLeones extends Situacion {
  def enfrentaAGrupo(grupo: Grupo): Grupo = grupo.transformarHeroesVivos(h => if (h == grupo.masLento()){h.bajarVida(h.saludActual)} else h)
}
case class Encuentro(heroe: Heroe) extends Situacion {
  def enfrentaAGrupo(grupo : Grupo): Grupo = {
    if (heroe.seLlevaBien(grupo) && grupo.getLider().get.seLlevaBien(grupo.agregarHeroe(heroe))) {
      grupo.agregarHeroe(heroe)
    } else {
      grupo.pelear(heroe)
    }
  }
}
