package com.knightboost.appoptimizeframework.gsonopttest

import android.os.SystemClock
import android.util.Log
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.knightboost.appoptimizeframework.gsonopttest.adapters.*

object GsonTest {


    val gson = GsonBuilder().create()
    //提前配置Type
    val gson2 = GsonBuilder().registerTypeAdapterFactory(object : TypeAdapterFactory {
            override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
                val rawType = type.rawType
                if (First::class.java.isAssignableFrom(rawType)) {
                    return FirstAdapter(gson) as TypeAdapter<T>
                } else if (Second::class.java.isAssignableFrom(rawType)) {
                    return SecondTypeAdapter(gson) as TypeAdapter<T>
                } else if (SecondTabImageModel::class.java.isAssignableFrom(rawType)) {
                    return SecondTabImageModelAdapter(gson) as TypeAdapter<T>
                } else if (TabGroupModel::class.java.isAssignableFrom(rawType)) {
                    return TabGroupModelAdapter(gson) as TypeAdapter<T>
                } else if (RecommendTabInfo::class.java.isAssignableFrom(rawType)) {
                    return RecommendTabInfoAdapter(gson) as TypeAdapter<T>
                }
                return null
            }
        }).create()


    /**
     * 未配置自定义Adapter
     */
    fun test() {
        //提前配置Type
        val begin = SystemClock.elapsedRealtimeNanos()
        val v = gson.fromJson<RecommendTabInfo>(text, RecommendTabInfo::class.java)
        val end = SystemClock.elapsedRealtimeNanos()
        Log.e("gsonTest", "gson解析耗时 ${(end - begin) / 1000} ns")
    }

    /**
     * 配置了TypeAdapter
     */
    fun testWithCustomTypeAdapter() {

        val begin = SystemClock.elapsedRealtimeNanos()
        val v = gson2.fromJson<RecommendTabInfo>(text, RecommendTabInfo::class.java)
        val end = SystemClock.elapsedRealtimeNanos()
        Log.e("gsonTest", "优化后 gson解析耗时 ${(end - begin) / 1000} ns")
    }


    val text = """
       {
	"first": [],
	"landingChannel": "",
	"requestTs": 1683277732896,
	"second": [{
			"cId": "200100",
			"cType": 0,
			"contentShowDetails": "",
			"contentShowType": 4,
			"imageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"lightImageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"name": "关注",
			"showType": 0
		},
		{
			"cId": "tab_three_column",
			"cType": 0,
			"contentShowDetails": "",
			"contentShowType": 0,
			"imageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"lightImageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"name": "广场",
			"showType": 0
		},
		{
			"cId": "200000",
			"cType": 0,
			"contentShowDetails": "",
			"contentShowType": 0,
			"imageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"lightImageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"name": "推荐",
			"showType": 0
		}, {
			"cId": "XG80LbXeOi",
			"cType": 2,
			"contentShowDetails": "",
			"contentShowType": 1,
			"imageNameUrls": {
				"chosen": "",
				"height": 64,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 131
			},
			"lightImageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"name": "运营频道+2023-05-04 18:59:59",
			"showType": 1
		}, {
			"cId": "tab_video_new",
			"cType": 0,
			"contentShowDetails": "",
			"contentShowType": 0,
			"imageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"lightImageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"name": "视频",
			"showType": 0
		}, {
			"cId": "206000",
			"cType": 0,
			"contentShowDetails": "",
			"contentShowType": 5,
			"imageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"lightImageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"name": "直播",
			"showType": 0
		}, {
			"cId": "C009Streetart",
			"cType": 1,
			"contentShowDetails": "",
			"contentShowType": 0,
			"imageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"lightImageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"name": "潮流文化",
			"showType": 0
		}, {
			"cId": "C010Digital",
			"cType": 1,
			"contentShowDetails": "",
			"contentShowType": 0,
			"imageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"lightImageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"name": "数码",
			"showType": 0
		}, {
			"cId": "C007Watch",
			"cType": 1,
			"contentShowDetails": "",
			"contentShowType": 0,
			"imageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"lightImageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"name": "手表",
			"showType": 0
		}, {
			"cId": "C001Shoes",
			"cType": 1,
			"contentShowDetails": "",
			"contentShowType": 0,
			"imageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"lightImageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"name": "潮鞋",
			"showType": 0
		}, {
			"cId": "C008Toy",
			"cType": 1,
			"contentShowDetails": "",
			"contentShowType": 0,
			"imageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"lightImageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"name": "潮玩",
			"showType": 0
		}, {
			"cId": "C004Skateboard",
			"cType": 1,
			"contentShowDetails": "",
			"contentShowType": 0,
			"imageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"lightImageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"name": "滑板",
			"showType": 0
		}, {
			"cId": "29qd0VmOQ3",
			"cType": 4,
			"contentShowDetails": "",
			"contentShowType": 3,
			"imageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"lightImageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"name": "专栏",
			"showType": 0
		}, {
			"cId": "C005Makeups",
			"cType": 1,
			"contentShowDetails": "",
			"contentShowType": 0,
			"imageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"lightImageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"name": "美妆",
			"showType": 0
		}, {
			"cId": "C002Outfit",
			"cType": 1,
			"contentShowDetails": "",
			"contentShowType": 0,
			"imageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"lightImageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"name": "穿搭",
			"showType": 0
		}, {
			"cId": "C003Sports",
			"cType": 1,
			"contentShowDetails": "",
			"contentShowType": 0,
			"imageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"lightImageNameUrls": {
				"chosen": "",
				"height": 0,
				"notChosen": "",
				"notChosenLucency": "",
				"webpUrl": "",
				"width": 0
			},
			"name": "运动健身",
			"showType": 0
		}
	],
	"secondMore": [],
	"secondMoreV2": [{
		"cId": "200100",
		"cType": 0,
		"contentShowDetails": "",
		"contentShowType": 4,
		"imageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"lightImageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"name": "关注",
		"showType": 0
	}, {
		"cId": "tab_three_column",
		"cType": 0,
		"contentShowDetails": "",
		"contentShowType": 0,
		"imageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"lightImageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"name": "广场",
		"showType": 0
	}, {
		"cId": "200000",
		"cType": 0,
		"contentShowDetails": "",
		"contentShowType": 0,
		"imageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"lightImageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"name": "推荐",
		"showType": 0
	}, {
		"cId": "tab_video_new",
		"cType": 0,
		"contentShowDetails": "",
		"contentShowType": 0,
		"imageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"lightImageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"name": "视频",
		"showType": 0
	}, {
		"cId": "206000",
		"cType": 0,
		"contentShowDetails": "",
		"contentShowType": 5,
		"imageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"lightImageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"name": "直播",
		"showType": 0
	}, {
		"cId": "C009Streetart",
		"cType": 1,
		"contentShowDetails": "",
		"contentShowType": 0,
		"imageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"lightImageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"name": "潮流文化",
		"showType": 0
	}, {
		"cId": "C010Digital",
		"cType": 1,
		"contentShowDetails": "",
		"contentShowType": 0,
		"imageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"lightImageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"name": "数码",
		"showType": 0
	}, {
		"cId": "C007Watch",
		"cType": 1,
		"contentShowDetails": "",
		"contentShowType": 0,
		"imageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"lightImageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"name": "手表",
		"showType": 0
	}, {
		"cId": "C001Shoes",
		"cType": 1,
		"contentShowDetails": "",
		"contentShowType": 0,
		"imageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"lightImageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"name": "潮鞋",
		"showType": 0
	}, {
		"cId": "C008Toy",
		"cType": 1,
		"contentShowDetails": "",
		"contentShowType": 0,
		"imageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"lightImageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"name": "潮玩",
		"showType": 0
	}, {
		"cId": "C004Skateboard",
		"cType": 1,
		"contentShowDetails": "",
		"contentShowType": 0,
		"imageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"lightImageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"name": "滑板",
		"showType": 0
	}, {
		"cId": "29qd0VmOQ3",
		"cType": 4,
		"contentShowDetails": "",
		"contentShowType": 3,
		"imageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"lightImageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"name": "专栏",
		"showType": 0
	}, {
		"cId": "C005Makeups",
		"cType": 1,
		"contentShowDetails": "",
		"contentShowType": 0,
		"imageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"lightImageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"name": "美妆",
		"showType": 0
	}, {
		"cId": "C002Outfit",
		"cType": 1,
		"contentShowDetails": "",
		"contentShowType": 0,
		"imageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"lightImageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"name": "穿搭",
		"showType": 0
	}, {
		"cId": "C003Sports",
		"cType": 1,
		"contentShowDetails": "",
		"contentShowType": 0,
		"imageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"lightImageNameUrls": {
			"chosen": "",
			"height": 0,
			"notChosen": "",
			"notChosenLucency": "",
			"webpUrl": "",
			"width": 0
		},
		"name": "运动健身",
		"showType": 0
	}],
	"undertakeCallback": 1}
""".trimIndent()

}