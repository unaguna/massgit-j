package jp.unaguna.massgit.common.io

import java.io.InputStream
import java.net.URL

class UrlResource(val url: URL) : InputStreamSource {
    override fun getInputStream(): InputStream {
        return url.openStream()
    }

    companion object {
        fun loadFromClasspathIfExist(
            name: String,
            classLoader: ClassLoader = UrlResource::class.java.classLoader,
        ): InputStreamSource? {
            val url = classLoader.getResource(name)
                ?: return null
            return UrlResource(url)
        }
    }
}

fun URL.toResource() = UrlResource(this)
