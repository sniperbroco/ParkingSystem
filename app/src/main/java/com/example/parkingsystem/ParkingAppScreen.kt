package com.example.parkingsystem

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.parkingsystem.ui.LoginScreen
import com.example.parkingsystem.view.RegisterScreen
import kotlinx.coroutines.launch


enum class ParkingAppScreen() {
    WelcomePage, Login, Register, MapView
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingAppBar(
    canNavigateBack: Boolean, navigateUp: () -> Unit, modifier: Modifier = Modifier
) {
    TopAppBar(title = { Text(stringResource(id = R.string.app_name)) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack, contentDescription = "Back Button"
                    )
                }
            }
        })
}

@Composable
fun ParkingApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ParkingAppScreen.MapView.name) {
        composable(ParkingAppScreen.WelcomePage.name) {
            ScaffoldWrapper(canNavigateBack = false, navigateUp = { /* Implement back navigation */ }) {
                WelcomePage(navController = navController)
            }
        }
        composable(ParkingAppScreen.Login.name) {
            ScaffoldWrapper(canNavigateBack = false, navigateUp = { /* Implement back navigation */ }) {
                LoginScreen(navController = navController)
            }
        }
        composable(ParkingAppScreen.Register.name) {
            RegisterScreen(navController = navController)
        }
        composable(ParkingAppScreen.MapView.name) {
            ParkingAreaList(navController = navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldWrapper(
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    content: @Composable (innerPadding: Modifier) -> Unit
) {
    Scaffold(
        topBar = {
            ParkingAppBar(
                canNavigateBack = canNavigateBack,
                navigateUp = navigateUp
            )
        }
    ) { innerPadding ->
        Modifier.padding(innerPadding).let { paddingModifier ->
            content(paddingModifier)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingAreaList(
    modifier: Modifier = Modifier.fillMaxSize(),
    navController: NavController = rememberNavController(),
) {
    // Define the state of the search text
    var searchText by remember { mutableStateOf(" ") }

    //Remember Clicked item state
    var selectedItemIndex by rememberSaveable {
        mutableStateOf(0)
    }

    // Use a Box to layer content
    Box() {
        // OSMDroidMapView as the background covering the whole screen
        OSMDroidMapView(
            modifier = Modifier.fillMaxHeight()
            // This ensures the map view covers the entire screen
        )

        // Column to hold the overlays
        Column(
            modifier = Modifier
                .fillMaxSize() // Allow the column to take the full space
                .padding(16.dp)
        ) {
            // Top section with title and search box
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {

                Row() {


                    val drawerState =
                        rememberDrawerState(DrawerValue.Closed) // Remember the drawer state
                    val scope = rememberCoroutineScope()

                    ///List of Navigation Items that will be clicked
                    val items = listOf(
                        NavigationItems(
                            title = "Login",
                            selectedIcon = Icons.Filled.Home,
                            unselectedIcon = Icons.Outlined.Home
                        ),
                        NavigationItems(
                            title = "Register",
                            selectedIcon = Icons.Filled.Info,
                            unselectedIcon = Icons.Outlined.Info
                        ),
                        NavigationItems(
                            title = "Edit",
                            selectedIcon = Icons.Filled.Edit,
                            unselectedIcon = Icons.Outlined.Edit,
                            badgeCount = 105
                        ),
                        NavigationItems(
                            title = "Settings",
                            selectedIcon = Icons.Filled.Settings,
                            unselectedIcon = Icons.Outlined.Settings
                        )
                    )

                    Box () {
                        ModalNavigationDrawer(
                            modifier = Modifier.height(300.dp).width(200.dp),
                            drawerState = drawerState,
                            drawerContent = {
                                ModalDrawerSheet(drawerContentColor = Color.Gray) {
                                    Spacer(
                                        modifier = Modifier
                                            .height(16.dp)
                                            .width(200.dp)
                                    ) //space (margin) from top
                                    items.forEachIndexed { index, item ->
                                        NavigationDrawerItem(
                                            label = { Text(text = item.title) },
                                            selected = index == selectedItemIndex,
                                            onClick = {
                                                //  navController.navigate(item.route)
                                                selectedItemIndex = index
                                                scope.launch {
                                                    Log.e("Clicked", "Selected: $selectedItemIndex")
                                                    if (selectedItemIndex==0){
                                                        navController.navigate(ParkingAppScreen.Login.name)
                                                    }
                                                    else if (selectedItemIndex==1){
                                                        navController.navigate(ParkingAppScreen.Register.name)
                                                    }
                                                }
                                            },
                                            icon = {
                                                Icon(
                                                    imageVector = if (index == selectedItemIndex) {
                                                        item.selectedIcon
                                                    } else item.unselectedIcon,
                                                    contentDescription = item.title
                                                )
                                            },
                                            badge = {  // Show Badge
                                                item.badgeCount?.let {
                                                    Text(text = item.badgeCount.toString())
                                                }
                                            },
                                            modifier = Modifier
                                                .padding(NavigationDrawerItemDefaults.ItemPadding) //padding between items
                                        )
                                    }

                                }
                            },
                            gesturesEnabled = drawerState.isOpen,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier.padding(top = 20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Menu",
                                        tint = Color.Black,
                                        modifier = Modifier.clickable {
                                            scope.launch {
                                                when (drawerState.isClosed) {
                                                    true -> {
                                                        drawerState.open()
                                                    }

                                                    false -> drawerState.close()
                                                }
                                            }
                                        }
                                    )
                                }
                                // Spacer to occupy remaining space
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0XFF101921))
                    ) {
                        TextField(value = searchText,
                            onValueChange = { searchText = it },
                            textStyle = TextStyle(fontSize = 20.sp),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Icon",
                                    modifier = Modifier.clickable {
                                        // Handle the search icon click here
                                        // For example, perform a search operation based on searchText
                                        //getGeoPoint(searchText)
                                    }
                                )
                            },
                            placeholder = { Text(text = "Search") })
                    }
                }
            }

            Spacer(modifier = Modifier.height(500.dp))

            // Sort and location section
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Sort by: Distance")
                Text("UP Tacloban")
                Text("Tacloban Place")
            }

            // Parking area items
            ParkingAreaItem(title = "RTR Plaza", price = "FREE")
            ParkingAreaItem(title = "Nique'Residence", price = "P20/HR")

        }
    }
}


// Create Navigation Items Class to Select Unselect items
data class NavigationItems(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int? = null
)


