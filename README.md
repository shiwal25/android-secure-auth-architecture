# Android Secure Auth Architecture

A production-style reference implementation of an Android authentication layer — encrypted token storage, automatic JWT refresh, and reactive auth-state-driven navigation — built with Jetpack Compose, Ktor, Koin, and Tink.

> ⚠️ This is an **architecture showcase**, not a finished product. The backend is a separate Go service (not included here) — `BASE_URL` points at a placeholder endpoint. The goal is to demonstrate how the pieces fit together, not to be a drop-in library.

---

## Why this exists

Most "auth demo" repos store a token in `SharedPreferences` and call it done. That's not how it works in a real app, where you have to handle:

- Tokens at rest needing **encryption**, not just storage
- Access tokens **expiring mid-session**, mid-request, with the request still needing to succeed
- The UI needing to **react** to login/logout instantly, from anywhere in the app, without manual `Activity` restarts
- A **cold start** where the stored token might already be dead and needs a silent refresh before the user sees anything

This repo is my answer to those four problems, isolated from the rest of the ride-hailing app so the architecture is easy to read in one sitting.

---

## What it actually does

| Capability | How |
|---|---|
| Encrypts tokens at rest | Tink `AES256-GCM` keyset, key wrapped by the Android Keystore, transparently applied to Proto/JSON DataStore |
| Auto-refreshes expired access tokens | Ktor `Auth` plugin's `bearer {}` provider — refresh happens inside the HTTP client, invisible to calling code |
| Re-authenticates silently on cold start | `JwtUtils` decodes the JWT payload locally and checks `exp` before the first screen renders |
| Drives navigation from auth state | The token `DataStore` is the single source of truth — `Flow<String?>` → `AuthState` → which nav graph is shown |
| Separates auth domains in the network layer | Two Ktor clients: one **plain** (login/register/refresh), one **authenticated** (everything else) |

---

## Architecture

<img width="2720" height="2400" alt="android_secure_auth_architecture_flow" src="https://github.com/user-attachments/assets/6300d55e-125c-423a-b6ba-71c8446e23de" />


**The core idea:** nothing in the UI layer ever asks "am I logged in?" by checking a boolean flag somewhere. The encrypted DataStore is the single source of truth, and every layer above it — `AuthViewModel`, `RootNavigation`, the Ktor `Auth` plugin — just *reacts* to what's actually in storage. Login, logout, and silent token refresh all funnel through the same `Flow`, so there's exactly one code path that decides whether the user is authenticated.

---

## Deep dive: the four hard parts

### 1. Encrypted token storage (Tink + DataStore)

`TokenDataStore` wraps a `DataStore<AuthPreferences>` with an `AeadSerializer` from `androidx.datastore.tink`. The actual encryption key is an AES-256-GCM keyset managed by `AndroidKeysetManager`, with the keyset itself wrapped by a key that never leaves the **Android Keystore** (hardware-backed on most devices).

```kotlin
val keysetHandle = AndroidKeysetManager.Builder()
    .withSharedPref(context, KEYSET_NAME, KEYSET_PREFS_NAME)
    .withKeyTemplate(KeyTemplate.createFrom(PredefinedAeadParameters.AES256_GCM))
    .withMasterKeyUri(MASTER_KEY_URI) 
    .build()
    .keysetHandle
```

Why DataStore-tink over `EncryptedSharedPreferences`: `EncryptedSharedPreferences` is in maintenance mode and built on the old, deprecated Security Crypto APIs. DataStore-tink gets you the same Keystore-backed AEAD encryption, but on top of DataStore's Flow-based, coroutine-native API — which matters a lot once the rest of the app (correctly) treats auth state as a reactive stream rather than a value you poll.

### 2. Reactive auth state, not a boolean flag

`AuthViewModel` doesn't have an `isLoggedIn: Boolean`. It collects `TokenDataStore.accessToken` directly:

```kotlin
private fun observeTokenForAuthState() {
    viewModelScope.launch {
        tokenDataStore.accessToken.collect { token ->
            if (isFirstEmission) {
                isFirstEmission = false
                handleStartup(token)       
            } else {
                _authState.value = if (token != null) Authenticated else Unauthenticated
            }
        }
    }
}
```

This means **login**, **logout**, and **silent mid-session refresh** all produce the exact same effect: a new emission from the same `Flow`, picked up by the same collector. There's no separate "now navigate to home" call after login — saving the token to `DataStore` *is* the navigation trigger. That's the part of this design I'm most happy with: it collapses three different "the user's auth changed" code paths into one.

### 3. Automatic refresh via Ktor's `Auth` plugin

The interesting failure mode in any JWT app is: an authenticated request goes out, the access token has *just* expired, and the request needs to transparently retry after a refresh — without every repository method having to know about refresh logic. Ktor's `Auth { bearer { ... } }` provider handles exactly this:

```kotlin
install(Auth) {
    bearer {
        loadTokens { /* read current tokens from DataStore */ }
        refreshTokens {
            val response = plainClient.post(refreshUrl) { setBody(RefreshRequest(...)) }
            when (response.status) {
                HttpStatusCode.OK -> { /* save new tokens, return them */ }
                Unauthorized, Forbidden -> { tokenDataStore.clearSession(); null }
                else -> null   
            }
        }
        sendWithoutRequest { request -> request.url.host == BACKEND_HOST }
    }
}
```

A couple of deliberate decisions here:
- **The refresh call uses the *plain* client**, not the authenticated one — otherwise a refresh request could itself trigger another refresh attempt and recurse.
- **5xx during refresh doesn't clear the session.** Only a `401`/`403` (the server explicitly rejecting the refresh token) logs the user out. A transient server error just fails that one request — the user stays logged in and can retry.
- **`JwtUtils.isTokenExpired()` adds a 30-second buffer** before the actual `exp` claim, so a request doesn't race a token that's about to expire mid-flight.

### 4. Two HTTP clients, one boundary

`HttpClientFactory` builds a `plain` client (no auth — used for `/login`, `/register`, `/refresh`) and a separate `auth` client (Ktor `Auth` plugin installed — used for everything that requires a logged-in user). DI wires these into the right repositories via Koin qualifiers (`named("plain")` / `named("auth")`), so it's structurally impossible to accidentally call an authenticated endpoint with a client that has no idea how to refresh tokens.

---

## Tech stack

| Layer | Choice |
|---|---|
| UI | Jetpack Compose, Material 3 |
| Navigation | Navigation 3 (`navigation3-ui`, `NavKey`/`NavBackStack`/`NavDisplay`) |
| DI | Koin (`koin-androidx-compose`, BOM 4.2.1) |
| Networking | Ktor Client 3.5.0 (Android engine, `ContentNegotiation`, `Auth`, `Logging`) |
| Serialization | kotlinx.serialization |
| Local storage | AndroidX DataStore + `datastore-tink` |
| Encryption | Google Tink (`tink-android` 1.21.0), AES-256-GCM, Android Keystore-backed |
| Language | Kotlin, `minSdk 28` / `targetSdk 37` |

---

## Project structure

```
core/
 ├─ data/local/        TokenDataStore, AuthPreferences (+ Serializer) — the encrypted store
 ├─ navigation/        Routes (NavKey), RootNavigation (auth-state-gated graph switch)
 ├─ network/           HttpClientFactory — plain + authenticated Ktor clients
 └─ util/              JwtUtils — local JWT exp decoding

feature/auth/
 ├─ data/remote/        AuthApiService, DTOs (LoginRequest, RegisterRequest, AuthResponse, RefreshResponse, ApiError)
 ├─ domain/repository/  AuthRepository (interface) + Impl
 └─ presentation/       AuthViewModel, AuthState, AuthUiState, LoginScreen, RegisterScreen, AuthNavGraph

feature/home/           Minimal authenticated screen, just enough to prove the gate works

di/                      networkModule, repositoryModule, viewModelModule (Koin)
```

---

## Running it

```bash
git clone https://github.com/shiwal25/android-secure-auth-architecture.git
```

Open in Android Studio (or `./gradlew assembleDebug`). There's no bundled backend — `BASE_URL` in `NetworkModule.kt` points at `https://api.yourbackend.com` as a placeholder. To actually exercise login/register/refresh against a real server, point `BASE_URL` and `sendWithoutRequest`'s host check at your own auth API exposing:

```
POST /auth/login     { email, password }        → { accessToken, refreshToken, name, userId }
POST /auth/register  { name, email, password }  → { accessToken, refreshToken, name, userId }
POST /auth/refresh    { refreshToken }            → { accessToken, refreshToken }
```

---

## Design decisions & trade-offs

- **Koin over Hilt** — faster to iterate on during early-stage solo development; no codegen, runtime DI graph is easy to reason about for a project this size. Hilt's compile-time safety nets become more valuable as the module count grows — would revisit at scale.
- **Navigation 3 over classic Navigation-Compose** — it's newer and the API surface is still settling, but the `NavKey`-based back stack is a much more natural fit for type-safe, state-driven graph switching than string routes.
- **Tink/DataStore over `EncryptedSharedPreferences`** — covered above; mainly about not building on a maintenance-mode API.
- **Result<Unit> over throwing exceptions across the repository boundary** — every `AuthRepository` method returns `Result<Unit>` with a human-readable failure message, so the ViewModel never has to catch anything; it just pattern-matches on success/failure.

---
<div align="center">

Built by **[@shiwal25](https://github.com/shiwal25)**

</div>
