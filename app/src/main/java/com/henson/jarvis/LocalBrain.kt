package com.henson.jarvis
import com.google.ai.edge.litertlm.*
import java.io.File

class LocalBrain(private val cache: String) : AutoCloseable {
    private var engine: Engine? = null
    private var conversation: Conversation? = null
    fun load(path: String) {
        close()
        require(File(path).isFile) { "Model file missing" }
        val e = Engine(EngineConfig(modelPath = path, backend = Backend.CPU(), cacheDir = cache))
        engine = e
        try {
            e.initialize()
            conversation = e.createConversation(ConversationConfig(
                systemInstruction = Contents.of("You are JARVIS, a concise, practical personal assistant. Address the user as sir. Never claim to have performed actions you cannot perform. Ask permission before sensitive actions.")
            ))
        } catch (t: Throwable) { close(); throw t }
    }
    fun ask(prompt: String): String = (conversation ?: error("Load a model first")).sendMessage(prompt).toString()
    override fun close() {
        try { conversation?.close() } finally { conversation = null; engine?.close(); engine = null }
    }
}
