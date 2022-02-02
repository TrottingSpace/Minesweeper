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

var fieldWith = 16
var fieldHeight = 8
var minesCount = 37

class Menu {
    var hidden by mutableStateOf(true)
}

class Field(private val fieldIndex: Int) {
    var content = 0
        private set
    var hidden by mutableStateOf(true)
        private set
    var flagged by mutableStateOf(false)
        private set
    private val nameList = listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "!!", "?")
    fun color(): CSSColorValue { return if (hidden) { Color.lightgray } else { Color.gray } }
    fun name(): String { return if (flagged) { nameList[10] } else if (hidden) { nameList[11] } else { nameList[content] } }
    fun show() { hidden = false }
    fun mineCounter() {
        val searchMinesAt = mutableListOf((-fieldWith-1), (-fieldWith), (-fieldWith+1), (-1), (1), (fieldWith-1), (fieldWith), (fieldWith+1))
        if (content != 9) {
            //
        }
    }
}

val menu = Menu()
val field: MutableList<Field> = mutableListOf()
fun rebuildMinefield() {
    while (field.size > 0) { field.removeAt(0) }
    for (indexF in 0 until (fieldHeight * fieldWith)) { field.add(Field(indexF)) }
}




var fieldSize = 16

fun main() {
    console.log("%c Welcome in Minesweeper! ", "color: white; font-weight: bold; background-color: black;")
    rebuildMinefield()

    console.log("Starting with:\n Field width:\t", fieldWith, "\n Field height:\t", fieldHeight, "\n Field size:\t", field.size, "\n Mines count:\t", minesCount)



    //calculating dimensions
    val boxSize: Int = if (window.innerHeight < window.innerWidth){ (window.innerHeight / (fieldSize + 1)) } else { (window.innerWidth / (fieldSize + 2)) }
    console.log(" Window height:\t", window.innerHeight, "\n Window width:\t", window.innerWidth, "\n Size factor: \t", boxSize)
    var boxSizeMultiplier by mutableStateOf(1.0)



    //"Minesweeper_root" div style applying
    //document.getElementById("Minesweeper_root")?.setAttribute("style", "padding: 0px; border: none; aspect-ratio: ${fieldWith.toDouble() / (fieldHeight + 1)}; position: relative; margin: 0px auto;")

    renderComposable(rootElementId = "Minesweeper_root") {

        //new content
        Div({ style { padding(5.px); position(Position.Relative) } }) {
            //menu div
            Div({ style { if (menu.hidden) { display(DisplayStyle.None) }; } }) {
                //
            }
            //menu bar table
            Table({ style { width(100.percent); height(100.percent); property("aspect-ratio", "$fieldWith / 1"); property("table-layout", "fixed"); border(1.px, LineStyle.Solid, Color.white) } }) {
                Tr {
                    Td({ style { border(1.px, LineStyle.Solid, Color.dimgray) } }) {
                        Text("Top bar")
                    }
                }
            }
            //minefield table
            Table({
                style {
                    width(100.percent)
                    height(100.percent)
                    property("aspect-ratio", "${fieldWith.toDouble() / fieldHeight}")
                    property("table-layout", "fixed")
                    property("border-spacing", "0px")
                    textAlign("center")
                    border(1.px, LineStyle.Solid, Color.white)
                }
            }) {
                for (i in 0 until fieldHeight) {
                    Tr {
                        for (j in 0 until fieldWith) {
                            val fieldNum = j + (i * fieldWith)
                            Td({ style { padding(0.px); border(1.px, LineStyle.Solid, Color.black) } }) {
                                Button({
                                    style { width(100.percent); height(100.percent); padding(0.px); border(1.px, LineStyle.Solid, Color.white); backgroundColor(field[fieldNum].color()) }
                                    if (field[fieldNum].hidden) {
                                        onClick { field[fieldNum].show() }
                                    }
                                }) {
                                    Text("#$fieldNum#")
                                    Text(field[fieldNum].name())
                                }
                            }
                        }
                    }
                }
            }
        }//Div-end
    }
}