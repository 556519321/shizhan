var app = new Vue({
    el: "#app",
    data: {
        //列表数据
        entityList: [],
        //总记录数
        total: 0,
        //页大小
        pageSize: 10,
        //当前页号
        pageNum: 1,
        //实体
        entity: {},
        //选择的id数组
        ids: [],
        //定一个空的搜索条件对象
        searchEntity: {},
        //商家状态
        sellerStatus: {0: "待审核", 1: "已审核", 2: "审核未通过", 3: "关闭"}
    },
    methods: {
        //更新商家状态
        updateStatus: function (sellerId, status) {
            axios.post("../seller/update.do", {"sellerId": sellerId, "status": status}).then(function (response) {
                if (response.data.success) {
                    app.searchList(app.pageNum);
                } else {
                    alert(response.data.message);
                }
            });
        },
        searchList: function (curPage) {
            this.pageNum = curPage;
            axios.post("../seller/search.do?pageNum=" + this.pageNum + "&pageSize=" + this.pageSize, this.searchEntity).then(function (response) {
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

            axios.post("../seller/" + method + ".do", this.entity).then(function (response) {
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
            axios.get("../seller/findOne/" + id + ".do").then(function (response) {
                app.entity = response.data;
            });
        },
        //删除；方法名不能直接使用vue关键字delete
        deleteList: function () {
            if (this.ids.length < 1) {
                alert("请选择要删除的记录");
                return;
            }
            if (confirm("确定要删除选中的记录吗？")) {
                axios.get("../seller/delete.do?ids=" + this.ids).then(function (response) {
                    if (response.data.success) {
                        app.searchList(1);
                        app.ids = [];
                    } else {
                        alert(response.data.message);
                    }
                });
            }
        }
    },
    computed: {
        // 商家审核页面需要先设置只查询 未审核的数据
        loadStatusData: function () {
            this.searchEntity.status = 0;
            return 0;
        }
    },
    created: function () {
        this.searchList(this.pageNum);
    }
});