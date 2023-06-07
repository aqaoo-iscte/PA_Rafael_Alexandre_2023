

# API JSONCraft
------------------------

A API JSONCraft é uma biblioteca Kotlin que permite trabalhar com dados no formato JSON.






**Tipos de Dados JSON:**
-------------------
A API JSONCraft suporta os seguintes tipos de dados JSON:

- JSON Object: Representa uma coleção de pares chave-valor separados por vírgulas e contidos entre chaves {}. A chave é uma string e o valor pode ser de qualquer tipo de JSONElement.
- JSON Array: Representa uma lista de valores separados por vírgulas e contidos entre [].
- JSON String: Representa uma sequência de caracteres contida entre "".
- JSON Boolean: Representa um valor booleano (true ou false).
- JSON Number: Representa um valor numérico, seja um inteiro ou um de virgula flutuante.
- JSON Null: Representa a ausência de um valor.





JSONTextBuilder:
------------------

A classe JSONTextBuilder é uma implementação da interface Visitor que constrói uma representação de texto JSON a partir de elementos JSON. Ela fornece os seguintes métodos e comportamentos:

- visit(jsonObject: JSONObject): Inicia a visita a um objeto JSON e adiciona o caractere de abertura "{" ao texto JSON resultante.
- visit(jsonArray: JSONArray): Inicia a visita a um array JSON e adiciona o caractere de abertura "[" ao texto JSON resultante.
- visit(jsonString: JSONString): Adiciona uma string JSON ao texto JSON resultante.
- visit(jsonBoolean: JSONBoolean): Adiciona um valor booleano JSON ao texto JSON resultante.
- visit(jsonNumber: JSONNumber): Adiciona um valor numérico JSON ao texto JSON resultante.
- visit(jsonNull: JSONNull): Adiciona o valor nulo JSON ao texto JSON resultante.
- visitProperty(propertyName: String): Inicia a visita a uma propriedade num objeto JSON e adiciona o nome da propriedade ao texto JSON resultante.
- visitElementInArray(index: Int): Inicia a visita a um elemento num array JSON e adiciona uma vírgula antes do elemento, se necessário.
- endVisit(jsonObject: JSONObject): Finaliza a visita a um objeto JSON e adiciona o caractere de encerramento "}" ao texto JSON resultante.
- endVisit(jsonArray: JSONArray): Finaliza a visita a um array JSON e adiciona o caractere de encerramento "]" ao texto JSON resultante.
- getJsonString(): Retorna o texto JSON resultante como uma string.

Exemplo de uso do JSONTextBuilder:

	val visitor = JSONTextBuilder()
	val jsonObject = JSONObject(
		mapOf(
	    	"nome" to JSONString("Rafael"),
	    	"idade" to JSONNumber(21),
	    	"amigos" to JSONArray(
	        	listOf(
	            	JSONString("Alexandre"),
	            	JSONString("Luís"),
	            	JSONString("João")
	        	)
	    	)
		)
	)
	jsonObject.accept(visitor)
	val jsonText = visitor.getJsonString()

Neste exemplo, criamos um JSONTextBuilder e visitamos um objeto JSON "jsonObject". De seguida, obtemos o texto JSON resultante usando o método getJsonString().

O JSONTextBuilder percorre a estrutura JSON e constrói o texto JSON correspondente, garantindo a formatação correta e a ordem adequada dos elementos JSON.

Output obtido: {"nome" : "Rafael", "idade" : 21, "amigos" : ["Alexandre", "Luís", "João"]}





Annotations:
------------------

A API JSONCraft fornece algumas anotações que podem ser usadas para personalizar o comportamento de serialização de objetos para JSON:

- @JSONIgnore: Essa anotação pode ser usada em propriedades de uma classe de dados para indicar que elas devem ser ignoradas durante a serialização para JSON.
- @JSONPersonalize: Essa anotação pode ser usada em propriedades de uma classe de dados para fornecer um nome personalizado para a propriedade durante a serialização para JSON. O valor da anotação é o nome personalizado desejado.
- @JSONForceString: Essa anotação pode ser usada em propriedades de uma classe de dados para forçar a serialização do valor como uma string, mesmo que o tipo real seja diferente.

Exemplo:

    data class Student(
      @JSONForceString
      val number: Int,

      @JSONPersonalize("nome estudante")
      val name: String,

      @JSONIgnore
      val type: StudentType? = null,

      val year_failed: Boolean
    )

    fun main() {
      val visitor = JSONTextBuilder()
      val estudante = Student(7,"rafael", StudentType.Master, true)
      val model = createModelFromObject(estudante)
      model.accept(visitor)
      val jsonText = visitor.getJsonString()
      println(jsonText)
    }

Neste exemplo, a propriedade "number" será sempre serializada como uma string, a propriedade "name" terá o nome "nome estudante" durante a serialização e a propriedade "type" será ignorada durante a serialização.

Output obtido: {"number" : "7", "nome estudante" : "rafael", "year_failed" : true}





Utilização:
------------------
Para utilizar a API JSONCraft no seu projeto Kotlin, siga as etapas abaixo:

1. Importe as classes necessárias do pacote Fase1.
2. Crie objetos JSON, arrays, strings, booleanos, números ou valores nulos conforme necessário.
3. Manipule os dados JSON usando as classes e funções fornecidas.
4. Utilize a classe JSONTextBuilder para construir uma representação de texto JSON de um elemento JSON.

Aqui está um exemplo básico de utilização da API JSONCraft:

    fun main() {
        // Cria um objeto JSON
        val jsonObject = JSONObject(
            mapOf(
                "nome" to JSONString("João"),
                "idade" to JSONNumber(25),
                "ativo" to JSONBoolean(true),
                "amigos" to JSONArray(
                    listOf(
                        JSONString("Maria"),
                        JSONString("Pedro"),
                        JSONString("Ana")
                    )
                )
            )
        )

        // Constrói uma representação de texto JSON do objeto JSON
        val jsonTextBuilder = JSONTextBuilder()
        jsonObject.accept(jsonTextBuilder)
        val jsonText = jsonTextBuilder.getJsonString()

        // Imprime o objeto JSON e o texto JSON resultante
        println("Objeto JSON:")
        println(jsonObject)
        println("Texto JSON:")
        println(jsonText)
    }

Neste exemplo, criamos um objeto JSON com as propriedades: "nome", "idade", "ativo" e "amigos". De seguida, usamos o JSONTextBuilder para construir uma representação de texto JSON desse objeto.
Por fim, imprimimos o objeto JSON e o texto JSON resultante.





Extensões da API:
-----------------------
A API JSONCraft também fornece algumas extensões úteis para trabalhar com elementos JSON:

- getObjectsWithProperties(properties: List<String>): List<JSONObject>: Retorna uma lista de objetos JSON que possuem todas as propriedades especificadas.
- getValuesOfProperty(property: String): List<JSONElement>: Retorna uma lista de valores de uma propriedade específica presente num elemento JSON.
- validateNumeroProperty(): Boolean: Valida a presença da propriedade "numero" como um JSONNumber.
- validateInscritosProperty(): Boolean: Valida a propriedade "inscritos" como um JSONArray com propriedades consistentes.






Considerações Finais:
-----------------------
A API JSONCraft oferece uma maneira conveniente e eficiente de trabalhar com dados JSON em projetos Kotlin.
[Ver Documentação](https://aqaoo-iscte.github.io/PA_Rafael_Alexandre_2023/index.html)

