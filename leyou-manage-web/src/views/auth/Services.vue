<template>
  <div>
    <v-data-table
        :headers="headers"
        :items="services"
        :pagination.sync="pagination"
        :total-items="totalServices"
        :loading="loading"
        class="elevation-1"
    >
      <template slot="items" slot-scope="props">
        <td class="text-xs-center">{{ props.item.id }}</td>
        <td class="text-xs-center">{{ props.item.name }}</td>
        <td class="text-xs-center">{{ props.item.info }}</td>
        <td class="justify-center layout px-0">
          <v-btn icon @click="editservice(props.item)">
            <i class="el-icon-edit"/>
          </v-btn>
          <v-btn icon @click="deleteservice(props.item)">
            <i class="el-icon-delete"/>
          </v-btn>
        </td>
      </template>
      <template v-slot:pageText="props">
        当前 {{ props.pageStart }} - {{ props.pageStop }} 共 {{ props.itemsLength }}条
      </template>
    </v-data-table>
    <!--弹出的对话框-->
    <v-dialog max-width="500" v-model="show" persistent scrollable>
      <service-form @close="closeWindow" :oldservice="oldservice" :isEdit="isEdit"/>
    </v-dialog>
  </div>
</template>

<script>
  export default {
    name: "Services",
    data(){
      return{
        headers:[
          {text: 'id', align: 'center', value: 'id'},
          {text: '服务名称', align: 'center', sortable: false, value: 'name'},
          {text: '功能描述', align: 'center', sortable: false, value: 'info'},
          {text: '操作', align: 'center', value: 'id', sortable: false}
        ],
        services:[
          {id:1, name:"user-service", info: "用户相关业务"},
          {id:2, name:"item-service", info: "商品相关业务"},
          {id:3, name:"search-service", info: "搜索相关业务"},
          {id:4, name:"page-service", info: "静态页处理业务"},
          {id:5, name:"upload-service", info: "文件上传业务"},
          {id:6, name:"auth-service", info: "授权中心"},
          {id:7, name:"order-service", info: "订单业务"},
          {id:8, name:"cart-service", info: "购物车业务"}
        ],
        totalServices: 14
      }
    }
  }
</script>

<style scoped>

</style>