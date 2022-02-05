import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.colspan
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable
import kotlin.random.Random

var fieldWidth = 8
var fieldHeight = 8
var minesCount = 10

var inGame by mutableStateOf(true)
var winCounter by mutableStateOf(0)
var revealing by mutableStateOf(true)
var boom by mutableStateOf(false)

var colorHidden by mutableStateOf(Color.lightgray)
var colorShown by mutableStateOf(Color.gray)

class Menu {
    var hidden by mutableStateOf(true)
}

class Field(private val indexF: Int) {
    private val row = indexF / fieldWidth
    private val col = indexF % fieldWidth
    var content = 0
        private set
    fun placeMine() { content = 9 }
    var hidden by mutableStateOf(true)
        private set
    var flagged by mutableStateOf(false)
        private set
    fun flag() {
        flagged = !flagged
        name = if (flagged) { nameList[10] } else { nameList[11] }
    }
    //nameList 0, 1, 2, 3, 4, 5, 6, 7, 8, 9-bomb, flagged, unFlagged, default, badFlagged
    private val nameList = listOf(".", "1", "2", "3", "4", "5", "6", "7", "8", "💣", "🚩", "❓", "❔", "❌")
    fun color(): CSSColorValue { return if (hidden) { colorHidden } else { colorShown } }
    var name by mutableStateOf(nameList[12])
        private set
    fun show() {
        if (hidden) { winCounter += 1 }
        hidden = false
        name = if (flagged && content == 9) { nameList[10] } else if (flagged) { nameList[13] } else if (hidden) { nameList[11] } else { nameList[content] }
    }
    fun mineCounter() {
        if (content != 9) {
            for (r in (row-1)..(row+1)) {
                for (c in (col-1)..(col+1)) {
                    if ((0 until fieldHeight).contains(r) && (0 until fieldWidth).contains(c)) {
                        if (field[(r * fieldWidth) + c].content == 9) { content += 1 }
                    }
                }
            }
        }
    }
    private var unmarked = true
    fun showConnected() {
        //console.log("Showing connected for $indexF")
        unmarked = false
        for (r in (row-1)..(row+1)) {
            for (c in (col-1)..(col+1)) {
                if ((0 until fieldHeight).contains(r) && (0 until fieldWidth).contains(c)) {
                    val fN = (r * fieldWidth) + c
                    if (field[fN].indexF != indexF) {
                        if (field[fN].content == 0 && field[fN].hidden && field[fN].unmarked) {
                            field[fN].showConnected()
                        } else if (field[fN].hidden) {
                            field[fN].show()
                        }
                    }
                }
            }
        }
        show()
    }
}

val menu = Menu()
val field: MutableList<Field> = mutableListOf()
fun rebuildMinefield() {
    winCounter = 0
    boom = false
    while (field.size > 0) { field.removeAt(0) }
    for (f in 0 until (fieldHeight * fieldWidth)) {
        field.add(Field(f))
    }
}

fun minePlacerAndCounter() {
    //placing mines
    val fields = (0 until (fieldHeight * fieldWidth)).map { it }.toMutableList()
    if (minesCount < fields.size) {
        var counter = 0
        while (counter < minesCount) {
            val rIndex = fields[Random.nextInt(fields.size)]
            field[rIndex].placeMine()
            fields.remove(rIndex)
            counter += 1
            winCounter += 1
        }
    } else {
        console.log("Too many mines!")
    }
    //counting mines
    (0 until field.size).forEach { field[it].mineCounter() }
}

fun revealAll() { (0 until field.size).forEach { field[it].show() } }


fun main() {
    console.log("%c Welcome in Minesweeper! ", "color: white; font-weight: bold; background-color: black;")
    rebuildMinefield()
    minePlacerAndCounter()

    console.log("Starting with:\n Field width:\t", fieldWidth, "\n Field height:\t", fieldHeight, "\n Field size:\t", field.size, "\n Mines count:\t", minesCount)



    val myFontSize = "xxx-large"

    //"Minesweeper_root" div style applying
    document.getElementById("Minesweeper_root")?.setAttribute("style", "padding: 0px; border: none; /*aspect-ratio: ${fieldWidth.toDouble() / (fieldHeight + 1)};*/ position: relative; margin: 0px auto;")

    renderComposable(rootElementId = "Minesweeper_root") {
        if (winCounter >= field.size) {
            inGame = false
            if (boom) {
                colorShown = Color.indianred
                //window.alert(" 💥 BOOM! 💥 ")
            } else {
                colorHidden = Color.limegreen
                //window.alert(" 🚩 You Won! 🚩 ")
            }
        }

        Div({ style { padding(5.px); position(Position.Relative) } }) {
            //menu div
            Div({ style { if (menu.hidden) { display(DisplayStyle.None) }; width(20.percent); height(20.percent); position(Position.Absolute); top(50.percent); left(50.percent); property("transform", "translate(-50%, -50%)") } }) {
                Button({
                    style { border(2.px, LineStyle.Solid, Color.indigo); width(100.percent); height(100.percent) }
                    onClick { menu.hidden = true }
                }) { Text("✅") }
            }
            //menu bar table
            Table({
                style {
                    width(100.percent)
                    height(100.percent)
                    property("aspect-ratio", "max(6, $fieldWidth)")
                    property("table-layout", "fixed")
                    property("border-spacing", "0px")
                    border(1.px, LineStyle.Solid, Color.white)
                    textAlign("center")
                    property("font-size", myFontSize)
                }
            }) {
                Tr {
                    Td({
                        style { padding(0.px); border(1.px, LineStyle.Solid, Color.black) }
                    }) {
                        Button({ style { width(100.percent); height(100.percent); padding(0.px); border(1.px, LineStyle.Solid, Color.white); backgroundColor(if (revealing) { Color.tomato } else { Color.limegreen }); property("font-size", myFontSize) } }) {
                            Text(if (revealing) { "🔍" } else { "🚩" })
                        }
                    }
                    Td({
                        style { padding(0.px); border(1.px, LineStyle.Solid, Color.black) }
                        colspan(2)
                    }) {
                        Button({
                            style { width(100.percent); height(100.percent); padding(0.px); border(1.px, LineStyle.Solid, Color.white); backgroundColor(Color.lightgray); if (revealing) { textDecoration("underline") }; property("font-size", myFontSize) }
                            if (inGame && !revealing) {
                                onClick { revealing = true }
                            }
                        }) {
                            Text("🔍")
                        }
                    }
                    Td({
                        style { padding(0.px); border(1.px, LineStyle.Solid, Color.black) }
                        colspan(2)
                    }) {
                        Button({
                            style { width(100.percent); height(100.percent); padding(0.px); border(1.px, LineStyle.Solid, Color.white); backgroundColor(Color.lightgray); if (!revealing) { textDecoration("underline") }; property("font-size", myFontSize) }
                            if (inGame && revealing) {
                                onClick { revealing = false }
                            }
                        }) {
                            Text("🚩")
                        }
                    }
                    Td({
                        style { padding(0.px); border(1.px, LineStyle.Solid, Color.black) }
                        onClick { menu.hidden = false }
                    }) {
                        Button({ style { width(100.percent); height(100.percent); padding(0.px); border(1.px, LineStyle.Solid, Color.white); backgroundColor(Color.lightgray); property("font-size", myFontSize) } }) {
                            Text("⚙")
                        }
                    }
                    if (fieldWidth > 6) {
                        Td({
                            style { padding(0.px); border(1.px, LineStyle.Solid, Color.black) }
                            colspan(fieldWidth - 6)
                        }) {
                            Button({
                                style { width(100.percent); height(100.percent); padding(0.px); border(1.px, LineStyle.Solid, Color.white); backgroundColor(Color.lightgray); property("font-size", myFontSize) }
                                onClick {
                                    //console.log(winCounter)
                                    window.alert("This \"button\" shows You this message.\n Nothing else :)")
                                }
                            }) {
                                Text("*")
                            }
                        }
                    }
                }
            }
            //minefield table
            Table({
                style {
                    width(100.percent)
                    height(100.percent)
                    property("aspect-ratio", "${fieldWidth.toDouble() / fieldHeight}")
                    property("table-layout", "fixed")
                    property("border-spacing", "0px")
                    textAlign("center")
                    border(1.px, LineStyle.Solid, Color.white)
                }
            }) {
                for (i in 0 until fieldHeight) {
                    Tr {
                        for (j in 0 until fieldWidth) {
                            val fNum = (i * fieldWidth) + j
                            Td({ style { padding(0.px); border(1.px, LineStyle.Solid, Color.black) } }) {
                                Button({
                                    style { width(100.percent); height(100.percent); padding(0.px); border(1.px, LineStyle.Solid, Color.white); backgroundColor(field[fNum].color()); property("font-size", myFontSize) }
                                    if (field[fNum].hidden && inGame) {
                                        onClick {
                                            if (revealing) {
                                                if (!field[fNum].flagged) {
                                                    when (field[fNum].content) {
                                                        9 -> {
                                                            boom = true
                                                            revealAll()
                                                        }
                                                        0 -> {
                                                            field[fNum].showConnected()
                                                        }
                                                        else -> {
                                                            field[fNum].show()
                                                        }
                                                    }
                                                }
                                            } else {
                                                field[fNum].flag()
                                            }
                                        }
                                    }
                                }) {
                                    //Text("$fNum")
                                    Text(field[fNum].name)
                                }
                            }
                        }
                    }
                }
            }
        }//Div-end
    }
}