package org.ekatra.alfred.data

/**
 * Configuration for different LLM models
 */
data class ModelConfig(
    val id: String,
    val name: String,
    val description: String,
    val url: String,
    val filename: String,
    val sizeInMB: Long,
    val minRamGB: Int,
    val recommended: Boolean = false
)

object AvailableModels {
    val models = listOf(
        ModelConfig(
            id = "qwen2.5-0.5b-instruct",
            name = "Qwen 2.5 0.5B Instruct",
            description = "Instruction-tuned, lightweight; good for on-device tutoring with concise answers.",
            url = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf",
            filename = "qwen2.5-0.5b-instruct-q4_k_m.gguf",
            sizeInMB = 469,
            minRamGB = 2,
            recommended = true
        ),
        ModelConfig(
            id = "qwen-1.5b",
            name = "Qwen 2.5 1.5B",
            description = "Better quality responses, needs 3GB+ RAM. More detailed explanations.",
            url = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf",
            filename = "qwen-1.5b-q4.gguf",
            sizeInMB = 934,
            minRamGB = 3,
            recommended = false
        ),
        ModelConfig(
            id = "phi-3-mini",
            name = "Phi-3 Mini 3.8B",
            description = "High quality, needs 4GB+ RAM. Best for detailed learning and complex topics.",
            url = "https://huggingface.co/microsoft/Phi-3-mini-4k-instruct-gguf/resolve/main/Phi-3-mini-4k-instruct-q4.gguf",
            filename = "phi-3-mini-q4.gguf",
            sizeInMB = 2300,
            minRamGB = 4,
            recommended = false
        ),
        ModelConfig(
            id = "tinyllama",
            name = "TinyLlama 1.1B (Ultra Light)",
            description = "Ultra-compact, works on 1.5GB+ RAM. Basic responses, very fast.",
            url = "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf",
            filename = "tinyllama-1.1b-q4.gguf",
            sizeInMB = 669,
            minRamGB = 2,
            recommended = false
        )
    )
    
    fun getModelById(id: String): ModelConfig? {
        return models.find { it.id == id }
    }
    
    fun getDefaultModel(): ModelConfig {
        return models.first { it.recommended }
    }
    
    fun getModelByFilename(filename: String): ModelConfig? {
        return models.find { it.filename == filename }
    }
}
