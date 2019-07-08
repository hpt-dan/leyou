package com.leyou.search.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.pojo.PageResult;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.pojo.BrandDTO;
import com.leyou.item.pojo.CategoryDTO;
import com.leyou.item.pojo.SpecParamDTO;
import com.leyou.search.client.ItemClient;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.pojo.Goods;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilterBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @package: com.leyou.search.service
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:查询模块的业务
 */
@Service
public class SearchService {

    @Autowired
    private ElasticsearchTemplate esTemplate;


    @Autowired
    private ItemClient itemClient;


    /**
     * 分页查询商品
     * @param searchRequest
     * @return
     */
    public PageResult<GoodsDTO> pageQuery(SearchRequest searchRequest) {

        String key = searchRequest.getKey();

        //健壮性判断
        if (StringUtils.isBlank(key)) {
            throw new LyException(ExceptionEnum.INVALID_PARAM);
        }

        //自定义条件搜索对象
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //添加搜索条件
        //添加分页条件
        queryBuilder.withQuery(buildBasicQuery(searchRequest));
        queryBuilder.withPageable(PageRequest.of(searchRequest.getPage() - 1, searchRequest.getSize()));

        //得到结果
        AggregatedPage<Goods> goods = this.esTemplate.queryForPage(queryBuilder.build(), Goods.class);

        //页面内容
        List<Goods> content = goods.getContent();

        //查无此品
        if (CollectionUtils.isEmpty(content)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOND);
        }

        int totalPages = goods.getTotalPages();
        long totalElements = goods.getTotalElements();


        //放在分页对象中，返回结果

        return new PageResult<GoodsDTO>(totalElements, totalPages, BeanHelper.copyWithCollection(content, GoodsDTO.class));
    }

    /**
     * 过滤的数据查询
     * @param searchRequest
     * @return
     */
    public Map<String, List<?>> queryFilters(SearchRequest searchRequest) {

        Map<String, List<?>> filteMap = new HashMap<>();

        //对品牌和类进行聚合
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //构建查询条件
        queryBuilder.withQuery(buildBasicQuery(searchRequest));

        //尽量不查询
        queryBuilder.withPageable(PageRequest.of(0, 1));

        //显示空的source
        queryBuilder.withSourceFilter(new FetchSourceFilterBuilder().build());

        //进行聚合
        String categoryAgg = "categoryAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAgg).field("categoryId"));

        String brandAgg = "brandAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAgg).field("brandId"));

        AggregatedPage<Goods> goods = esTemplate.queryForPage(queryBuilder.build(), Goods.class);
        Aggregations aggregations = goods.getAggregations();

        //获取类的聚合
        LongTerms categoryIdS = aggregations.get(categoryAgg);

        List<Long> cateList = categoryIdS.getBuckets()
                .stream()
                .map(LongTerms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .collect(Collectors.toList());

        LongTerms brandIdS = aggregations.get(brandAgg);

        List<Long> brandList = brandIdS.getBuckets()
                .stream()
                .map(LongTerms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .collect(Collectors.toList());

        //根据类的ids,获取类的集合
        List<CategoryDTO> categoryDTOS = this.itemClient.queryByIds(cateList);
        filteMap.put("分类",categoryDTOS);

        //根据品牌的ids,获取品牌的集合
        List<BrandDTO> brandDTOS = this.itemClient.queryBrandByIds(brandList);

        //获取品牌的聚合
        filteMap.put("品牌",brandDTOS);
        //对规格参数进行聚合

        //对规格参数进行聚合
        if (categoryDTOS != null && 1 == categoryDTOS.size()) {
            handlerAggSpecs(searchRequest,cateList.get(0),filteMap);
        }

        return filteMap;
    }

    /**
     * 规格参数的处理。
     * @param cid
     * @param result
     */
    public void handlerAggSpecs(SearchRequest searchRequest,Long cid,Map<String, List<?>> result) {

        //根据类的id获取可搜索的规格参数对象的集合
        List<SpecParamDTO> specParamDTOS = this.itemClient.querySpecParams(null, cid, true);

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //构建查询条件
        queryBuilder.withQuery(buildBasicQuery(searchRequest));

        //尽量不查询
        queryBuilder.withPageable(PageRequest.of(0, 1));

        //显示空的source
        queryBuilder.withSourceFilter(new FetchSourceFilterBuilder().build());

        //然后对规格参数聚合
        specParamDTOS.forEach(specParam->{
            String name = specParam.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs."+name+".keyword"));
        });

        AggregatedPage<Goods> goods = esTemplate.queryForPage(queryBuilder.build(), Goods.class);
        Aggregations aggregations = goods.getAggregations();



        specParamDTOS.forEach(specParam->{
            String name = specParam.getName();
            StringTerms stringTerms = aggregations.get(name);

            List<String> values = stringTerms.getBuckets()
                    .stream()
                    .map(StringTerms.Bucket::getKeyAsString)
                    .collect(Collectors.toList());

            result.put(name, values);
        });
    }



    /**
     * 根据查询词构建查询条件。过滤查询
     * @param searchRequest
     * @return
     */
    private QueryBuilder buildBasicQuery(SearchRequest searchRequest){

        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.must(QueryBuilders.matchQuery("all", searchRequest.getKey()).operator(Operator.AND));

        //获取过滤的条件
        Set<Map.Entry<String, String>> entries = searchRequest.getFilter().entrySet();

        entries.forEach(entrie->{
            String key = entrie.getKey();
            if(key != null){
                if ("分类".equals(key)) {
                    key = "categoryId";
                }else if("品牌".equals(key)){
                    key = "brandId";
                }else {
                    key = "specs." + key + ".keyword";
                }
            }

            //添加过滤条件
            queryBuilder.filter(QueryBuilders.termQuery(key, entrie.getValue()));
        });


        return queryBuilder;
    }




    /**
     * 查询过滤的品牌，与类
     * @param searchRequest
     * @return
     */
   /* public Map<String, List<?>> queryFilters(SearchRequest searchRequest) {

        // 1.创建过滤项集合
        Map<String, List<?>> filterList = new HashMap<>();

        //查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(QueryBuilders.matchQuery("all", searchRequest.getKey()).operator(Operator.AND));
        queryBuilder.withPageable(PageRequest.of(0, 1));
        //显示空的source
        queryBuilder.withSourceFilter(new FetchSourceFilterBuilder().build());
        //聚合查询
        queryBuilder.addAggregation(AggregationBuilders.terms("categoryAgg").field("categoryId"));
        queryBuilder.addAggregation(AggregationBuilders.terms("brandAgg").field("brandId"));

        AggregatedPage<Goods> goods = esTemplate.queryForPage(queryBuilder.build(), Goods.class);

        Aggregations aggregations = goods.getAggregations();
        //获取category桶
        LongTerms categoryAgg = aggregations.get("categoryAgg");
        List<Long> idList = handleCategoryAgg(categoryAgg, filterList);

        //获取brand桶
        LongTerms brandAgg = aggregations.get("brandAgg");
        handleBrandAgg(brandAgg, filterList);


        //规格参数处理
        if (filterList != null && idList.size() == 1) {
            // 处理规格，需要参数：分类的id，查询条件，过滤项集合
            handleSpecAgg(idList.get(0), filterList,searchRequest);
        }

        return filterList;
    }*/

    /**
     * 根据聚合的品牌id集，查询品牌集合
     * @param terms
     * @param filterList
     */
    /*private void handleBrandAgg(LongTerms terms, Map<String, List<?>> filterList) {
        // 解析bucket，得到id集合
        List<Long> idList = terms.getBuckets().stream()
                .map(LongTerms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .collect(Collectors.toList());
        // 根据id集合查询品牌
        List<BrandDTO> brandList = itemClient.queryBrandByIds(idList);
        // 存入map
        filterList.put("品牌", brandList);
    }*/

    /**
     * 根据聚合的类id集，查询类集合
     * @param terms
     * @param filterList
     */
   /* private List<Long> handleCategoryAgg(LongTerms terms, Map<String, List<?>> filterList) {
        // 解析bucket，得到id集合
        List<Long> idList = terms.getBuckets().stream()
                .map(LongTerms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .collect(Collectors.toList());
        // 根据id集合查询分类
        List<CategoryDTO> categoryList = itemClient.queryByIds(idList);
        // 存入map
        filterList.put("分类", categoryList);
        return idList;
    }*/

    /**
     * 获取规格参数聚合
     * @param cid
     * @param filterList
     * @param searchRequest
     */
   /* private void handleSpecAgg(Long cid, Map<String, List<?>> filterList,SearchRequest searchRequest) {
        List<SpecParamDTO> specParamDTOS = itemClient.querySpecParams(null, cid, true);

        //查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(QueryBuilders.matchQuery("all", searchRequest.getKey()).operator(Operator.AND));
        queryBuilder.withPageable(PageRequest.of(0, 1));
        //显示空的source
        queryBuilder.withSourceFilter(new FetchSourceFilterBuilder().build());

        //聚合条件
        // 3.聚合条件
        for (SpecParamDTO param : specParamDTOS) {
            // 获取param的name，作为聚合名称
            String name = param.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs." + name + ".keyword"));
        }

        //查询结果
        AggregatedPage<Goods> result = esTemplate.queryForPage(queryBuilder.build(), Goods.class);
        Aggregations aggregations = result.getAggregations();
        // 5.解析聚合结果
        for (SpecParamDTO param : specParamDTOS) {
            // 获取param的name，作为聚合名称
            String name = param.getName();
            StringTerms terms = aggregations.get(name);
            // 获取聚合结果，注意，规格聚合的结果 直接是字符串，不用做特殊处理
            List<String> paramValues = terms.getBuckets()
                    .stream()
                    .map(StringTerms.Bucket::getKeyAsString)
                    // 过滤掉空的待选项
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.toList());
            // 存入map
            filterList.put(name, paramValues);
        }
    }*/





}
