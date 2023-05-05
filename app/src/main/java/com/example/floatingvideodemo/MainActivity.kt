package com.example.floatingvideodemo

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.ExternalTexture
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment

class MainActivity : AppCompatActivity() {
    var videoRenderable: ModelRenderable? = null
    val HEIGHT = 0.95f
    private val VIDEO_HEIGHT_METERS = 0.85f

    private lateinit var arFragment: ArFragment
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var anchorNode: AnchorNode
    private lateinit var videoTexture: ExternalTexture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaPlayer = MediaPlayer.create(this, R.raw.video)
        videoTexture = ExternalTexture()
        mediaPlayer.setSurface(videoTexture.surface)
        mediaPlayer.isLooping = true

        ModelRenderable.builder()
            .setSource(this, R.raw.video_screen)
            .build()
            .thenAccept { renderable ->
                videoRenderable = renderable
                videoRenderable?.material?.setExternalTexture("videoTexture", videoTexture)
                videoRenderable?.material
                    ?.setFloat4("keyColor", Color(0.01843f, 1.0f, 0.098f))
            }

        arFragment = supportFragmentManager.findFragmentById(R.id.fragment) as ArFragment

        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)
            val videoNode = Node()
            videoNode.setParent(anchorNode)

            val videoWidth = mediaPlayer.videoWidth.toFloat()
            val videoHeight = mediaPlayer.videoHeight.toFloat()

            videoNode.localScale = Vector3(
                VIDEO_HEIGHT_METERS * (videoWidth / videoHeight), VIDEO_HEIGHT_METERS, 1.0f
            )

            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()

                videoTexture.surfaceTexture
                    .setOnFrameAvailableListener { surfaceTexture ->
                        videoNode.renderable = videoRenderable
                        videoTexture.surfaceTexture.setOnFrameAvailableListener(null)
                    }
            } else {
                videoNode.renderable = videoRenderable
            }

            anchorNode.localScale = Vector3(HEIGHT * (videoWidth / videoHeight), HEIGHT, 0.95f)
            arFragment.arSceneView.scene.addChild(anchorNode)
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.pause()
    }

}