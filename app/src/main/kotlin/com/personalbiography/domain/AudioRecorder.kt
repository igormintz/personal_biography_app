package com.personalbiography.domain

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.util.UUID

/**
 * Thin wrapper over Android's [MediaRecorder]. Records to AAC inside an MP4
 * container (`.m4a`) — same format the existing Telegram-bot voice notes
 * decode to, and accepted by OpenAI's Whisper endpoint.
 *
 * Files are written to `context.cacheDir/audio/`. They survive a process
 * restart so an in-flight transcription can be resumed (future polish), but
 * they're not durable user storage and Android can evict them under
 * pressure. Successful entries persist their `audioPath` in Room; if the
 * cache is cleared the path becomes a stale pointer and the UI just
 * doesn't offer playback.
 */
class AudioRecorder(
    private val appContext: Context,
) {
    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null
    private var startedAtNanos: Long = 0L

    /** True iff [start] has been called and we haven't yet [stop]ped/[cancel]led. */
    val isRecording: Boolean get() = recorder != null

    /** Best-effort duration so far, in seconds. Zero when not recording. */
    fun elapsedSeconds(): Double {
        if (!isRecording) return 0.0
        return (System.nanoTime() - startedAtNanos) / 1_000_000_000.0
    }

    fun start(): File {
        check(recorder == null) { "AudioRecorder already started" }
        val dir = File(appContext.cacheDir, "audio").apply { mkdirs() }
        val file = File(dir, "rec_${UUID.randomUUID()}.m4a")

        @Suppress("DEPRECATION")
        val r =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(appContext)
            } else {
                MediaRecorder()
            }

        try {
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioChannels(1)
            r.setAudioSamplingRate(SAMPLE_RATE_HZ)
            r.setAudioEncodingBitRate(BIT_RATE_BPS)
            r.setOutputFile(file.absolutePath)
            r.prepare()
            r.start()
        } catch (e: Throwable) {
            try {
                r.release()
            } catch (_: Throwable) {
                // already released
            }
            file.delete()
            throw e
        }

        recorder = r
        currentFile = file
        startedAtNanos = System.nanoTime()
        return file
    }

    /**
     * Stop recording. Returns the recorded file + its (wall-clock) duration
     * in seconds. The caller is responsible for deleting the file once the
     * pipeline is done — typically the entry persists the path and we keep
     * the audio for playback.
     */
    fun stop(): Result {
        val r = recorder ?: error("AudioRecorder not started")
        val file = currentFile ?: error("AudioRecorder has no active file")
        val elapsed = elapsedSeconds()
        try {
            r.stop()
        } catch (e: RuntimeException) {
            // MediaRecorder.stop() throws if no audio reached the encoder
            // (e.g. tap-tap on the mic button). Treat as cancel-like — drop
            // the file and surface the empty result so the caller can fall
            // back to a friendly error.
            try {
                r.release()
            } catch (_: Throwable) {
            }
            file.delete()
            recorder = null
            currentFile = null
            startedAtNanos = 0L
            throw e
        }
        r.release()
        recorder = null
        currentFile = null
        startedAtNanos = 0L
        return Result(file = file, durationSec = elapsed)
    }

    fun cancel() {
        val r = recorder ?: return
        val file = currentFile
        try {
            r.stop()
        } catch (_: RuntimeException) {
            // ignore — we're cancelling
        }
        try {
            r.release()
        } catch (_: Throwable) {
        }
        file?.delete()
        recorder = null
        currentFile = null
        startedAtNanos = 0L
    }

    data class Result(val file: File, val durationSec: Double)

    companion object {
        private const val SAMPLE_RATE_HZ = 44_100
        private const val BIT_RATE_BPS = 96_000
    }
}
