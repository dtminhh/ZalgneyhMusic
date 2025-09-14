# Capstone Project - 🎵 MusicApp – Online & Offline Music Streaming Application
### 📖 Project Summary

With the rising demand for online music streaming, users expect a consistent experience: quickly finding their favorite songs, seamless playback even when switching apps, and a clean way to manage their personal library.
However, many platforms still face pain points such as:
  - Cluttered and inconsistent UI.
  - Slow response times during peak hours.
  - Unstable background playback.
  - Inaccurate search results and poor recommendations.
  - Complicated playlist, favorites, and history management.

This project aims to build a music application with:
  - A clean, intuitive interface.
  - Efficient search with filters.
  - Stable background playback.
  - Clear personal library management (playlists, favorites, history).
  - Real-time data synchronization to reduce delays and errors.
  - Discovery features: recommendations, charts, and mood-based playlists.

🎯 Main Objectives
- Build a user-friendly music application.
- Support online & offline playback, background play, and notifications.
- Manage personal playlists, listening history, and favorite songs.
- Provide real-time updates for artists, albums, and charts.
- Implement basic music recommendations (lightweight ML, e.g., frequency-based suggestions).
- Ensure security, performance optimization, and scalability.

📌 Core Contents
- Overview: Online music streaming demand & current app limitations.
- System functions & database design: Artists, songs, albums, playlists, users.
- System implementation:
  + Backend (Firebase): manage songs/artists/albums; real-time sync for personal libraries; secure read/write rules; store images & metadata.
  + Android App (Kotlin): MVVM architecture, smooth navigation; Explore, Search, Library, and Player screens.
  + Playback (ExoPlayer): MediaSession, background playback, notification controls (Play/Pause/Next), continuous queue/playlist, handling interruptions (calls, headphones).
  + Search & Discovery: search by song/artist/album; recommendations, charts, themed lists.
  + Basic Personalization: recommend music by frequency and listening time (ML-lite).
- Testing & Deployment: ensure stability, security, performance, bug minimization.
- Future Work: bug fixes, improvements, and multi-platform expansion.

✅ Expected Outcomes
1. Apply MVVM and possibly Clean Architecture to avoid scaling/maintenance issues.
2. Improve problem-solving and debugging skills.
3. Deliver a secure and stable system.

🗓️ Project Timeline
No.	Phase	Duration
1	Research & Analysis	2 weeks
2	System Design	2 weeks
3	Development	6 weeks
4	Testing & Finalization	2 weeks

📚 References
1. Android Developers Documentation
2. Kotlin Language Documentation
3. Firebase Documentation
