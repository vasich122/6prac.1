package com.example.a6prac

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.a6prac.ui.theme._6pracTheme
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _6pracTheme {
                GreetingPreview()
            }
        }
    }
}

@Composable
fun downloadImagesLinks(onUrlEntered: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(
            value = text,
            onValueChange = { text = it },
            label = {
                Text("Введите ссылку для картинки, которую хотите скачать")
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        )
        Button(onClick = { onUrlEntered(text) }) {
            Text("Загрузить изображение")
        }
    }
}

@Composable
fun downloadedImage(imageUrl: String) {
    if (imageUrl.isNotEmpty()) {
        val painter = rememberImagePainter(data = imageUrl)

        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.padding(vertical = 20.dp)
        )
    }
}

@Composable
fun GreetingPreview() {
    var imageUrl by remember { mutableStateOf("") }
    val context = LocalContext.current // Получаем контекст текущего компонента

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        downloadImagesLinks { url ->
            imageUrl = url // Обновляем imageUrl, когда пользователь нажимает кнопку
            downloadAndSaveImage(url, context) // Запускаем загрузку и сохранение изображения
        }
        downloadedImage(imageUrl) // Отображаем изображение, если URL не пустой
    }
}

// Функция для загрузки изображения и его сохранения
private fun downloadAndSaveImage(url: String, context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val bitmap = downloadImage(url, context)
            if (bitmap != null) {
                saveImageToInternalStorage(bitmap, context)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

// Функция для загрузки изображения
private suspend fun downloadImage(url: String, context: Context): Bitmap? {
    return withContext(Dispatchers.IO) {
        val request = ImageRequest.Builder(context) // Используем переданный контекст
            .data(url)
            .size(Size.ORIGINAL)
            .build()

        val result = context.imageLoader.execute(request)
        if (result.drawable is BitmapDrawable) {
            (result.drawable as BitmapDrawable).bitmap
        } else {
            null
        }
    }
}

// Функция для сохранения изображения во внутренней памяти
private fun saveImageToInternalStorage(bitmap: Bitmap, context: Context) {
    val file = File(context.filesDir, "downloaded_image.png") // Имя файла

    FileOutputStream(file).use { outputStream ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream) // Сохраняем изображение
    }
}
