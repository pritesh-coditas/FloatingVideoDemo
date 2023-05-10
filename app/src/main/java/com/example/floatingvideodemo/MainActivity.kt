package com.example.floatingvideodemo

import android.app.admin.SystemUpdatePolicy.ValidationFailedException
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Config
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.ExternalTexture
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class MainActivity : AppCompatActivity() {
    companion object {
        const val HEIGHT = 0.20f
    }

    private var videoRenderable: ModelRenderable? = null
    private lateinit var arFragment: ArFragment
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var anchorNode: AnchorNode
    private lateinit var videoTexture: ExternalTexture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        arFragment = supportFragmentManager.findFragmentById(R.id.fragment) as ArFragment
       /* arFragment.planeDiscoveryController.hide()
        arFragment.planeDiscoveryController.setInstructionView(null)
        arFragment.arSceneView.planeRenderer.isEnabled = false*/

     /*   arFragment.arSceneView.session?.config?.let { config ->
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            config.focusMode = Config.FocusMode.AUTO
        }*/

        videoTexture = ExternalTexture()

        mediaPlayer = MediaPlayer.create(this, R.raw.ted)
        mediaPlayer.setSurface(videoTexture.surface)
        mediaPlayer.isLooping = true

        ModelRenderable.builder()
            .setSource(this, R.raw.chroma_key_video)
            .build()
            .thenAccept { renderable: ModelRenderable ->
                videoRenderable = renderable
                renderable.material.setExternalTexture("videoTexture", videoTexture)
                renderable.material.setFloat4(
                    "keyColor",
                    Color(0.1843f, 1.0f, 0.098f)
                )
            }
            .exceptionally {
                Toast.makeText(this, "Unable to load video renderable", Toast.LENGTH_LONG).show()
                null
            }

        arFragment.setOnTapArPlaneListener { hitResult: HitResult, _, _ ->
            if (videoRenderable == null) {
                return@setOnTapArPlaneListener
            }

            val anchor = hitResult.createAnchor()
            anchorNode =
                AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)

            val videoNode = TransformableNode(arFragment.transformationSystem)
            videoNode.setParent(anchorNode)

            val videoWidth = mediaPlayer.videoWidth.toFloat()
            val videoHeight = mediaPlayer.videoHeight.toFloat()
         /*   videoNode.localScale = Vector3(
                HEIGHT * (videoWidth / videoHeight),
                HEIGHT,
                0.95f
            )*/

            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()

                videoTexture
                    .surfaceTexture
                    .setOnFrameAvailableListener {
                        videoNode.renderable = videoRenderable
                        videoTexture.surfaceTexture.setOnFrameAvailableListener(null)
                    }
            } else {
                videoNode.renderable = videoRenderable
            }
            anchorNode.localScale = Vector3(HEIGHT*(videoWidth/videoHeight), HEIGHT,0.95f)
            arFragment.arSceneView.scene.addChild(anchorNode)
            videoNode.select()
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.pause()
    }
}