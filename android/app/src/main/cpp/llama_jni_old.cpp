/**
 * llama_jni.cpp - JNI bridge for llama.cpp
 * 
 * Real implementation using llama.cpp library for on-device inference.
 */

#include <jni.h>
#include <android/log.h>
#include <string>
#include <vector>
#include <mutex>
#include <thread>

#include "llama.h"
#include "common.h"

#define LOG_TAG "LlamaJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Global State
static llama_model* g_model = nullptr;
static llama_context* g_ctx = nullptr;
static llama_sampler* g_sampler = nullptr;
static std::mutex g_mutex;

static int g_n_threads = 4;
static int g_n_ctx = 2048;
static int g_n_past = 0;
static bool g_is_generating = false;

// Helper: Convert token to string
static std::string token_to_piece(const llama_model* model, llama_token token) {
    std::vector<char> buf(256);
    int n = llama_token_to_piece(model, token, buf.data(), buf.size(), 0, true);
    if (n < 0) {
        buf.resize(-n);
        n = llama_token_to_piece(model, token, buf.data(), buf.size(), 0, true);
    }
    return std::string(buf.data(), n);
}

// Helper: Tokenize text
static std::vector<llama_token> tokenize(const llama_model* model, const std::string& text, bool add_bos) {
    int n_tokens = text.length() + (add_bos ? 1 : 0) + 16;
    std::vector<llama_token> tokens(n_tokens);
    n_tokens = llama_tokenize(model, text.c_str(), text.length(), tokens.data(), tokens.size(), add_bos, true);
    if (n_tokens < 0) {
        tokens.resize(-n_tokens);
        n_tokens = llama_tokenize(model, text.c_str(), text.length(), tokens.data(), tokens.size(), add_bos, true);
    }
    tokens.resize(n_tokens);
    return tokens;
}

extern "C" {

JNIEXPORT jboolean JNICALL
Java_org_ekatra_alfred_LlamaEngine_nativeLoadModel(
    JNIEnv* env,
    jobject,
    jstring path,
    jint threads,
    jint contextSize
) {
    std::lock_guard<std::mutex> lock(g_mutex);
    
    // Cleanup existing
    if (g_sampler) { llama_sampler_free(g_sampler); g_sampler = nullptr; }
    if (g_ctx) { llama_free(g_ctx); g_ctx = nullptr; }
    if (g_model) { llama_free_model(g_model); g_model = nullptr; }
    
    const char* pathStr = env->GetStringUTFChars(path, nullptr);
    LOGI("Loading model: %s", pathStr);
    
    g_n_threads = threads > 0 ? threads : 4;
    g_n_ctx = contextSize > 0 ? contextSize : 2048;
    
    llama_backend_init();
    
    // Load model
    llama_model_params model_params = llama_model_default_params();
    g_model = llama_load_model_from_file(pathStr, model_params);
    env->ReleaseStringUTFChars(path, pathStr);
    
    if (!g_model) {
        LOGE("Failed to load model");
        return JNI_FALSE;
    }
    
    // Create context
    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = g_n_ctx;
    ctx_params.n_threads = g_n_threads;
    ctx_params.n_threads_batch = g_n_threads;
    ctx_params.n_batch = 512;
    
    g_ctx = llama_new_context_with_model(g_model, ctx_params);
    if (!g_ctx) {
        LOGE("Failed to create context");
        llama_free_model(g_model);
        g_model = nullptr;
        return JNI_FALSE;
    }
    
    // Create sampler
    llama_sampler_chain_params sparams = llama_sampler_chain_default_params();
    g_sampler = llama_sampler_chain_init(sparams);
    llama_sampler_chain_add(g_sampler, llama_sampler_init_temp(0.7f));
    llama_sampler_chain_add(g_sampler, llama_sampler_init_top_k(40));
    llama_sampler_chain_add(g_sampler, llama_sampler_init_top_p(0.9f, 1));
    llama_sampler_chain_add(g_sampler, llama_sampler_init_dist(LLAMA_DEFAULT_SEED));
    
    g_n_past = 0;
    LOGI("Model loaded! Vocab: %d, Ctx: %d", llama_n_vocab(g_model), llama_n_ctx(g_ctx));
    
    return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL
Java_org_ekatra_alfred_LlamaEngine_nativePrepare(
    JNIEnv* env,
    jobject,
    jstring prompt
) {
    std::lock_guard<std::mutex> lock(g_mutex);
    
    if (!g_model || !g_ctx) {
        LOGE("Model not loaded");
        return JNI_FALSE;
    }
    
    const char* promptStr = env->GetStringUTFChars(prompt, nullptr);
    std::string prompt_text(promptStr);
    env->ReleaseStringUTFChars(prompt, promptStr);
    
    LOGI("Preparing prompt (%zu chars)", prompt_text.length());
    
    llama_kv_cache_clear(g_ctx);
    g_n_past = 0;
    
    std::vector<llama_token> tokens = tokenize(g_model, prompt_text, true);
    LOGI("Tokenized: %zu tokens", tokens.size());
    
    if (tokens.empty()) return JNI_FALSE;
    if ((int)tokens.size() > g_n_ctx - 4) tokens.resize(g_n_ctx - 4);
    
    // Evaluate prompt
    const int batch_size = 512;
    for (size_t i = 0; i < tokens.size(); i += batch_size) {
        int n_eval = std::min((int)(tokens.size() - i), batch_size);
        llama_batch batch = llama_batch_init(n_eval, 0, 1);
        
        for (int j = 0; j < n_eval; j++) {
            llama_batch_add(batch, tokens[i + j], g_n_past + j, {0}, j == n_eval - 1);
        }
        
        if (llama_decode(g_ctx, batch) != 0) {
            llama_batch_free(batch);
            return JNI_FALSE;
        }
        
        g_n_past += n_eval;
        llama_batch_free(batch);
    }
    
    g_is_generating = true;
    LOGI("Ready to generate (n_past=%d)", g_n_past);
    return JNI_TRUE;
}

JNIEXPORT jstring JNICALL
Java_org_ekatra_alfred_LlamaEngine_nativeGenerateToken(
    JNIEnv* env,
    jobject
) {
    std::lock_guard<std::mutex> lock(g_mutex);
    
    if (!g_model || !g_ctx || !g_sampler || !g_is_generating) return nullptr;
    if (g_n_past >= g_n_ctx - 1) { g_is_generating = false; return nullptr; }
    
    llama_token new_token = llama_sampler_sample(g_sampler, g_ctx, -1);
    
    if (llama_token_is_eog(g_model, new_token)) {
        g_is_generating = false;
        return nullptr;
    }
    
    std::string piece = token_to_piece(g_model, new_token);
    
    llama_batch batch = llama_batch_init(1, 0, 1);
    llama_batch_add(batch, new_token, g_n_past, {0}, true);
    
    if (llama_decode(g_ctx, batch) != 0) {
        llama_batch_free(batch);
        g_is_generating = false;
        return nullptr;
    }
    
    g_n_past++;
    llama_batch_free(batch);
    llama_sampler_accept(g_sampler, new_token);
    
    return env->NewStringUTF(piece.c_str());
}

JNIEXPORT void JNICALL
Java_org_ekatra_alfred_LlamaEngine_nativeUnload(
    JNIEnv*,
    jobject
) {
    std::lock_guard<std::mutex> lock(g_mutex);
    
    g_is_generating = false;
    if (g_sampler) { llama_sampler_free(g_sampler); g_sampler = nullptr; }
    if (g_ctx) { llama_free(g_ctx); g_ctx = nullptr; }
    if (g_model) { llama_free_model(g_model); g_model = nullptr; }
    llama_backend_free();
    g_n_past = 0;
    
    LOGI("Model unloaded");
}

} // extern "C"
