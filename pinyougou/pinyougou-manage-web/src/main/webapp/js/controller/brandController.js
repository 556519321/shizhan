var app = new Vue({
    el: "#app",
    data: {
        //总记录数
        total: 0,
        //当前页号
        pageNum: 1,
        //页大小
        pageSize: 10,
        //列表
        entityList: [],
        //实体
        entity: {},
        //品牌id数组
        ids:[],
        //搜索对象
        searchEntity:{},
        //是否全选被勾选
        checkAll:false
    },
    methods: {
        //全选
        selectAll:function(){
            if(!this.checkAll){
                //选中
                this.ids = [];
                for (let i = 0; i < this.entityList.length; i++) {
                    const entity = this.entityList[i];
                    this.ids.push(entity.id);
                }
            } else {
                this.ids = [];
            }
        },
        //查询
        searchList: function (pageNum) {
            this.pageNum = pageNum;
            /*axios.get("../brand/findPage.do?pageNum=" + this.pageNum + "&pageSize=" + this.pageSize).then(function (response) {
                app.total = response.data.total;
                app.entityList = response.data.list;
            });*/
            axios.post("../brand/search.do?pageNum=" + this.pageNum + "&pageSize=" + this.pageSize, this.searchEntity).then(function (response) {
                app.total = response.data.total;
                app.entityList = response.data.list;
            });
        },
        //删除
        deleteList: function(){
            //1、判断是否有选择
            if(this.ids.length == 0){
                alert("请先选择要删除的记录！");
                return;
            }
            //2、确认是否要删除 confirm点击 确认的时候返回true，否则false
            if (confirm("你确定要删除选择了的记录吗？")) {
                //3、删除
                axios.get("../brand/delete.do?ids=" + this.ids).then(function (response) {
                    if(response.data.success){
                        app.ids = [];
                        app.searchList(1);
                    } else {
                        alert(response.data.message);
                    }
                });
            }
        },
        //根据id查询
        findOne: function(id){
            axios.get("../brand/findOne/"+id+".do").then(function (response) {
                app.entity = response.data;
            });
        },
        //保存
        save: function () {
            var method = "add";
            if(this.entity.id != null){
                method = "update";
            }
            axios.post("../brand/"+method+".do", this.entity).then(function (response) {
                if(response.data.success){
                    //刷新列表
                    app.searchList(app.pageNum);
                } else {
                    alert(response.data.message);
                }
            });
        }
    },
    //监控vue实例中的数据属性的变化
    watch:{
        ids:{
            //开启深度监控
            deep:true,
            handler:function (newValue, oldValue) {

                if (this.ids.length == this.entityList.length) {
                    this.checkAll = true;
                } else{
                    this.checkAll = false;
                }
            }
        }
    },
    created() {
        /*axios.get("../brand/findAll.do").then(function (response) {
            //response里面：data,status,statusText,headss,config
            console.log(response);
            //this表示窗口；只能使用vue实例变量app
            app.entityList = response.data;

        }).catch(function () {
            alert("加载数据失败！")
        });*/
        this.searchList(1);
    }
});
