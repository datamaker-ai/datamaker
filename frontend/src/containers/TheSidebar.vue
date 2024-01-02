<template>
  <CSidebar 
    fixed 
    :minimize="minimize"
    :show.sync="show"
  >
    <div class="c-sidebar-brand">
      <img class="c-sidebar-brand-full" width="200" height="50" alt="Logo" src="../../public/img/brand/datamaker-white.svg" />
      <img class="c-sidebar-brand-minimized" width="46" height="46" alt="Logo" src="../../public/img/brand/datamaker-signet-white.svg" />
    </div>
    <template v-if="currentLanguage === 'en'">
      <CRenderFunction flat :content-to-render="nav"/>
    </template>
    <template v-if="currentLanguage === 'fr'">
      <CRenderFunction flat :content-to-render="nav_fr"/>
    </template>
    <CSidebarMinimizer
      class="d-md-down-none"
      @click.native="minimize = !minimize"
    />
  </CSidebar>
</template>

<script>
import nav from './_nav'
import nav_fr from './_nav_fr'
import {isAdmin} from "../constants/api";
import {Translation} from "@/plugins/Translation";
import {i18n} from "@/plugins/i18n";

export default {
  name: 'TheSidebar',
  data () {
    return {
      minimize: false,
      nav,
      nav_fr,
      show: 'responsive',
      currentLanguage: this.$i18n.locale
    }
  },
  mounted () {
    if (!isAdmin()) {
      this.nav[0]._children.forEach(c => { if (c.items) {
          c.items = c.items.filter(i => !("admin" in i) || !i.admin)
        }
      })
      this.nav[0]._children = this.nav[0]._children.filter(i => !("admin" in i) || !i.admin)
    }
    this.$root.$on('toggle-sidebar', () => {
      const sidebarOpened = this.show === true || this.show === 'responsive'
      this.show = sidebarOpened ? false : 'responsive'
    })
    this.$root.$on('toggle-sidebar-mobile', () => {
      const sidebarClosed = this.show === 'responsive' || this.show === false
      this.show = sidebarClosed ? true : 'responsive'
    })
    this.$eventHub.$on('change-language', (lang) => {
      this.currentLanguage = lang
    })
  }
}
</script>
