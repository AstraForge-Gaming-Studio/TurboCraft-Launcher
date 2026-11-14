package com.movtery.zalithlauncher.game.control.importer

import com.movtery.layer_controller.data.ButtonSize
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.layout.loadLayoutFromFile
import com.movtery.layer_controller.utils.saveToFile
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class PojavImporterTest {
    private val screen = ImportScreen(1000, 500, 2f)
    private fun root(json: String) = Json.parseToJsonElement(json).jsonObject

    @Test fun expressions() {
        val vars = mapOf("screen_width" to 1000.0, "width" to 100.0, "margin" to 4.0)
        assertEquals(892.0, PojavExpression.evaluate("\${screen_width} - \${width} - px(4)", vars, 2.0), 0.001)
        assertEquals(6.0, PojavExpression.evaluate("dp(8) + 2", vars, 2.0), 0.001)
        assertTrue(runCatching { PojavExpression.evaluate("\${unknown}", vars, 2.0) }.isFailure)
        assertTrue(runCatching { PojavExpression.evaluate("1 / 0", vars, 2.0) }.isFailure)
        assertTrue(runCatching { PojavExpression.evaluate("evil(1)", vars, 2.0) }.isFailure)
    }

    @Test fun importsButtonsAndPersistsNativeLayout() = runBlocking {
        val input = root("""{"version":4,"mControlDataList":[
            {"name":"Keyboard","width":50,"height":50,"dynamicX":"${'$'}{right}","dynamicY":"${'$'}{bottom}","keycodes":[-1,-3,87,0],"isToggle":true,"isSwipeable":true,"passThruEnabled":true},
            {"name":"Unsupported","width":50,"height":50,"dynamicX":"bad(1)","dynamicY":"0","keycodes":[-2,-5,999]}
        ],"mDrawerDataList":[],"scaledAt":100}""")
        assertTrue(PojavImporter.matches(input))
        val result = PojavImporter.convert(input, screen)
        val first = result.layout.layers.single().normalButtons.first()
        assertEquals(10000, first.position.x)
        assertEquals(10000, first.position.y)
        assertEquals(ButtonSize.Type.Dp, first.buttonSize.type)
        assertEquals(listOf("launcher.event.switch_ime", "GLFW_MOUSE_BUTTON_LEFT", "GLFW_KEY_W"), first.clickEvents.map { it.key })
        assertEquals(ClickEvent.Type.LauncherEvent, first.clickEvents[1].type)
        assertTrue(first.isToggleable && first.isSwipple && first.isPenetrable)
        assertTrue(result.warnings.any { it.contains("control visibility toggle") })
        assertTrue(result.warnings.any { it.contains("virtual mouse toggle") })
        assertTrue(result.warnings.any { it.contains("cannot evaluate") })
        val file = File.createTempFile("pojav-import", ".json")
        try {
            result.layout.saveToFile(file)
            val loaded = loadLayoutFromFile(file)
            assertEquals(first.clickEvents, loaded.layers.single().normalButtons.first().clickEvents)
            assertEquals(result.layout.styles.size, loaded.styles.size)
        } finally { file.delete() }
    }

    @Test fun drawersAndUnverifiedFeaturesAreReported() {
        val input = root("""{"version":4,"controlDataList":[],"mDrawerDataList":[{"properties":{"name":"Drawer","keycodes":[],"dynamicX":"0","dynamicY":"0"},"buttonProperties":[{"name":"Child","keycodes":[84],"dynamicX":"0","dynamicY":"0","image":"foo.png"}]}],"mJoystickDataList":[{}]}""")
        val result = PojavImporter.convert(input, screen)
        assertEquals(2, result.layout.layers.single().normalButtons.size)
        assertTrue(result.warnings.any { it.contains("Drawers") })
        assertTrue(result.warnings.any { it.contains("image") })
        assertTrue(result.warnings.any { it.contains("joystick") })
    }

    @Test fun skipsInvalidButtonsAndRejectsAnEntirelyUnusableFile() {
        val input = root("""{"version":4,"mControlDataList":[{"width":-1},{"dynamicX":"0","dynamicY":"0","keycodes":[65]}]}""")
        val result = PojavImporter.convert(input, screen)
        assertEquals(1, result.layout.layers.single().normalButtons.size)
        assertTrue(result.warnings.any { it.contains("skipped") })
        assertTrue(runCatching { PojavImporter.convert(root("""{"mControlDataList":[{"width":-1}]}"""), screen) }.isFailure)
        assertFalse(PojavImporter.matches(root("""{"editorVersion":99,"mControlDataList":[]}""")))
    }
}
