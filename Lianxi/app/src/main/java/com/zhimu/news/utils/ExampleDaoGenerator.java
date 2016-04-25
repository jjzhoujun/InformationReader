package com.zhimu.news.utils;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

/**
 * 自动创建实体类：数据库中存储对象
 * 开源库greenDao
 *
 */
public class ExampleDaoGenerator {

    public static void main(String[] args) throws Exception {

        // target package for dao files
        // 第一个参数是数据库版本号，第二个参数是包的根目录的包
        Schema schema = new Schema(1, "com.zhimu.news.dao");

        addNewsCollect(schema);
        addNewsCollectImgUrl(schema);

        //'..'代表工程前一个目录,接着是工程名/app(AndroidStudio生成)/
        new DaoGenerator().generateAll(schema,
                "../Lianxi/app/src/main/java/com/zhimu/news/");
    }

    /**
     * 指定实体类：NewsCollect（新闻相关）
     * @param schema 实体类
     */
    private static void addNewsCollect(Schema schema) {

        Entity newsCollect = schema.addEntity("NewsCollect");
        // 0: id + 1: 标题 + 2: 来源 + 3: 描述 +  4: 内容 + 5: 收藏日期 + 6：网页链接 + 7: 是否有图片链接; 总共8个
        // 添加id属性
        newsCollect.addIdProperty();
        // 添加列title,指定非空
        newsCollect.addStringProperty("title").notNull();
        // 添加列source
        newsCollect.addStringProperty("source");
        // 添加列desc
        newsCollect.addStringProperty("desc");
        // 添加列html
        newsCollect.addStringProperty("html");
        // 添加列pubDate
        newsCollect.addStringProperty("pubDate");
        // 添加列link
        newsCollect.addStringProperty("link");
        // 添加列isImgUrl
        newsCollect.addBooleanProperty("isImgUrl");

    }

    /**
     * NewsCollectImgUrl图片链接地址实体类
     * @param schema 实体
     */
    private static void addNewsCollectImgUrl(Schema schema) {
        Entity newsCollectImgUrl = schema.addEntity("NewsCollectImgUrl");
        // 添加id属性
        newsCollectImgUrl.addIdProperty();
        // 添加列title,指定非空
        newsCollectImgUrl.addStringProperty("title").notNull();
        // 添加列imgUrl
        newsCollectImgUrl.addStringProperty("imgUrl");
    }

}
