package domain

import org.scalatest.freespec.AnyFreeSpec

class test extends AnyFreeSpec {

  "Trabajos" - {
    "un heroe con 100 de fuerza base y nivel 1 siendo guerrero debe tener 120 de fuerza" in {

      val atributo = Atributos(100, 50)
      val unGuerrero = Heroe(atributo, 1, 100, Guerrero, Introvertido, Heroico())
      assert(unGuerrero.getFuerza() == 120)
    }
    "un ladron con habilidad base 30 y nivel 2 debe tener habilidad final 36" in {

      val heroe = Ladrón(30)
      assert(heroe.habilidadEnSusManos(2)   == 36)
    }
    "un mago cuando tiene el nivel para aprender un hechizo, adquiera dicho hechizo" in {

      val hechizos = List(HechizoAprendible(2, Vislumbrar), HechizoAprendible(10, AprobarElTP) )
      var heroe = Mago(hechizos)
      assert(heroe.conoceElHechizo(Vislumbrar,2))
    }
  }

  "Grupos" - {
    "si la salud de un aventurero baja a cero, es eliminado del grupo" in {
      var atributos = Atributos(100, 50)
      var hechizos = List(HechizoAprendible(2, Vislumbrar))
      val ladron = Heroe(atributos, 2, 100, Ladrón(30), Loquitos, Heroico())
      val guerrerouno = Heroe(atributos, 2, 100, Guerrero, Loquitos, Heroico())
      val guerrerodos = Heroe(atributos, 2, 100, Guerrero, Loquitos, Heroico())
      val mago = Heroe(atributos, 2, 8, Mago(hechizos), Loquitos, Heroico())

      var heroes=List[Heroe](guerrerouno,ladron,guerrerodos,mago)
      var cofre=Cofre(List(Ganzúas,Llave),List("chuchillo","pistola"),45)
      var grupo = Grupo( heroes,cofre,List.empty)

      val resuno = Habitacion(NoPasaNada,List.empty).recorrerHabitacion(grupo)
      assert(resuno.cantidadDeVivos()==4)

      // Se muere el MAGO por que el dardo quita 10 de salud y tiene 8
      val resdos = Habitacion(MuchosMuchosDardos,List.empty).recorrerHabitacion(grupo)
      assert(resdos.cantidadDeVivos()==3)
    }

    "si una de las habitaciones tiene trampa de leones, muere el mas lento" in {
      var atributos = Atributos(100, 50)
      var atributosMasLento = Atributos(100, 5)
      var hechizos = List(HechizoAprendible(2, Vislumbrar))
      val ladron = Heroe(atributos, 2, 100, Ladrón(30), Loquitos, Heroico())
      val guerrerouno = Heroe(atributos, 2, 100, Guerrero, Loquitos, Heroico())
      val guerrerodos = Heroe(atributosMasLento, 2, 100, Guerrero, Loquitos, Heroico())
      val mago = Heroe(atributos, 2, 100, Mago(hechizos), Loquitos, Heroico())

      var heroes=List[Heroe](guerrerouno,ladron,guerrerodos,mago)

      var cofre=Cofre(List(Ganzúas,Llave),List("chuchillo","pistola"),45)
      //var grupo=GrupoVivo(heroes,cofre,Habitacion((NoPasaNada,TrampaDeLeones),Puerta((NoPasaNada,TesoroPerdido),(Cerrada,Escondida))))
      var grupo=Grupo(heroes,cofre,List.empty)


/*      val calabozo = new Calabozo(Puerta(Some(Habitacion(TrampaDeLeones,List.empty)),List.empty),Puerta(None,List.empty))
      assert(calabozo.recorrer(grupo).cantidadDeVivos() == 3)*/

      val restres = Habitacion(TrampaDeLeones,List.empty).recorrerHabitacion(grupo)
      assert(restres.cantidadDeVivos() === 3)
    }
  }

  "Calabozo - con grupo perdido" - {
    val heroeAEncontrarse: Heroe = Heroe(Atributos(10,2),5,25,Mago(List(HechizoAprendible(15,Vislumbrar))),Bigotes,Heroico())

    val puertaCD: Puerta = Puerta(Habitacion/*D*/(MuchosMuchosDardos, List()),List(Encantada(AprobarElTP)),"CD")
    val puertaFG: Puerta = Puerta(Habitacion/*G*/(TrampaDeLeones, List()),List(Escondida),"FG")
    val puertaEF: Puerta = Puerta(Habitacion/*F*/(TesoroPerdido(Ganzúas), List(puertaFG)),List(Encantada(Vislumbrar)),"EF")
    val puertaEH: Puerta = Puerta(Habitacion/*H*/(Encuentro(heroeAEncontrarse), List()),List(Escondida),"EH")
    val puertaBC: Puerta = Puerta(Habitacion/*C*/(Encuentro(heroeAEncontrarse), List(puertaCD)),List(Escondida),"BC")
    val puertaAE: Puerta = Puerta(Habitacion/*E*/(NoPasaNada, List(puertaEF,puertaEH)),List(Cerrada,Escondida),"AE")
    val puertaAB: Puerta = Puerta(Habitacion/*B*/(NoPasaNada, List(puertaBC)),List(Cerrada,Escondida),"AB")
    val puertaPrincipal: Puerta = Puerta(Habitacion/*A*/(NoPasaNada, List(puertaAB,puertaAE)),List(Escondida,Cerrada),"Principal")

    val calabozoTenebroso: Calabozo = new Calabozo(puertaPrincipal,puertaCD)

    var atributos = Atributos(100, 50)
    var atributosMasLento = Atributos(100, 5)
    var hechizos = List(HechizoAprendible(1, Vislumbrar))
    val ladron = Heroe(atributos, 0, 100, Ladrón(0), Loquitos, Heroico())
    val guerrerouno = Heroe(atributos, 2, 100, Guerrero, Loquitos, Heroico())
    val guerrerodos = Heroe(atributosMasLento, 2, 100, Guerrero, Loquitos, Heroico())
    val mago = Heroe(atributos, 2, 100, Mago(hechizos), Loquitos, Heroico())

    val heroicos: List[Heroe] = List(ladron,guerrerouno,guerrerodos,mago)
    var cofre=Cofre(List(Ganzúas,Llave),List("chuchillo","pistola"),45)

    val grupoFantastico: Grupo = Grupo(heroicos,cofre) ;

    val grupoDespDelRecorrido = calabozoTenebroso.recorrerCalabozo(grupoFantastico)
    println(grupoDespDelRecorrido)
    println(grupoDespDelRecorrido.grupo._heroes.map(h=> (h.saludActual,h.trabajo,h.nivel)))
    println(grupoDespDelRecorrido.grupo.puertasAbiertas.map(p=> p.nombre))
    println()
  }

  "Calabozo con grupo exitoso" - {

    val puertaSalida: Puerta = Puerta(Habitacion/*B*/(NoPasaNada, List()),List(Cerrada))
    val puertaPrincipal: Puerta = Puerta(Habitacion/*A*/(NoPasaNada, List(puertaSalida)),List(Cerrada))

    val calabozoTenebroso: Calabozo = new Calabozo(puertaPrincipal,puertaSalida)

    var atributos = Atributos(100, 50)
    var atributosMasLento = Atributos(100, 5)
    var hechizos = List(HechizoAprendible(2, Vislumbrar))
    val ladron = Heroe(atributos, 2, 100, Ladrón(30), Loquitos, Heroico())
    val guerrerouno = Heroe(atributos, 2, 100, Guerrero, Loquitos, Heroico())
    val guerrerodos = Heroe(atributosMasLento, 2, 100, Guerrero, Loquitos, Heroico())
    val mago = Heroe(atributos, 2, 100, Mago(hechizos), Loquitos, Heroico())

    val heroicos: List[Heroe] = List(ladron,guerrerouno,guerrerodos,mago)
    var cofre=Cofre(List(Llave),List("chuchillo","pistola"),45)
    val grupoFantastico: Grupo = Grupo(heroicos,cofre) ;

    val grupoDespDelRecorrido = calabozoTenebroso.recorrerCalabozo(grupoFantastico)
    println(grupoDespDelRecorrido)
    println(grupoDespDelRecorrido.grupo._heroes.map(h=> (h.saludActual,h.trabajo,h.nivel)))
    println(grupoDespDelRecorrido.grupo.puertasAbiertas.map(p=> p.nombre))
    println()
  }

  "Calabozo con grupo muerto" - {

    val puertaSalida: Puerta = Puerta(Habitacion/*B*/(NoPasaNada, List()),List(Cerrada))
    val atributosExtranjero = Atributos(300,50)
    val asesino = Heroe(atributosExtranjero,2,100,Guerrero,Loquitos,Vidente())
    val puertaPrincipal: Puerta = Puerta(Habitacion/*A*/(Encuentro(asesino), List(puertaSalida)),List(Cerrada))
    val calabozoTenebroso: Calabozo = new Calabozo(puertaPrincipal,puertaSalida)
    val atributos = Atributos(5, 50)
    val atributosMasLento = Atributos(5, 5)
    val hechizos = List(HechizoAprendible(2, Vislumbrar))
    val ladron = Heroe(atributos, 2, 5, Ladrón(30), Loquitos, Heroico())
    val guerrerouno = Heroe(atributos, 2, 5, Guerrero, Loquitos, Heroico())
    val guerrerodos = Heroe(atributosMasLento, 2, 5, Guerrero, Loquitos, Heroico())
    val mago = Heroe(atributos, 2, 5, Mago(hechizos), Loquitos, Heroico())

    val heroicos: List[Heroe] = List(ladron,guerrerouno,guerrerodos,mago)
    var cofre=Cofre(List(Llave),List("chuchillo","pistola"),45)

    val grupoFantastico: Grupo = Grupo(heroicos,cofre) ;

    val grupoDespDelRecorrido = calabozoTenebroso.recorrerCalabozo(grupoFantastico)
    println(grupoDespDelRecorrido)
    println(grupoDespDelRecorrido.grupo._heroes.map(h=> (h.saludActual,h.trabajo,h.nivel)))
    println(grupoDespDelRecorrido.grupo.puertasAbiertas.map(p=> p.nombre))
    println()
    }

  "Calabozo con grupo exitoso" - {

    val puertaSalida: Puerta = Puerta(Habitacion/*B*/(NoPasaNada, List()),List(Cerrada))
    val puertaPrincipal: Puerta = Puerta(Habitacion/*A*/(NoPasaNada, List(puertaSalida)),List(Cerrada))

    val calabozoTenebroso: Calabozo = new Calabozo(puertaPrincipal,puertaSalida)

    var atributos = Atributos(100, 50)
    var atributosMasLento = Atributos(100, 5)
    var hechizos = List(HechizoAprendible(2, Vislumbrar))
    val ladron = Heroe(atributos, 2, 100, Ladrón(30), Loquitos, Heroico())
    val guerrerouno = Heroe(atributos, 2, 100, Guerrero, Loquitos, Heroico())
    val guerrerodos = Heroe(atributosMasLento, 2, 100, Guerrero, Loquitos, Heroico())
    val mago = Heroe(atributos, 2, 100, Mago(hechizos), Loquitos, Heroico())

    val heroicos: List[Heroe] = List(ladron,guerrerouno,guerrerodos,mago)
    var cofre=Cofre(List(Llave),List("chuchillo","pistola"),45)
    val grupoFantastico: Grupo = Grupo(heroicos,cofre) ;

    val grupoDespDelRecorrido = calabozoTenebroso.recorrerCalabozo(grupoFantastico)
    println(grupoDespDelRecorrido)
    println(grupoDespDelRecorrido.grupo._heroes.map(h=> (h.saludActual,h.trabajo,h.nivel)))
    println(grupoDespDelRecorrido.grupo.puertasAbiertas.map(p=> p.nombre))
    println()
  }
}