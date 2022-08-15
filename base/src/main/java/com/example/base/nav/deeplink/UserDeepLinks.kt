package com.example.base.nav.deeplink

import android.net.Uri
import androidx.core.net.toUri
import androidx.navigation.NavDeepLinkRequest

const val ACTION_DEFAULT = 0
const val ACTION_BACK = 1


val WelcomeDeepLink =
    NavDeepLinkRequest(Uri.parse("app://com.example.cloud/user/welcome"), null, null)

val SelectUserDeepLink =
    NavDeepLinkRequest(Uri.parse("app://com.example.cloud/user/select_user"), null, null)

/**
 * 构造跳转到角色选择界面的deeplink
 * @param action back or enter
 */
fun createSelectUserDeepLink(action: Int): NavDeepLinkRequest {
    NavDeepLinkRequest.Builder.fromUri("".toUri())
        .build()
    return NavDeepLinkRequest(
        Uri.parse("app://com.example.cloud/user/select_user?action=$action"),
        null,
        null
    )
}


