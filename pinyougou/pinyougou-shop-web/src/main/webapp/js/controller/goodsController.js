var goodsControllerMixin = {
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
        entity: {
            goods: {typeTemplateId: 0, isEnableSpec:1},
            goodsDesc: {itemImages: [], customAttributeItems: [], specificationItems: []}
        },
        //选择的id数组
        ids: [],
        //定一个空的搜索条件对象
        searchEntity: {},
        //图片实体对象
        image_entity: {url: "", color: ""},
        //1级商品分类列表
        itemCatList1: [],
        //2级商品分类列表
        itemCatList2: [],
        //3级商品分类列表
        itemCatList3: [],
        //品牌列表
        brandList: [],
        //规格及选项列表
        specList: [],
        //商品sku列表
        itemList:[],
        //商品状态
        goodsStatusArray: ["未审核","审核中","审核通过","审核未通过","关闭"],
        //所有商品分类
        allItemCatList: {},
        //扩展属性备份
        customAttributeItemsBak: [],
        //分类模版id备份
        typeTemplateIdBak:0
    },
    //监听变量值的变化
    watch: {
        //监听1级分类值的变化，获取2级分类
        "entity.goods.category1Id": function (newValue, oldValue) {
            //加载2级分类
            this.findItemCatListByParentId(newValue, "itemCatList2");
            //清空3级分类
            this.itemCatList3 = [];
            //分类模版的id为0
            this.entity.goods.typeTemplateId = 0;
        },
        //监听2级分类值的变化，获取3级分类
        "entity.goods.category2Id": function (newValue, oldValue) {
            //加载3级分类
            this.findItemCatListByParentId(newValue, "itemCatList3");
            //分类模版的id为0
            this.entity.goods.typeTemplateId = 0;
        },
        //监听3级分类值的变化；获取分类模版
        "entity.goods.category3Id": function (newValue, oldValue) {
            //分类模版的id为0
            axios.get("../itemCat/findOne/" + newValue + ".do").then(function (response) {
                app.entity.goods.typeTemplateId = response.data.typeId;
            });
        },
        //监听分类模版id值的变化；获取品牌、规格及选项
        "entity.goods.typeTemplateId": function (newValue, oldValue) {
            if (newValue > 0) {
                axios.get("../typeTemplate/findOne/" + newValue + ".do").then(function (response) {
                    //设置品牌下拉框列表数据
                    app.brandList = JSON.parse(response.data.brandIds);

                    if(app.typeTemplateIdBak!=0 && newValue==app.typeTemplateIdBak){
                        //如果是修改的时候，那么应该用数据库保存的扩展属性
                        app.entity.goodsDesc.customAttributeItems = app.customAttributeItemsBak;
                    }else{
                        //扩展属性
                        app.entity.goodsDesc.customAttributeItems=JSON.parse( response.data.customAttributeItems );
                    }

                    //加载规格及其选项；格式如：[{"id":1,"specName":"机身内存","options":[{"id":1,"optionName":"16G"},{"id":1,"optionName":"32G"}]},...,{}]
                    axios.get("../typeTemplate/findSpecList.do?id=" + newValue).then(function (response) {
                        app.specList = response.data;
                    });
                });
            } else {
                //清空品牌
                this.brandList = [];
                //清空扩展属性
                this.entity.goodsDesc.customAttributeItems = [];
            }
        }
    },
    methods: {
        //修改商品的状态
        updateStatus: function (status) {
            if(this.ids.length < 1) {
                alert("请先选择商品");
                return;
            }
            if(confirm("确定要更新选中的商品状态吗？")){
                axios.get("../goods/updateStatus.do?status="+status+"&ids="+this.ids).then(function (response) {
                    if(response.data.success) {
                        //刷新列表并清空选中的那些商品
                        app.searchList(app.pageNum);
                        app.ids = [];
                    } else {
                        alert(response.data.message);
                    }
                });
            }
        },
        //查询所有商品分类列表
        findAllItemCatList: function(){
            axios.get("../itemCat/findAll.do").then(function (response) {
                //查询完商品分类之后再加载列表
                app.searchList(1);
                for (var i = 0; i < response.data.length; i++) {
                    var itemCat = response.data[i];
                    app.allItemCatList[itemCat.id] = itemCat.name;
                }
            });
        },
        //选择了具体规格选项后保存到商品描述中
        selectSpec: function (event, specName, optionName) {
            //查看当前的规格是否已经选择过
            var specObj = this.findObjectByKeyAndValue(this.entity.goodsDesc.specificationItems, "attributeName", specName);
            if (specObj != null) {
                if (event.target.checked) {
                    specObj.attributeValue.push(optionName);
                } else {
                    var optIndex = specObj.attributeValue.indexOf(optionName);
                    specObj.attributeValue.splice(optIndex, 1);

                    //如果该规格没有任何选项了，那么也要删除该规格
                    if (specObj.attributeValue.length === 0) {
                        var specIndex = this.entity.goodsDesc.specificationItems.indexOf(specObj);
                        this.entity.goodsDesc.specificationItems.splice(specIndex, 1);
                    }
                }
            } else {
                this.entity.goodsDesc.specificationItems.push({"attributeName": specName, "attributeValue": [optionName]});
            }

            //构建商品sku列表
            this.createItemList();
        },
        //每次点击了规格选项后生成最新的SKU列表
        createItemList: function () {
            //初始化
            this.entity.itemList = [{spec: {}, price: 0, num: 9999, status: "0", isDefault: "0"}];

            for (var i = 0; i < this.entity.goodsDesc.specificationItems.length; i++) {
                var spec = this.entity.goodsDesc.specificationItems[i];
                this.entity.itemList = this.addColumn(this.entity.itemList, spec.attributeName, spec.attributeValue);
            }
        },
        addColumn: function (itemList, specName, specOptions) {
            //最终返回的列表数据，也就是表格的数据
            var newItemList = [];
            for (var i = 0; i < itemList.length; i++) {
                var oldItem = itemList[i];
                for (var j = 0; j < specOptions.length; j++) {
                    var option = specOptions[j];
                    var newItem = JSON.parse(JSON.stringify(oldItem));
                    newItem.spec[specName] = option;

                    newItemList.push(newItem);
                }
            }
            return newItemList;
        },
        //在一个集合中根据某个对象的属性值找该对象并返回
        findObjectByKeyAndValue: function (list, keyName, keyValue) {
            for (var i = 0; i < list.length; i++) {
                if (list[i][keyName] == keyValue) {
                    return list[i];
                }
            }
            return null;
        },
        //判断规格选项是否已经存在选择的里面
        checkAttributeValue: function (specName, optionName) {
            var items = this.entity.goodsDesc.specificationItems;
            var spec = this.findObjectByKeyAndValue(items, "attributeName", specName);
            if (spec != null) {
                if (spec.attributeValue.indexOf(optionName) >= 0) {
                    return true;
                }
            }
            return false;
        },
        //根据父分类获取其子分类；父分类id，第几级的商品分类列表变量名称
        findItemCatListByParentId: function (parentId, itemCatGrade) {
            axios.get("../itemCat/findByParentId.do?parentId=" + parentId).then(function (response) {
                app[itemCatGrade] = response.data;
            });
        },
        //删除商品图片
        delete_image_entity: function (index) {
            this.entity.goodsDesc.itemImages.splice(index, 1);
        },
        //将上传的图片加入到对应商品描述属性
        add_image_entity: function () {
            this.entity.goodsDesc.itemImages.push(this.image_entity);
            this.image_entity = {url: "", color: ""};
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
                    app.image_entity.url = response.data.message;
                } else {
                    //上传失败
                    alert(response.data.message);
                }
            })

        },
        searchList: function (curPage) {
            this.pageNum = curPage;
            axios.post("../goods/search.do?pageNum=" + this.pageNum + "&pageSize=" + this.pageSize, this.searchEntity).then(function (response) {
                //this表示axios；所以使用app设置entityList的数据
                app.entityList = response.data.list;
                app.total = response.data.total;
            });
        },
        //保存数据
        save: function () {
            //设置商品描述信息
            this.entity.goodsDesc.introduction = editor.html();

            var method = "add";
            if (this.entity.goods.id != null) {
                //如果id存在则说明是 修改
                method = "update"
            }

            axios.post("../goods/" + method + ".do", this.entity).then(function (response) {
                if (response.data.success) {
                    alert(response.data.message);
                    editor.html("");
                    //location.href = "goods.html";
                } else {
                    alert(response.data.message);
                }
            });
        },
        //根据主键查询
        findOne: function () {
            //获取地址栏的ID参数
            var id = this.getParameterByName('id');
            if(id != null) {
                //说明是修改 则需要根据id查询数据并回显
                axios.get("../goods/findOne/" + id + ".do").then(function (response) {
                    app.entity = response.data;

                    //向富文本编辑器设置商品内容
                    editor.html(app.entity.goodsDesc.introduction);

                    //备份模板ID和扩展属性值
                    app.typeTemplateIdBak=app.entity.goods.typeTemplateId;
                    app.customAttributeItemsBak=JSON.parse( app.entity.goodsDesc.customAttributeItems );

                    //转换商品图片列表
                    app.entity.goodsDesc.itemImages = JSON.parse(app.entity.goodsDesc.itemImages);
                    //转换商品扩展属性
                    app.entity.goodsDesc.customAttributeItems = JSON.parse(app.entity.goodsDesc.customAttributeItems);
                    //转换商品规格属性
                    app.entity.goodsDesc.specificationItems = JSON.parse(app.entity.goodsDesc.specificationItems);
                    //商品SKU列表中的每一个SKU商品的spec转换为json对象
                    if(app.entity.itemList.length > 0){
                        for (var i = 0; i < app.entity.itemList.length; i++) {
                            app.entity.itemList[i].spec = JSON.parse(app.entity.itemList[i].spec);
                        }
                    }
                });
            }
        },
        //删除；方法名不能直接使用vue关键字delete
        deleteList: function () {
            if (this.ids.length < 1) {
                alert("请选择要删除的记录");
                return;
            }
            if (confirm("确定要删除选中的记录吗？")) {
                axios.get("../goods/delete.do?ids=" + this.ids).then(function (response) {
                    if (response.data.success) {
                        app.searchList(1);
                        app.ids = [];
                    } else {
                        alert(response.data.message);
                    }
                });
            }
        },
        //根据参数名字获取参数
        getParameterByName: function (name) {
            return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.href) || [, ""])[1].replace(/\+/g, '%20')) || null
        }
    },
    created: function () {
        //this.searchList(this.pageNum);
    }
};