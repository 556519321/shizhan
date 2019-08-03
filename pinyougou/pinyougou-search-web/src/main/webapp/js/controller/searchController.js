var app = new Vue({
    el: "#app",
    data: {
        //搜索条件对象
        searchMap: {"keywords": "", "brand": "", "category": "", "spec":{}, "price":"", "pageNo":1, "pageSize":20, "sortField":"","sort":""},
        //返回结果
        resultMap: {itemList: []},
        //在导航条中要显示的页号数组
        pageNoList:[],
        //分页导航条前面3个点
        frontDot:false,
        //分页导航条后面3个点
        backDot:false
    },
    methods: {
        //排序
        sortSearch:function(sortField, sort){
          this.searchMap.sortField = sortField;
          this.searchMap.sort = sort;

          this.searchMap.pageNo = 1;
          this.search();
        },
        //删除过滤条件
        removeSearchItem: function (key) {
            if ("brand"==key || "category"==key|| "price"==key) {
                this.searchMap[key] = "";
            } else {
                //删除一个对象中的属性
                this.$set(this.searchMap.spec, key, null);
                delete this.searchMap.spec[key];
            }
            //查询条件的改变需要重置页号
            this.searchMap.pageNo = 1;

            //查询
            this.search();
        },
        //添加过滤条件
        addSearchItem: function (key, value) {
            if ("brand"==key || "category"==key || "price"==key) {
                this.searchMap[key] = value;
            } else {
                //this.searchMap.spec[key] = value;
                //参数1：要设置值的对象， 参数2：对象属性，参数3：对象属性值
                this.$set(this.searchMap.spec, key, value);
            }

            //查询条件的改变需要重置页号
            this.searchMap.pageNo = 1;

            //查询
            this.search();
        },
        //查询
        search: function () {
            axios.post("../itemSearch/search.do", this.searchMap).then(function (response) {
                app.resultMap = response.data;

                //构造分页导航条
                app.buildPagination();
            });
        },
        //构造分页导航条
        buildPagination: function () {
            this.pageNoList = [];
            //起始页号
            var startPageNo = 1;
            //结束页号
            var endPageNo = this.resultMap.totalPages;

            //在导航条中要显示的页号总数
            var showPageNoTotal = 5;

            if (this.resultMap.totalPages > 5){
                //当前页左右两边的间隔数
                var interval = Math.floor(showPageNoTotal/2);

                startPageNo = this.searchMap.pageNo - interval;
                endPageNo = this.searchMap.pageNo + interval;

                if (startPageNo > 0) {
                    if(endPageNo > this.resultMap.totalPages){
                        endPageNo = this.resultMap.totalPages;
                        startPageNo = endPageNo - showPageNoTotal +1;
                    }
                } else {
                    startPageNo = 1;
                    endPageNo = showPageNoTotal;
                }
            }

            this.frontDot = false;
            this.backDot = false;
            if (startPageNo > 1) {
                this.frontDot = true;
            }
            if (endPageNo < this.resultMap.totalPages) {
                this.backDot =true;
            }


            for (var i = startPageNo; i <= endPageNo; i++) {
                this.pageNoList.push(i);
            }

        },
        //根据页号查询
        queryByPageNo:function (pageNo) {
            if (1 <= pageNo && pageNo <= this.resultMap.totalPages) {
                this.searchMap.pageNo = pageNo;
                this.search();
            }
        },
        //根据参数名字获取参数
        getParameterByName: function (name) {
            return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.href) || [, ""])[1].replace(/\+/g, '%20')) || null
        }
    },
    created: function () {
        //获取地址栏中的搜索关键字
        var keywords = this.getParameterByName("keywords");
        if (keywords != null && keywords != "") {
            this.searchMap.keywords = keywords;
        }
        this.search();

    }
});