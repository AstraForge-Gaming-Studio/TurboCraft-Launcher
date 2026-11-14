# Pojav control import

Use the existing control-layout import action to select a JSON file. Classic
Pojav-style files containing `mControlDataList` (or `controlDataList`) are
converted into native editable TCL layouts. Native TCL layouts still use the
normal version-checked import path. Imported layouts are available in the
control list/editor; review them before playing.

Classic Pojav v4 button dimensions are in dp, while position expressions are in
pixels. The importer evaluates supported expressions for the importing device's
landscape display and density, then stores their positions as TCL percentages.
This is a snapshot, not a live dynamic expression. Screen insets, author device
settings, preferred scale (assumed 100), and control scale may differ. Check
sizes and positions in the editor, especially on different aspect ratios.
Operators `+ - * /`, parentheses, the standard position variables and `px()` /
`dp()` are supported; unsupported expressions are positioned at zero with a
warning. Button colors, borders, text, multi-key bindings, toggle, swipe and
pass-through are converted on a best-effort basis.

Import notes are displayed after import. Drawer children are flattened; drawer
open/close and placement can differ. Pojav's control-visibility toggle (-2),
virtual mouse (-5), unknown keycodes, image fields and unverified joystick
formats are not mapped to a different action; they are reported. Camera
joysticks, image assets, ZIP packs, and other launcher formats are not currently
supported. If nothing usable can be converted, import fails rather than saving
an empty broken layout. Old Pojav versions and fork-specific variants are not
verified. Please provide real MJ Launcher exports to validate their schema.

Format reference: PojavLauncher v3_openjdk `ControlData.java`,
`ControlDrawerData.java` and `assets/default.json`.
