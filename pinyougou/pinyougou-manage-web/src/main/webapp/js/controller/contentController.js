var app = new Vue({
    el: "#app",
    data: {
        //列表数据
        entityList:[],
        //总记录数
        total: 0,
        //页大小
        pageSize: 10,
        //当前页号
        pageNum: 1,
        //实体
        entity:{pic:"",status:1},
        //选择的id数组
        ids: [],
        //定一个空的搜索条件对象
        searchEntity:{},
        //内容分类列表
        contentCategoryList:[],
        //内容状态
        contentStatus: ["无效","有效"],
        //所有内容分类对象
        allContentCategory:{}
    },
    methods: {
        //获取全部内容分类列表
        findContentCategoryList: function(){
            axios.get("../contentCategory/findAll.do").then(function (response) {
                //新建中的分类列表
                app.contentCategoryList = response.data;
                //查询内容列表
                app.searchList(app.pageNum);
                //列表中的分类显示
                for (var i = 0; i < response.data.length; i++) {
                    var cc = response.data[i];
                    app.allContentCategory[cc.id] = cc.name;
                }

            });
        },
        //上传图片文件
        uploadFile: function () {
            // 声明一个FormData对象
            var formData = new FormData();
            // file 这个名字要和后台获取文件的名字一样; querySelector获取选择器对应的第一个元素
            formData.append('file', document.querySelector('input[type=file]').files[0]);
            //post提交
            axios({
                url: '/upload.do',
                data: formData,
                method: 'post',
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            }).then(function (response) {
                if (response.data.success) {
                    //上传成功
                    app.entity.pic = response.data.message;
                } else {
                    //上传失败
                    alert(response.data.message);
                }
            })

        },
        searchList: function (curPage) {
            this.pageNum = curPage;
            axios.post("../content/search.do?pageNum=" + this.pageNum + "&pageSize=" + this.pageSize, this.searchEntity).then(function (response) {
                //this表示axios；所以使用app设置entityList的数据
                app.entityList = response.data.list;
                app.total = response.data.total;
            });
        },
        //保存数据
        save: function () {
            var method = "add";
            if (this.entity.id != null) {
                //如果id存在则说明是 修改
                method = "update"
            }

            axios.post("../content/"+method+".do", this.entity).then(function (response) {
                if (response.data.success) {
                    //刷新列表
                    app.searchList(1);
                } else {
                    alert(response.data.message);
                }
            });
        },
        //根据主键查询
        findOne: function (id) {
            axios.get("../content/findOne/" + id + ".do").then(function (response) {
                app.entity = response.data;
            });
        },
        //删除；方法名不能直接使用vue关键字delete
        deleteList: function () {
            if (this.ids.length < 1) {
                alert("请选择要删除的记录");
                return;
            }
            if(confirm("确定要删除选中的记录吗？")){
                axios.get("../content/delete.do?ids=" + this.ids).then(function (response) {
                    if(response.data.success){
                        app.searchList(1);
                        app.ids = [];
                    } else {
                        alert(response.data.message);
                    }
                });
            }
        }
    },
    created: function () {
        //加载分类列表
        this.findContentCategoryList();

        //this.searchList(this.pageNum);
    }
});