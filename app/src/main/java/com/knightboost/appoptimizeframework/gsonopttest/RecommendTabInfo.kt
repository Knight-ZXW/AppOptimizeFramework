package com.knightboost.appoptimizeframework.gsonopttest

import android.os.Parcelable
import com.knightboost.gsonopt.AutoGsonAdapter

data class RecommendTabInfo(
    var second: ArrayList<Second>? = ArrayList(),
    var first: ArrayList<First>? = ArrayList(),
    var secondMore: ArrayList<TabGroupModel>? = ArrayList(),
    var secondMoreV2: ArrayList<Second>? = arrayListOf(),
    var landingChannel: String? = "",
    var undertakeCallback: Int = 0, // 是否需要回调增长（0=不需要，1=需要）
    var requestTs: Long = 0, //收到请求时的时间戳
    var landingXlabVideo: Int? = null//  是否路由到沉浸式视频tab  0-否 1-是
)  {

    fun getSafeSecond(): ArrayList<Second> {
        second?.let {
            return it
        }
        val tempList = ArrayList<Second>()
        second = tempList
        return tempList
    }

    fun getSafeFirst(): ArrayList<First> {
        first?.let {
            return it
        }
        val tempList = ArrayList<First>()
        first = tempList
        return tempList
    }

    fun getLandingVideo(): Int {
        if (landingXlabVideo == null) {
            return 1
        }
        return landingXlabVideo?:0
    }
}

fun ArrayList<Second>?.replaceTabName(cid: String, tabName: String): Boolean {
    this ?: return false
    firstOrNull { it.cId == cid }?.run {
        if (name == tabName) return false
        name = tabName
        return true
    }
    return false
}

data class Second(
    var cId: String? = "",
    var name: String? = "",
    var cType: Int = 0, // 1-普通频道，2-运营频道，3-鞋评, 4-资讯
    var showType: Int = 0, // 0文字 1图片
    var imageNameUrls: SecondTabImageModel? = SecondTabImageModel(), // 深色风格图（默认）
    var lightImageNameUrls: SecondTabImageModel? = SecondTabImageModel(), // 浅色风格图
    var contentShowType: Int = 0, //0-feed流，1-h5, 老版本不下发，2："物评", 3-资讯，4："关注"，5："直播"
    var contentShowDetails: String? = "" //h5 url
)  {
    companion object {
        const val CONTENT_SHOW_TYPE_FEED = 0
        const val CONTENT_SHOW_TYPE_H5 = 1
        const val CONTENT_SHOW_TYPE_NEWS = 3
        const val CONTENT_SHOW_TYPE_ATTENTION = 4
        const val CONTENT_SHOW_TYPE_LIVE = 5
        const val SHOW_TYPE_TEXT = 0
        const val SHOW_TYPE_IMAGE = 1
    }

    fun getSafeSecondTabImageModel(): SecondTabImageModel {
        imageNameUrls?.let {
            return it
        }
        return SecondTabImageModel()
    }

    fun getSafeSecondTabImageModelLight(): SecondTabImageModel {
        lightImageNameUrls?.let {
            return it
        }
        return SecondTabImageModel()
    }
}

/**
 * 频道二级tab图片样式model
 * @param notChosen 未选中运营位icon
 * @param notChosen 选中运营位icon
 * @param notChosenLucency 未选中透明运营位icon
 * @param height icon高度，可能为0
 * @param width icon宽度，可能为0
 * @param webpUrl 选中运营位动图
 */
@AutoGsonAdapter
data class SecondTabImageModel(
    var notChosen: String? = "",
    var chosen: String? = "",
    var notChosenLucency: String? = "",
    var height: Int = 0,
    var width: Int = 0,
    var webpUrl: String? = "" //冷启动播放一次的动图
)  {
    fun getSafeChosen(): String {
        return chosen.orEmpty()
    }

    fun getSafeNotChosen(): String {
        return notChosen.orEmpty()
    }

    fun getSafeNotChosenLucency(): String {
        return notChosenLucency.orEmpty()
    }
}

data class First(
    var name: String? = "",
    var fillPoint: String? = ""
)

/**
 * 频道二级直达区
 */
data class TabGroupModel(
    var group: String? = "",
    var icon: String? = "",
    var categories: ArrayList<Second>? = ArrayList()
)
