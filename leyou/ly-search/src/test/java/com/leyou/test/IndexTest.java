package com.leyou.test;

import com.leyou.LySearchApplication;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.client.ItemClient;
import com.leyou.item.pojo.SpuDTO;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.IndexService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;


/**
 * @package: com.leyou.test
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LySearchApplication.class)
public class IndexTest {

    @Autowired
    private ElasticsearchTemplate esTemplate;


    @Autowired
    private ItemClient itemClient;


    @Autowired
    private IndexService indexService;

    @Autowired
    private GoodsRepository goodsRepository;



    //创建索引
    //添加映射
    @Test
    public void testCreateIndexes(){
        esTemplate.createIndex(Goods.class);
    }

    //添加映射
    @Test
    public void testPutMapping(){
        esTemplate.putMapping(Goods.class);
    }

    //添加数据

    /**
     * 因为goods所有的数据都直接或间接的依赖数据源，应该将spu转化为goods;
     */
    @Test
    public void testLoadData(){
        //1:分页查询spu,转化为goods
        int page = 1;

        while (true){
            PageResult<SpuDTO> spuDTOPageResult = this.itemClient.querySpuByPage(page, 50, null, null);

            if (spuDTOPageResult == null) {
                break;
            }

            page++;
            List<Goods> goodsList = new ArrayList<>();

            List<SpuDTO> items = spuDTOPageResult.getItems();
            items.forEach(item->{
                Goods goods = indexService.buildGoods(item);
                goodsList.add(goods);
            });
            this.goodsRepository.saveAll(goodsList);
        }
    }
}
