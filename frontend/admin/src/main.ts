import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './app/App.vue'
import router from './app/router'
import './app/admin.css'
import './shared/admin-content.css'

createApp(App).use(router).use(ElementPlus).mount('#app')
