package com.aryan.calculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryan.calculator.ui.theme.AccentPink
import com.aryan.calculator.ui.theme.BgDark

enum class NavTab(val label: String) {
    HOME("Home"),
    SEARCH("Search"),
    LIBRARY("Library"),
    PROFILE("Profile")
}

@Composable
fun BottomNav(selected: NavTab, onSelect: (NavTab) -> Unit) {
    NavigationBar(
        containerColor = BgDark.copy(alpha = 0.95f),
        contentColor = Color.White,
        modifier = Modifier.height(70.dp)
    ) {
        NavigationBarItem(
            selected = selected == NavTab.HOME,
            onClick = { onSelect(NavTab.HOME) },
            icon = {
                Icon(
                    if (selected == NavTab.HOME) Icons.Rounded.Home else Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            },
            label = {
                Text(
                    "Home",
                    fontSize = 10.sp,
                    fontWeight = if (selected == NavTab.HOME) FontWeight.SemiBold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentPink,
                selectedTextColor = AccentPink,
                unselectedIconColor = Color.White.copy(alpha = 0.5f),
                unselectedTextColor = Color.White.copy(alpha = 0.5f),
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selected == NavTab.SEARCH,
            onClick = { onSelect(NavTab.SEARCH) },
            icon = {
                Icon(
                    if (selected == NavTab.SEARCH) Icons.Rounded.Search else Icons.Outlined.Search,
                    contentDescription = "Search"
                )
            },
            label = {
                Text(
                    "Search",
                    fontSize = 10.sp,
                    fontWeight = if (selected == NavTab.SEARCH) FontWeight.SemiBold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentPink,
                selectedTextColor = AccentPink,
                unselectedIconColor = Color.White.copy(alpha = 0.5f),
                unselectedTextColor = Color.White.copy(alpha = 0.5f),
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selected == NavTab.LIBRARY,
            onClick = { onSelect(NavTab.LIBRARY) },
            icon = {
                Icon(
                    if (selected == NavTab.LIBRARY) Icons.Rounded.LibraryMusic else Icons.Outlined.LibraryMusic,
                    contentDescription = "Library"
                )
            },
            label = {
                Text(
                    "Library",
                    fontSize = 10.sp,
                    fontWeight = if (selected == NavTab.LIBRARY) FontWeight.SemiBold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentPink,
                selectedTextColor = AccentPink,
                unselectedIconColor = Color.White.copy(alpha = 0.5f),
                unselectedTextColor = Color.White.copy(alpha = 0.5f),
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selected == NavTab.PROFILE,
            onClick = { onSelect(NavTab.PROFILE) },
            icon = {
                Icon(
                    if (selected == NavTab.PROFILE) Icons.Rounded.Person else Icons.Outlined.Person,
                    contentDescription = "Profile"
                )
            },
            label = {
                Text(
                    "Profile",
                    fontSize = 10.sp,
                    fontWeight = if (selected == NavTab.PROFILE) FontWeight.SemiBold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = AccentPink,
                selectedTextColor = AccentPink,
                unselectedIconColor = Color.White.copy(alpha = 0.5f),
                unselectedTextColor = Color.White.copy(alpha = 0.5f),
                indicatorColor = Color.Transparent
            )
        )
    }
}
