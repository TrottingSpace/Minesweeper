import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable

fun main() {
    val fieldSize: Int = 12
    val minesCount: Int = 7

    val boxSize: Int = if (window.innerHeight < window.innerWidth){ (window.innerHeight / (fieldSize + 1)) } else { (window.innerWidth / (fieldSize + 1)) }
    console.log(window.innerHeight, window.innerWidth, boxSize)

    renderComposable(rootElementId = "root") {
        Text("$fieldSize $minesCount")
        Div({ style { padding((boxSize / 4).px) } }) {
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
                            Td ({ style { width(boxSize.px); margin(0.px); border(1.px, LineStyle.Solid, Color.blueviolet) } }){
                                Text(i.toString() + "\n" + j.toString())
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