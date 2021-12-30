//import org.jetbrains.compose.web.attributes.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.max
import org.jetbrains.compose.web.attributes.min
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable
import kotlin.random.Random

var fieldSize = 16
var minesCount = 32

fun main() {
    console.log("%c Welcome in Minesweeper! ", "color: white; font-weight: bold; background-color: black;")
    var minesCountCheck = 0//control variable
    var checkingMode by mutableStateOf(true)
    var stillPlaying by mutableStateOf(true)
    var winCounter by mutableStateOf(0)
    var rebuildConfirm by mutableStateOf(false)

    var newFieldSize = fieldSize
    var newMinesCount = minesCount

    val fieldBack: MutableList<MutableList<Int>> = mutableStateListOf(*(0 until fieldSize).map { mutableStateListOf(*(0 until fieldSize).map { 0 }.toTypedArray()) }.toTypedArray())
    val fieldVisibility: MutableList<MutableList<Int>> = mutableStateListOf(*(0 until fieldSize).map { mutableStateListOf(*(0 until fieldSize).map { 0 }.toTypedArray()) }.toTypedArray())
    val fieldFront: MutableList<MutableList<String>> = mutableStateListOf(*(0 until fieldSize).map { mutableStateListOf(*(0 until fieldSize).map { "❔" }.toTypedArray()) }.toTypedArray())
    val fieldMarked: MutableList<MutableList<Boolean>> = mutableStateListOf(*(0 until fieldSize).map { mutableStateListOf(*(0 until fieldSize).map { false }.toTypedArray()) }.toTypedArray())
    val fieldConnected: MutableList<MutableList<Int>> = mutableStateListOf(*(0 until fieldSize).map { mutableStateListOf(*(0 until fieldSize).map { 0 }.toTypedArray()) }.toTypedArray())

    val fieldsList: MutableList<MutableList<Int>> = mutableStateListOf(*(0 until (fieldSize * fieldSize)).map { mutableStateListOf(*(0..1).map { 0 }.toTypedArray()) }.toTypedArray())
    for (i in 0 until fieldSize) {
        for (j in 0 until fieldSize) {
            fieldsList[(i * fieldSize) + j][0] = i
            fieldsList[(i * fieldSize) + j][1] = j
            //console.log("Position:", ((i * fieldSize) + j), "\t\t Field:", fieldsList[(i * fieldSize) + j][0], fieldsList[(i * fieldSize) + j][1])
        }
    }

    /*
    //mine placing for small amounts of mines! - to many mines can lead to infinite loop!
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
    */

    //mine placing v2
    for (i in 0 until minesCount) {
        val chosenField = Random.nextInt(fieldsList.size)
        fieldBack[fieldsList[chosenField][0]][fieldsList[chosenField][1]] = 9
        minesCountCheck += 1
        winCounter += 1
        fieldsList.removeAt(chosenField)
    }

    //mine counters
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
    console.log(" Window height:\t", window.innerHeight, "\n Window width:\t", window.innerWidth, "\n Size factor: \t", boxSize)
    var boxSizeMultiplier by mutableStateOf(1.0)

    console.log(" Minefield size:\t", fieldSize, "\n Fields in total:\t", (fieldSize * fieldSize), "\n Mines generated:\t", minesCountCheck, "/", minesCount)

    renderComposable(rootElementId = "root") {

        Div({ style { padding(1.px) } }) {

            console.log("\t Win counter:\t", winCounter, "/", (fieldSize * fieldSize))
            if (winCounter >= (fieldSize * fieldSize)) {
                stillPlaying = false
                Span ({style { fontSize(((boxSize * boxSizeMultiplier) * 0.5).px) }}){ Text("  ✅ You Won! ✅  ") }
                console.log("\t All", winCounter, "fields are now known. You Won!")
            }

            Table({
                style {
                    fontSize(((boxSize * boxSizeMultiplier) * 0.45).px)
                    border(1.px, LineStyle.Solid, Color.black)
                    textAlign("center")
                    property("vertical-align", "center")
                    property("table-layout", "fixed")
                    property("border-spacing", "0px")
                    width(((fieldSize * (boxSize * boxSizeMultiplier)) + 2).px)
                    height(((boxSize * boxSizeMultiplier) + 2).px)
                    margin(0.px)
                    padding(0.px)
                }
            }) {
                Tr({ style { height((boxSize * boxSizeMultiplier).px); margin(0.px); border(1.px, LineStyle.Solid, Color.black); padding(0.px) } }) {
                    Td({ style { width((boxSize * boxSizeMultiplier).px); margin(0.px); border(1.px, LineStyle.Solid, Color.black); padding(0.px) } }) {
                        Text(if (checkingMode) { "🔍" }else { "🚩" }.toString())
                    }//Td-end
                    Td({
                        style { width(((boxSize * boxSizeMultiplier) * 2.5).px); margin(0.px); border(1.px, LineStyle.Solid, Color.black); padding(0.px); textDecoration(if (checkingMode) { "underline" }else { "none" }.toString()) }
                        if (stillPlaying && !checkingMode) {
                            onClick {
                                checkingMode = true
                                console.log("Checking mode:", checkingMode)
                                rebuildConfirm = false
                            }//onClick-end
                        }
                    }) {
                        Text("Reveal 🔍")
                    }//Td-end
                    Td({
                        style { width(((boxSize * boxSizeMultiplier) * 2.5).px); margin(0.px); border(1.px, LineStyle.Solid, Color.black); padding(0.px); textDecoration(if (checkingMode) { "none" }else { "underline" }.toString()) }
                        if (stillPlaying && checkingMode) {
                            onClick {
                                checkingMode = false
                                console.log("Checking mode:", checkingMode)
                                rebuildConfirm = false
                            }//onClick-end
                        }
                    }) {
                        Text("Mark 🚩")
                    }//Td-end
                    Td({ style { width((boxSize * boxSizeMultiplier).px); margin(0.px); border(1.px, LineStyle.Solid, Color.black); padding(0.px) } }) {
                        Text("*")
                    }//Td-end
                    Td({ style { fontSize(((boxSize * boxSizeMultiplier) * 0.2).px); width(((boxSize * boxSizeMultiplier) / 2).px); margin(0.px); border(1.px, LineStyle.Solid, Color.black); padding(0.px) } }) {
                        Text("🔲")
                    }//Td-end
                    Td({
                        style { width((boxSize * boxSizeMultiplier).px); margin(0.px); border(1.px, LineStyle.Solid, Color.black); padding(0.px) }
                    }) {
                        Input(InputType.Number) {
                            style { width(((boxSize * boxSizeMultiplier) - 4).px); height(((boxSize * boxSizeMultiplier) - 4).px); border(1.px, LineStyle.Solid, Color.darkgreen); outline("none"); fontSize(((boxSize * boxSizeMultiplier) * 0.35).px); margin(0.px); padding(0.px) }
                            min("2")
                            max("32")
                            defaultValue(fieldSize)
                            onChange {
                                newFieldSize = it.value as Int
                                console.log("newFieldSize", newFieldSize)
                                if (newFieldSize > 32) { window.alert(" Warning!\n New field size is over 32, that's over 1024 individual fields!\n Game may load for a VERY LONG time! ") }
                                rebuildConfirm = false
                            }
                        }
                    }//Td-end
                    Td({ style { fontSize(((boxSize * boxSizeMultiplier) * 0.2).px); width(((boxSize * boxSizeMultiplier) / 2).px); margin(0.px); border(1.px, LineStyle.Solid, Color.black); padding(0.px) } }) {
                        Text("💣")
                    }//Td-end
                    Td({
                        style { width((boxSize * boxSizeMultiplier).px); margin(0.px); border(1.px, LineStyle.Solid, Color.black); padding(0.px) }
                    }) {
                        Input(InputType.Number) {
                            style { width(((boxSize * boxSizeMultiplier) - 4).px); height(((boxSize * boxSizeMultiplier) - 4).px); border(1.px, LineStyle.Solid, Color.darkred); outline("none"); fontSize(((boxSize * boxSizeMultiplier) * 0.35).px); margin(0.px); padding(0.px) }
                            min("1")
                            //max(((newFieldSize * newFieldSize)-1).toString())
                            defaultValue(minesCount)
                            onChange {
                                newMinesCount = it.value as Int
                                console.log("newMinesCount", newMinesCount)
                                rebuildConfirm = false
                            }
                        }
                    }//Td-end
                    Td({
                        style { width(((boxSize * boxSizeMultiplier) * 2).px); margin(0.px); border(1.px, LineStyle.Solid, Color.black); padding(0.px) }
                        onClick {
                            console.log("Button 'Rebuild' clicked...")
                            if (rebuildConfirm) {
                                if ((1 until (newFieldSize * newFieldSize)).contains(newMinesCount)) {
                                    if (newFieldSize < 2) { newFieldSize = 2 }
                                    fieldSize = newFieldSize
                                    minesCount = newMinesCount
                                    console.log("\t Rebuilding")
                                    document.getElementById("root")?.innerHTML = ""
                                    main()
                                } else {
                                    console.log("\t Incorrect numbers requested.")
                                    window.alert(" New field size:  $newFieldSize \n Fields in total:  " + (newFieldSize * newFieldSize) + " \n $newMinesCount mines is not allowed for " + (newFieldSize * newFieldSize) + " fields! ")
                                }
                                rebuildConfirm = false
                            } else {
                                rebuildConfirm = true
                                console.log("\t Click again to confirm.")
                            }
                        }//onClick-end
                    }) {
                        Text(if (rebuildConfirm) { "Confirm" }else { "Rebuild" }.toString())
                    }//Td-end
                    Td({ style { width(((boxSize * boxSizeMultiplier) / 2).px); margin(0.px); border(1.px, LineStyle.Solid, Color.black); padding(0.px) } }) {
                        Text("*")
                    }//Td-end
                    Td({
                        style { width((boxSize * boxSizeMultiplier).px); margin(0.px); border(1.px, LineStyle.Solid, Color.black); padding(0.px) }
                        onClick {
                            boxSizeMultiplier -= 0.1
                            console.log("Base size factor:", boxSize, "Multiplier:", boxSizeMultiplier, "Current size factor:", (boxSize * boxSizeMultiplier))
                            rebuildConfirm = false
                        }//onClick-end
                    }) {
                        Text("➖")
                    }//Td-end
                    Td({
                        style { width((boxSize * boxSizeMultiplier).px); margin(0.px); border(1.px, LineStyle.Solid, Color.black); padding(0.px) }
                        onClick {
                            boxSizeMultiplier = 1.0
                            console.log("Base size factor:", boxSize, "Multiplier:", boxSizeMultiplier, "Current size factor:", (boxSize * boxSizeMultiplier))
                            rebuildConfirm = false
                        }//onClick-end
                    }) {
                        Text("0️⃣")
                    }//Td-end
                    Td({
                        style { width((boxSize * boxSizeMultiplier).px); margin(0.px); border(1.px, LineStyle.Solid, Color.black); padding(0.px) }
                        onClick {
                            boxSizeMultiplier += 0.1
                            console.log("Base size factor:", boxSize, "Multiplier:", boxSizeMultiplier, "Current size factor:", (boxSize * boxSizeMultiplier))
                            rebuildConfirm = false
                        }//onClick-end
                    }) {
                        Text("➕")
                    }//Td-end
                }
            }

            Table({
                style {
                    fontSize(((boxSize * boxSizeMultiplier) * 0.45).px)
                    border(1.px, LineStyle.Solid, Color.black)
                    textAlign("center")
                    property("vertical-align", "center")
                    property("table-layout", "fixed")
                    property("border-spacing", "0px")
                    width(((fieldSize * (boxSize * boxSizeMultiplier)) + 2).px)
                    height(((fieldSize * (boxSize * boxSizeMultiplier)) + 2).px)
                    margin(0.px)
                    padding(0.px)
                }
            }) {
                for (i in 0 until fieldSize) {
                    Tr ({ style { height((boxSize * boxSizeMultiplier).px); margin(0.px); border(1.px, LineStyle.Solid, Color.black); padding(0.px) } }){
                        for (j in 0 until fieldSize) {
                            Td ({
                                style { /*width((boxSize * boxSizeMultiplier).px); */margin(0.px); border(1.px, LineStyle.Solid, Color.black); padding(0.px) }
                                if (stillPlaying && fieldVisibility[i][j] == 0) {
                                    onClick {
                                        console.log("Clicked:", i, j, /*"\t\t Found:", fieldBack[i][j],*/ "\t\t Win counter:", winCounter)
                                        if (checkingMode) {
                                            //console.log("\t Found:", fieldBack[i][j])
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
                                                fieldFront[i][j] = "❓"
                                                console.log(i, j, "is no longer marked")
                                            }
                                            //console.log(i, j, "is marked", fieldMarked[i][j])
                                        }
                                        rebuildConfirm = false
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