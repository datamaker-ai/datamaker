<template>
  <Breadcrumb v-bind="props">
    <slot></slot>
  </Breadcrumb>
</template>

<script>
import Breadcrumb from './Breadcrumb'
import {i18n} from "@/plugins/i18n";

export default {
  name: 'BreadcrumbRouter',
  components: {
    Breadcrumb
  },
  props: {
    addClasses: [String, Array, Object],
    addLinkClasses: [String, Array, Object],
    addLastItemClasses: [String, Array, Object],
    name: {
      type: String
    }
  },
  computed: {
    items () {
      const routes = this.$route.matched.filter(route => {
        return route.name || (route.meta && route.meta.label) || (this.$route.query.name)
      })
      return routes.map(route => {
        const meta = route.meta || {}
        let name = meta.label || route.name
        //console.log(name + ' ' + this.name)
        // console.log(this.$route.params.id + ' ' + this.$route.props.name + ' ' + name)
        if (!name && this.$route.query.name) {
          name = this.$route.query.name
        }
        return {
          to: route,
          text: i18n.te(name)
              ? i18n.t(name)
              : name
        }
      })
    },
    props () {
      return {
        items: this.items,
        addClasses: this.addClasses,
        addLinkClasses: this.addLinkClasses,
        addLastItemClasses: this.addLastItemClasses
      }
    }
  }
}
</script>
