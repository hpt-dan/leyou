<template>
  <v-card>
    <v-toolbar class="elevation-0">
      <v-btn color="primary" @click="addGoods">新增商品</v-btn>
      <v-spacer/>
      <v-flex xs3>
        状态：
        <v-btn-toggle mandatory v-model.lazy="saleable">
          <v-btn>
            全部
          </v-btn>
          <v-btn :value="true">
            上架
          </v-btn>
          <v-btn :value="false">
            下架
          </v-btn>
        </v-btn-toggle>
      </v-flex>
      <v-flex xs3>
        <v-text-field
            append-icon="search"
            label="搜索"
            single-line
            hide-details
            v-model="search"
            @click:append="getDataFromServer"
        />
      </v-flex>
    </v-toolbar>
    <v-divider/>
    <v-data-table
        :headers="headers"
        :items="goodsList"
        :pagination.sync="pagination"
        :total-items="totalGoods"
        :loading="loading"
        class="elevation-1"
    >
      <template slot="items" slot-scope="props">
        <td class="text-xs-center">{{ props.item.id }}</td>
        <td class="text-xs-center">{{ props.item.name }}</td>
        <td class="text-xs-center">{{props.item.categoryName}}</td>
        <td class="text-xs-center">{{ props.item.brandName }}</td>
        <td class="justify-center layout px-0">
          <v-tooltip left>
            <v-btn slot="activator" icon @click="goodsDetail(props.item.id)">
              <i class="el-icon-search"/>
            </v-btn>
            <span> 查看商品</span>
          </v-tooltip>
          <v-tooltip left>
            <v-btn slot="activator" icon @click="editGoods(props.item)">
              <i class="el-icon-edit"/>
            </v-btn>
            <span> 修改商品</span>
          </v-tooltip>
          <v-tooltip left>
            <v-btn icon slot="activator">
              <i class="el-icon-delete"/>
            </v-btn>
            <span> 删除商品</span>
          </v-tooltip>
          <v-tooltip left>
            <v-btn slot="activator" icon @click="updateSaleable(props.item.id, !props.item.saleable)">
              <i class="el-icon-upload2" v-if="!props.item.saleable"/>
              <i class="el-icon-download" v-else/>
            </v-btn>
            <span v-if="!props.item.saleable"> 上架商品</span>
            <span v-else> 下架商品</span>
          </v-tooltip>
        </td>
      </template>
    </v-data-table>
    <!--弹出的对话框-->
    <v-dialog max-width="800" v-model="show" persistent scrollable>
      <v-card>
        <!--对话框的标题-->
        <v-toolbar dense dark color="primary">
          <v-toolbar-title>{{isEdit ? '修改' : '新增'}}商品</v-toolbar-title>
          <v-spacer/>
          <!--关闭窗口的按钮-->
          <v-btn icon @click="closeWindow">
            <v-icon>close</v-icon>
          </v-btn>
        </v-toolbar>
        <!--对话框的内容，表单-->
        <v-card-text class="px-3" style="height: 600px">
          <goods-form :oldGoods="oldGoods" :step="step" @close="closeWindow" :is-edit="isEdit" ref="goodsForm"/>
        </v-card-text>
        <!--底部按钮，用来操作步骤线-->
        <v-card-actions class="elevation-10">
          <v-flex class="xs3 mx-auto">
            <v-btn @click="previous" color="primary" :disabled="step === 1">上一步</v-btn>
            <v-btn @click="next" color="primary" :disabled="step === 4">下一步</v-btn>
          </v-flex>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-card>
</template>

<script>
  // 导入自定义的表单组件
  import GoodsForm from './GoodsForm'

  export default {
    name: "goods",
    data() {
      return {
        saleable: true, // 上架还是下架
        search: '', // 搜索过滤字段
        totalGoods: 0, // 总条数
        goodsList: [], // 当前页品牌数据
        loading: true, // 是否在加载中
        pagination: {}, // 分页信息
        headers: [
          {text: 'id', align: 'center', sortable: false, value: 'id'},
          {text: '商品名称', align: 'center', sortable: false, value: 'name'},
          {text: '商品分类', align: 'center', sortable: false, value: 'categoryName'},
          {text: '品牌', align: 'center', value: 'brandName', sortable: false,},
          {text: '操作', align: 'center', sortable: false}
        ],
        show: false,// 控制对话框的显示
        oldGoods: {}, // 即将被编辑的商品信息
        isEdit: false, // 是否是编辑
        step: 1, // 子组件中的步骤线索引，默认为1
      }
    },
    mounted() { // 渲染后执行
      // 查询数据
      this.getDataFromServer();
    },
    watch: {
      pagination: { // 监视pagination属性的变化
        deep: true, // deep为true，会监视pagination的属性及属性中的对象属性变化
        handler() {
          // 变化后的回调函数，这里我们再次调用getDataFromServer即可
          this.getDataFromServer();
        }
      },
      saleable(){
        this.getDataFromServer();
      }
    },
    methods: {
      getDataFromServer() { // 从服务的加载数的方法。
        // 发起请求
        this.$http.get("/item/spu/page", {
          params: {
            key: this.search, // 搜索条件
            saleable: this.saleable === 0 ? null : this.saleable, // 上下架
            page: this.pagination.page,// 当前页
            rows: this.pagination.rowsPerPage,// 每页大小
          }
        }).then(resp => { // 这里使用箭头函数
          this.goodsList = resp.data.items;
          this.totalGoods = resp.data.total;
          // 完成赋值后，把加载状态赋值为false
          this.loading = false;
        }).catch(() => {
          this.goodsList = [];
          this.totalGoods = 0;
          // 完成赋值后，把加载状态赋值为false
          this.loading = false;
        })
      },
      addGoods() {
        // 修改标记
        this.isEdit = false;
        // 控制弹窗可见：
        this.show = true;
        // 把oldBrand变为null
        this.oldGoods = {};
      },
      async editGoods(oldGoods) {
        if (oldGoods.saleable) {
          // 如果是上架商品，则不允许修改
          this.$message.error("不能修改上架商品，请先下架！");
          return;
        }
        // 发起请求，查询商品详情和skus
        oldGoods.spuDetail = await this.$http.loadData("/item/spu/detail?id=" + oldGoods.id);
        oldGoods.skus = await this.$http.loadData("/item/sku/of/spu?id=" + oldGoods.id);
        // 修改标记
        this.isEdit = true;
        // 控制弹窗可见：
        this.show = true;
        // 获取要编辑的goods
        this.oldGoods = oldGoods;
      },
      closeWindow() {
        console.log(1)
        // 重新加载数据
        this.getDataFromServer();
        // 关闭窗口
        this.show = false;
        // 将步骤调整到1
        this.step = 1;
      },
      previous() {
        if (this.step > 1) {
          this.step--
        }
      },
      next() {
        if (this.step < 4 && this.$refs.goodsForm.$refs.basic.validate()) {
          this.step++
        }
      },
      updateSaleable(id, saleable) {
        this.$http.put("/item/spu/saleable", this.$qs.stringify({id, saleable}))
          .then(resp => {
            this.getDataFromServer();
            this.$message.success("操作成功！");
          })
          .catch(error => {
            this.$message.error("操作失败，请重试！");
          })
      },
      goodsDetail(id){
        this.$message.info("同学，自己也动手写点东西吧。");
      }
    },
    components: {
      GoodsForm
    }
  }
</script>

<style scoped>

</style>
