package com.example.floatingvideodemo

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Config
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.ExternalTexture
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment

class MainActivity : AppCompatActivity() {
    companion object {
        const val HEIGHT = 0.25f
    }

    private var videoRenderable: ModelRenderable? = null
    private lateinit var arFragment: ArFragment
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var anchorNode: AnchorNode
    private lateinit var videoTexture: ExternalTexture
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        arFragment = supportFragmentManager.findFragmentById(R.id.fragment) as ArFragment
        arFragment.planeDiscoveryController.hide()
        arFragment.planeDiscoveryController.setInstructionView(null)
        arFragment.arSceneView.planeRenderer.isEnabled = false

        arFragment.arSceneView.session?.config?.let { config ->
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            config.focusMode = Config.FocusMode.AUTO
        }

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

/*        arFragment.setOnTapArPlaneListener { hitResult: HitResult, _, _ ->
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
            videoNode.localScale = Vector3(
                HEIGHT * (videoWidth / videoHeight),
                HEIGHT,
                1.0f
            )

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
            arFragment.arSceneView.scene.addChild(anchorNode)
            videoNode.select()
        }*/

        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                val x = e.x
                val y = e.y

                Log.d("gestureDetector", "user taped")
                Toast.makeText(this@MainActivity, "tap", Toast.LENGTH_SHORT).show()

                val hitResult = arFragment.arSceneView.arFrame?.hitTest(x, y)?.firstOrNull()

      /*          if (hitResult != null) {
                    Toast.makeText(this@MainActivity, "tap", Toast.LENGTH_SHORT).show()

                    val anchor = hitResult.createAnchor()
                    anchorNode =
                        AnchorNode(anchor)
                    anchorNode.setParent(arFragment.arSceneView.scene)

                    val videoNode = TransformableNode(arFragment.transformationSystem)
                    videoNode.setParent(anchorNode)

                    val videoWidth = mediaPlayer.videoWidth.toFloat()
                    val videoHeight = mediaPlayer.videoHeight.toFloat()
                    videoNode.localScale = Vector3(
                        HEIGHT * (videoWidth / videoHeight),
                        HEIGHT,
                        1.0f
                    )

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
                    arFragment.arSceneView.scene.addChild(anchorNode)
                    videoNode.select()
                }*/
                return super.onSingleTapUp(e)
            }
        })
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.pause()
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d("touchEvent","user taped")
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }
}