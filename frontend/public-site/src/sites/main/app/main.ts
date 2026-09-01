import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import '../styles/style.css'
import '../styles/public-pages.css'
import '../styles/footer.css'

createApp(App).use(router).use(ElementPlus).mount('#app')
