var app = new Vue({
    el: "#app",
    data: {
        //当前选择的sku
        sku:{},
        num: 1,
        //记录选择了的规格
        specificationItems: {}
    },
    methods: {
        addNum: function (num) {
            this.num = this.num + num;
            if (this.num < 1) {
                this.num = 1;
            }
        },
        selectSpecification: function (specName, value) {
            this.specificationItems[specName] = value;

            //获取sku
            this.searchSku();
        },
        //判断是否是选中了的规格
        isSelected: function (specName, value) {
            if(this.specificationItems[specName] == value) {
                return true;
            }
            return false;
        },
        //加载默认sku
        loadSku: function () {
            this.sku = skuList[0];//默认第一个SKU商品

            //设置当前选择的规格
            this.specificationItems = JSON.parse(JSON.stringify(this.sku.spec));
        },
        //比较两个对象是否一致
        matchObject: function (map1, map2) {
            for (var j in map1) {
                if (map1[j] != map2[j]) {
                    return false;
                }
            }
            for (var k in map2) {
                if (map1[k] != map2[k]) {
                    return false;
                }
            }
            return true;
        },
        searchSku: function () {
            for (var i = 0; i < skuList.length; i++) {
                var obj = skuList[i];
                if (this.matchObject(this.specificationItems, skuList[i].spec)) {
                    this.sku = skuList[i];
                    return;
                }
            }

            //如果没有找到匹配的，则默认如下：
            this.sku = {"id": 0, "title": "-----", "price": 0};
        },
        //添加到购物车
        addToCart: function () {
            axios.get("http://cart.pinyougou.com/cart/addItemToCartList.do?itemId="
                + this.sku.id + "&num=" + this.num, {"withCredentials": true})
                .then(function (response) {
                    if (response.data.success) {
                        location.href = "http://cart.pinyougou.com";
                    } else {
                        alert(response.data.message);
                    }
                });
        }
    },
    created() {
        //加载默认选中的sku
        this.loadSku();
    }
});