import Vue from 'vue'
import Router from 'vue-router'
import Home from "../views/Home";
import Cluster from "../views/cluster/Cluster";
import Plugin from "../views/plugin/Plugin";
import {deleteRequest, getRequest, postKeyValueRequest, postRequest, putRequest, setLoadingMsg} from "../utils/api";

Vue.prototype.postRequest = postRequest;
Vue.prototype.getRequest = getRequest;
Vue.prototype.postKeyValueRequest = postKeyValueRequest;
Vue.prototype.putRequest = putRequest;
Vue.prototype.deleteRequest = deleteRequest;
Vue.prototype.setLoadingMsg = setLoadingMsg;
Vue.use(Router);
Vue.config.productionTip = false;

export default new Router({
  routes: [
    {
      path: '/',
      name: '首页',
      component: Home,
      hidden : true
    },
    {
      path: '/home',
      name: 'home',
      component: Home,
      hidden : true,
      children: [
        {
          path: '/cluster',
          name: '集群管理',
          component: Cluster,
          hidden: true
        },
        {
          path: '/plugin',
          name: '插件管理',
          component: Plugin,
          hidden: true
        }]
    }
  ]
})
