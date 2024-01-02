<template>
  <CRow>
    <CCol lg="8">
      <CCard>
        <CCardBody>
          <CForm class="novalidate" novalidate="novalidate">
            <CInput :label="$t('users.username_label')" disabled="disabled" :value="this.profile.username" horizontal/>
            <CSelect :label="$t('users.groups_label')" disabled="disabled" :options="this.profile.groups" multiple="true" horizontal/>
            <CInput :label="$t('users.role_label')" disabled="disabled" :value="this.profile.role" horizontal/>
            <CInput
                :description="$t('users.username_description')"
                :label="$t('users.password_label')"
                horizontal
                :placeholder="$t('users.password_placeholder')"
                v-model="profile.password"
                type="password"
                :disabled="profile.userType === 'EXTERNAL'"
                :invalid-feedback="$t('users.password_invalid_feedback')"
                pattern="(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{8,}"
                required
            />
            <CInput
                :description="$t('users.username_description')"
                :label="$t('users.confirm_password_label')"
                horizontal
                :placeholder="$t('users.confirm_password_placeholder')"
                v-model="profile.confirmPassword"
                type="password"
                :disabled="profile.userType === 'EXTERNAL'"
                :is-valid="profile.password === profile.confirmPassword"
                :invalid-feedback="$t('users.confirm_password_invalid_feedback')"
                :was-validated="false"
                required
            />
          </CForm>
        </CCardBody>
        <CCardFooter>
          <CButton type="submit" size="sm" color="primary" @click="submitForm" class="mr-1">
            <CIcon name="cil-check-circle"/>
            {{ $t('common.submit_button') }}
          </CButton>
        </CCardFooter>
      </CCard>
    </CCol>
  </CRow>
</template>

<script>
  import {api, processState} from "../../constants/api";

  export default {
    name: "EditProfile",
    data() {
      return {
        profile: {
          username: '',
          externalId: '',
          role: '',
          groups: [],
          password: '',
          confirmPassword: '',
          userType: 'EXTERNAL'
        },
        disabledButtons: false,
        displayError: false
      }
    },
    mounted() {
      api.get("/me")
      .then(response => {
        this.profile = response.data
      })
      .catch(err => {
      })
    },
    methods: {
      submitForm: function (event) {
        processState(this, "beforeSubmit")

        let form = this.$el.querySelector('form')
        if (!form.checkValidity()) {
          form.classList.add('was-validated')
          this.disabledButtons = false
          return false
        }

        // TODO use reals groups
        api.put(`/user/${this.profile.externalId}/change-password`,{
          password: this.profile.password,
          confirmPassword: this.profile.confirmPassword
        })
        .then(response => {
          this.$router.push('/')
        })
        .catch(err => {
          processState(this, "onError", err)
        })
      }
    }
  }
</script>

<style scoped>

</style>