# JARVIS ChatGPT for Android

This separate Android package runs JARVIS locally on the phone and uses the official open-source **Sign in with ChatGPT** flow. It does not use an OpenAI API key, an OpenRouter key, or a hosted ChatGPT proxy.

## Install and connect

Install the `JARVIS-ChatGPT.apk` release asset, open **JARVIS ChatGPT**, and choose **Continue with ChatGPT**. The system browser handles account selection, workspace selection, and permission to use the existing ChatGPT plan. Its local callback stages the code without contacting OpenAI while JARVIS is backgrounded. Return through the **Return to JARVIS** link after sign-in; the foreground app then exchanges and verifies the code. DNS failures retain the staged attempt, and Retry connection resumes it without silently replaying an already exchanged code. Tap **Start JARVIS** and grant the Android microphone permission once.

Keep extra-credit usage and automatic credit purchases disabled in ChatGPT if no additional charges are allowed. This app does not add an API-key fallback or buy credits. Included plan limits still apply.

## Architecture and security

- An HTTP server binds only to `127.0.0.1`, preferring port 1455. OAuth callbacks use `/auth/callback`, as required by OpenAI's OSS registration flow.
- Initial registration uses `dynamic_agent_client`, the actual app name `Henson JARVIS`, a stable host UUID, random state and nonce, and PKCE S256. Later sign-ins reuse the issued client ID for the selected account.
- State, token signatures, issuer, issued-client audience, nonce, expiration, subject continuity, and granted ChatGPT plan scopes are validated before credentials are activated.
- Credentials are encrypted using AES-GCM and an Android Keystore key in private app storage. Tokens never enter the WebView, browser storage, JavaScript, GitHub, or Vercel.
- Only locally bundled pages load in the WebView. External links open in the system browser. The JavaScript bridge is restricted to the local origin. Microphone access requires Android permission and a matching trusted origin.
- Models come from the connected account's authenticated model catalog. Responses use `store:false`, `stream:true`, complete conversation input, and instructions. Interrupted/failed/incomplete streams are not saved as completed answers.
- Token refresh is serialized and rotating credentials are persisted together. Sign-out attempts remote refresh-token revocation and clears local tokens even if remote revocation cannot be confirmed.
- Kokoro George, a British male voice, is bundled and synthesizes speech on-device. Microphone recognition uses Android's speech service. Listening pauses outside the foreground; this is not a lock-screen/background hotword service.
- The original offline Android module is retained separately.

## Verification

The ChatGPT Android workflow builds the web assets and Android APK, runs Android lint, and runs instrumented tests for PKCE, signed JWT claims, rejected signatures/nonce/audience/issuer/expiry, required scopes/state, and encrypted credential storage before publishing the APK.

These checks do not authenticate the owner's account. Actual ChatGPT authorization, account eligibility, a completed live response, and Samsung-specific microphone/audio behavior require the owner's phone. Do not describe them as verified from compilation or mocked tests.

The release is a debug-signed personal testing build with a SHA-256 checksum, not a Play Store production release. A separate application ID avoids replacing the earlier JARVIS app or deleting its data.

## Official protocol references

- https://developers.openai.com/siwc/token-sharing-open-source
- https://developers.openai.com/siwc/token-sharing-open-source/sign-in
- https://developers.openai.com/siwc/token-sharing-open-source/models-and-inference
- https://developers.openai.com/siwc/token-sharing-open-source/profiles-and-sessions
- https://developers.openai.com/siwc/token-sharing-open-source/preview-limitations
