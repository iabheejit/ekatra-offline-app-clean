# Ekatra Alfred - Grassroots Edition Plan

> **Mission**: An AI tutor that works 100% offline on basic ₹6,000 phones for rural students.

---

## 📱 Target Device Specs

| Spec | Minimum | Recommended |
|------|---------|-------------|
| RAM | 3 GB | 4 GB |
| Storage | 32 GB (550 MB free) | 64 GB |
| Android | 8.0+ | 10+ |
| CPU | ARM64 (any) | Snapdragon 665+ |
| Example Phones | Redmi 9A, Realme C11 | Moto G73, Redmi Note 11 |

---

## 🎯 Core Features (Grassroots Focus)

### 1. ✅ Works Offline Forever
- Download once at cyber cafe (550 MB total)
- Never needs internet again
- Model included in app

### 2. ✅ Simple Chat Interface
- Ask any question
- Get answer in 10-15 seconds
- 🆕 Start fresh conversation anytime

### 3. 📚 Subject Quick-Starts (NEW)
Pre-loaded example questions by subject:
```
┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐
│ Science │ │  Math   │ │ English │ │ Social  │
│   🔬    │ │   📐    │ │   📝    │ │   🌍    │
└─────────┘ └─────────┘ └─────────┘ └─────────┘
```

### 4. 💾 Save & Share Answers (NEW)
- **Save button**: Store important Q&A pairs locally
- **Share button**: Copy to WhatsApp/SMS
- **Saved Library**: Browse all saved answers
- **Charts/Diagrams**: Save as images

### 5. 📡 Hotspot Mode (Existing)
- Teacher's phone serves 10+ students
- No internet needed, just WiFi hotspot

---

## 🧠 Maximum Context Size

### Current vs Proposed

| Setting | Current | Proposed | Impact |
|---------|---------|----------|--------|
| Context Size | 512 tokens | **2048 tokens** | 4x longer conversations |
| Max Response | 150 tokens | **256 tokens** | Fuller answers |
| RAM Usage | ~600 MB | ~900 MB | Still works on 3GB |
| Conversations | ~3-4 exchanges | **~12-15 exchanges** | Much better! |

### Why 2048 is the Sweet Spot
- Qwen 0.5B supports up to 32K context
- But RAM increases with context size
- 2048 tokens = good balance for 3GB phones
- Can have meaningful multi-turn conversations

### Context Configuration
```kotlin
// LlamaEngine.kt
private val contextSize = 2048  // Up from 512
private val maxTokens = 256     // Up from 150

// System prompt optimized for education
private val systemPrompt = """
You are Alfred, a helpful tutor for Indian students.
- Give clear, simple explanations
- Use examples from daily life
- Keep answers concise but complete
- For math: show step-by-step solutions
- For science: explain concepts simply
"""
```

---

## 💾 SQLite Storage Schema

### Tables

```sql
-- Saved Q&A pairs
CREATE TABLE saved_answers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    subject TEXT,           -- 'science', 'math', 'english', 'social', 'other'
    created_at INTEGER,     -- Unix timestamp
    is_favorite INTEGER DEFAULT 0
);

-- Saved charts/diagrams (as base64 images)
CREATE TABLE saved_charts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    image_data BLOB,        -- PNG/JPEG bytes
    related_answer_id INTEGER,
    created_at INTEGER,
    FOREIGN KEY (related_answer_id) REFERENCES saved_answers(id)
);

-- App settings
CREATE TABLE settings (
    key TEXT PRIMARY KEY,
    value TEXT
);

-- Usage stats (for optional anonymous analytics)
CREATE TABLE usage_stats (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    date TEXT,              -- 'YYYY-MM-DD'
    questions_asked INTEGER DEFAULT 0,
    answers_saved INTEGER DEFAULT 0,
    subjects_used TEXT      -- JSON: {"science": 5, "math": 3}
);
```

### Storage Estimates
| Data | Size per item | 1000 items |
|------|---------------|------------|
| Q&A pair | ~2 KB | ~2 MB |
| Chart image | ~50 KB | ~50 MB |
| **Total after 1 year** | - | **~60 MB** |

Very lightweight! No issues even on 32GB phones.

---

## 📱 UI/UX Design

### Main Chat Screen
```
┌────────────────────────────────────┐
│ 🤖 Alfred          [🆕] [📡] [⚙️] │  ← Header with actions
├────────────────────────────────────┤
│                                    │
│  ┌──────────────────────────────┐  │
│  │ Hello! I'm Alfred, your      │  │
│  │ study helper. Ask me anything│  │
│  └──────────────────────────────┘  │
│                                    │
│  ┌────────────────────────────┐    │
│  │ What is photosynthesis?    │ 👤 │
│  └────────────────────────────┘    │
│                                    │
│  ┌──────────────────────────────┐  │
│  │ Photosynthesis is how plants│  │
│  │ make food using sunlight... │  │
│  │                              │  │
│  │ [💾 Save] [📋 Copy] [📤 Share]│ │  ← Action buttons!
│  └──────────────────────────────┘  │
│                                    │
├────────────────────────────────────┤
│ Quick Topics:                      │
│ [🔬 Science] [📐 Math] [📝 English]│
├────────────────────────────────────┤
│ ┌────────────────────────────┐ [➤] │
│ │ Type your question...      │     │
│ └────────────────────────────┘     │
└────────────────────────────────────┘
```

### Saved Library Screen
```
┌────────────────────────────────────┐
│ ← Saved Answers        [🔍 Search] │
├────────────────────────────────────┤
│ Filter: [All ▼] [⭐ Favorites]     │
├────────────────────────────────────┤
│ ┌──────────────────────────────┐  │
│ │ 🔬 What is photosynthesis?   │  │
│ │ Plants make food using...    │  │
│ │ 📅 Today  [⭐] [🗑️]          │  │
│ └──────────────────────────────┘  │
│                                    │
│ ┌──────────────────────────────┐  │
│ │ 📐 How to solve quadratic?   │  │
│ │ Use the formula x = ...      │  │
│ │ 📅 Yesterday  [⭐] [🗑️]      │  │
│ └──────────────────────────────┘  │
│                                    │
│ ┌──────────────────────────────┐  │
│ │ 📷 Photosynthesis Diagram    │  │
│ │ [Image Preview]              │  │
│ │ 📅 2 days ago  [⭐] [🗑️]     │  │
│ └──────────────────────────────┘  │
└────────────────────────────────────┘
```

---

## 📂 File Structure (Additions)

```
app/src/main/java/org/ekatra/alfred/
├── data/
│   ├── local/
│   │   ├── EkatraDatabase.kt      # Room database setup
│   │   ├── SavedAnswerDao.kt      # Q&A CRUD operations
│   │   ├── SavedChartDao.kt       # Charts CRUD
│   │   └── SettingsDao.kt         # App settings
│   │
│   ├── model/
│   │   ├── SavedAnswer.kt         # Data class
│   │   ├── SavedChart.kt          # Data class
│   │   └── Subject.kt             # Enum
│   │
│   └── repository/
│       └── SavedContentRepository.kt
│
├── ui/
│   ├── screens/
│   │   ├── ChatScreen.kt          # Enhanced with save/share
│   │   ├── SavedLibraryScreen.kt  # NEW: Browse saved items
│   │   └── SettingsScreen.kt      # Enhanced
│   │
│   └── components/
│       ├── MessageBubble.kt       # With action buttons
│       ├── SubjectChips.kt        # Quick topic buttons
│       ├── SavedAnswerCard.kt     # Library item
│       └── ShareSheet.kt          # Share options
│
└── util/
    └── ShareUtils.kt              # Copy/share helpers
```

---

## 🔧 Implementation Tasks

### Phase 1: Maximum Context (30 min)
- [ ] Update `contextSize` to 2048 in LlamaEngine.kt
- [ ] Update `maxTokens` to 256
- [ ] Optimize system prompt for education
- [ ] Test on device (verify RAM usage)

### Phase 2: SQLite Storage (1-2 hours)
- [ ] Add Room dependency to build.gradle
- [ ] Create database entities (SavedAnswer, SavedChart)
- [ ] Create DAOs
- [ ] Create repository

### Phase 3: Save/Share UI (1-2 hours)
- [ ] Add action buttons to MessageBubble
- [ ] Implement save functionality
- [ ] Implement copy to clipboard
- [ ] Implement share intent
- [ ] Create SavedLibraryScreen

### Phase 4: Subject Quick-Starts (30 min)
- [ ] Create SubjectChips component
- [ ] Add pre-loaded questions per subject
- [ ] Wire up to chat input

### Phase 5: Polish (30 min)
- [ ] Add success toasts/feedback
- [ ] Handle edge cases
- [ ] Test full flow

---

## 📊 Success Metrics

| Metric | Target |
|--------|--------|
| App size (installed) | < 600 MB |
| RAM usage (active) | < 1 GB |
| Works on 3GB phone | ✅ Yes |
| Time to first answer | < 15 seconds |
| Conversation length | 10+ exchanges |
| Storage after 1 year | < 100 MB |

---

## 🚀 Stretch Goals (Future)

1. **Voice Input** - Ask questions by speaking (uses Android STT)
2. **Hindi Support** - Model understands Hindi/Hinglish
3. **Textbook OCR** - Camera captures textbook → asks questions
4. **Quiz Mode** - Alfred asks questions, student answers
5. **Progress Tracking** - Simple stats on subjects studied
6. **Smaller Model Option** - 300MB model for very basic phones

---

## 📝 Notes

- **No internet required after install** - This is the key value
- **Keep it simple** - Rural students shouldn't need tutorials
- **Fast is better than perfect** - 10 second answers beat 30 second "better" answers
- **WhatsApp sharing is king** - Most rural users share via WhatsApp
- **Battery matters** - Optimize for low power consumption

---

## Ready to Implement?

Start with Phase 1 (Maximum Context) → Immediate improvement for demo!
