package com.example.farmdirect.ui.farmer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

object FarmerDestinations {
    const val PRODUCTS_ROUTE = "products"
    const val ADD_PRODUCT_ROUTE = "add_product"
    const val EDIT_PRODUCT_ROUTE = "edit_product"
}

@Composable
fun FarmerNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = FarmerDestinations.PRODUCTS_ROUTE) {
        composable(FarmerDestinations.PRODUCTS_ROUTE) {
            val productsViewModel: ProductsViewModel = viewModel()
            val needsRefresh by navController.currentBackStackEntry
                ?.savedStateHandle
                ?.getStateFlow<Boolean>("needsRefresh", false)
                ?.collectAsState()!!

            LaunchedEffect(needsRefresh) {
                if (needsRefresh) {
                    productsViewModel.refresh()
                    navController.currentBackStackEntry?.savedStateHandle?.set("needsRefresh", false)
                }
            }

            ProductsRoute(
                viewModel = productsViewModel,
                onAddProduct = { navController.navigate(FarmerDestinations.ADD_PRODUCT_ROUTE) },
                onEditProduct = { productId ->
                    navController.navigate("${FarmerDestinations.EDIT_PRODUCT_ROUTE}/$productId")
                }
            )
        }
        composable(FarmerDestinations.ADD_PRODUCT_ROUTE) {
            AddProductRoute(
                onUpClick = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("needsRefresh", true)
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = "${FarmerDestinations.EDIT_PRODUCT_ROUTE}/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            EditProductRoute(
                onUpClick = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("needsRefresh", true)
                    navController.popBackStack()
                },
                productId = productId
            )
        }
    }
}
