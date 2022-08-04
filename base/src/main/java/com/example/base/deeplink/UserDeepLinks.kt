package com.example.base.deeplink

import android.net.Uri
import androidx.navigation.NavDeepLinkRequest

val WelcomeDeepLink =
    NavDeepLinkRequest(Uri.parse("android:app://com.example.cloud/user/welcome"), null, null)

val SelectUserDeepLink =
    NavDeepLinkRequest(Uri.parse("android:app://com.example.cloud/user/select_user"), null, null)

val MainDeepLink =
    NavDeepLinkRequest(Uri.parse("android:app://com.example.cloud/cloud/main"), null, null)


