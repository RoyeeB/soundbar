
# 🎵 SoundBarPlayerView

**SoundBarPlayerView** is a custom Android library that displays an interactive waveform (sound bars) for audio files.  
It provides a modern, touch-friendly UI for playing, seeking, and visualizing audio playback.

---

## ✨ Features

- 🎚 Interactive waveform display with seek support  
- 🌈 Customizable color for the active progress bar  
- ⚡ Smooth gradient animation and glow effect  
- ⏱ Built-in display of current and total time  
- 📱 Fully touch-compatible with drag-to-seek  
- 🔌 Easy integration with `MediaPlayer`

---

## 📷 Screenshots & Video

### 📸 Screenshots
<p float="left">
  <img src="screenshots/screenshot1.png" width="300"/>
  <img src="screenshots/screenshot2.png" width="300"/>
  <img src="screenshots/screenshot3.png" width="300"/>
</p>

> 📁 Put your screenshots in a `screenshots/` folder in your repo.

### 🎥 Demo Video

[![Watch Demo](https://img.youtube.com/vi/VIDEO_ID_HERE/0.jpg)](https://www.youtube.com/watch?v=VIDEO_ID_HERE)

> Replace `VIDEO_ID_HERE` with your actual YouTube video ID.

---

## 🧩 How It Works

The library converts a **WAV file** into a list of amplitude values (bars), which are rendered visually.  
During playback, the library highlights the played portion and listens for touch gestures to seek.

---

## 📲 Usage

### 1. Add to your XML layout:

```xml
<com.example.soundbarlib.SoundBarPlayerView
    android:id="@+id/soundBarView"
    android:layout_width="match_parent"
    android:layout_height="200dp"
    app:barColor="#00E676" />
```

### 2. Load audio from `res/raw`:

```java
soundBarView.loadAudioResource(context, R.raw.mysong);
```

### 3. Update played progress from `MediaPlayer`:

```java
int position = mediaPlayer.getCurrentPosition();
int duration = mediaPlayer.getDuration();
float percent = position / (float) duration;
int playedBars = (int)(percent * soundBarView.getTotalBars());
soundBarView.setPlayedBars(playedBars);
```

### 4. Support user dragging to seek:

```java
soundBarView.setOnSeekListener(percent -> {
    if (mediaPlayer != null) {
        int seekTo = (int)(mediaPlayer.getDuration() * percent);
        mediaPlayer.seekTo(seekTo);
    }
});
```

### 5. Change bar color dynamically:

```java
soundBarView.setBarColor(Color.parseColor("#FFEB3B")); // Yellow
```

---

## 🧪 Requirements

- Android API 21+
- WAV files only (for accurate waveform generation)
- Works with `MediaPlayer` (local files only)

---

## 📱 Demo App

A full sample app is included, demonstrating:

- A list of 3 songs with titles  
- A `SoundBarPlayerView` showing real-time progress  
- Play / Pause button  
- Highlighting the currently playing song  
- 3 color buttons to change waveform color live  

You can use this demo to learn how to connect the library with your own `MediaPlayer`.

---

## 📁 File Structure

- `SoundBarPlayerView.java` – Main custom view class  
- `WaveformGenerator.java` – Converts WAV to amplitude data  
- `FileUtil.java` – Utility to convert raw resources to WAV files  
- `attrs.xml` – Support for XML styling like bar color

---

## 💡 Coming Soon?

- MP3 and streaming support  
- Saving and loading waveform cache  
- Zoom or scrollable waveform

---

## 👨‍💻 Author

Made with ❤️ by **Roee**  
Final-year software engineering student & Android enthusiast.

[LinkedIn](#) • [GitHub](#) • [Portfolio](#)
