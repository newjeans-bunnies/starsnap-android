package com.sns.starsnap.di

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

class PersistentCookieJar(context: Context) : CookieJar {

    private val prefs = context.getSharedPreferences("cookie_store", Context.MODE_PRIVATE)
    private val cookieStore: ConcurrentHashMap<String, MutableList<Cookie>> = ConcurrentHashMap()

    init {
        // 앱 시작 시 저장된 쿠키를 메모리로 복원
        prefs.all.forEach { (host, value) ->
            if (value is String) {
                val cookies = deserializeCookies(value)
                if (cookies.isNotEmpty()) {
                    cookieStore[host] = cookies.toMutableList()
                }
            }
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isEmpty()) return
        val host = url.host
        val list = cookieStore.getOrPut(host) { mutableListOf() }
        synchronized(list) {
            for (cookie in cookies) {
                val idx = list.indexOfFirst { it.name == cookie.name }
                if (idx >= 0) list[idx] = cookie else list.add(cookie)
            }
            persistCookies(host, list)
        }
    }

    // 로그아웃 등으로 세션을 초기화할 때 저장된 쿠키(HttpOnly refresh 쿠키 포함)를 모두 제거
    fun clear() {
        cookieStore.clear()
        prefs.edit().clear().apply()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        val list = cookieStore[host] ?: return emptyList()
        val valid = mutableListOf<Cookie>()
        synchronized(list) {
            val iterator = list.iterator()
            while (iterator.hasNext()) {
                val cookie = iterator.next()
                if (cookie.expiresAt < System.currentTimeMillis()) {
                    iterator.remove()
                } else if (cookie.matches(url)) {
                    valid.add(cookie)
                }
            }
        }
        return valid
    }

    // persistent == true인 쿠키(만료 시간이 명시된 쿠키)만 디스크에 저장
    private fun persistCookies(host: String, cookies: List<Cookie>) {
        val toSave = cookies.filter { it.persistent && it.expiresAt > System.currentTimeMillis() }
        if (toSave.isEmpty()) {
            prefs.edit().remove(host).apply()
        } else {
            prefs.edit().putString(host, serializeCookies(toSave)).apply()
        }
    }

    private fun serializeCookies(cookies: List<Cookie>): String {
        val array = JSONArray()
        for (cookie in cookies) {
            array.put(
                JSONObject().apply {
                    put("name", cookie.name)
                    put("value", cookie.value)
                    put("domain", cookie.domain)
                    put("path", cookie.path)
                    put("expiresAt", cookie.expiresAt)
                    put("secure", cookie.secure)
                    put("httpOnly", cookie.httpOnly)
                }
            )
        }
        return array.toString()
    }

    private fun deserializeCookies(json: String): List<Cookie> {
        return runCatching {
            val array = JSONArray(json)
            (0 until array.length()).mapNotNull { i ->
                val obj = array.getJSONObject(i)
                val expiresAt = obj.getLong("expiresAt")
                if (expiresAt <= System.currentTimeMillis()) return@mapNotNull null
                Cookie.Builder()
                    .name(obj.getString("name"))
                    .value(obj.getString("value"))
                    .domain(obj.getString("domain"))
                    .path(obj.getString("path"))
                    .expiresAt(expiresAt)
                    .apply {
                        if (obj.getBoolean("secure")) secure()
                        if (obj.getBoolean("httpOnly")) httpOnly()
                    }
                    .build()
            }
        }.getOrDefault(emptyList())
    }
}
