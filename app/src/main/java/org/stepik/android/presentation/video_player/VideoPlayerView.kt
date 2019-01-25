package org.stepik.android.presentation.video_player

import org.stepik.android.view.video_player.model.VideoPlayerData

interface VideoPlayerView {
    fun setVideoPlayerData(videoPlayerData: VideoPlayerData)
    fun setIsRotateVideo(isRotateVideo: Boolean)
    fun showPlayInBackgroundPopup()
}