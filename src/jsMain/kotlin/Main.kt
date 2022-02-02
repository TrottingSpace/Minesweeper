import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.browser.document
import kotlinx.browser.window
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable
import kotlin.random.Random

var fieldWidth = 32
var fieldHeight = 16
var minesCount = 27

class Menu {
    var hidden by mutableStateOf(true)
}

class Field(private val row: Int, private val col: Int) {
    val index = (row * fieldWidth) + col
    var content = 0
        private set
    fun placeMine() { content = 9 }
    var hidden by mutableStateOf(true)
        private set
    var flagged by mutableStateOf(false)
        private set
    private val nameList = listOf(".", "1", "2", "3", "4", "5", "6", "7", "8", "(#)", "!!", "?")
    fun color(): CSSColorValue { return if (hidden) { Color.lightgray } else { Color.gray } }
    fun name(): String { return if (flagged) { nameList[10] } else if (hidden) { nameList[11] } else { nameList[content] } }
    fun show() { hidden = false }
    fun mineCounter() {
        if (content != 9) {
            for (r in (row-1)..(row+1)) {
                for (c in (col-1)..(col+1)) {
                    if ((0 until fieldHeight).contains(r) && (0 until fieldWidth).contains(c)) {
                        if (field[r][c].content == 9) { content += 1 }
                    }
                }
            }
        }
    }
}

val menu = Menu()
val field: MutableList<MutableList<Field>> = mutableListOf()
fun rebuildMinefield() {
    while (field.size > 0) { field.removeAt(0) }
    for (i in 0 until fieldHeight) {
        field.add(mutableListOf())
        for (j in 0 until fieldWidth) {
            field[i].add(Field(i, j))
        }
    }
}

fun minePlacerAndCounter() {
    //placing mines
    val fields = (0 until (fieldHeight * fieldWidth)).map { it }.toMutableList()
    if (minesCount < fields.size) {
        var counter = 0
        while (counter < minesCount) {
            val rIndex = fields[Random.nextInt(fields.size)]
            field[rIndex/fieldWidth][rIndex%fieldWidth].placeMine()
            fields.remove(rIndex)
            counter += 1
        }
    } else {
        console.log("Too many mines!")
    }
    //counting mines
    (0 until (fieldHeight * fieldWidth)).forEach { field[it/fieldWidth][it%fieldWidth].mineCounter() }
}

fun revealAll() { (0 until (fieldHeight * fieldWidth)).forEach { field[it/fieldWidth][it%fieldWidth].show() } }


fun main() {
    console.log("%c Welcome in Minesweeper! ", "color: white; font-weight: bold; background-color: black;")
    rebuildMinefield()
    minePlacerAndCounter()

    console.log("Starting with:\n Field width:\t", fieldWidth, "\n Field height:\t", fieldHeight, "\n Field size:\t", (fieldHeight * fieldWidth), "\n Mines count:\t", minesCount)





    //"Minesweeper_root" div style applying
    document.getElementById("Minesweeper_root")?.setAttribute("style", "padding: 0px; border: none; /*aspect-ratio: ${fieldWidth.toDouble() / (fieldHeight + 1)};*/ position: relative; margin: 0px auto;")

    renderComposable(rootElementId = "Minesweeper_root") {

        //new content
        Div({ style { padding(5.px); position(Position.Relative) } }) {
            //menu div
            Div({ style { if (menu.hidden) { display(DisplayStyle.None) }; } }) {
                //
            }
            //menu bar table
            Table({ style { width(100.percent); height(100.percent); property("aspect-ratio", "$fieldWidth / 1"); property("table-layout", "fixed"); border(1.px, LineStyle.Solid, Color.white) } }) {
                Tr {
                    Td({
                        style { border(1.px, LineStyle.Solid, Color.dimgray) }
                        onClick { revealAll(); window.alert("BOOM!") }
                    }) {
                        Text("Top bar   Work In Progress")
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
                            val fieldNum = (i * fieldWidth) + j
                            Td({ style { padding(0.px); border(1.px, LineStyle.Solid, Color.black) } }) {
                                Button({
                                    style { width(100.percent); height(100.percent); padding(0.px); border(1.px, LineStyle.Solid, Color.white); backgroundColor(field[i][j].color()) }
                                    if (field[i][j].hidden) {
                                        onClick {
                                            if (field[i][j].content == 9) { revealAll() } else { field[i][j].show() }
                                        }
                                    }
                                }) {
                                    //Text("#$fieldNum#")
                                    Text(field[i][j].name())
                                    //Text("${field[i][j].index}")
                                }
                            }
                        }
                    }
                }
            }
        }//Div-end
    }
}