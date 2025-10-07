# 🚀 Quick Start - Test API trong 5 phút

## Bước 1: Lấy IP máy tính (1 phút)

Mở Command Prompt (Windows) và chạy:
```cmd
ipconfig
```

Tìm dòng **IPv4 Address**, ví dụ: `192.168.1.100`

## Bước 2: Cập nhật IP trong project (2 phút)

### Tìm và thay thế trong 3 file:

**1. `app/src/main/java/com/example/zalgneyhmusic/di/AppModule.kt`**
- Dòng 32: `private const val BASE_URL = "http://192.168.1.100:3000/"`
- Thay `192.168.1.100` bằng IP của bạn

**2. `app/src/main/java/com/example/zalgneyhmusic/data/api/RetrofitInstance.kt`**
- Dòng 15: `private const val BASE_URL = "http://192.168.1.100:3000/"`
- Thay `192.168.1.100` bằng IP của bạn

**3. `app/src/main/java/com/example/zalgneyhmusic/data/api/ApiHelper.kt`**
- Dòng 7: `private const val BASE_URL = "http://192.168.1.100:3000/"`
- Thay `192.168.1.100` bằng IP của bạn

**4. `app/src/main/res/xml/network_security_config.xml`**
- Dòng 4: `<domain includeSubdomains="true">192.168.1.100</domain>`
- Thay `192.168.1.100` bằng IP của bạn

## Bước 3: Kiểm tra API Server (1 phút)

1. Đảm bảo API server đang chạy trên port 3000
2. Test bằng browser trên điện thoại: `http://YOUR_IP:3000/api/songs/trending?limit=5`
3. Nếu thấy JSON data → OK!

## Bước 4: Chạy app (1 phút)

1. Build project: `Build > Make Project`
2. Run app trên điện thoại/emulator
3. Navigate đến `MusicListFragment` để test

## 🎯 Sử dụng trong Fragment của bạn

### Cách đơn giản nhất:

```kotlin
@AndroidEntryPoint
class YourFragment : Fragment() {
    
    private val musicViewModel: MusicViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Load trending songs
        musicViewModel.loadTrendingSongs(20)
        
        // Observe và hiển thị
        viewLifecycleOwner.lifecycleScope.launch {
            musicViewModel.trendingSongs.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Hiển thị loading
                        progressBar.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        // Hiển thị danh sách
                        progressBar.visibility = View.GONE
                        val songs = resource.data ?: emptyList()
                        // Update UI với songs
                    }
                    is Resource.Error -> {
                        // Hiển thị lỗi
                        progressBar.visibility = View.GONE
                        Toast.makeText(context, resource.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
```

## 📋 Các function có sẵn trong MusicViewModel:

```kotlin
// Load danh sách
viewModel.loadTrendingSongs(limit = 20)
viewModel.loadNewSongs(limit = 20)
viewModel.loadAllSongs(page = 1, limit = 20)
viewModel.loadAllArtists(page = 1, limit = 20)
viewModel.loadAllPlaylists()

// Tìm kiếm
viewModel.searchSongs("query")

// Load theo filter
viewModel.loadSongsByGenre("Pop", limit = 20)
viewModel.loadArtistSongs("artistId")

// Set current song
viewModel.setCurrentSong(song)
```

## 📊 StateFlow có sẵn để observe:

```kotlin
viewModel.trendingSongs    // StateFlow<Resource<List<Song>>>
viewModel.newSongs         // StateFlow<Resource<List<Song>>>
viewModel.songs            // StateFlow<Resource<List<Song>>>
viewModel.searchResults    // StateFlow<Resource<List<Song>>>
viewModel.artists          // StateFlow<Resource<List<Artist>>>
viewModel.playlists        // StateFlow<Resource<List<Playlist>>>
viewModel.currentSong      // StateFlow<Song?>
```

## 🖼️ Load hình ảnh:

```kotlin
//import coil.load
//import com.example.zalgneyhmusic.data.api.ApiHelper

imageView.load(ApiHelper.getImageUrl(song.imageUrl)) {
    crossfade(true)
    placeholder(R.drawable.ic_launcher_background)
    error(R.drawable.ic_launcher_background)
}
```

## 🎵 Stream URL (cho ExoPlayer):

```kotlin
val streamUrl = ApiHelper.getStreamUrl(song.id)
// Returns: "http://YOUR_IP:3000/api/songs/stream/{id}"
```

## ⚠️ Common Issues:

### "Unable to resolve host"
- Kiểm tra IP đã đúng chưa
- Server có đang chạy không
- Điện thoại và máy tính cùng WiFi

### "Cleartext HTTP traffic not permitted"
- Đã được fix sẵn trong manifest và network_security_config.xml
- Nếu vẫn lỗi, clean và rebuild project

### Không có data
- Test API bằng browser trước
- Xem Logcat filter "OkHttp" để xem request/response
- Check server logs

## 🧪 Test Fragment có sẵn:

Đã tạo sẵn `MusicListFragment` để test:
- Tự động load trending songs
- Hiển thị trong RecyclerView
- Có SwipeRefresh
- Có loading và error states

## 📱 Thêm Fragment vào Navigation:

Nếu dùng Navigation Component, thêm vào `nav_graph.xml`:

```xml
<fragment
    android:id="@+id/musicListFragment"
    android:name="com.example.zalgneyhmusic.ui.fragment.MusicListFragment"
    android:label="Music List"
    tools:layout="@layout/fragment_music_list" />
```

Hoặc trong Activity:

```kotlin
supportFragmentManager.beginTransaction()
    .replace(R.id.container, MusicListFragment())
    .commit()
```

## ✅ Xong! Bây giờ bạn có thể:

- ✅ Load danh sách nhạc từ API
- ✅ Hiển thị hình ảnh
- ✅ Tìm kiếm bài hát
- ✅ Filter theo genre
- ✅ Load artists và playlists
- ⏳ Stream nhạc (sẽ implement sau)

---

**Need help?** Check `API_INTEGRATION_GUIDE.md` để biết chi tiết hơn.

