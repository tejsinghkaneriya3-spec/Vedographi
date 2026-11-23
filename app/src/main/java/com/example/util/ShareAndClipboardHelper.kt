package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.example.domain.SocialPlatform

object ShareAndClipboardHelper {

    fun copyToClipboard(context: Context, text: String, label: String = "VEDOGRAPHi Text"): Boolean {
        if (text.isEmpty()) return false
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return false
            clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
            true
        } catch (_: Exception) {
            false
        }
    }

    fun pasteFromClipboard(context: Context): String? {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return null
            if (!clipboard.hasPrimaryClip()) return null
            val clip = clipboard.primaryClip ?: return null
            if (clip.itemCount == 0) return null
            val item = clip.getItemAt(0) ?: return null
            item.text?.toString() ?: item.coerceToText(context)?.toString()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Shares text directly to WhatsApp, Instagram, Facebook, Telegram, or the system chooser.
     * Always copies to clipboard first so apps like Instagram Bio/Captions have instant paste fallback.
     * Returns a user-friendly status message.
     */
    fun shareToPlatform(context: Context, text: String, platform: SocialPlatform): String {
        copyToClipboard(context, text)

        if (platform == SocialPlatform.MORE) {
            val opened = launchChooser(context, text, "Share via VEDOGRAPHi")
            return if (opened) "Opening share sheet…" else "Copied to clipboard!"
        }

        val installedPkg = platform.packages.firstOrNull { isPackageInstalled(context, it) }
        if (installedPkg != null) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                setPackage(installedPkg)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return try {
                context.startActivity(intent)
                "Opening ${platform.displayName}…"
            } catch (_: Exception) {
                // Instagram/Facebook sometimes restrict direct ACTION_SEND text without media;
                // try launching the app directly with clipboard ready, or open chooser.
                val launchIntent = context.packageManager.getLaunchIntentForPackage(installedPkg)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (launchIntent != null && platform == SocialPlatform.INSTAGRAM) {
                    context.startActivity(launchIntent)
                    "Copied! Paste into Instagram Bio or Caption."
                } else {
                    launchChooser(context, text, "Share to ${platform.displayName}")
                    "Copied! Pick ${platform.displayName} from share sheet."
                }
            }
        } else {
            val opened = launchChooser(context, text, "Share styled text")
            return if (opened) {
                "${platform.displayName} not found — copied & opened share sheet."
            } else {
                "Copied styled text to clipboard!"
            }
        }
    }

    private fun launchChooser(context: Context, text: String, title: String): Boolean {
        return try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            val chooser = Intent.createChooser(sendIntent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }
}
