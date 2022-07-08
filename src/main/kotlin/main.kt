fun main(args: Array<String>) {

    while (true) {
        println()
        println()
        println("Taschenrechner nach polnischer Notation")
        println()
        println()
        print("Eingabe: ")
        var addition = readln()
        addition += " "
        var result = 0.0

        try {
            result = CalcParser(Lexer(addition)).quickParse()
            if (result % 1.0 != 0.0) {
                println("${addition} = ${result}")
            } else println("${addition}= ${result.toInt()}")
        } catch (e: Exception) {
            println("Oops, da lief was schief :)")
        }

    }
}