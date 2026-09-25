package com.cam.core.feed

/**
 * FrameSource — THE seam of the whole project.
 *
 * Runs 3–5 (hooks) consume this; Run 2 implements it. Defining it now in Run 0
 * means hook code can be written against a stable contract from day one.
 *
 * Implementations MUST be safe to call from binder/IPC threads.
 */
interface FrameSource {
    /** Begin producing frames for the given request. Idempotent. */
    fun start(request: FrameRequest)

    /** Stop producing frames. Releases decoder resources. Idempotent. */
    fun stop()

    /**
     * Latest produced frame, in the requested format.
     * Returns null before first frame is ready — callers must poll or register listener.
     */
    fun latestFrame(): ByteArray?

    /** Metadata accompanying [latestFrame]. Null symmetric with frame. */
    fun frameMeta(): FrameMeta?

    /** Push-mode listener for capture-session style consumers. */
    fun setListener(l: (ByteArray, FrameMeta) -> Unit)
}

data class FrameRequest(
    val width: Int,
    val height: Int,
    val fpsTarget: Int = 30,
    val format: Format = Format.NV21
) {
    enum class Format { NV21, YUV_420_888, JPEG }
}

data class FrameMeta(
    val timestampNanos: Long,   // monotonic, same clock as SystemClock.elapsedRealtimeNanos()
    val width: Int,
    val height: Int,
    val strideRow: Int,          // bytes per row (stride) for plane 0
    val frameIndex: Long         // monotonically increasing per start()
)
