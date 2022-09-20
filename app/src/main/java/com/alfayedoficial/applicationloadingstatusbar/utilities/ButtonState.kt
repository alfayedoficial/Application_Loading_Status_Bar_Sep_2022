package com.alfayedoficial.applicationloadingstatusbar.utilities

sealed class ButtonState {
    object Clicked : ButtonState()
    object Loading : ButtonState()
    object Completed : ButtonState()
}