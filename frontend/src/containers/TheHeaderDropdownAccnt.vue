<template>
  <CDropdown
    inNav
    class="c-header-nav-items"
    placement="bottom-end"
    add-menu-classes="pt-0"
  >
    <template #toggler>
      <CHeaderNavLink>
        <div class="c-avatar">
          <img
            src="../../public/img/avatars/profile-image-icon-clipart-1.png"
            class="c-avatar-img "
          />
        </div>
      </CHeaderNavLink>
    </template>
    <CDropdownHeader
      tag="div"
      class="text-center"
      color="light"
    >
      <strong>{{ $t('common.account') }}</strong>
    </CDropdownHeader>
    <CDropdownItem to="/profile">
      <CIcon name="cil-user" />
      {{ $t('common.profile') }}
    </CDropdownItem>
    <CDropdownItem>
      <CIcon name="cil-settings" />
      {{ $t('common.settings') }}
    </CDropdownItem>
    <CDropdownDivider/>
    <CDropdownItem @click="logout">
      <CIcon name="cil-account-logout" />
      {{ $t('common.logout') }}
    </CDropdownItem>
  </CDropdown>
</template>

<script>
  import {api} from "../constants/api"

  export default {
  name: 'TheHeaderDropdownAccnt',
  data () {
    return { 
      itemsCount: 42
    }
  },
  methods: {
    logout: function() {
      sessionStorage.removeItem("username")
      sessionStorage.removeItem("isAdmin")
      sessionStorage.removeItem("isAuthenticated")
      // this.$router.push('/logout')

      api.post('/user/logout')
      .then(response => {
        this.$router.push('/login')
      })
      .catch(err => {})
    },
    getUsername () {
      return sessionStorage.getItem('username')
    }
  }
}
</script>

<style scoped>
  .c-icon {
    margin-right: 0.3rem;
  }
</style>