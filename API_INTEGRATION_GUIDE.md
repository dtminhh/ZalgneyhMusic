# 🎵 Hướng dẫn Test API trong ZalgneyhMusic

## ⚙️ Cấu hình trước khi chạy

### 1. Lấy IP máy tính chạy API Server

**Windows:**
```cmd
ipconfig
```
Tìm dòng "IPv4 Address" (VD: 192.168.1.100)

**Mac/Linux:**
```bash
ifconfig
```

### 2. Cập nhật IP trong các file sau:

#### File 1: `AppModule.kt`
```kotlin
companion ;object {
    private const val BASE_URL = "http://YOUR_IP:3000/"  // Thay YOUR_IP
}
```

#### File 2: `RetrofitInstance.kt`
```kotlin
private const val BASE_URL = "http://YOUR_IP:3000/"  // Thay YOUR_IP
```

#### File 3: `ApiHelper.kt`
```kotlin
private const val BASE_URL = "http://YOUR_IP:3000/"  // Thay YOUR_IP
```

#### File 4: `network_security_config.xml`
```xml
<domain includeSubdomains="true">YOUR_IP</domain>
```

### 3. Đảm bảo API Server đang chạy

- Server phải chạy ở port 3000
- Điện thoại và máy tính cùng WiFi
- Test bằng browser trên điện thoại: `http://YOUR_IP:3000/api/songs`

## 📱 Sử dụng API trong Fragment/Activity

### Ví dụ 1: Hiển thị danh sách Trending Songs

```kotlin
@AndroidEntryPoint
class YourFragment : Fragment() {
    
    private val viewModel: MusicViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Load trending songs
        viewModel.loadTrendingSongs(limit = 20)
        
        // Observe data
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.trendingSongs.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Show loading
                    }
                    is Resource.Success -> {
                        // Display songs: resource.data
                    }
                    is Resource.Error -> {
                        // Show error: resource.message
                    }
                }
            }
        }
    }
}
```

### Ví dụ 2: Tìm kiếm bài hát

```kotlin
// Trong Fragment
searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let { viewModel.searchSongs(it) }
        return true
    }
    
    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }
})

// Observe search results
viewLifecycleOwner.lifecycleScope.launch {
    viewModel.searchResults.collect { resource ->
        when (resource) {
            is Resource.Success -> {
                // Display search results
            }
            // ... handle other states
        }
    }
}
```

### Ví dụ 3: Load theo thể loại

```kotlin
// Load songs by genre
viewModel.loadSongsByGenre("Pop", limit = 20)

// Observe songs
viewLifecycleOwner.lifecycleScope.launch {
    viewModel.songs.collect { resource ->
        // Handle resource
    }
}
```

## 🎵 Phát nhạc với ExoPlayer (Coming Soon)

Sẽ được implement trong các commit tiếp theo. API URL streaming:
```kotlin
val streamUrl = ApiHelper.getStreamUrl(song.id)
// URL: http://YOUR_IP:3000/api/songs/stream/{songId}
```

## 🖼️ Load hình ảnh với Coil

```kotlin
imageView.load(ApiHelper.getImageUrl(song.imageUrl)) {
    crossfade(true)
    placeholder(R.drawable.placeholder)
    error(R.drawable.error_image)
}
```

## 🧪 Test Fragment đã tạo sẵn

Đã tạo sẵn `MusicListFragment` để test API:

1. Thêm fragment vào Activity hoặc Navigation:
```xml
<fragment
    android:id="@+id/musicListFragment"
    android:name="com.example.zalgneyhmusic.ui.fragment.MusicListFragment"
    android:label="Music List" />
```

2. Fragment sẽ tự động:
   - Load trending songs khi mở
   - Hiển thị trong RecyclerView
   - Hỗ trợ SwipeRefresh
   - Hiển thị loading/error states

## 📊 Available API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/songs` | Lấy tất cả bài hát (có phân trang) |
| GET | `/api/songs/{id}` | Lấy thông tin 1 bài hát |
| GET | `/api/songs/trending` | Bài hát trending |
| GET | `/api/songs/new` | Bài hát mới |
| GET | `/api/songs/search?q={query}` | Tìm kiếm |
| GET | `/api/songs/genre/{genre}` | Lọc theo thể loại |
| GET | `/api/songs/stream/{id}` | Stream nhạc |
| GET | `/api/artists` | Lấy danh sách nghệ sĩ |
| GET | `/api/artists/{id}/songs` | Bài hát của nghệ sĩ |
| GET | `/api/playlists` | Lấy danh sách playlist |

## 🐛 Troubleshooting

### Lỗi "Unable to resolve host"
- Kiểm tra IP đã đúng chưa
- Kiểm tra server có chạy không
- Cùng WiFi

### Lỗi "Cleartext HTTP traffic not permitted"
- Đã thêm `usesCleartextTraffic="true"` trong manifest
- Đã tạo `network_security_config.xml`

### Không load được hình
- Kiểm tra URL: `ApiHelper.getImageUrl(imagePath)`
- Kiểm tra Internet permission

### ExoPlayer không phát nhạc
- Sẽ implement trong version tiếp theo
- URL đúng: `http://YOUR_IP:3000/api/songs/stream/{id}`

## ✅ Checklist

- [ ] Cập nhật IP trong 4 file
- [ ] Server đang chạy
- [ ] Cùng WiFi
- [ ] Test URL bằng browser
- [ ] Build project thành công
- [ ] Run app và test MusicListFragment

## 📞 Debug Tips

1. **Xem log network:**
```kotlin
// Đã setup HttpLoggingInterceptor
// Xem Logcat filter: "OkHttp"
```

2. **Test API bằng Postman/Browser:**
```
GET http://YOUR_IP:3000/api/songs/trending?limit=10
```

3. **Kiểm tra Resource State:**
```kotlin
when (resource) {
    is Resource.Loading -> Log.d("TAG", "Loading...")
    is Resource.Success -> Log.d("TAG", "Success: ${resource.data?.size}")
    is Resource.Error -> Log.e("TAG", "Error: ${resource.message}")
}
```