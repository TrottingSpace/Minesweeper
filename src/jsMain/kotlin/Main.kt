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
    var winCounter by mutableStateOf(0)

    val fieldBack: MutableList<MutableList<Int>> = mutableStateListOf(*(0 until fieldSize).map { mutableStateListOf(*(0 until fieldSize).map { 0 }.toTypedArray()) }.toTypedArray())
    val fieldVisibility: MutableList<MutableList<Int>> = mutableStateListOf(*(0 until fieldSize).map { mutableStateListOf(*(0 until fieldSize).map { 0 }.toTypedArray()) }.toTypedArray())
    val fieldFront: MutableList<MutableList<String>> = mutableStateListOf(*(0 until fieldSize).map { mutableStateListOf(*(0 until fieldSize).map { "?" }.toTypedArray()) }.toTypedArray())
    val fieldMarked: MutableList<MutableList<Boolean>> = mutableStateListOf(*(0 until fieldSize).map { mutableStateListOf(*(0 until fieldSize).map { false }.toTypedArray()) }.toTypedArray())
    val fieldConnected: MutableList<MutableList<Int>> = mutableStateListOf(*(0 until fieldSize).map { mutableStateListOf(*(0 until fieldSize).map { 0 }.toTypedArray()) }.toTypedArray())

    //mine placing
    console.log("\t Placing mines")
    var whileCounter = minesCount
    while (whileCounter > 0) {
        val randRow = Random.nextInt(fieldSize)
        val randCol = Random.nextInt(fieldSize)
        if (fieldBack[randRow][randCol] == 0) {
            fieldBack[randRow][randCol] = 9
            whileCounter -= 1
            minesCountCheck += 1
            winCounter += 1
        }
    }
    console.log("\t Mines placed:", minesCountCheck)

    //mine counters
    console.log("\t Counting mines")
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
    console.log("\t Mines counted")

    //mapping background values to front visuals
    fun mapFront(x: Int, y: Int) {
        if (fieldBack[x][y] == 0) {
            fieldFront[x][y] = " "
        } else if (fieldBack[x][y] == 9) {
            fieldFront[x][y] = "💣"
            if (fieldMarked[x][y]) { fieldFront[x][y] = "🚩" }
        } else {
            fieldFront[x][y] = fieldBack[x][y].toString()
        }
    }

    //revealing everything - function
    fun fieldRevealEverything(x: Int, y: Int) {
        for (i in 0 until fieldSize) {
            for (j in 0 until fieldSize) {
                if (x == i && y == j) {
                    fieldFront[i][j] = "💥"
                } else {
                    mapFront(i, j)
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
                        if (fieldVisibility[k][l] == 0) { winCounter += 1 }
                        fieldVisibility[k][l] = 1
                        mapFront(k, l)
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
    val boxSize: Int = if (window.innerHeight < window.innerWidth){ (window.innerHeight / (fieldSize + 1)) } else { (window.innerWidth / (fieldSize + 2)) }
    console.log("Window height:", window.innerHeight, "Window width:", window.innerWidth, "Calculated value:", boxSize)

    console.log("Field size:", fieldSize, "Fields total:", (fieldSize * fieldSize), "Mines requested:", minesCount, "Mines generated:", minesCountCheck)

    renderComposable(rootElementId = "root") {
        //Text("$fieldSize $minesCount $checkingMode $minesCountCheck")

        Div({ style { padding(1.px) } }) {
            Button( attrs = {
                style { fontSize((boxSize * 0.5).px); width((boxSize * 2).px); height(boxSize.px); textAlign("center"); property("vertical-align", "center") }
                if (checkingMode) { disabled() }
                if (stillPlaying) {
                    onClick {
                        checkingMode = true
                        console.log("Checking mode:", checkingMode)
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
                        console.log("Checking mode:", checkingMode)
                    }//onClick-end
                }
            } ){
                Text("Mark")
            }//Button-end

            Span ({style { fontSize((boxSize * 0.5).px) }}){ Text(if (checkingMode) { " Revealing " }else { " Marking " }.toString()) }

            console.log("\t Win counter:", winCounter)
            if (winCounter >= (fieldSize * fieldSize)) {
                stillPlaying = false
                Span ({style { fontSize((boxSize * 0.5).px) }}){ Text("  ✅ You Won! ✅  ") }
                console.log("\t All", winCounter, "fields are now known. You Won!")
            }

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
                    Tr ({ style { height(boxSize.px); margin(0.px); border(1.px, LineStyle.Solid, Color.blueviolet); padding(0.px) } }){
                        for (j in 0 until fieldSize) {
                            Td ({
                                style { width(boxSize.px); margin(0.px); border(1.px, LineStyle.Solid, Color.blueviolet); padding(0.px) }
                                if (stillPlaying && fieldVisibility[i][j] == 0) {
                                    onClick {
                                        console.log("Clicked:", i, j, /*"\t\t Found:", fieldBack[i][j],*/ "\t\t Win counter:", winCounter)
                                        if (checkingMode) {
                                            console.log("\t Found:", fieldBack[i][j])
                                            if (fieldMarked[i][j]) {
                                                console.log("Field $i $j is marked")
                                            } else if (fieldBack[i][j] == 9) {
                                                console.log("BOOM!")
                                                stillPlaying = false
                                                fieldRevealEverything(i, j)
                                            } else if (fieldBack[i][j] == 0) {
                                                connectedReveal(i, j)
                                            } else {
                                                fieldVisibility[i][j] = 1
                                                mapFront(i, j)
                                                winCounter += 1
                                            }
                                        } else {
                                            //console.log(i, j, "was marked", fieldMarked[i][j])
                                            fieldMarked[i][j] = !fieldMarked[i][j]
                                            if (fieldMarked[i][j]) {
                                                fieldFront[i][j] = "🚩"
                                                console.log(i, j, "is now marked")
                                            } else {
                                                fieldFront[i][j] = "?"
                                                console.log(i, j, "is no longer marked")
                                            }
                                            //console.log(i, j, "is marked", fieldMarked[i][j])
                                        }
                                    }//onClick-end
                                }
                            }){
                                Text(fieldFront[i][j])
                            }//Td-end
                        }
                    }//Tr-end
                }
            }//Table-end
        }//Div-end
    }
}