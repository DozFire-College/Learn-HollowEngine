overlay("encore:homework_check") {
    anchor = "crosshair"
    placement = HudPlacement.AFTER
    autoShow = true

    content {
        Box(
            modifier = Modifier
                .padding(4.px)
                .background(UiColor(0f, 0f, 0f, 0.55f))
        ) {
            Text("HUD Выборы Магазин Журнал")
        }
    }
}