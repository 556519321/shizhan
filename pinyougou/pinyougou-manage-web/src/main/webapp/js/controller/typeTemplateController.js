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
        entity:{customAttributeItems:[]},
        //选择的id数组
        ids: [],
        //定一个空的搜索条件对象
        searchEntity:{},
        //品牌下拉列表数据
        brandList: [],
        //规格下拉列表数据
        specificationList: []
    },
    methods: {
        //获取格式化的品牌列表；格式为：[{id:'1',text:'联想'},{id:'2',text:'华为'}]
        findBrandList: function(){
            axios.get("../brand/selectOptionList.do").then(function (response) {
                app.brandList = response.data;
            });
        },
        //获取格式化的规格列表；格式为：[{id:'1',text:'屏幕尺寸'},{id:'2',text:'机身大小'}]
        findSpecificationList: function(){
            axios.get("../specification/selectOptionList.do").then(function (response) {
                app.specificationList = response.data;
            });
        },
        //删除属性
        deleteTableRow: function(index){
            //删除的元素索引号，要删除的元素个数
            this.entity.customAttributeItems.splice(index, 1);
        },
        //添加属性
        addTableRow: function(){
            this.entity.customAttributeItems.push({});
        },
        searchList: function (curPage) {
            this.pageNum = curPage;
            axios.post("../typeTemplate/search.do?pageNum=" + this.pageNum + "&pageSize=" + this.pageSize, this.searchEntity).then(function (response) {
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

            axios.post("../typeTemplate/"+method+".do", this.entity).then(function (response) {
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
            axios.get("../typeTemplate/findOne/" + id + ".do").then(function (response) {
                app.entity = response.data;
                //字符串类型内容转换为json对象，vue才能对对象进行处理
                app.entity.brandIds = JSON.parse(app.entity.brandIds);
                app.entity.specIds = JSON.parse(app.entity.specIds);
                app.entity.customAttributeItems = JSON.parse(app.entity.customAttributeItems);
            });
        },
        //删除；方法名不能直接使用vue关键字delete
        deleteList: function () {
            if (this.ids.length < 1) {
                alert("请选择要删除的记录");
                return;
            }
            if(confirm("确定要删除选中的记录吗？")){
                axios.get("../typeTemplate/delete.do?ids=" + this.ids).then(function (response) {
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
        this.searchList(this.pageNum);

        //加载品牌列表；格式为：[{id:'1',text:'联想'},{id:'2',text:'华为'}]
        this.findBrandList();
        //加载规格列表；格式为：[{id:'1',text:'屏幕尺寸'},{id:'2',text:'机身大小'}]
        this.findSpecificationList();
    }
});