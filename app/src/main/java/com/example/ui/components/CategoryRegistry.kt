package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.IOSBlue
import com.example.ui.theme.IOSGreen
import com.example.ui.theme.IOSGray
import com.example.ui.theme.IOSOrange
import com.example.ui.theme.IOSPurple
import com.example.ui.theme.IOSRed
import com.example.ui.theme.IOSTeal
import com.example.ui.theme.IOSYellow

data class CategoryMeta(
    val icon: ImageVector,
    val color: Color
)

object CategoryRegistry {
    val categories = mapOf(
        "Ingresos" to CategoryMeta(Icons.Default.TrendingUp, IOSGreen),
        "Comida" to CategoryMeta(Icons.Default.Restaurant, IOSOrange),
        "Transporte" to CategoryMeta(Icons.Default.DirectionsCar, IOSBlue),
        "Entretenimiento" to CategoryMeta(Icons.Default.SportsEsports, IOSPurple),
        "Hogar" to CategoryMeta(Icons.Default.Home, IOSRed),
        "Salud" to CategoryMeta(Icons.Default.MedicalServices, IOSTeal),
        "Educación" to CategoryMeta(Icons.Default.School, IOSYellow),
        "Otros" to CategoryMeta(Icons.Default.Category, IOSGray)
    )

    fun getIcon(category: String): ImageVector {
        return categories[category]?.icon ?: Icons.Default.Category
    }

    fun getColor(category: String): Color {
        return categories[category]?.color ?: IOSGray
    }
}
