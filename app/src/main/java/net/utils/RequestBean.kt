package net.utils

import java.io.Serializable


data class RequestBean(
    var appName: String = "",
    var appId: String = "",
    var applink: String = "",
    var ref: String = "",
    var token: String = "",
    var istatus: Boolean = false
):Serializable
