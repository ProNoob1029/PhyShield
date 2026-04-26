package com.example.polihackplm2.functionality

import androidx.core.net.toUri

object UrlUnwrapper {
    /**
     * Unwraps Google redirect URLs to get the original destination URL.
     * Gmail often wraps links in https://www.google.com/url?q=...
     * 
     * @param url The URL to unwrap.
     * @return The original URL if it was a Google redirect, or the input URL otherwise.
     */
    fun unwrap(url: String): String {
        val uri = try {
            url.toUri()
        } catch (_: Exception) {
            return url
        }

        val host = uri.host ?: return url
        val path = uri.path ?: ""

        // Check if it's a google redirector
        // Matches google.com, google.ro, www.google.com, etc.
        val isGoogleHost = host == "google.com" || host.endsWith(".google.com") || 
                           host.split(".").any { it == "google" }
        
        val isUrlPath = path == "/url"

        if (isGoogleHost && isUrlPath) {
            // Google uses 'q' for external redirects and sometimes 'url'
            val originalUrl = uri.getQueryParameter("q") ?: uri.getQueryParameter("url")
            if (originalUrl != null) {
                return originalUrl
            }
        }

        return url
    }
}
