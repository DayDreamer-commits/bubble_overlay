package com.adriankohls.bubble_overlay

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

/** BubbleOverlayPlugin */
class BubbleOverlayPlugin : ActivityAware, FlutterPlugin, MethodChannel.MethodCallHandler {
    private lateinit var activity: Activity
    private lateinit var channel: MethodChannel
    private val channelName: String = "com.adriankohls/bubble_overlay"
    private val CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084
    private lateinit var mOverlayService: BubbleOverlayService
    private lateinit var connection: ServiceConnection
    private var mBound: Boolean = false
    private var arguments: List<Any?> = arrayListOf();

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        connection = object : ServiceConnection {

            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                val binder = service as BubbleOverlayService.LocalBinder
                mOverlayService = binder.getService()
                mBound = true

                if (call.method == "openBubble") {

                    val title: String? = arguments[0] as String?
                    val customText: String? = arguments[1] as String?
                    val bottomText: String? = arguments[2] as String?
                    val titleColor: String? = arguments[3] as String?
                    val textColor: String? = arguments[4] as String?
                    val bottomColor: String? = arguments[5] as String?
                    val backgroundColor: String? = arguments[6] as String?
                    val topIconAsset: ByteArray? = arguments[7] as ByteArray?
                    val bottomIconAsset: ByteArray? = arguments[8] as ByteArray?

                    if (title != null)
                        mOverlayService.updateTitle(title)
                    if (customText != null)
                        mOverlayService.updateText(customText)
                    if (bottomText != null)
                        mOverlayService.updateBottomText(bottomText)
                    if (titleColor != null)
                        mOverlayService.updateTitleColor(titleColor)
                    if (textColor != null)
                        mOverlayService.updateTextColor(textColor)
                    if (bottomColor != null)
                        mOverlayService.updateTextColor(bottomColor)
                    if (backgroundColor != null)
                        mOverlayService.updateBubbleColor(backgroundColor)
                    if (topIconAsset != null)
                        mOverlayService.updateIconTop(topIconAsset)
                    if (bottomIconAsset != null)
                        mOverlayService.updateIconBottom(bottomIconAsset)
                }
            }

            override fun onServiceDisconnected(arg0: ComponentName) {
                mBound = false
                mOverlayService.stopSelf()
            }
        }
        Intent(activity, BubbleOverlayService::class.java).also { intent ->
            activity.bindService(intent, connection, 0)
        }
        when (call.method) {
            "openBubble" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
                    val packageName = activity.packageName
                    activity.startActivityForResult(
                            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")),
                            CODE_DRAW_OVER_OTHER_APP_PERMISSION)
                } else {
                    arguments = call.arguments as List<Any?>
                    activity.startService(
                            Intent(activity, BubbleOverlayService::class.java))
                    activity.moveTaskToBack(true)
                }
            }
            "closeBubble" -> {
                if (mBound)
                    mOverlayService.stopSelf()
            }
            "updateBubbleText" -> {
                if (mBound) {
                    val text = call.arguments as String
                    mOverlayService.updateText(text)
                } else
                    throw Exception("BubbleService not running.")
            }
            "updateBubbleTextColor" -> {
                if (mBound) {
                    val textColor = call.arguments as String
                    mOverlayService.updateTextColor(textColor)
                } else
                    throw Exception("BubbleService not running.")
            }
            "updateBubbleTitle" -> {
                if (mBound) {
                    val text = call.arguments as String
                    mOverlayService.updateTitle(text)
                } else
                    throw Exception("BubbleService not running.")
            }
            "updateBubbleTitleColor" -> {
                if (mBound) {
                    val textColor = call.arguments as String
                    mOverlayService.updateTitleColor(textColor)
                } else
                    throw Exception("BubbleService not running.")
            }
            "updateBubbleBottomText" -> {
                if (mBound) {
                    val text = call.arguments as String
                    mOverlayService.updateBottomText(text)
                } else
                    throw Exception("BubbleService not running.")
            }
            "updateBubbleBottomTextColor" -> {
                if (mBound) {
                    val textColor = call.arguments as String
                    mOverlayService.updateBottomTextColor(textColor)
                } else
                    throw Exception("BubbleService not running.")
            }
            "updateBubbleColor" -> {
                if (mBound) {
                    val bubbleColor = call.arguments as String
                    mOverlayService.updateBubbleColor(bubbleColor)
                } else
                    throw Exception("BubbleService not running.")
            }
            "updateBubbleTopIcon" -> {
                if (mBound) {
                    val icon = call.arguments as ByteArray
                    mOverlayService.updateIconTop(icon)
                } else
                    throw Exception("BubbleService not running.")
            }
            "updateBubbleBottomIcon" -> {
                if (mBound) {
                    val icon = call.arguments as ByteArray
                    mOverlayService.updateIconBottom(icon)
                } else
                    throw Exception("BubbleService not running.")
            }
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, channelName)
        channel.setMethodCallHandler(this)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        activity.unbindService(connection)
        mOverlayService.stopSelf()
        mBound = false
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        // TODO: your plugin is now attached to a new Activity after a configuration change.
    }

    override fun onDetachedFromActivityForConfigChanges() {
        // TODO: the Activity your plugin was attached to was destroyed to change configuration.
        // This call will be followed by onReattachedToActivityForConfigChanges().
    }
}