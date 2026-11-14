package com.movtery.zalithlauncher.game.control.importer

import androidx.compose.ui.graphics.Color
import com.movtery.layer_controller.data.*
import com.movtery.layer_controller.data.lang.createTranslatable
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.layout.ControlLayout
import com.movtery.layer_controller.layout.EmptyControlLayout
import com.movtery.layer_controller.layout.createNewLayer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import java.util.UUID
import kotlin.math.roundToInt

internal data class ImportScreen(val width: Int, val height: Int, val density: Float)
internal data class ImportResult(val layout: ControlLayout, val warnings: List<String>)

/** Converts the classic Pojav control format; native TCL layouts take a separate path. */
internal object PojavImporter {
    fun matches(root: JsonObject): Boolean = root["editorVersion"] == null &&
        (root["mControlDataList"] is JsonArray || root["controlDataList"] is JsonArray)

    fun convert(root: JsonObject, screen: ImportScreen): ImportResult {
        require(screen.width > 0 && screen.height > 0 && screen.density.isFinite() && screen.density > 0)
        val warnings = linkedSetOf<String>()
        val buttons = mutableListOf<NormalData>()
        val styles = mutableListOf<ButtonStyle>()
        val width = screen.width.toDouble()
        val height = screen.height.toDouble()
        val density = screen.density.toDouble()
        val version = root.int("version")
        if (version != 4) warnings += "Pojav version ${version ?: "unknown"} has not been verified; size and position may differ."
        warnings += "Positions are calculated for this device in landscape at import time; dynamic expressions are not retained. Check the layout in the editor."
        if (root["scaledAt"] != null) warnings += "The source control scale is not applied; check button sizes in the editor."

        fun addButton(element: JsonElement, label: String) {
            try {
                val b = element as? JsonObject ?: error("Not a button object")
                val name = b.string("name") ?: "Button"
                val w = b.number("width") ?: 50.0
                val h = b.number("height") ?: 50.0
                require(w.isFinite() && h.isFinite() && w >= 5 && h >= 5 && w <= 10000 && h <= 10000) { "Invalid size" }
                val wp = w * density
                val hp = h * density
                val vars = mapOf("screen_width" to width, "screen_height" to height, "width" to wp, "height" to hp,
                    "top" to 0.0, "left" to 0.0, "right" to width-wp, "bottom" to height-hp,
                    "margin" to 2.0*density, "preferred_scale" to 100.0)
                fun coordinate(axis: String, fallback: String, space: Double): Int {
                    val expression = b.string(axis) ?: b.string(fallback) ?: "0".also { warnings += "$label: missing $axis; positioned at 0." }
                    val value = try { PojavExpression.evaluate(expression, vars, density) } catch (e: Exception) {
                        warnings += "$label: cannot evaluate $axis ($expression); positioned at 0."
                        0.0
                    }
                    if (space <= 0 || value < 0 || value > space) warnings += "$label: position or size exceeds the screen and was clamped."
                    return ((value / space.coerceAtLeast(1.0) * 10000).coerceIn(0.0, 10000.0)).roundToInt()
                }
                val events = mutableListOf<ClickEvent>()
                val codes = b["keycodes"] as? JsonArray
                if (b["keycodes"] != null && codes == null) warnings += "$label: invalid keycodes."
                codes?.forEach { item ->
                    val code = (item as? JsonPrimitive)?.intOrNull
                    if (code == null) { warnings += "$label: invalid keycode."; return@forEach }
                    val event = when (code) {
                        0 -> null
                        -2 -> { warnings += "$label: control visibility toggle is not supported (no action assigned)."; null }
                        -5 -> { warnings += "$label: virtual mouse toggle is not supported (no action assigned)."; null }
                        -1 -> ClickEvent(ClickEvent.Type.LauncherEvent, "launcher.event.switch_ime")
                        -3 -> ClickEvent(ClickEvent.Type.LauncherEvent, "GLFW_MOUSE_BUTTON_LEFT")
                        -4 -> ClickEvent(ClickEvent.Type.LauncherEvent, "GLFW_MOUSE_BUTTON_RIGHT")
                        -6 -> ClickEvent(ClickEvent.Type.LauncherEvent, "GLFW_MOUSE_BUTTON_MIDDLE")
                        -7 -> ClickEvent(ClickEvent.Type.LauncherEvent, "launcher.event.scroll_up")
                        -8 -> ClickEvent(ClickEvent.Type.LauncherEvent, "launcher.event.scroll_down")
                        -9 -> ClickEvent(ClickEvent.Type.LauncherEvent, "launcher.event.switch_menu")
                        else -> pojavKeys[code]?.let { ClickEvent(ClickEvent.Type.Key, it) }.also {
                            if (it == null) warnings += "$label: unsupported keycode $code (no action assigned)."
                        }
                    }
                    if (event != null) events += event
                }
                if (b.bool("isDynamicBtn") == true || b.bool("isHideable") == true) warnings += "$label: dynamic/hideable button behavior is not supported."
                val knownFields = setOf("name", "width", "height", "dynamicX", "dynamicY", "x", "y", "keycodes",
                    "isDynamicBtn", "isHideable", "isSwipeable", "isToggle", "passThruEnabled", "displayInGame", "displayInMenu",
                    "bgColor", "strokeColor", "strokeWidth", "cornerRadius", "opacity")
                val extraFields = b.keys - knownFields
                if (extraFields.isNotEmpty()) warnings += "$label: unsupported fields ignored: ${extraFields.sorted().joinToString()}."
                val opacity = (b.number("opacity") ?: 1.0).coerceIn(0.0, 1.0).toFloat()
                val bg = Color((b.long("bgColor") ?: 0x4d000000L).toInt())
                val border = Color((b.long("strokeColor") ?: 0xffffffffL).toInt())
                val radius = ((b.number("cornerRadius") ?: 0.0).coerceIn(0.0, 100.0) / 100.0 * minOf(w, h) / 2).coerceAtMost(100.0).toFloat()
                val stroke = (b.number("strokeWidth") ?: 0.0).coerceIn(0.0, 50.0).roundToInt()
                val styleId = id()
                val shape = ButtonShape(radius)
                val config = DefaultButtonStyleConfig.copy(alpha = opacity, pressedAlpha = opacity,
                    backgroundColor = bg, pressedBackgroundColor = bg, borderColor = border, pressedBorderColor = border,
                    borderWidth = stroke, pressedBorderWidth = stroke, borderRadius = shape, pressedBorderRadius = shape)
                val visibility = when {
                    b.bool("displayInGame") == false && b.bool("displayInMenu") == false -> {
                        warnings += "$label: hidden in both game and menu; imported visible."
                        VisibilityType.ALWAYS
                    }
                    b.bool("displayInGame") == false -> VisibilityType.IN_MENU
                    b.bool("displayInMenu") == false -> VisibilityType.IN_GAME
                    else -> VisibilityType.ALWAYS
                }
                val pos = ButtonPosition(coordinate("dynamicX", "x", width - wp), coordinate("dynamicY", "y", height - hp))
                val size = ButtonSize(ButtonSize.Type.Dp, w.toFloat(), h.toFloat(), 100, 100,
                    ButtonSize.Reference.ScreenHeight, ButtonSize.Reference.ScreenHeight)
                val normal = NormalData(text = createTranslatable(name), uuid = id(), position = pos, buttonSize = size,
                    buttonStyle = styleId, textAlignment = TextAlignment.Center, visibilityType = visibility,
                    _clickEvents = events, isSwipple = b.bool("isSwipeable") == true,
                    isPenetrable = b.bool("passThruEnabled") == true, isToggleable = b.bool("isToggle") == true)
                styles += ButtonStyle(name, styleId, false, true, config, config)
                buttons += normal
            } catch (e: Exception) {
                warnings += "$label: skipped (${e.message ?: "invalid button"})."
            }
        }
        val list = (root["mControlDataList"] ?: root["controlDataList"]) as? JsonArray
            ?: throw SerializationException("Missing Pojav button list")
        list.forEachIndexed { i, element -> addButton(element, "Button ${i + 1}") }
        val knownRootFields = setOf("mControlDataList", "controlDataList", "mDrawerDataList", "mJoystickDataList", "scaledAt", "version")
        val extraRootFields = root.keys - knownRootFields
        if (extraRootFields.isNotEmpty()) warnings += "Unsupported layout fields ignored: ${extraRootFields.sorted().joinToString()}."
        val drawers = root["mDrawerDataList"]
        if (drawers is JsonArray && drawers.isNotEmpty()) {
            warnings += "Drawers cannot open or close: their buttons were imported separately; positions may overlap."
            drawers.forEachIndexed { i, entry ->
                val drawer = entry as? JsonObject
                if (drawer == null) { warnings += "Drawer ${i + 1}: invalid drawer skipped."; return@forEachIndexed }
                (drawer["properties"] as? JsonObject)?.let { addButton(it, "Drawer ${i + 1} handle") }
                val children = (drawer["buttonProperties"] ?: drawer["buttons"] ?: drawer["mControlDataList"]) as? JsonArray
                if (children == null) warnings += "Drawer ${i + 1}: no recognizable button list."
                children?.forEachIndexed { j, child -> addButton(child, "Drawer ${i + 1} button ${j + 1}") }
            }
        } else if (drawers != null && drawers !is JsonArray && drawers !is JsonNull) warnings += "Unrecognized drawer data was skipped."
        val joys = root["mJoystickDataList"]
        if (joys is JsonArray && joys.isNotEmpty()) warnings += "${joys.size} joystick(s) were not imported: this joystick format is not yet verified."
        else if (joys != null && joys !is JsonArray && joys !is JsonNull) warnings += "Unrecognized joystick data was skipped."
        if (buttons.isEmpty() && (list.isNotEmpty() || (drawers is JsonArray && drawers.isNotEmpty()) || (joys is JsonArray && joys.isNotEmpty())))
            throw SerializationException("No usable controls could be imported. This Pojav variant may not be supported.")
        val info = ControlLayout.Info(createTranslatable("Imported Pojav layout"), createTranslatable("Unknown"),
            createTranslatable("Imported from Pojav format"), 1, "1.0")
        return ImportResult(EmptyControlLayout.copy(info = info, layers = listOf(createNewLayer("Imported").copy(normalButtons = buttons)),
            styles = styles), warnings.toList())
    }
    private fun id() = UUID.randomUUID().toString().replace("-", "").take(18)
    private fun JsonObject.string(key: String) = (this[key] as? JsonPrimitive)?.contentOrNull
    private fun JsonObject.number(key: String) = (this[key] as? JsonPrimitive)?.doubleOrNull?.takeIf { it.isFinite() }
    private fun JsonObject.int(key: String) = (this[key] as? JsonPrimitive)?.intOrNull
    private fun JsonObject.long(key: String) = (this[key] as? JsonPrimitive)?.longOrNull
    private fun JsonObject.bool(key: String) = (this[key] as? JsonPrimitive)?.booleanOrNull
}
