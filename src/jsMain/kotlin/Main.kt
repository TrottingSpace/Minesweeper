import androidx.compose.runtime.*
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable
import kotlin.random.Random

fun main() {
    val fieldSize = 20
    val minesCount = 46
    var minesCountCheck = 0//control variable
    var checkingMode by mutableStateOf(true)
    var stillPlaying by mutableStateOf(true)

    val fieldBack: MutableList<MutableList<Int>> = mutableStateListOf(*(0 until fieldSize).map { mutableStateListOf(*(0 until fieldSize).map { 0 }.toTypedArray()) }.toTypedArray())
    val fieldHidden: MutableList<MutableList<Boolean>> = mutableStateListOf(*(0 until fieldSize).map { mutableStateListOf(*(0 until fieldSize).map { true }.toTypedArray()) }.toTypedArray())
    val fieldFront: MutableList<MutableList<String>> = mutableStateListOf(*(0 until fieldSize).map { mutableStateListOf(*(0 until fieldSize).map { "?" }.toTypedArray()) }.toTypedArray())
    val fieldMarked: MutableList<MutableList<Boolean>> = mutableStateListOf(*(0 until fieldSize).map { mutableStateListOf(*(0 until fieldSize).map { false }.toTypedArray()) }.toTypedArray())
    val fieldConnected: MutableList<MutableList<Int>> = mutableStateListOf(*(0 until fieldSize).map { mutableStateListOf(*(0 until fieldSize).map { 0 }.toTypedArray()) }.toTypedArray())

    //mine placing
    console.log("@ placing mines")
    var whileCounter = minesCount
    while (whileCounter > 0) {
        val randRow = Random.nextInt(fieldSize)
        val randCol = Random.nextInt(fieldSize)
        if (fieldBack[randRow][randCol] == 0) {
            fieldBack[randRow][randCol] = 9
            whileCounter -= 1
            minesCountCheck += 1
        }
    }
    console.log("@ mines placed", minesCountCheck)

    //mine counters
    console.log("@ counting mines")
    for (i in 0 until fieldSize) {
        for (j in 0 until fieldSize) {
            if (fieldBack[i][j] != 9) {
                var miniCount = 0
                for (k in (i-1)..(i+1)) {
                    for (l in (j-1)..(j+1)) {
                        if ((0 until fieldSize).contains(k) && (0 until fieldSize).contains(l)) {
                            if (fieldBack[k][l] == 9) { miniCount += 1 }
                        }
                    }
                }
                fieldBack[i][j] = miniCount
            }
        }
    }
    console.log("@ mines counted")

    //mapping background values to front visuals TODO fix
    fun mapFront() {
        if (stillPlaying) {
            for (i in 0 until fieldSize) {
                for (j in 0 until fieldSize) {
                    if (fieldMarked[i][j]) {
                        fieldFront[i][j] = "🚩"
                    } else if (fieldHidden[i][j]) {
                        fieldFront[i][j] = "?"
                    } else if (fieldBack[i][j] == 0) {
                        fieldFront[i][j] = " "
                    } else {
                        fieldFront[i][j] = fieldBack[i][j].toString()
                    }
                }
            }
        }
    }

    //revealing everything - function
    fun fieldReveal(x: Int, y: Int) {
        for (i in 0 until fieldSize) {
            for (j in 0 until fieldSize) {
                if (fieldBack[i][j] == 0) {
                    fieldFront[i][j] = " "
                } else if (fieldBack[i][j] == 9) {
                    fieldFront[i][j] = "💣"
                    if (fieldMarked[i][j]) { fieldFront[i][j] = "🚩" }
                    if (x == i && y == j) { fieldFront[i][j] = "💥" }
                } else {
                    fieldFront[i][j] = fieldBack[i][j].toString()
                }
            }
        }
    }

    //revealing connected - function
    fun connectedReveal(x: Int, y: Int) {
        if (fieldBack[x][y] == 0) {
            for (k in (x-1)..(x+1)) {
                for (l in (y-1)..(y+1)) {
                    if ((0 until fieldSize).contains(k) && (0 until fieldSize).contains(l)) {
                        fieldConnected[x][y] = 2
                        if (fieldBack[k][l] == 0 && fieldConnected[k][l] == 0) { fieldConnected[k][l] = 1 }
                        fieldHidden[k][l] = false
                        fieldFront[k][l] = fieldBack[k][l].toString()//TODO temporary
                    }
                }
            }
        }
        for (i in 0 until fieldSize) {
            for (j in 0 until fieldSize) {
                if (fieldConnected[i][j] == 1) { connectedReveal(i, j) }
            }
        }
    }

    //calculating dimensions
    val boxSize: Int = if (window.innerHeight < window.innerWidth){ (window.innerHeight / (fieldSize + 1)) } else { (window.innerWidth / (fieldSize + 1)) }
    console.log(window.innerHeight, window.innerWidth, boxSize)

    renderComposable(rootElementId = "root") {
        Text("$fieldSize $minesCount $checkingMode $minesCountCheck")
        Div({ style { padding((boxSize / 4).px) } }) {
            Button( attrs = {
                style { fontSize((boxSize * 0.5).px); width((boxSize * 2).px); height(boxSize.px); textAlign("center"); property("vertical-align", "center") }
                if (checkingMode) { disabled() }
                if (stillPlaying) {
                    onClick {
                        checkingMode = true
                        console.log(checkingMode)
                    }//onClick-end
                }
            } ){
                Text("Reveal")
            }//Button-end
            Button( attrs = {
                style { fontSize((boxSize * 0.5).px); width((boxSize * 2).px); height(boxSize.px); textAlign("center"); property("vertical-align", "center") }
                if (!checkingMode) { disabled() }
                if (stillPlaying) {
                    onClick {
                        checkingMode = false
                        console.log(checkingMode)
                    }//onClick-end
                }
            } ){
                Text("Mark")
            }//Button-end
            Span ({style { fontSize((boxSize * 0.5).px) }}){ Text(if (checkingMode) { " Revealing " }else { " Marking " }.toString()) }
            Table({
                style {
                    fontSize((boxSize * 0.35).px)
                    border(1.px, LineStyle.Solid, Color.blueviolet)
                    textAlign("center")
                    property("vertical-align", "center")
                    property("table-layout", "fixed")
                }
            }) {
                for (i in 0 until fieldSize) {
                    Tr ({ style { height(boxSize.px); margin(0.px); border(1.px, LineStyle.Solid, Color.blueviolet) } }){
                        for (j in 0 until fieldSize) {
                            Td ({
                                style { width(boxSize.px); margin(0.px); border(1.px, LineStyle.Solid, Color.blueviolet) }
                                if (stillPlaying && fieldHidden[i][j]) {
                                    onClick {
                                        console.log("click", i, j)
                                        console.log("\t found", fieldBack[i][j])
                                        if (checkingMode) {
                                            if (fieldMarked[i][j]) {
                                                console.log("field $i $j is marked")
                                            } else if (fieldBack[i][j] == 9) {
                                                fieldReveal(i, j)
                                                stillPlaying = false
                                            } else if (fieldBack[i][j] == 0) {
                                                connectedReveal(i, j)
                                            }
                                        } else if (fieldHidden[i][j]) {
                                            console.log(i, j, "was marked", fieldMarked[i][j])
                                            fieldMarked[i][j] = !fieldMarked[i][j]
                                            console.log(i, j, "is marked", fieldMarked[i][j])
                                        }
                                    }//onClick-end
                                }
                            }){
                                //mapFront()//TODO broken function
                                if (fieldFront[i][j] == "0") { fieldFront[i][j] = " " }//TODO temporary
                                Text(fieldFront[i][j])
                            }//Td-end
                        }
                    }//Tr-end
                }
            }//Table-end
            Text("Boom")
        }//Div-end
        Span ({style { fontSize((boxSize * 0.5).px) }}){ Text("Welcome") }
    }
}