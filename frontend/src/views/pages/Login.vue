<template>
  <CContainer class="d-flex flex-column align-items-center vh-100">
        <CCard class="mx-auto my-auto w-50">
          <CCardHeader>
            <img alt="Logo" src="../../../public/img/brand/datamaker.svg" />
          </CCardHeader>
          <CCardBody>
            <CAlert color="danger" :show.sync="alertLogin">{{ $t('login.exception') }}</CAlert>
            <CAlert v-if="profile === 'demo'">
              <CBadge color="info">
                DEMO MODE
              </CBadge>
              <div>Username: admin</div>
              <div>Password: changeme</div>
            </CAlert>
            <CForm>
              <h1>{{ $t('login.title') }}</h1>
              <p class="text-muted">{{ $t('login.sub_title') }}</p>
              <LanguageSwitcher/>
              <CInput
                :placeholder="$t('login.username')"
                autocomplete="username email"
                name="username"
                v-model="username"
                v-bind:disabled="disabled"
                required
                :invalid-feedback="$t('common.input_required')"
              >
                <template #prepend-content><CIcon name="cil-user"/></template>
              </CInput>
              <CInput
                :placeholder="$t('login.password')"
                type="password"
                autocomplete="current-password"
                name="password"
                v-model="password"
                v-bind:disabled="disabled"
                required
                :invalid-feedback="$t('common.input_required')"
              >
                <template #prepend-content><CIcon name="cil-lock-locked"/></template>
              </CInput>
              <CRow>
                <CCol       v-if="showLogin"
                            col="6">
                  <CButton type="submit" color="primary" @click="submitForm" class="px-4">{{ $t('login.title') }}</CButton>
                </CCol>
                <CCol       v-if="!showLogin"
                            col="10">
                  <CButton type="button" disabled="disabled" color="primary" class="px-4"><CSpinner class="mr-2" color="white" size="sm"/>{{ $t('common.loader') }}</CButton>
                </CCol>
                <!--
                <CCol col="6" class="text-right">
                  <CButton color="link" class="px-0">Forgot password?</CButton>
                </CCol>
                -->
              </CRow>
              <br>
              <CForm v-if="Object.keys(providers).length !== 0">
                <h2>{{ $t('login.oauth') }}</h2>
                <template v-for="provider in Object.keys(providers)">
                  <CLink :href="providers[provider]">{{ provider }}</CLink>
                </template>
              </CForm>
            </CForm>
          </CCardBody>
          <CCardFooter>
            <div class="ml-auto">
              <a href="https://www.datamaker.ai" target="_blank">More info</a>
              <span class="ml-1">&copy; 2021 Datamaker</span>
              <span > - version: {{version}}</span>
            </div>
          </CCardFooter>
        </CCard>

  </CContainer>
</template>

<script>
import axios from "axios";
import LanguageSwitcher  from "../../components/LanguageSwitcher";
import {endpoint} from "../../constants/api";

export default {
  name: 'Login',
  components: {
    LanguageSwitcher
  },
  data () {
    return {
      alertLogin: false,
      username: '',
      password: '',
      disabled: false,
      showLogin: true,
      version: VERSION,
      profile: PROFILE,
      providers: {}
    }
  },
  mounted() {
    axios.get(`${endpoint}/oauth-providers`)
        .then(response => {
          this.providers = response.data
        })
        .catch(err => {
          console.log(err)
        })
  },
  methods: {
    notEmpty: function(val) {
      return val ? val.length >= 1 : false
    },
    submitForm: function (event) {
      let form = this.$el.querySelector('form')
      if (!form.checkValidity()) {
        form.classList.add('was-validated')
        return false
      }
      this.disabled = true
      this.alertLogin = false
      this.showLogin = false

      const config = {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        crossdomain: true,
        withCredentials: true
      }

      var formData = new FormData()
      formData.append('username', this.username)
      formData.append('password', this.password)

      axios.post(`${endpoint}/login`, formData, config)
      .then(response => {
        sessionStorage.setItem("isAdmin", response.data.roles.includes("ROLE_ADMIN").toString())
        sessionStorage.setItem("isAuthenticated", "true")
        sessionStorage.setItem("username", response.data.username)
        this.$router.replace('/')
      })
      .catch(err => {
        this.alertLogin = true
        this.showLogin = true
        this.disabled = false
        //alert(err);
        // Manage the state of the application if the request
        // has failed
      });
    }
  }
}
</script>
