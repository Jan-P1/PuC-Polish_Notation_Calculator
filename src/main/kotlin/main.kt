fun main(args: Array<String>) {

    while (true) {
        println()
        println()
        println("Taschenrechner nach polnischer Notation")
        println()
        println()
        print("Eingabe: ")
        val input = readln()
        println(input)

        val result = CalcParser(Lexer(input)).quickParse()
        if (result % 1.0 == 0.0) {
            println("${input} = ${result.toInt()}")
        } else println("${input} = ${result}")

    }
}