describe 'TEST Aspects' do

  class MiClase
    def foo
    end

    def bar
    end
  end

  class MiClaseConParametros
    def foo(p1, p2, p3, p4='a', p5='b', p6='c')
    end
    def bar(p1, p2='a', p3='b', p4='c')
    end
    def fooParam(param1, param2)
    end
    def barParam(param1)
    end


  end


  class ClaseTest
    attr_accessor :metodosTest
  end

  claseTest = ClaseTest.new

  it 'Debe tener al menos 1 argumento' do
    # Los Procedimientos van con {} osea no estoy esperando algo como un return. si puedo capturar un raise
    expect{Aspects.on {}}.to raise_error(ArgumentError)
  end

  it 'Pepe no pertenece al origen' do
    expect{Aspects.on /Pepe/ do
                end}.to raise_error(ArgumentError)
  end

  it "Machear el nombre del metodo" do
    class Juan
      def saludar
        "hola"
      end
    end

    class ClaseTest
      attr_accessor :metodosTest
    end

    Aspects.on Juan do
      claseTest.metodosTest = where name(/saludar/)
    end
    expect(claseTest.metodosTest).to eq ([:saludar])
  end

  it "Machear el nombre del metodo, con 2 names" do

    claseTest.metodosTest = Array.new
    Aspects.on MiClase do
      claseTest.metodosTest = where name(/fo{2}/), name(/fo/)
    end
    expect(claseTest.metodosTest).to eq ([:foo])
  end

  it "No machea el nombre del metodo con name" do

    claseTest.metodosTest = Array.new
    Aspects.on MiClase do
      claseTest.metodosTest = where name(/^fi+/)
    end
    expect(claseTest.metodosTest).to eq ([])
  end

  it "No machea el nombre del metodo con dos name" do

    claseTest.metodosTest = Array.new
    Aspects.on MiClase do
      claseTest.metodosTest = where name(/foo/), name(/bar/)
    end
    expect(claseTest.metodosTest).to eq ([])
  end

  it "Metodos con la misma cantidad de parametros y ademas que sean obligatorios" do

    claseTest.metodosTest=Array.new

    Aspects.on MiClaseConParametros do
      claseTest.metodosTest = where has_parameters(3, mandatory)
    end
    expect(claseTest.metodosTest).to eq([:foo])
  end

  it "Metodos con la misma cantidad de parametros" do

    claseTest.metodosTest=Array.new

    Aspects.on MiClaseConParametros do
      claseTest.metodosTest = where has_parameters(6)
    end
    expect(claseTest.metodosTest).to eq([:foo])
  end

  it "Metodos con la misma cantidad de parametros opcionales" do

    claseTest.metodosTest=Array.new

    Aspects.on MiClaseConParametros do
      claseTest.metodosTest = where has_parameters(3, optional)
    end
    expect(claseTest.metodosTest).to eq([:foo,:bar])
  end

  it "La ClaseA ejecuta el metodo saludar de la ClaseB" do
    class ClaseA
      def saludar(x)
        "Hola, " + x
      end
    end

    class ClaseB
      def saludar(x)
        "Adiosín, " + x
      end
    end

    Aspects.on ClaseA do
      transform(where name(/saludar/)) do
        redirect_to(ClaseB.new)
      end
    end

    expect(ClaseA.new.saludar("mundo")).to eq("Adiosín, mundo")
  end

  it 'inyeccion de parametros normal' do
    class MiClase
      def hace_algo(p1, p2)
        p1 + '-' + p2
      end
      def hace_otra_cosa(p2, ppp)
        p2 + ':' + ppp
      end
    end

    Aspects.on MiClase do
      transform(where has_parameters(1, /p2/)) do
        inject(p2: 'bar')
      end
    end

    instancia = MiClase.new

    expect(instancia.hace_algo("foo")).to eq("foo-bar")
    expect(instancia.hace_algo("foo", "foo")).to eq("foo-bar")
    expect(instancia.hace_otra_cosa("foo", "foo")).to eq("bar:foo")
  end

  it 'inyeccion de parametros normal version module' do
    module MiModule
      def hace_algo(p1, p2)
        p1 + '-' + p2
      end
      def hace_otra_cosa(p2, ppp)
        p2 + ':' + ppp
      end
    end

    Aspects.on MiModule do
      transform(where has_parameters(1, /p2/)) do
        inject(p2: 'bar')
      end
    end

    class MiClase2
      include MiModule
    end

    instancia = MiClase2.new

    expect(instancia.hace_algo("foo")).to eq("foo-bar")
    expect(instancia.hace_algo("foo", "foo")).to eq("foo-bar")
    expect(instancia.hace_otra_cosa("foo", "foo")).to eq("bar:foo")
  end

  it "Inyeccion Logica: BEFORE/AFTER/INSTEAD OF version module" do
    module MiModule
      attr_accessor :x
      def m1(x, y)
        x + y
      end
      def m2(x)
        @x = x
      end
      def m3(x)
        @x = x
      end
    end

    Aspects.on MiModule do
      transform(where name(/m1/)) do
        before do |instance, cont, *args|
          @x = 10
          new_args = args.map{ |arg| arg * 10 }
          cont.call(self, nil, *new_args)
        end
      end

      transform(where name(/m2/)) do
        after do |instance, *args|
          if @x > 100
            2 * @x
          else
            @x
          end
        end
      end

      transform(where name(/m3/)) do
        instead_of do |instance, *args|
          @x = 123
        end
      end
    end

    class MiClase2
      include MiModule
    end

    instancia = MiClase2.new
    expect(instancia.m1(1, 2)).to eq(30)
    expect(instancia.x).to eq(10)

    instancia = MiClase2.new
    expect(instancia.m2(10)).to eq(10)
    expect(instancia.m2(200)).to eq(400)

    instancia = MiClase2.new
    instancia.m3(10)
    expect(instancia.x).to eq(123)
  end

  it 'inyeccion de parametros con un proc' do
    class MiClase
      def hace_algo(p1, p2)
        p1 + "-" + p2
      end
    end

    Aspects.on MiClase do
      transform(where has_parameters(1,/p2/)) do
        inject(p2: proc{ |receptor, mensaje, arg_anterior|
          "bar(#{mensaje}->#{arg_anterior})"
        })
      end
    end

    expect(MiClase.new.hace_algo('foo', 'foo')).to eq('foo-bar(hace_algo->foo)')
  end

  it 'inyeccion de parametros con un proc version module' do
    module MiModule
      def hace_algo(p1, p2)
        p1 + "-" + p2
      end
    end

    Aspects.on MiModule do
      transform(where has_parameters(1,/p2/)) do
        inject(p2: proc{ |receptor, mensaje, arg_anterior|
          "bar(#{mensaje}->#{arg_anterior})"
        })
      end
    end

    class MiClase2
      include MiModule
    end

    expect(MiClase2.new.hace_algo('foo', 'foo')).to eq('foo-bar(hace_algo->foo)')
  end

  it "Inyeccion Logica: BEFORE/AFTER/INSTEAD OF" do
    class MiClase
      attr_accessor :x
      def m1(x, y)
        x + y
      end
      def m2(x)
        @x = x
      end
      def m3(x)
        @x = x
      end
    end

    Aspects.on MiClase do
      transform(where name(/m1/)) do
        before do |instance, cont, *args|
          @x = 10
          new_args = args.map{ |arg| arg * 10 }
          cont.call(self, nil, *new_args)
        end
      end

      transform(where name(/m2/)) do
        after do |instance, *args|
          if @x > 100
            2 * @x
          else
            @x
          end
        end
      end

      transform(where name(/m3/)) do
        instead_of do |instance, *args|
          @x = 123
        end
      end
    end

    instancia = MiClase.new
    expect(instancia.m1(1, 2)).to eq(30)
    expect(instancia.x).to eq(10)

    instancia = MiClase.new
    expect(instancia.m2(10)).to eq(10)
    expect(instancia.m2(200)).to eq(400)

    instancia = MiClase.new
    instancia.m3(10)
    expect(instancia.x).to eq(123)
  end
end