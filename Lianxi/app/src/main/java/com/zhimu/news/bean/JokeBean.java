package com.zhimu.news.bean;

import java.util.List;

/**
 * 笑话实体类
 * Created by Administrator on 2016.3.10.
 */
public class JokeBean {


    /**
     * showapi_res_code : 0
     * showapi_res_error :
     * showapi_res_body : {
     *                      "allNum":23660,
     *                      "allPages":1183,
     *                      "contentlist":[],
     *                      "currentPage":1,
     *                      "maxResult":20,
     *                      "ret_code":0
     *                    }
     */

    private int showapi_res_code;
    private String showapi_res_error;
    private ShowapiResBody showapi_res_body;

    public void setShowapi_res_code(int showapi_res_code) {
        this.showapi_res_code = showapi_res_code;
    }

    public void setShowapi_res_error(String showapi_res_error) {
        this.showapi_res_error = showapi_res_error;
    }

    public void setShowapi_res_body(ShowapiResBody showapi_res_body) {
        this.showapi_res_body = showapi_res_body;
    }

    public int getShowapi_res_code() {
        return showapi_res_code;
    }

    public String getShowapi_res_error() {
        return showapi_res_error;
    }

    public ShowapiResBody getShowapi_res_body() {
        return showapi_res_body;
    }

    @Override
    public String toString() {
        return "JokeBean{" +
                "showapi_res_code=" + showapi_res_code +
                ", showapi_res_error='" + showapi_res_error + '\'' +
                ", showapi_res_body=" + showapi_res_body +
                '}';
    }

    /**
     * 所有参数
     *
     */
    public static class ShowapiResBody {
        private int allNum;
        private int allPages;
        private int currentPage;
        private int maxResult;
        private int ret_code;

        private List<Contentlist> contentlist;

        public void setAllNum(int allNum) {
            this.allNum = allNum;
        }

        public void setAllPages(int allPages) {
            this.allPages = allPages;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public void setMaxResult(int maxResult) {
            this.maxResult = maxResult;
        }

        public void setRet_code(int ret_code) {
            this.ret_code = ret_code;
        }

        public void setContentlist(List<Contentlist> contentlist) {
            this.contentlist = contentlist;
        }

        public int getAllNum() {
            return allNum;
        }

        public int getAllPages() {
            return allPages;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public int getMaxResult() {
            return maxResult;
        }

        public int getRet_code() {
            return ret_code;
        }

        public List<Contentlist> getContentlist() {
            return contentlist;
        }

        @Override
        public String toString() {
            return "ShowapiResBody{" +
                    "allNum=" + allNum +
                    ", allPages=" + allPages +
                    ", currentPage=" + currentPage +
                    ", maxResult=" + maxResult +
                    ", ret_code=" + ret_code +
                    ", contentlist=" + contentlist +
                    '}';
        }

        /**
         * 内容实体
         */
        public static class Contentlist {

            /**
             * ct : 时间：2016-03-10 16:32:25.714
             * img : 图片链接地址
             * text:文本笑话内容，图片笑话则没有
             * title : 标题
             * type : 2
             *          图片笑话是2，文本笑话是1
             */
            private String ct;
            private String img;
            private String title;
            private String text;
            private int type;

            public void setCt(String ct) {
                this.ct = ct;
            }

            public void setImg(String img) {
                this.img = img;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public void setType(int type) {
                this.type = type;
            }

            public String getCt() {
                return ct;
            }

            public String getImg() {
                return img;
            }

            public String getTitle() {
                return title;
            }

            public int getType() {
                return type;
            }

            public String getText() {
                return text;
            }

            public void setText(String text) {
                this.text = text;
            }

            @Override
            public String toString() {
                return "Contentlist{" +
                        "ct='" + ct + '\'' +
                        ", img='" + img + '\'' +
                        ", title='" + title + '\'' +
                        ", text='" + text + '\'' +
                        ", type=" + type +
                        '}';
            }
        }
    }
}
