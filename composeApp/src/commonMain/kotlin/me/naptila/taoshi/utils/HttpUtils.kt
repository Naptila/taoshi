package me.naptila.taoshi.utils

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object HttpUtils {
    val URLENCODED: MediaType = "application/x-www-form-urlencoded".toMediaType()
    const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.5112.81 Safari/537.36 Edg/104.0.1293.47"

    fun login(username: String, password: String, cookie: CookieJar): String =
        post(
            API.LOGIN,
            cookie,
            "user=$username&pwd=${password.md5()}".toRequestBody(URLENCODED)
        )

    fun getAnswer(paperId: String, rsid: String, cookie: CookieJar): Pair<HashMap<String, String>, Int> {
        val answerPage = get(API.VIEW_ANSWER + "?paperId=$paperId&rsid=$rsid", cookie)

        val questionCount = answerPage.substringAfter("本大题共").substringBefore("题，总分").toInt();

        val map = hashMapOf<String, String>()
        for (count in 1..questionCount) {
            val title = answerPage.substringAfter("<b>$count.", "").substringBefore("</b>")
            val answer = answerPage.substringAfter(title).substringBefore("分</span></div>")
                .substringAfter("【正确答案：】</span><span >")

            map[title] = answer
        }
        return Pair(map, questionCount)
    }

    fun doHomeWork(questionCount: Int, paperId: String, kcid: String, answers: HashMap<String, String>, cookie: CookieJar): Boolean {
        val homeworkPage = get(API.DO_HOMEWORK + "?kcid=$kcid&paperId=$paperId", cookie)

        val rsid = homeworkPage.substringAfter("\"rsid\":\"").substringBefore("\",")

        for (count in 1..questionCount) {
            val questionPage = homeworkPage.substringAfter("<b>$count.")
            val title = questionPage.substringBefore("</b>")
            val choosePage = questionPage.substringAfter("</ul>").substringBefore("</ul>")
            val answerIn = answers[title]!!
            if (choosePage.contains("multipleAnswer")) {
                multipleAnswer(choosePage, answerIn, rsid, cookie)
            } else if (choosePage.contains("chooseAnswer")) {
                chooseAnswer(choosePage, answerIn, rsid, cookie)
            }
        }

        val info = post(API.SAVE_ANSWER, cookie, "paperId=$paperId&rsid=$rsid".toRequestBody(URLENCODED))
        // post
        return info == "true"
    }

    private fun chooseAnswer(choosePage: String, answerIn: String, rsid: String, cookie: CookieJar) {
        val answerPage = choosePage.substringAfter("value=\"$answerIn").substringBefore(");\">$answerIn</label></li>").substringAfter("\" onclick=\"chooseAnswer(").replace("'", "")

        val splits = answerPage.split(",")

        cacheQuestion(splits[0], splits[1], splits[2], rsid, cookie)
    }

    private fun multipleAnswer(choosePage: String, answerIn: String, rsid: String, cookie: CookieJar) {
        for (answer in answerIn.indices) {
            val answerPage = choosePage.substringAfter("value=\"${answerIn[answer]}").substringBefore(");\">${answerIn[answer]}</label></li>").substringAfter("\" onclick=\"multipleAnswer(").replace("'", "")

            val splits = answerPage.split(",")

            cacheQuestion(splits[0], splits[2], splits[3], rsid, cookie)
        }
    }

    private fun cacheQuestion(questionId: String, answer: String, questionType: String, rsid: String, cookie: CookieJar) {
        post(
            API.CACHE_QUESTION_ANSWER,
            cookie,
            "qid=$questionId&answer=$answer&qtype=$questionType&rsid=$rsid&persist=1".toRequestBody(URLENCODED)
        )
    }

    fun get(url: String, cookie: CookieJar): String {
        val client = OkHttpClient.Builder().cookieJar(cookie).build()

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val cookies = Cookie.parseAll(request.url, response.headers)

            if (cookies.isNotEmpty())
                client.cookieJar.saveFromResponse(request.url, cookies)

            return response.body.string()
        }
    }

    fun post(url: String, cookie: CookieJar, body: RequestBody): String {
        val client = OkHttpClient.Builder().cookieJar(cookie).build()

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val cookies = Cookie.parseAll(request.url, response.headers)

            if (cookies.isNotEmpty())
                client.cookieJar.saveFromResponse(request.url, cookies)

            return response.body.string()
        }
    }
}