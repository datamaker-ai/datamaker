<template>
  <CHeader fixed with-subheader light>
    <CToggler
      in-header
      class="ml-3 d-lg-none"
      v-c-emit-root-event:toggle-sidebar-mobile
    />
    <CToggler
      in-header
      class="ml-3 d-md-down-none"
      v-c-emit-root-event:toggle-sidebar
    />
    <CHeaderNav class="d-md-down-none mr-auto">
      <CHeaderNavItem class="px-3">
        <CHeaderNavLink to="/dashboard">
          {{ $t('header.dashboard') }}
        </CHeaderNavLink>
      </CHeaderNavItem>
      <CHeaderNavItem class="px-3" v-if="isAdmin()">
        <CHeaderNavLink to="/users" exact>
          <CIcon name="cil-user" />
          {{ $t('header.users') }}
        </CHeaderNavLink>
      </CHeaderNavItem>
      <CHeaderNavItem class="px-3">
        <CHeaderNavLink href="/datamaker/docs">
          {{ $t('header.docs') }}
        </CHeaderNavLink>
      </CHeaderNavItem>
      <CHeaderNavItem class="px-3">
          <LanguageSwitcher style="margin-bottom: 0px;" id="lang"/>
      </CHeaderNavItem>
    </CHeaderNav>
    <CHeaderNav class="mr-4">
      <CHeaderNavItem class="d-md-down-none mx-2">
        <CInput @input="search" id="search" :placeholder="$t('search.placeholder')" class="px-3" type="text"/>
      </CHeaderNavItem>
      <TheHeaderDropdownAccnt/>
    </CHeaderNav>
    <CSubheader class="px-3">
      <BreadcrumbRouter class="border-0"/>
    </CSubheader>
  </CHeader>
</template>

<script>
import TheHeaderDropdownAccnt from './TheHeaderDropdownAccnt'
import LanguageSwitcher  from "../components/LanguageSwitcher"
import BreadcrumbRouter from "../components/breadcrumb/BreadcrumbRouter"

export default {
  name: 'TheHeader',
  components: {
    TheHeaderDropdownAccnt,
    LanguageSwitcher,
    BreadcrumbRouter
  },
  methods: {
    isAdmin () {
      return sessionStorage.getItem("isAdmin") === 'true'
    },
    search (value) {
      this.$router.replace({path:'/search', query: {query: value}})
      //this.$eventHub.$emit('search-query', value);
    }
  }
}
</script>

<style>
  #lang select {
    width: 4rem;
  }
  #lang label {
    width: 16rem;
  }
  #lang .col-sm-9 {
  }
  .breadcrumb {
    margin: 0;
  }
  .px-3.form-group {
    margin-bottom: 0;
  }
  #search {
    margin-bottom: 0;
  }
</style>