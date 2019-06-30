import Vue from 'vue'
import Vuetify from 'vuetify/lib'
import 'vuetify/src/stylus/app.styl'
import config from '../config/config';
import '@mdi/font/css/materialdesignicons.css'
import 'element-ui/lib/theme-chalk/index.css';

Vue.use(Vuetify, {
  iconfont: 'mdi',
  theme: config.theme
})
