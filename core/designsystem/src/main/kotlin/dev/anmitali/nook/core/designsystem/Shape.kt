package dev.anmitali.nook.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val NookShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

object NookExtraShapes {
    val fileCard = NookShapes.small
    val dialog = NookShapes.large
    val bottomSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val fab = NookShapes.extraLarge
}
