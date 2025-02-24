package com.example.taskmanager.core.utils

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/**
 * @author Abdallah Elsokkary
 */
fun NavGraphBuilder.animatedComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable () -> Unit
) {
    composable(
        route = route,
        arguments = arguments,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        content()
    }
}
// swipe up animation
//    composable(
//        route = route,
//        enterTransition = {
//            slideInVertically(
//                initialOffsetY = { fullHeight -> fullHeight },
//                animationSpec = tween(300)
//            ) + fadeIn(animationSpec = tween(300))
//        },
//        exitTransition = {
//            slideOutVertically(
//                targetOffsetY = { fullHeight -> -fullHeight },
//                animationSpec = tween(300)
//            ) + fadeOut(animationSpec = tween(300))
//        },
//        popEnterTransition = {
//            slideInVertically(
//                initialOffsetY = { fullHeight -> -fullHeight },
//                animationSpec = tween(300)
//            ) + fadeIn(animationSpec = tween(300))
//        },
//        popExitTransition = {
//            slideOutVertically(
//                targetOffsetY = { fullHeight -> fullHeight },
//                animationSpec = tween(300)
//            ) + fadeOut(animationSpec = tween(300))
//        }
//    ) {
//        content()
//    }

// fade animation
//composable(
//route = route,
//enterTransition = {
//    scaleIn(
//        initialScale = 0.8f,
//        animationSpec = tween(300)
//    ) + fadeIn(animationSpec = tween(300))
//},
//exitTransition = {
//    scaleOut(
//        targetScale = 1.2f,
//        animationSpec = tween(300)
//    ) + fadeOut(animationSpec = tween(300))
//},
//popEnterTransition = {
//    scaleIn(
//        initialScale = 1.2f,
//        animationSpec = tween(300)
//    ) + fadeIn(animationSpec = tween(300))
//},
//popExitTransition = {
//    scaleOut(
//        targetScale = 0.8f,
//        animationSpec = tween(300)
//    ) + fadeOut(animationSpec = tween(300))
//}
//) {
//    content()
//}