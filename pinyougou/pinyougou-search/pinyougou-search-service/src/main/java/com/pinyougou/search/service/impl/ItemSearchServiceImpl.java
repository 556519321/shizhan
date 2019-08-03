package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.dao.ItemDao;
import com.pinyougou.search.service.ItemSearchService;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private ElasticsearchTemplate esTemplate;

    @Override
    public Map<String, Object> search(Map<String, Object> searchMap) {
        Map<String, Object> resultMap = new HashMap<>();

        //创建查询构建对象
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();

        //在没有查询条件的时候查询全部数据
        builder.withQuery(QueryBuilders.matchAllQuery());

        //是否高亮
        boolean highlight = false;

        if (searchMap != null) {
            //根据搜索关键字查询
            String keywords = searchMap.get("keywords")+"";
            if (StringUtils.isNotBlank(keywords)) {
                //参数1：查询关键字，参数2：查询的域；分词之后的关系是 并列
                builder.withQuery(QueryBuilders.multiMatchQuery(keywords, "title", "brand", "seller", "category").operator(Operator.AND));

                //设置高亮信息
                highlight = true;
                HighlightBuilder.Field highlightField = new HighlightBuilder.Field("title")
                        .preTags("<span style='color:red'>")
                        .postTags("</span>");

                builder.withHighlightFields(highlightField);
            }

            //设置过滤查询条件（创建组合查询）
            BoolQueryBuilder booleanQuery = QueryBuilders.boolQuery();

            //分类过滤查询
            String category = searchMap.get("category")+"";
            if (StringUtils.isNotBlank(category)) {
                //词条查询，对查询条件不再进行分词
                booleanQuery.must(QueryBuilders.termQuery("category", category));
            }

            //品牌过滤查询
            String brand = searchMap.get("brand")+"";
            if (StringUtils.isNotBlank(brand)) {
                //词条查询，对查询条件不再进行分词
                booleanQuery.must(QueryBuilders.termQuery("brand", brand));
            }

            //规格过滤查询
            //因为规格在es中使用的是嵌套域，域名为：specMap.规格名称.keyword
            if (searchMap.get("spec") != null) {
                Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
                for (Map.Entry<String, String> entry : specMap.entrySet()) {
                    String field = "specMap." + entry.getKey() + ".keyword";
                    NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("specMap",
                            QueryBuilders.matchQuery(field, entry.getValue()), ScoreMode.Max);
                    booleanQuery.must(nestedQuery);
                }
            }

            //价格过滤
            String priceStr = searchMap.get("price")+"";
            if (StringUtils.isNotBlank(priceStr)) {
                String[] prices = priceStr.split("-");

                //价格下限
                booleanQuery.must(QueryBuilders.rangeQuery("price").gte(prices[0]));

                //价格上限
                if (!"*".equals(prices[1])) {
                    booleanQuery.must(QueryBuilders.rangeQuery("price").lt(prices[1]));
                }
            }

            builder.withFilter(booleanQuery);
        }

        //设置分页信息
        //页号
        int pageNo = 1;
        String pageNoStr = searchMap.get("pageNo")+"";
        if (StringUtils.isNotBlank(pageNoStr)) {
            pageNo = Integer.parseInt(pageNoStr);
        }
        //页大小
        int pageSize = 20;
        String pageSizeStr = searchMap.get("pageSize")+"";
        if (StringUtils.isNotBlank(pageSizeStr)) {
            pageSize = Integer.parseInt(pageSizeStr);
        }

        builder.withPageable(PageRequest.of(pageNo-1, pageSize));

        //排序
        String sortField = searchMap.get("sortField")+"";
        String sort = searchMap.get("sort")+"";
        if (StringUtils.isNotBlank(sortField) && StringUtils.isNotBlank(sort)) {
            //创建排序构造对象
            FieldSortBuilder sortBuilder = SortBuilders.fieldSort(sortField).order(sort.equals("DESC") ? SortOrder.DESC : SortOrder.ASC);
            builder.withSort(sortBuilder);
        }

        //获取查询对象
        NativeSearchQuery searchQuery = builder.build();

        //查询
        AggregatedPage<TbItem> pageResult;
        if (highlight) {
            pageResult = esTemplate.queryForPage(searchQuery, TbItem.class, new SearchResultMapper() {
                @Override
                public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                    List<T> itemList = new ArrayList<>();

                    //将查询到的没有高亮的数据一个个遍历并获取具体商品的高亮标题
                    TbItem item = null;
                    for (SearchHit hit : searchResponse.getHits()) {
                        //hit.getSourceAsString();//表示商品json格式字符串
                        item = JSON.parseObject(hit.getSourceAsString(), TbItem.class);

                        //获取高亮标题
                        HighlightField highlightField = hit.getHighlightFields().get("title");
                        if (highlightField != null && highlightField.fragments().length > 0) {
                            StringBuilder sb = new StringBuilder();
                            for (Text fragment : highlightField.getFragments()) {
                                //fragment 高亮片段
                                sb.append(fragment.toString());
                            }

                            //高亮标题设置
                            item.setTitle(sb.toString());
                        }

                        itemList.add((T)item);
                    }

                    return new AggregatedPageImpl<>(itemList, pageable, searchResponse.getHits().getTotalHits());
                }
            });
        } else {
            //不需要高亮处理
            pageResult = esTemplate.queryForPage(searchQuery, TbItem.class);
        }

        //商品列表
        resultMap.put("itemList", pageResult.getContent());
        //总页数
        resultMap.put("totalPages", pageResult.getTotalPages());
        //总记录数
        resultMap.put("total", pageResult.getTotalElements());

        return resultMap;
    }

    @Autowired
    private ItemDao itemDao;

    @Override
    public void importItemList(List<TbItem> itemList) {
        itemDao.saveAll(itemList);
    }

    @Override
    public void deleteItemByIds(Long[] ids) {
        /*for (Long id:ids) {
            TbItem item = new TbItem();
            item.setGoodsId(id);
            itemDao.delete(item);
        }*/
        itemDao.deleteByGoodsIdIn(ids);
    }
}
