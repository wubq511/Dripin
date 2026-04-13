# Dripin MVP Design

## 1. Goal

Build a pure local-first Android MVP for collecting content shared from other apps, saving it with lightweight metadata and tags, browsing it later, and receiving a daily local recommendation notification. The app does not use any server, account system, large model, or built-in reader.

## 2. Product Scope

### In Scope

- Receive shared links, text, and images from Android system share sheet.
- Normalize incoming data into a single local content model.
- Open a save screen before persistence so users can edit key fields.
- Auto-fetch link titles over the network when missing.
- Save all content locally with Room.
- Apply lightweight rule-based classification and tag generation.
- Show a home list with basic filters.
- Show a detail screen with complete metadata and edit actions.
- Generate and store a daily recommendation batch with WorkManager.
- Send a daily local notification that opens a recommendation page.
- Persist notification and recommendation preferences locally.

### Out of Scope

- Any server sync, account, login, or cloud backup.
- Any LLM-based summarization, classification, or recommendation.
- In-app reading mode, offline article archive, full-page capture, OCR, or PDF reader.
- Complex image processing.
- Tablet-first or landscape-first layouts.
- Full search, bulk actions, and import/export for MVP.

## 3. Product Principles

- Local first: all user content and settings remain on device.
- Friction low on capture: receiving a share should reach an editable save screen quickly.
- Data model unified: link, text, and image items should share one storage and UI flow where possible.
- UI coherent: the app should feel visually consistent across home, capture, detail, recommendation, and settings screens.
- Visual hierarchy differentiated, not disconnected: the home screen stays restrained and utilitarian, while the daily recommendation page can feel more curated and atmospheric without breaking the same visual system.
- Extension-friendly structure: the MVP architecture should leave clear seams for search, backup, smarter rules, and future sync.

## 4. Reference Notes from Karakeep

Karakeep is used as a product and modeling reference, not as an architectural base. The following ideas are worth borrowing:

- A unified bookmark/content concept that can represent links, text, and assets.
- A lightweight mobile share entry flow that quickly normalizes incoming content.
- Duplicate-aware save semantics, where the system can tell the user that an item already exists instead of silently storing a second copy.
- Separate tag management from the core item payload so automatic and manual tags can coexist.

The following parts are intentionally not reused:

- Server-driven APIs, auth, sync, crawling, AI tagging, summaries, archives, reader features, and web-oriented complexity.
- React Native / Expo application structure.

## 5. Recommended Technical Stack

- Language: Kotlin
- UI: Jetpack Compose
- Design system: Material 3
- Local DB: Room
- Settings storage: DataStore
- Background scheduling: WorkManager
- Image loading: Coil
- Dependency injection: Hilt
- Navigation: Navigation Compose
- Networking for link metadata: OkHttp + Jsoup

Reasoning:

- Room fits relational local storage, duplicate checks, and persisted daily recommendation batches.
- DataStore is a better fit than Room for a small set of global preferences.
- WorkManager is the correct Android-native choice for resilient daily scheduling.
- Hilt keeps the app modular enough to extend without adding excessive manual wiring.

## 6. High-Level Architecture

Use a single-activity Compose app with clear module boundaries:

- `app`
  - Application class, Hilt setup, navigation host, theme, notification channel, app-level wiring.
- `core/model`
  - Domain enums, UI-independent model types, filter and preference types.
- `core/common`
  - Shared utilities such as date formatting, URL normalization, package-name mapping, and result wrappers.
- `core/designsystem`
  - Theme, typography, spacing, reusable chips/cards/buttons, and shared motion tokens.
- `data/local`
  - Room entities, DAOs, database, relation models.
- `data/repository`
  - Repository implementations that coordinate DAOs, metadata fetchers, classifiers, and recommendation generation.
- `feature/capture`
  - Share intake parsing, save screen, editable tags, duplicate handling.
- `feature/home`
  - Saved content list, filters, and item cards.
- `feature/detail`
  - Full item detail view and edit actions.
- `feature/recommendation`
  - Today page, recommendation card stack, read/later/open actions.
- `feature/settings`
  - Notification time, daily count, repeat behavior, and ranking preference UI.
- `worker`
  - Daily recommendation worker, scheduling helper, and notification publisher.

This is intentionally lighter than a full enterprise clean architecture split, but each feature still has a clear dependency direction:

- UI -> ViewModel -> Repository -> DAO / local service / metadata fetcher.

## 7. Primary User Flows

### 7.1 Share Capture Flow

1. User shares content from any compatible Android app.
2. `ShareReceiverActivity` receives the share intent and translates it into a normalized `IncomingSharePayload`.
3. The app opens the Compose save screen immediately.
4. The save screen pre-fills editable fields from the payload.
5. If the item is a link and has no usable title, metadata fetch starts in the background.
6. Automatic source/platform/domain/tag suggestions are derived locally.
7. User edits fields and taps save.
8. Repository normalizes data, performs duplicate handling, writes Room records, and returns either:
   - newly created item
   - duplicate matched and existing item updated
9. App navigates to the detail screen or back to the list with success feedback.

### 7.2 Daily Recommendation Flow

1. App startup and settings changes ensure WorkManager scheduling stays aligned with the chosen daily time.
2. At the configured time, the worker loads preferences and checks whether today already has a recommendation batch.
3. Eligible unread content is selected from Room according to preference rules.
4. The chosen batch is stored in dedicated recommendation tables.
5. Related item push metadata is updated.
6. A local notification is shown.
7. Tapping the notification opens the Today page displaying the persisted batch.

### 7.3 Browsing Flow

1. User lands on Home.
2. User filters by content type, read state, or push state.
3. User opens detail view for any item.
4. User can mark read/unread, edit title/note/tags, and open source content.
5. User can open Today page to consume the current daily batch.

## 8. Data Model

### 8.1 Saved Item

Persist one main row per saved item:

- `id: Long`
- `contentType: LINK | TEXT | IMAGE`
- `title: String?`
- `rawUrl: String?`
- `canonicalUrl: String?`
- `textContent: String?`
- `imageUri: String?`
- `sourceAppPackage: String?`
- `sourceAppLabel: String?`
- `sourcePlatform: String?`
- `sourceDomain: String?`
- `topicCategory: String?`
- `note: String?`
- `createdAt: Instant`
- `updatedAt: Instant`
- `isRead: Boolean`
- `readAt: Instant?`
- `pushCount: Int`
- `lastPushedAt: Instant?`
- `lastRecommendedDate: LocalDate?`

Field intent:

- `rawUrl` preserves the original incoming value.
- `canonicalUrl` supports duplicate detection.
- `sourcePlatform` stores normalized labels like `微信`, `X`, `GitHub`, `B站`, `抖音`.
- `topicCategory` is the lightweight rule-based thematic category inferred from title/domain, such as `开发`, `文章`, `视频`.

### 8.2 Tag Tables

`tags`

- `id: Long`
- `name: String`
- `normalizedName: String`
- `tagType: DOMAIN | PLATFORM | TOPIC | MANUAL`

`item_tags`

- `itemId: Long`
- `tagId: Long`

This design supports:

- automatic tag generation without duplicating strings per item
- manual tag editing
- future tag browse/filter screens without schema changes

### 8.3 Daily Recommendation Tables

`daily_recommendations`

- `id: Long`
- `recommendationDate: LocalDate`
- `createdAt: Instant`
- `notificationSent: Boolean`

`daily_recommendation_items`

- `recommendationId: Long`
- `itemId: Long`
- `rank: Int`

This makes a recommendation batch persistent and stable for the whole day.

## 9. Duplicate Handling

Duplicate protection applies only to link items in MVP.

### 9.1 Canonicalization Rules

Before saving a link:

- lowercase scheme and host
- remove URL fragment
- remove common tracking parameters such as `utm_*`, `spm`, and `si`
- keep path and meaningful query parameters intact
- normalize trailing slash only when safe

### 9.2 Persistence Rule

- `canonicalUrl` gets a unique index in Room.
- If insertion collides, the repository resolves the existing item and returns a duplicate result.

### 9.3 UX Rule

When a duplicate link is detected:

- show a non-blocking duplicate message on the save screen
- switch primary action from “Save” to “Update existing”
- allow users to update title, note, and tags on the existing row
- do not create a second row for the same canonical link

This is closer to Karakeep’s `alreadyExists` behavior than silent duplication, but tailored for an editable local save workflow.

## 10. Incoming Share Parsing

Normalize Android share intents into:

- `contentType`
- `sharedText`
- `sharedUrl`
- `sharedImageUri`
- `sourceAppPackage`
- `sourceAppLabel`

Rules:

- `ACTION_SEND`
  - `text/plain`: detect whether the payload is mostly a URL or plain text
  - image MIME types: treat as image content with URI
- `ACTION_SEND_MULTIPLE`
  - MVP supports only the first image item; multiple-item support is deferred
- prefer URL extraction from `EXTRA_TEXT` if both plain text and URL-like content exist

The parser should be isolated in `feature/capture` or `core/common` so new share shapes can be added later.

## 11. Metadata Fetching for Links

If a link arrives without a good title, the app should attempt metadata fetching over the network.

### 11.1 Behavior

- Start automatically when the save screen loads.
- Do not block editing or saving.
- Update the title field only if the user has not manually overridden it.

### 11.2 Fetch Order

1. Open Graph title (`og:title`)
2. HTML `<title>`
3. Fallback to domain or last path segment

### 11.3 Failure Handling

- Network failure, unsupported pages, redirects, or parsing errors should not interrupt the save flow.
- Metadata errors stay silent unless debugging is enabled.

## 12. Rule-Based Classification and Tagging

No AI is used in MVP. The app performs lightweight local inference.

### 12.1 Source Platform Inference

Priority:

1. Map source app package to known labels
2. Fallback to URL domain mapping
3. Fallback to source app label or domain host

Examples:

- `com.tencent.mm` -> `微信`
- `com.github.android` -> `GitHub`
- `tv.danmaku.bili` -> `B站`
- `com.ss.android.ugc.aweme` -> `抖音`

### 12.2 Domain Tag Inference

Extract main domain and create a domain tag:

- `github.com` -> `github`
- `mp.weixin.qq.com` -> `weixin`
- `x.com` -> `x`

### 12.3 Topic Category Inference

Infer a simple thematic category from title/domain:

- contains `repo`, `release`, `issue`, `github` -> `开发`
- contains `video`, `bilibili`, `youtube` -> `视频`
- contains `blog`, `article`, `newsletter`, `medium` -> `文章`

This rule engine should live behind a dedicated classifier interface so a future smarter classifier can replace it without changing the rest of the app.

### 12.4 Tag Editing Rule

Automatic tags should appear on the save screen as editable chips:

- removable individually
- manual tags addable from the same UI
- tag persistence happens in the join table, not as serialized JSON

## 13. Screen Design

### 13.1 Visual System

The app should feel cohesive and calm:

- use a restrained Material 3-based palette with one distinctive accent family
- keep typography and spacing consistent across screens
- avoid novelty-driven decoration that makes one page feel like a different app
- use motion sparingly for confirmations, recommendation-card entrance, and inline state changes
- maintain a premium but practical tone

The home screen should remain clean and legible. The recommendation page can feel more curated and atmospheric through layout, card emphasis, and subtle motion, but it must still clearly belong to the same app.

### 13.2 Home Screen

Purpose:

- primary overview of saved items
- quick filtering
- entry point to detail, settings, and today page

Content:

- top app bar
- compact filter row
- saved-item list
- empty state when no data exists

Each list item should show:

- title
- content type indicator
- source platform or domain
- save time
- note preview if present

Filters:

- content type
- pushed / not pushed
- read / unread

### 13.3 Save Screen

This is the core capture UX and should not feel like a raw admin form.

Sections:

- content preview header
- editable metadata fields
- automatic and manual tag chips
- duplicate notice when applicable
- fixed save/update action area

Editable fields:

- title
- link
- text content / summary
- source app
- source domain
- save time (read-only display for MVP)
- note
- content type (read-only after detection for MVP)

### 13.4 Detail Screen

Show complete item information and lightweight edit controls:

- title
- type
- URL if present
- full text if present
- image preview if present
- source app/platform/domain
- saved time
- note
- tags
- push history summary

Actions:

- mark read/unread
- open external link
- edit note/title/tags

### 13.5 Today Recommendation Screen

This screen can be slightly more expressive than Home while staying within the same design system.

Structure:

- date header
- small explanatory subtitle
- vertically stacked recommendation cards

Card behavior by type:

- link card
  - title
  - source
  - summary/note snippet
  - open link action
- text card
  - prominent text preview
  - expand/collapse body
- image card
  - image preview
  - title and note

Actions:

- mark read
- keep for later
- open source link if applicable

### 13.6 Settings Screen

Settings should be simple and local:

- notification enabled
- daily push time
- daily push count
- whether unread pushed items can repeat
- recommendation ranking mode

## 14. Home Filtering Model

Represent filter state as a small immutable UI model:

- `contentType: All | Link | Text | Image`
- `pushState: All | Pushed | Unpushed`
- `readState: All | Read | Unread`

Room should expose efficient queries for these combinations. For MVP, multiple DAO query variants are acceptable; a more advanced query builder is unnecessary.

## 15. Recommendation Rules

### 15.1 Preferences

Store in DataStore:

- `notificationsEnabled = true`
- `dailyPushTime = 21:00`
- `dailyPushCount = 3`
- `repeatPushedUnreadItems = true`
- `recommendationSortMode = OLDEST_SAVED_FIRST`

Other supported sort modes in MVP:

- `NEWEST_SAVED_FIRST`
- `LEAST_PUSHED_FIRST`

### 15.2 Candidate Eligibility

An item is eligible when:

- it is unread
- it contains at least one usable primary payload field
- if `repeatPushedUnreadItems` is false, `pushCount` must be `0`

### 15.3 Batch Selection

Selection algorithm:

1. read today’s settings
2. skip if notifications are disabled
3. skip if today already has a batch
4. load eligible items
5. sort by selected strategy
6. take the first `dailyPushCount` items
7. persist recommendation batch and rows
8. update selected items’ push metadata
9. publish local notification

This is intentionally simple and deterministic for MVP.

## 16. WorkManager Scheduling

### 16.1 Worker Responsibilities

`DailyRecommendationWorker` should:

- load settings
- determine whether a recommendation batch already exists for today
- generate a new batch when needed
- create and send the notification

### 16.2 Scheduling Rule

When app starts and whenever relevant settings change:

- cancel existing periodic work if needed
- schedule the next daily run for the configured local time

Implementation note:

- if exact daily timing proves unreliable under WorkManager alone, use a one-time rescheduling pattern rather than pretending exact alarm semantics are guaranteed
- MVP still stays on WorkManager as requested

## 17. Notification Behavior

Create one notification channel, for example:

- channel id: `daily_recommendation`

Notification payload:

- title reflects item count
- body encourages opening today’s list
- tap action deep-links into the Today screen

If today has no eligible content, the worker should skip notifying rather than sending an empty recommendation.

## 18. Persistence Strategy

- Room stores item records, tags, relationships, and recommendation batches.
- DataStore stores user preferences.
- Shared image content stores only URI/path references for MVP.

This separation keeps transactional content distinct from lightweight app settings.

## 19. Error Handling

### Capture and Save

- malformed share payload -> show friendly invalid-content state
- metadata fetch failure -> continue silently
- duplicate link -> switch to update-existing mode
- DB write failure -> show retryable error feedback

### Recommendation

- no eligible items -> no notification, no crash
- worker failure -> retry according to WorkManager backoff
- corrupted settings -> fall back to safe defaults

### External Actions

- missing browser/image permission or invalid URI -> show non-blocking error feedback

## 20. Testing Strategy

### Unit Tests

- URL normalization and canonicalization
- source platform inference
- topic classification
- recommendation selection rules
- duplicate handling logic
- save-screen state reducer / ViewModel state transitions

### Database Tests

- Room insertion and unique index behavior
- item-tag relation queries
- recommendation batch persistence
- filter queries

### Worker Tests

- recommendation generation with notifications enabled
- skip behavior when already generated today
- skip behavior when notifications disabled

### UI Tests

- share payload opens save screen with prefilled fields
- home filters update visible content
- tapping notification deep-links into Today page

## 21. MVP Delivery Boundaries

The first working MVP is complete when:

- a user can share link, text, or image into the app
- the app opens a save screen with editable metadata
- missing link titles auto-fetch when possible
- saved content persists locally and duplicate links are handled
- automatic platform/domain/topic tags are generated and editable
- the home list displays saved items with the requested filters
- detail pages show full information
- users can configure notification time, count, and repeat preference
- daily recommendation batches are stored locally and notified on schedule
- notification taps open a visually polished Today page

## 22. Future Extension Paths

Planned seams left intentionally open:

- full-text search
- export / local backup
- richer ranking rules
- archive / snooze / skip actions
- more complete source parser library
- smarter local classification engine
- optional sync layer in a later product phase

## 23. Open Implementation Notes

No blocking open questions remain for the MVP spec. The following assumptions are locked:

- link title fetching is allowed over the network
- unread items may repeat in daily recommendations by default
- users can disable repeated pushes in settings
- phone portrait is the primary target
- “content type” in the second classification requirement is interpreted as thematic category derived from title/domain
