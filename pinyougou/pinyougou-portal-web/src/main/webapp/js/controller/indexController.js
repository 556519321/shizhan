var app = new Vue({
    el:"#app",
    data:{
        //广告数据
        contentData:{1:[]},
        //搜索关键字
        keywords:""
    },
    methods: {
        //根据内容分类id查询内容列表
        findContentListByCategoryId: function (categoryId) {
            axios.get("../content/findContentListByCategoryId.do?categoryId=" + categoryId).then(function (response) {
                app.contentData[categoryId] = response.data;
            });

        },
        //跳转到搜索系统
        search: function () {
            location.href="http://search.pinyougou.com/search.html?keywords=" + this.keywords;

        }
    },
    created(){
        this.findContentListByCategoryId(1);
    }
});