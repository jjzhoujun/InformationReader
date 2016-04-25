package com.zhimu.news.bean;

import java.io.Serializable;
import java.util.List;

/**
 * javabean数据：新闻
 * 2016-02-23
 */
public class NewsBean {

    // api返回码:0表示成功
    private int showapi_res_code;
    // 错误信息
    private String showapi_res_error;
    // 正文信息
    private Body showapi_res_body;

    public int getShowapi_res_code() {
        return showapi_res_code;
    }

    public void setShowapi_res_code(int showapi_res_code) {
        this.showapi_res_code = showapi_res_code;
    }

    public String getShowapi_res_error() {
        return showapi_res_error;
    }

    public void setShowapi_res_error(String showapi_res_error) {
        this.showapi_res_error = showapi_res_error;
    }

    public Body getShowapi_res_body() {
        return showapi_res_body;
    }

    public void setShowapi_res_body(Body showapi_res_body) {
        this.showapi_res_body = showapi_res_body;
    }

    @Override
    public String toString() {
        return "NewsBean{" +
                "showapi_res_code=" + showapi_res_code +
                ", showapi_res_error='" + showapi_res_error + '\'' +
                ", showapi_res_body=" + showapi_res_body +
                '}';
    }

    /**
     * 正文
     */
    public static class Body {

        private PageBean pagebean;
        private int ret_code;

        public PageBean getPagebean() {
            return pagebean;
        }

        public void setPagebean(PageBean pagebean) {
            this.pagebean = pagebean;
        }

        public int getRet_code() {
            return ret_code;
        }

        public void setRet_code(int ret_code) {
            this.ret_code = ret_code;
        }

        @Override
        public String toString() {
            return "Body{" +
                    "pagebean=" + pagebean +
                    ", ret_code=" + ret_code +
                    '}';
        }
    }

    /**
     * 新闻详情
     */
    public static class PageBean {

        // 所有数量
        private int allNum;
        // 所有页数
        private int allPage;
        // 内容列表
        private List<ContentList> contentlist;
        // 当前页
        private int currentPage;
        // 最大数量
        private int maxResult;

        public int getAllNum() {
            return allNum;
        }

        public void setAllNum(int allNum) {
            this.allNum = allNum;
        }

        public int getAllPage() {
            return allPage;
        }

        public void setAllPage(int allPage) {
            this.allPage = allPage;
        }

        public List<ContentList> getContentlist() {
            return contentlist;
        }

        public void setContentlist(List<ContentList> contentlist) {
            this.contentlist = contentlist;
        }

        public int getMaxResult() {
            return maxResult;
        }

        public void setMaxResult(int maxResult) {
            this.maxResult = maxResult;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        @Override
        public String toString() {
            return "PageBean{" +
                    "allNum=" + allNum +
                    ", allPage=" + allPage +
                    ", contentlist=" + contentlist +
                    ", currentPage=" + currentPage +
                    ", maxResult=" + maxResult +
                    '}';
        }
    }

    /**
     * 新闻内容列表
     */
    public static class ContentList {
        // 新闻频道id
        private String channelId;
        // 新闻频道名称
        private String channelName;
        // 新闻描述
        private String desc;
        // 图片链接地址
        private List<ImageUrl> imageurls;
        // 网页地址
        private String link;
        // 日期
        private String pubDate;
        // 来源
        private String source;
        // 标题
        private String title;
        // 内容txt格式
        private String content;
        // 内容html格式
        private String html;

        public String getHtml() {
            return html;
        }

        public void setHtml(String html) {
            this.html = html;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getPubDate() {
            return pubDate;
        }

        public void setPubDate(String pubDate) {
            this.pubDate = pubDate;
        }

        public List<ImageUrl> getImageurls() {
            return imageurls;
        }

        public void setImageurls(List<ImageUrl> imageurls) {
            this.imageurls = imageurls;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getChannelName() {
            return channelName;
        }

        public void setChannelName(String channelName) {
            this.channelName = channelName;
        }

        public String getChannelId() {
            return channelId;
        }

        public void setChannelId(String channelId) {
            this.channelId = channelId;
        }

        @Override
        public String toString() {
            return "ContentList{" +
                    "channelId='" + channelId + '\'' +
                    ", channelName='" + channelName + '\'' +
                    ", desc='" + desc + '\'' +
                    ", imageurls=" + imageurls +
                    ", link='" + link + '\'' +
                    ", pubDate='" + pubDate + '\'' +
                    ", source='" + source + '\'' +
                    ", title='" + title + '\'' +
                    ", content='" + content + '\'' +
                    ", html='" + html + '\'' +
                    '}';
        }
    }

    /**
     * 图片
     */
    public static class ImageUrl implements Serializable {
        private int height;
        private int width;
        private String url;

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public String toString() {
            return "ImageUrl{" +
                    "height=" + height +
                    ", width=" + width +
                    ", url='" + url + '\'' +
                    '}';
        }

    }

}
