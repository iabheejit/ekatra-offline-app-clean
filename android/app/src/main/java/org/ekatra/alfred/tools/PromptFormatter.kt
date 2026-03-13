package org.ekatra.alfred.tools

/**
 * PromptFormatter — formats chat prompts for different model families.
 *
 * Currently supports:
 *   • Qwen (ChatML: `<|im_start|>` / `<|im_end|>`)
 *   • Phi-3  (`<|user|>` / `<|assistant|>`)
 *   • Llama / TinyLlama (`[INST]` / `[/INST]`)
 *
 * The formatter is **stateless** — call [format] with the model id,
 * system prompt, conversation body, and user turn to get a complete
 * prompt string ready for the inference engine's [generateRaw].
 */
class PromptFormatter {

    /**
     * Build a fully-formatted prompt for the given model.
     *
     * @param modelId   one of the ids from [ModelConfig] (e.g. "qwen2.5-0.5b-instruct")
     * @param system    system prompt text (may be blank)
     * @param body      pre-assembled conversation body (history + current turn).
     *                  For the native-app path this is built in ChatViewModel;
     *                  for the server path it is built in AlfredServer.
     * @return a prompt string wrapped in the correct chat-ML template.
     */
    fun format(modelId: String, system: String, body: String): String {
        return when {
            modelId.startsWith("qwen")     -> formatQwen(system, body)
            modelId.startsWith("phi")      -> formatPhi(system, body)
            modelId.startsWith("tinyllama") -> formatLlama(system, body)
            else                           -> formatQwen(system, body) // safe default
        }
    }

    // ---- Qwen ChatML ----
    private fun formatQwen(system: String, body: String): String = buildString {
        append("<|im_start|>system\n")
        append(system)
        append("<|im_end|>\n")
        append("<|im_start|>user\n")
        append(body)
        append("<|im_end|>\n")
        append("<|im_start|>assistant\n")
    }

    // ---- Phi-3 ----
    private fun formatPhi(system: String, body: String): String = buildString {
        append("<|system|>\n")
        append(system)
        append("<|end|>\n")
        append("<|user|>\n")
        append(body)
        append("<|end|>\n")
        append("<|assistant|>\n")
    }

    // ---- Llama / TinyLlama ----
    private fun formatLlama(system: String, body: String): String = buildString {
        append("[INST] <<SYS>>\n")
        append(system)
        append("\n<</SYS>>\n\n")
        append(body)
        append(" [/INST]\n")
    }
}
