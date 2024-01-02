import Vue from 'vue'
import App from './App'
import router from './router'
import CoreuiVue from '@coreui/vue'
import { iconsSet as icons } from './assets/icons/icons.js'
import { i18n } from './plugins/i18n'

Vue.config.performance = true
Vue.use(CoreuiVue)

Vue.prototype.$eventHub = new Vue(); // Global event bus
Vue.prototype.$workspace = ''
Vue.prototype.$appLang = i18n.locale

export const vm = new Vue({
  el: '#app',
  i18n,
  router,
  icons,
  template: '<App/>',
  components: {
    App
  }
})
