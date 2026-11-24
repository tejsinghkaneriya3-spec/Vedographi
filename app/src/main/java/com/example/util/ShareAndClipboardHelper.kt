package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.auth.findActivity
import com.example.domain.SocialPlatform
import com.example.ui.theme.HindiCalligraphyPreset
import java.io.File
import java.io.FileOutputStream

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
     * Opens the native Android share sheet with VEDOGRAPHi's brand summary and public link.
     */
    fun shareAppLink(context: Context): String {
        val appName = context.getString(R.string.app_name)
        val tagline = context.getString(R.string.brand_tagline)
        val shareUrl = context.getString(R.string.app_share_url)
        val message = "$appName — $tagline\n\n" +
            "Create 220+ stylish Unicode fonts, Hindi calligraphy cards, bios, and WhatsApp statuses:\n$shareUrl"
        copyToClipboard(context, shareUrl, label = "$appName Link")
        return if (launchChooser(context, message, "Share $appName")) {
            "Opening share sheet…"
        } else {
            "App link copied to clipboard!"
        }
    }

    /**
     * Renders a crisp gradient Hindi Calligraphy Card PNG using the selected bundled TTF typeface
     * and shares it via native Android Intent.ACTION_SEND using FileProvider (zero storage permissions).
     */
    fun shareCalligraphyCard(
        context: Context,
        text: String,
        preset: HindiCalligraphyPreset
    ): String {
        val displayText = text.trim().ifEmpty { context.getString(R.string.brand_tagline) }
        return try {
            val width = 1080
            val height = 640
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f,
                    0f,
                    width.toFloat(),
                    height.toFloat(),
                    intArrayOf(0xFF0B0617.toInt(), 0xFF2B125C.toInt(), 0xFF140B29.toInt()),
                    floatArrayOf(0f, 0.52f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            val cardRect = RectF(44f, 44f, width - 44f, height - 44f)
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 4f
                shader = LinearGradient(
                    cardRect.left,
                    cardRect.top,
                    cardRect.right,
                    cardRect.bottom,
                    intArrayOf(0xFFA855F7.toInt(), 0xFFE879F9.toInt(), 0xFF7C3AED.toInt()),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRoundRect(cardRect, 42f, 42f, borderPaint)

            val typeface = preset.fontResId?.let { resId ->
                try {
                    ResourcesCompat.getFont(context, resId)
                } catch (_: Exception) {
                    null
                }
            } ?: Typeface.DEFAULT

            val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFFFFFFF.toInt()
                textSize = when {
                    displayText.length <= 24 -> 66f
                    displayText.length <= 56 -> 52f
                    else -> 40f
                }
                this.typeface = typeface
            }

            val maxTextWidth = width - 160
            val layout = StaticLayout.Builder
                .obtain(displayText, 0, displayText.length, textPaint, maxTextWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(10f, 1.15f)
                .build()

            canvas.save()
            val textTop = ((height - layout.height) / 2f).coerceAtLeast(110f)
            canvas.translate(80f, textTop - 16f)
            layout.draw(canvas)
            canvas.restore()

            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFD8B4FE.toInt()
                textSize = 26f
                textAlign = Paint.Align.CENTER
                this.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }
            canvas.drawText(
                "VEDOGRAPHi • ${preset.hindiLabel} (${preset.displayName})",
                width / 2f,
                height - 76f,
                footerPaint
            )

            val dir = File(context.cacheDir, "calligraphy_cards").apply { mkdirs() }
            val imageFile = File(dir, "vedographi_calligraphy.png")
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            bitmap.recycle()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, displayText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(sendIntent, "Share Calligraphy Card")
            startSafely(context, chooser)
            "Sharing ${preset.displayName} calligraphy card…"
        } catch (_: Exception) {
            shareToPlatform(context, displayText, SocialPlatform.MORE)
        }
    }

    /**
     * Shares text directly to WhatsApp, Instagram, Facebook, Telegram, or the system chooser.
     * Always copies to clipboard first so apps like Instagram Bio/Captions have instant paste fallback.
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
            }
            return try {
                startSafely(context, intent)
                "Opening ${platform.displayName}…"
            } catch (_: Exception) {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(installedPkg)
                if (launchIntent != null && platform == SocialPlatform.INSTAGRAM) {
                    startSafely(context, launchIntent)
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
            val chooser = Intent.createChooser(sendIntent, title)
            startSafely(context, chooser)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun startSafely(context: Context, intent: Intent) {
        val activity = context.findActivity()
        if (activity != null) {
            activity.startActivity(intent)
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
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
