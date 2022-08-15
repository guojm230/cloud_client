package com.example.base.nav

import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavOptions


fun NavController.clearAndNavigate(
    deepLinkRequest: NavDeepLinkRequest,
    options: NavOptions? = null
) {
    while (popBackStack()) {
    }
    navigate(deepLinkRequest, options)
}