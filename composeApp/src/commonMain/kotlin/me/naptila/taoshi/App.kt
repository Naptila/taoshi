package me.naptila.taoshi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.naptila.taoshi.utils.API
import me.naptila.taoshi.utils.CookieJarHelper
import me.naptila.taoshi.utils.HttpUtils
import okhttp3.CookieJar
import okhttp3.RequestBody.Companion.toRequestBody
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(DelicateCoroutinesApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            var title by remember { mutableStateOf("") }
            var subtitle by remember { mutableStateOf("") }
            var showingDialog by remember { mutableStateOf(false) }
            var showUsernameError by remember { mutableStateOf(false) }
            var showPasswordError by remember { mutableStateOf(false) }

            if (showingDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showingDialog = false
                    },
                    title = {
                        Text(text = title)
                    },
                    text = {
                        Text(text = subtitle)
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showingDialog = false
                            },
                            modifier = Modifier
                                .padding(16.dp)
                        ) {
                            Text("关闭")
                        }
                    },
                )
            }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var username by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var passwordHidden by remember { mutableStateOf(true) }
                var enabled by remember { mutableStateOf(true) }

                Text(text = "51taoshi", style = TextStyle(fontSize = 40.sp, fontFamily = FontFamily.Cursive))
                Spacer(modifier = Modifier.height(20.dp))
                TextField(
                    label = { Text(text = "用户名") },
                    value = username,
                    onValueChange = { username = it },
                    supportingText = { if (showUsernameError) Text("用户名错误") },
                    isError = showUsernameError)

                Spacer(modifier = Modifier.height(20.dp))
                TextField(
                    label = { Text(text = "密码") },
                    value = password,
                    visualTransformation = if (passwordHidden) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordHidden = !passwordHidden }) {
                            val visibilityIcon =
                                if (passwordHidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val description = if (passwordHidden) "显示密码" else "隐藏密码"
                            Icon(imageVector = visibilityIcon, contentDescription = description)
                        }
                    },
                    onValueChange = { password = it },
                    supportingText = { if (showPasswordError) Text("密码错误") },
                    isError = showPasswordError
                )

                Spacer(modifier = Modifier.height(20.dp))
                Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
                    Button(
                        onClick = {
                            GlobalScope.launch(Dispatchers.Default) {
                                enabled = false
                                val cookie = CookieJarHelper()

                                when (val result = HttpUtils.login(username, password, cookie)) {
                                    // 登录成功
                                    "1" -> {
                                        showUsernameError = false
                                        showPasswordError = false

                                        val allHomeWork = HttpUtils.get(API.ALL_HOMEWORK, cookie)

                                        val result = doHomeWork(allHomeWork, cookie)

                                        title = "完成账号"
                                        subtitle = "姓名: ${allHomeWork.substringAfter("ft16 text-white mb5\"><span>").substringBefore("同学</span></div>")}" + "\n" + "成功: ${result.first}" + "\n" + "失败: ${result.second}"
                                        showingDialog = true
                                    }
                                    // 没有此用户
                                    "nouser" -> {
                                        showUsernameError = true
                                        showPasswordError = false
                                    }
                                    "pwderror" -> {
                                        showUsernameError = false
                                        showPasswordError = true
                                    }
                                    else -> {
                                        showUsernameError = false
                                        showPasswordError = false

                                        title = "登录失败"
                                        subtitle = result
                                        showingDialog = true
                                    }
                                }
                                enabled = true
                            }
                        },
                        shape = RoundedCornerShape(50.dp),
                        enabled = username.isNotEmpty() && password.isNotEmpty() && enabled,
                        modifier = Modifier
                            .height(50.dp)
                    ) {
                        Text(text = "开始")
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                TextButton(onClick = {
                    title = "关于"
                    subtitle = "此软件由Naptila制作\n此软件完全免费，如果你是购买获得说明你被骗了"
                    showingDialog = true
                }, content = {
                    Text("关于")
                })
            }
        }
    }
}

fun doHomeWork(allHomeWork: String, cookie: CookieJar): Pair<Int, Int> {
    val tr = allHomeWork.substringAfter("</tr>-->").substringBefore("</tbody>").substringAfter("<tr>")

    val questions = tr.split("<tr>")

    var success = 0
    var failed = 0

    for (question in questions) {
        val reback = question.contains("撤回重做")

        val kcid = if (reback) {
            question.substringAfter("reback('").substringBefore("')\"")
        } else {
            question.substringAfter("view('").substringBefore("')\"")
        }

        if (reback)
            HttpUtils.post(API.RE_BACK_HOMEWORK, cookie, "kcid=$kcid".toRequestBody(HttpUtils.URLENCODED))

        // 签到
        if (question.contains("没有签到"))
            HttpUtils.get(API.SIGN_IN + "?$kcid&_=" + System.currentTimeMillis(), cookie)

        if (question.contains("补作业") || question.contains("做作业") || reback) {
            val questionPage = HttpUtils.get(API.VIEW_HOMEWORK + "?kcid=$kcid", cookie)
            val paperId = questionPage.substringAfter("gotoHomeWorkPage('").substringBefore("',")
            val answers = HttpUtils.getAnswer(paperId, kcid, cookie)
            if (HttpUtils.doHomeWork(answers.second, paperId, kcid, answers.first, cookie)) {
                println("完成作业:$kcid")
                success++
            } else {
                println("失败:$kcid")
                failed++
            }
        }
    }
    return Pair(success, failed)
}