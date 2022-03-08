module Properties
  def mandatory
    1
  end
  def optional
    2
  end
  def ambos
    3
  end
end

module Name
  def name(nombre)
    proc do |un_metodo|
      nombre.match?(un_metodo.to_s)
    end
  end
end

module Has_Parameters
  include Properties
  def has_parameters(cantidad, criterio = ambos)
    proc do |metodo|
      parametros = @origen.instance_method(metodo).parameters
      if(criterio == mandatory)
        parametros = parametros.select{ |un_parametro| un_parametro.at(0).to_s == "req"}
      end

      if(criterio == optional)
        parametros = parametros.select{ |un_parametro| un_parametro.at(0).to_s == "opt"}
      end

      if(criterio.is_a? Regexp)
        parametros = parametros.select{ |un_parametro| criterio.match?(un_parametro.at(1).to_s)}
      end

      parametros.size == cantidad
    end
  end
end

module Neg
  def neg(*args)
    proc do |un_metodo|
      args.none? do |una_condicion|
        una_condicion.call(un_metodo)
      end
    end
  end
end

module Inject
  def inject(un_hash)
    @metodos_filtrados.each do |metodo_filtrado|
      parametros_nuevos = []
      @origen.instance_method(metodo_filtrado.to_sym).parameters.each do |parametro|

        parametro_nuevo = { es_nuevo: false, valor: nil}

        if un_hash.key? parametro[1]
          param = un_hash[parametro[1]]
          parametro_nuevo[:es_nuevo] = true
          parametro_nuevo[:valor] = param
        end

        parametros_nuevos.push(parametro_nuevo)
      end

      metodo_nuevo = "#{metodo_filtrado}_original".to_sym
      @origen.alias_method metodo_nuevo, metodo_filtrado.to_sym
      @origen.define_method(metodo_filtrado.to_sym) do |*args|
        if parametros_nuevos.size > args.size
          args.concat(Array.new(parametros_nuevos.size - args.size))
        end

        nuevo_args = args.map.with_index do |x,i|
          if parametros_nuevos[i][:es_nuevo]
            if parametros_nuevos[i][:valor].is_a? Proc
              parametros_nuevos[i][:valor].call(@origen,metodo_filtrado,x)
            else
              parametros_nuevos[i][:valor]
            end
          else
            x
          end
        end
        send(metodo_nuevo, *nuevo_args)
      end
    end
  end
end

module RedirectTo
  def redirect_to(objeto)
    @metodos_filtrados.each do |metodo_filtrado|
      redireccion = proc do |*args|
        objeto.send(metodo_filtrado.to_sym,*args)
      end
        @origen.define_method(metodo_filtrado.to_sym,redireccion)
    end
  end
end

module InyeccionLogica
  def before(&bloque)
    @metodos_filtrados.each do |metodo_filtrado|
      @origen.alias_method "#{metodo_filtrado}_super_before".to_sym, metodo_filtrado.to_sym
      @origen.define_method("#{metodo_filtrado}_before".to_sym) do |instancia,cont,*args|
        send("#{metodo_filtrado}_super_before".to_sym,*args)
      end

      metodo = @origen.instance_method("#{metodo_filtrado}_before".to_sym) # "algo".to_sym => :algo

      @origen.define_method("#{metodo_filtrado}".to_sym) do |*args|
        instance_exec(@origen,metodo.bind(self),*args,&bloque) # Siempre el bloque al final
        #            (instance, cont, *args) correponden a los parametros del '&bloque'
      end
    end
  end

  def after(&bloque)
    @metodos_filtrados.each do |metodo_filtrado|
      @origen.alias_method "#{metodo_filtrado}_after".to_sym, metodo_filtrado.to_sym
      @origen.define_method("#{metodo_filtrado}".to_sym) do |*args|
        send("#{metodo_filtrado}_after".to_sym,*args)
        instance_exec(@origen,*args,&bloque) # Siempre el bloque al final
      end
    end
  end

  def instead_of(&bloque)
    @metodos_filtrados.each do |metodo_filtrado|
      @origen.alias_method "#{metodo_filtrado}_instead_of".to_sym, metodo_filtrado.to_sym
      @origen.define_method("#{metodo_filtrado}".to_sym) do |*args|
        instance_exec(@origen,*args,&bloque) # Siempre el bloque al final
      end
    end
  end
end


class Aspects

  def self.on(*argumentos,&bloque)
    clases_modulos_encontrados = [] # lista que almacena (si es que existen) las clases/modulos/objetos pasados por parametro

    if argumentos.empty?
      raise ArgumentError.new('wrong number of arguments (0 for +1)')
    else # reviso si el argumento es una expresion regular
      argumentos.each do |arg|
        if arg.is_a? Regexp
          clases_modulos_encontrados.concat Object.constants.select { |constante| arg.match?(constante.to_s)}
        else
          clases_modulos_encontrados << arg
        end
      end

      raise ArgumentError.new("origen vacio") if clases_modulos_encontrados.empty?
    end

    @origenes = clases_modulos_encontrados.flat_map{ |un_origen| Origen.new(un_origen) }
    @origenes.each { |un_origen| un_origen.instance_eval(&bloque) }

  end

end

class Origen
  attr_accessor :origen, :metodos_filtrados
  include Name
  include Has_Parameters
  include Neg
  include Inject
  include RedirectTo
  include InyeccionLogica

  def initialize(origennuevo)
    self.origen = get_origen_posta origennuevo
    self
  end

  def where(*args)
    self.origen.instance_methods.select { |un_metodo| args.all?{ |una_condicion| una_condicion.call(un_metodo)}}
  end

  def transform(metodos_filtrados, &bloque)
    @metodos_filtrados = metodos_filtrados
    instance_eval(&bloque)
  end

  private def get_origen_posta (origenposta)
    if origenposta.is_a? Symbol
      (Kernel.const_get (origenposta.to_s))
    else
      if origenposta.is_a? Module
        origenposta
      else origenposta.singleton_class
      end
    end
  end
end
