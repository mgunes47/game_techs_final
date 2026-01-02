# 3D Blok Dünyası Projesi - Uygulama Planı

Minecraft benzeri bir 3D blok dünyası oluşturmak için Java + LWJGL + Gradle kullanarak kapsamlı bir proje geliştirilecek.

## Teknoloji Seçimleri

| Bileşen | Teknoloji | Versiyon |
|---------|-----------|----------|
| Programlama Dili | Java | 17+ |
| 3D Grafik | LWJGL (OpenGL) | 3.3.3 |
| Build Sistemi | Gradle | 8.x |
| Matematik | JOML | 1.10.5 |
| Pencere/Input | GLFW (LWJGL içinde) | - |

## Proje Yapısı

```
odev3/
├── build.gradle
├── settings.gradle
├── src/
│   └── main/
│       ├── java/
│       │   └── com/blockworld/
│       │       ├── Main.java
│       │       ├── engine/
│       │       │   ├── Window.java
│       │       │   ├── GameLoop.java
│       │       │   └── Input.java
│       │       ├── graphics/
│       │       │   ├── Shader.java
│       │       │   ├── Mesh.java
│       │       │   ├── Renderer.java
│       │       │   └── Camera.java
│       │       ├── world/
│       │       │   ├── Block.java
│       │       │   ├── World.java
│       │       │   └── Raycaster.java
│       │       └── ui/
│       │           ├── UIRenderer.java
│       │           └── Hotbar.java
│       └── resources/
│           └── shaders/
│               ├── vertex.glsl
│               └── fragment.glsl
```

---

## Aşama 1: Temel 3D Dünyanın ve Görüntüleme Motorunun Kurulumu

### Yapılacaklar:
1. Gradle projesi oluşturma ve LWJGL bağımlılıklarını ekleme
2. Ana pencere ve OpenGL context oluşturma
3. 3D render döngüsü implementasyonu
4. Shader sistemi kurulumu (vertex + fragment shaders)
5. Küp mesh oluşturma ve render etme
6. Basit zemin oluşturma (küplerden düz alan)
7. Temel kamera sistemi (projeksiyon + view matrix)

---

## Aşama 2: Kamera ve Oyuncu Hareketleri

### Yapılacaklar:
1. Mouse input ile kamera bakış açısı kontrolü
2. WASD klavye kontrolü ile hareket
3. Kamera sınırları ve yumuşak hareket

---

## Aşama 3: Dünya ile Etkileşim (Blok Ekleme/Kırma)

### Yapılacaklar:
1. Raycasting sistemi implementasyonu
2. Sağ tık ile blok ekleme
3. Sol tık ile blok kaldırma
4. Seçili bloğun görsel göstergesi

---

## Aşama 4: Kullanıcı Arayüzü (UI) ve Temel Varlık Sistemi

### Yapılacaklar:
1. UI render sistemi (2D overlay)
2. Alt menü (hotbar) tasarımı
3. Varlık seçimi ve gösterimi

---

## Aşama 5: Gelişmiş Varlık Sistemi

### Yapılacaklar:
1. Yapı seçim aracı (bounding box)
2. Yapı kaydetme sistemi
3. Kaydedilen yapıları menüye ekleme
4. Özel varlıkları yerleştirme

---

## Kontroller

| Tuş | Eylem |
|-----|-------|
| W/A/S/D | Hareket |
| Mouse | Bakış yönü |
| Sol Tık | Blok kır |
| Sağ Tık | Blok yerleştir |
| 1-9 | Hotbar slot seçimi |
| ESC | Menü / Çıkış |
