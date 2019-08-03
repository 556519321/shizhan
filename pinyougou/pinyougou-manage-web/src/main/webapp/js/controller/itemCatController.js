//注册下拉框组件
Vue.component('v-select', VueSelect.VueSelect);
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
        entity:{parentId:0},
        //二级分类
        entity_2:{},
        //三级分类
        entity_3:{},
        //当前级别；默认第一级
        grade: 1,
        //选择的id数组
        ids: [],
        //定一个空的搜索条件对象
        searchEntity:{},
        //分类模版列表
        typeTemplateList:[]
    },
    methods: {
        //选择分类
        selectList: function(grade, itemCat){
            this.grade = grade;

            switch (grade) {
                case 1:
                    this.entity_2 = {};
                    this.entity_3 = {};
                    break;
                case 2:
                    this.entity_2 = itemCat;
                    this.entity_3 = {};
                    break;
                default:
                    this.entity_3 = itemCat;
            }

            //设置父分类id
            this.entity.parentId = itemCat.id;

            //查询当前父分类的子分类列表
            this.findByParentId(this.entity.parentId);
        },
        //获取分类模板列表
        findTypeTemplateList: function(){
            axios.get("../typeTemplate/findAll.do").then(function (response) {
                app.typeTemplateList = response.data;
            });
        },
        //根据父分类获取其子分类
        findByParentId: function(parentId){
            axios.get("../itemCat/findByParentId.do?parentId=" + parentId).then(function (response) {
                app.entityList = response.data;
            });
        },
        searchList: function (curPage) {
            this.pageNum = curPage;
            axios.post("../itemCat/search.do?pageNum=" + this.pageNum + "&pageSize=" + this.pageSize, this.searchEntity).then(function (response) {
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

            axios.post("../itemCat/"+method+".do", this.entity).then(function (response) {
                if (response.data.success) {
                    //刷新列表
                    app.findByParentId(app.entity.parentId);
                } else {
                    alert(response.data.message);
                }
            });
        },
        //根据主键查询
        findOne: function (id) {
            axios.get("../itemCat/findOne/" + id + ".do").then(function (response) {
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
                axios.get("../itemCat/delete.do?ids=" + this.ids).then(function (response) {
                    if(response.data.success){
                        app.findByParentId(app.entity.parentId);
                        app.ids = [];
                    } else {
                        alert(response.data.message);
                    }
                });
            }
        }
    },
    created: function () {
        this.findByParentId(0);
        //获取分类模板列表
        this.findTypeTemplateList();
    }
});