<template>
  <v-card>
    <v-card-title>
      <span class="headline" v-text="isEdit ? '修改品牌' : '新增品牌'"></span>
    </v-card-title>
    <v-divider/>
    <v-card-text class="px-3">
      <v-form v-model="valid" ref="myBrandForm">
        <v-container grid-list-md>
          <v-layout wrap>
            <v-flex class="md6">
              <v-text-field v-model="brand.name" label="品牌名称" required :rules="nameRules"/>
            </v-flex>
            <v-flex class="md6">
              <v-text-field v-model="brand.letter" label="品牌首字母" required :rules="letterRules"/>
            </v-flex>
            <v-flex>
              <v-cascader
                url="/item/category/of/parent"
                multiple
                required
                v-model="brand.categories"
                label="商品分类"/>
            </v-flex>
          </v-layout>
          <v-layout row>
            <v-flex md4>
              <span class="subheading font-weight-thin">品牌LOGO：</span>
            </v-flex>
            <v-flex>
              <v-upload v-model="brand.image" :multiple="false" :pic-width="250" :pic-height="90"
                        url="/upload/signature" need-signature/>
            </v-flex>
          </v-layout>
        </v-container>
      </v-form>
    </v-card-text>
    <v-card-actions>
      <v-spacer/>
      <v-btn depressed  small @click="submit">提交</v-btn>
      <v-btn depressed small @click="clear">重置</v-btn>
      <v-btn depressed  small @click="close">关闭</v-btn>
    </v-card-actions>
  </v-card>

</template>

<script>
  export default {
    name: "brand-form",
    props: {
      oldBrand: {
        type: Object
      },
      isEdit: {
        type: Boolean,
        default: false
      }
    },
    data() {
      return {
        valid: false, // 表单校验结果标记
        brand: {
          name: '', // 品牌名称
          letter: '', // 品牌首字母
          image: '',// 品牌logo
          categories: [], // 品牌所属的商品分类数组
        },
        nameRules: [
          v => !!v || "品牌名称不能为空",
          v => /^.{2,}$/.test(v) || "品牌名称至少2位"
        ],
        letterRules: [
          v => !!v || "首字母不能为空",
          v => /^[a-zA-Z]$/.test(v) || "品牌字母只能是1个字母"
        ]
      }
    },
    methods: {
      submit() {
        // 表单校验
        if (this.$refs.myBrandForm.validate()) {
          // 定义一个请求参数对象，通过解构表达式来获取brand中的属性
          const {categories, letter, ...params} = this.brand;
          // 数据库中只要保存分类的id即可，因此我们对categories的值进行处理,只保留id，并转为字符串
          params.cids = categories.map(c => c.id).join(",");
          // 将字母都处理为大写
          params.letter = letter.toUpperCase();
          // 将数据提交到后台
          // this.$http.post('/item/brand', this.$qs.stringify(params))
          this.$http({
            method: this.isEdit ? 'put' : 'post',
            url: '/item/brand',
            data: this.$qs.stringify(params)
          }).then(() => {
            // 关闭窗口
            this.$emit("close");
            this.$message.success("保存成功！");
          })
            .catch(() => {
              this.$message.error("保存失败！");
            });
        }
      },
      clear() {
        // 重置表单
        this.$refs.myBrandForm.reset();
        // 需要手动清空商品分类
        this.categories = [];
      },
      close(){
        this.$emit("close");
      }
    },
    watch: {
      oldBrand: {// 监控oldBrand的变化
        handler(val) {
          if (val) {
            // 注意不要直接复制，否则这边的修改会影响到父组件的数据，copy属性即可
            this.brand = Object.deepCopy(val)
          } else {
            // 为空，初始化brand
            this.brand = {
              name: '',
              letter: '',
              image: '',
              categories: [],
            };
            this.$refs.myBrandForm.reset();
          }
        },
        deep: true
      }
    }
  }
</script>

<style scoped>

</style>
