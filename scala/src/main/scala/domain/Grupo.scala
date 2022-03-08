package domain;

case class Grupo(val _heroes : List[Heroe], val _cofre : Cofre, val habitacionesRecorridas: List[Habitacion] = List(), val puertasAbiertas : List[Puerta] = List()) {
  def agregarPuerta(puerta: Puerta): Grupo = this.copy(puertasAbiertas = puertasAbiertas.appended(puerta))

  def agregarHabitacion(habitacionRecorrida: Habitacion): Grupo = {
    this.copy(habitacionesRecorridas = habitacionesRecorridas.appended(habitacionRecorrida))
  }

  def puntaje(): Int = cantidadDeVivos() * 10 - cantidadDeMuertos() * 5 + tamañoBotin() + nivelMasAlto()
  def tamañoBotin(): Int = _cofre.items.length
  def nivelMasAlto(): Int = _heroes.map(heroe => heroe.nivel).max

  def agregarHeroe(heroeExtranjero: Heroe): Grupo = this.copy(_heroes = _heroes.appended(heroeExtranjero))
  def filter (funcion: Heroe => Boolean) : Grupo = this.copy(_heroes = _heroes.filter(funcion));
  def exists (funcion: Heroe => Boolean) : Boolean = this._heroes.exists(funcion)
  def agregarABotin(item: Item) : Grupo = this.copy(_cofre = _cofre.agregarItem(item));
  def cantidadDeMuertos(): Int = {
    _heroes.count { unHeroe => !unHeroe.estoyVivo() };
  }
  // A es las puertas podes abrir ahora
  // B son las puertas abriste
  // A - B
  def filtrarPuertasAbribles : List[Puerta] = habitacionesRecorridas.flatMap(h => h.puertas.filter(p => p.puedoSerAbierta(this))).filter(pu => !(puertasAbiertas.contains(pu)))
  def cantidadDeVivos(): Int = {
    _heroes.length - cantidadDeMuertos();
  }

  def getLider(): Option[Heroe] = _heroes.find(_.estoyVivo())
  def estadoDelGrupo() : EstadoRecorrido = {
    if(this._heroes.exists(heroe => heroe.estoyVivo())){
      RecorridoEnProceso(this)
    }
    else{
      RecorridoFallidoPorMuerte(this);
    }
  }
  //Aca hay que devolver un Vivo
  def masLento(): Heroe = {
    _heroes.sortBy(uh => uh.getVelocidad()).head
  }

  def fuerzaTotal(): Int = {
    _heroes.foldRight[Int](0)(_.getFuerza()+_)
  }

  // ENCUENTRO ------------------------------------------
  def pelear(heroeExtranjero: Heroe): Grupo = {
    val fuerzaDelExtranjero = heroeExtranjero.getFuerza()
    if (fuerzaTotal() > fuerzaDelExtranjero) {
      aumentarNiveles(1)
    }
    else {
      // Todos los integrantes(vivos) pierden vida igualitariamente
      this.transformarHeroesVivos(h => h.perderVida((fuerzaDelExtranjero / cantidadDeVivos()).toInt))
    }
  }

  def hayLadrones(): Boolean = _heroes.exists(h => h.trabajo match {
    case l : Ladrón => true
    case _ => false
  })

  def contieneItem(unItem: Item): Boolean = _cofre.contieneItem(unItem)

  def transformarHeroesVivos(funcion: Heroe => Heroe ): Grupo = this.copy(_heroes = _heroes.map(unHeroe => if(unHeroe.estoyVivo()) {funcion.apply(unHeroe)} else unHeroe))

  def aumentarNiveles(niveles: Int): Grupo = {
    this.copy(_heroes = _heroes.map(h => if (h.estoyVivo()) {h.subirNivel(niveles)} else h))
  }
}

case class Cofre(val items : List[Item], val armas : List[String], val tesoroAcumulado : Int) {
  def agregarItem(item: Item): Cofre = this.copy(items = items.appended(item));
  def contieneItem(item: Item): Boolean = items.contains(item);
}

trait Item
case object Ganzúas extends Item
case object Llave extends Item

