package me.naptila.taoshi.utils

object API {
    const val MAIN = "https://infotech.51taoshi.com/hw/fore/index.do"
    const val LOGIN = "https://infotech.51taoshi.com/users/login/loginAjax.do"
    const val ALL_HOMEWORK = "https://infotech.51taoshi.com/hw/stu/myHomework.do"

    // Use arg: https://infotech.51taoshi.com/hw/stu/signIn.do?kcid=$kcid&_=
    const val SIGN_IN = "https://infotech.51taoshi.com/hw/stu/signIn.do"

    // Use arg: https://infotech.51taoshi.com/hw/stu/viewHomework.do?kcid=$kcid
    const val VIEW_HOMEWORK = "https://infotech.51taoshi.com/hw/stu/viewHomework.do"

    const val RE_BACK_HOMEWORK = "https://infotech.51taoshi.com/hw/stu/reback.do"

    // Use arg: https://infotech.51taoshi.com/hw/stu/doHomework.do?kcid=$kcid&paperId=$paperId
    const val DO_HOMEWORK = "https://infotech.51taoshi.com/hw/stu/doHomework.do"

    // Use arg: https://infotech.51taoshi.com/hw/stu/viewAnswer.do?paperId=$paperId&rsid=$rsid
    const val VIEW_ANSWER = "https://infotech.51taoshi.com/hw/stu/viewAnswer.do"

    const val SAVE_ANSWER = "https://infotech.51taoshi.com/hw/stu/saveAnswers.do"

    const val CACHE_QUESTION_ANSWER = "https://infotech.51taoshi.com/hw/stu/cacheQuestionAnswer.do"

    const val CHANGE_STUDENT_ID = "https://infotech.51taoshi.com/hw/stu/postMatchKeyNo.do"
}