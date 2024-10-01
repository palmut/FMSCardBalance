@file:OptIn(ExperimentalDecomposeApi::class)

package net.palmut.fmscardbalance.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import net.palmut.fmscardbalance.component.ApplicationComponent

@Composable
fun MainScreen(component: ApplicationComponent) {
    Box(
        modifier = Modifier
            .background(Color(0xFFFFFFF9))
            .fillMaxSize()
    ) {
        Children(
            component = component,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun Children(component: ApplicationComponent, modifier: Modifier = Modifier) {
    Children(
        stack = component.childStack,
        modifier = modifier,
        animation = (component as? BackHandlerOwner)?.backHandler?.let {
            predictiveBackAnimation(
                backHandler = it,
                fallbackAnimation = stackAnimation(scale() + fade()),
                onBack = component::onBackPressed
            )
        }
    ) {
        when (val child = it.instance) {
            is ApplicationComponent.Child.Main -> CardListScreen(component = child.component)
        }
    }
}
