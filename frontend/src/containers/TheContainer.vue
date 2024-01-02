<template>
  <div class="c-app">
    <TheSidebar/>
    <div class="c-wrapper">
      <TheHeader/>
      <div class="c-body">
        <main class="c-main">
          <CContainer fluid>
            <transition name="fade">
              <router-view></router-view>
            </transition>
          </CContainer>
        </main>
      </div>
      <TheFooter/>
    </div>
    <CToaster :autohide="10000">
      <template v-for="toast in fixedToasts">
        <CToast
            :key="'toast' + toast"
            :show="true"
            header="Alert"
        >
          <template v-if="typeof message === 'string' || message instanceof String">
            {{ message }}
          </template>
          <template v-else-if="message.response && message.response.data">
            <div>{{ message.response.data.title }}</div>
            <div>{{ message.response.data.detail }}</div>
          </template>
          <template v-else>{{ message }}</template>
        </CToast>
      </template>
    </CToaster>
  </div>
</template>

<script>
import TheSidebar from './TheSidebar'
import TheHeader from './TheHeader'
import TheFooter from './TheFooter'

export default {
  name: 'TheContainer',
  components: {
    TheSidebar,
    TheHeader,
    TheFooter
  },
  mounted () {
    this.$eventHub.$on('show-toast', (message) => {
      this.message = message
      this.showToast = true
      this.fixedToasts++
    })
  },
  data () {
    return {
      fixedToasts: 0,
      message: '',
      showToast: false,
      timeout: null
    }
  }
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter,
.fade-leave-to {
  opacity: 0;
}
</style>
