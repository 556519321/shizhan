package com.pinyougou.pojo;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Table(name = "tb_item")
/**
 * 索引名：pyg-item
 * 索引类型：在es 6.*+版本，类型将慢慢过时，推荐使用_doc
 * 分2片，开发就不需要备份分片了
 */
@Document(indexName = "pyg-item", type = "_doc", shards = 2, replicas = 0)
public class TbItem implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @org.springframework.data.annotation.Id
    @Field(store = true, type = FieldType.Long)
    private Long id;

    /**
     * 存储
     * 创建索引的时候使用最细粒度分词
     * 搜索使用智能最少分词
     * text类型 会进行分词
     * copyTo 复制域，将内容复制到keywords（任意起名字）域中
     */
    @Field(store = true, analyzer = "ik_max_word", searchAnalyzer = "ik_smart",
            type = FieldType.Text, copyTo = "keywords")
    private String title;

    @Field(store = true, type = FieldType.Double)
    private Double price;

    /**
     * keyword类型不分词（相当于string，string在5.x+过时了）
     */
    @Field(index = false, store = true, type = FieldType.Keyword)
    private String image;

    @Field(store = true, type = FieldType.Long)
    private Long goodsId;

    @Field(type = FieldType.Date)
    private Date updateTime;

    @Field(store = true, type = FieldType.Keyword, copyTo = "keywords")
    private String category;

    @Field(store = true, type = FieldType.Keyword, copyTo = "keywords")
    private String brand;

    @Field(store = true, type = FieldType.Keyword, copyTo = "keywords")
    private String seller;

    //嵌套域
    @Field(type = FieldType.Nested)
    private Map<String, String> specMap;

    private String status;

    private String sellPoint;

    private Integer stockCount;

    private Integer num;

    private String barcode;

    private Long categoryid;

    private Date createTime;

    private String itemSn;

    private Double costPirce;

    private Double marketPrice;

    private String isDefault;

    private String sellerId;

    private String cartThumbnail;

    private String spec;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSellPoint() {
        return sellPoint;
    }

    public void setSellPoint(String sellPoint) {
        this.sellPoint = sellPoint;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getStockCount() {
        return stockCount;
    }

    public void setStockCount(Integer stockCount) {
        this.stockCount = stockCount;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Long getCategoryid() {
        return categoryid;
    }

    public void setCategoryid(Long categoryid) {
        this.categoryid = categoryid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getItemSn() {
        return itemSn;
    }

    public void setItemSn(String itemSn) {
        this.itemSn = itemSn;
    }

    public Double getCostPirce() {
        return costPirce;
    }

    public void setCostPirce(Double costPirce) {
        this.costPirce = costPirce;
    }

    public Double getMarketPrice() {
        return marketPrice;
    }

    public void setMarketPrice(Double marketPrice) {
        this.marketPrice = marketPrice;
    }

    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getCartThumbnail() {
        return cartThumbnail;
    }

    public void setCartThumbnail(String cartThumbnail) {
        this.cartThumbnail = cartThumbnail;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public Map<String, String> getSpecMap() {
        return specMap;
    }

    public void setSpecMap(Map<String, String> specMap) {
        this.specMap = specMap;
    }

    @Override
    public String toString() {
        return "TbItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", image='" + image + '\'' +
                ", goodsId=" + goodsId +
                ", updateTime=" + updateTime +
                ", category='" + category + '\'' +
                ", brand='" + brand + '\'' +
                ", seller='" + seller + '\'' +
                ", specMap=" + specMap +
                ", status='" + status + '\'' +
                '}';
    }
}