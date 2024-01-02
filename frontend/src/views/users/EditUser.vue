<template>
  <CRow>
    <CCol col="12" lg="6">
      <CCard>
        <CCardHeader>
          <span v-html="$t('users.edit_user_header')"></span>
          <div v-if="user.userType === 'EXTERNAL'" v-html="$t('users.external_user_warning')"></div>
        </CCardHeader>
        <CCardBody>
          <CAlert :show="displayError" color="danger">
            <div>{{ $t('common.error_message_template', { msg: errorMessage}) }}</div>
            <div v-if="errorDetails">{{ $t('common.error_details_template', { msg: errorDetails }) }}</div>
          </CAlert>
          <CForm id="user">
            <CInput
                :description="$t('users.first_name_description')"
                :label="$t('users.first_name_label')"
                horizontal
                :placeholder="$t('users.first_name_placeholder')"
                v-model="user.firstName"
                required
                :disabled="user.userType === 'EXTERNAL'"
            />
            <CInput
                :description="$t('users.last_name_description')"
                :label="$t('users.last_name_label')"
                horizontal
                :placeholder="$t('users.last_name_placeholder')"
                v-model="user.lastName"
                required
                :disabled="user.userType === 'EXTERNAL'"
            />
            <CInput
                :description="$t('users.username_description')"
                :label="$t('users.username_label')"
                horizontal
                :placeholder="$t('users.username_placeholder')"
                v-model="user.username"
                :disabled="user.username === 'admin' || user.userType === 'EXTERNAL'"
                required
            />
            <CInput
                :description="$t('users.username_description')"
                :label="$t('users.password_label')"
                horizontal
                :placeholder="$t('users.password_placeholder')"
                v-model="user.password"
                type="password"
                :invalid-feedback="$t('users.password_invalid_feedback')"
                pattern="(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{8,}"
                required
                :disabled="user.userType === 'EXTERNAL'"
            />
            <CInput
                :description="$t('users.username_description')"
                :label="$t('users.confirm_password_label')"
                horizontal
                :placeholder="$t('users.confirm_password_placeholder')"
                v-model="user.confirmPassword"
                type="password"
                :is-valid="user.password === user.confirmPassword"
                :invalid-feedback="$t('users.confirm_password_invalid_feedback')"
                :was-validated="false"
                required
                :disabled="user.userType === 'EXTERNAL'"
            />
            <CSelect
                :value.sync="user.authority"
                :label="$t('users.role_label')"
                horizontal
                :disabled="user.username === 'admin' || user.userType === 'EXTERNAL'"
                :options="[{'label': 'Admin', 'value': 'ROLE_ADMIN'}, {'label': 'User', 'value': 'ROLE_USER'}]"
            >
            </CSelect>
            <CSelect
                :value.sync="user.languageTag"
                :label="$t('common.language')"
                horizontal
                :options="supportedLanguages"
                required
                :disabled="user.userType === 'EXTERNAL'"
            >
            </CSelect>
            <input type="hidden" name="enabled" value="true">
            <div class="form-group form-row">
              <CCol tag="label" sm="3" class="col-form-label">
                {{ $t('users.groups_header') }}
              </CCol>
              <CCol sm="9">
                <template v-for="option in userGroups">
                  <CFormGroup class="form-check">
                    <template #input>
                      <template v-if="option.name === 'Everyone'">
                        <input
                            type="checkbox"
                            :value="option.externalId"
                            name="groups"
                            class="form-check-input"
                            checked="checked"
                            :id="option.name"
                            disabled="disabled"
                        >
                        <label class="form-check-label">
                          {{ $t('users.everyone_label') }}
                        </label>
                      </template>
                      <template v-else>
                        <input
                            type="checkbox"
                            :value="option.externalId"
                            name="groups"
                            v-model="user.groupIds"
                            class="form-check-input"
                            :id="option.name"
                            :disabled="user.userType === 'EXTERNAL'"
                        >
                        <label :for="option.name" class="form-check-label">
                          {{ option.name }}
                        </label>
                      </template>
                    </template>
                  </CFormGroup>
                </template>
              </CCol>
            </div>
          </CForm>
        </CCardBody>
        <CCardFooter>
          <CButton type="submit" size="sm" color="primary" @click="submitForm" :disabled="disabledButtons" class="mr-1">
            <CIcon name="cil-check-circle"/>
            {{ $t('common.submit_button') }}
          </CButton>
          <CButton type="reset" size="sm" color="danger" @click="resetForm" :disabled="disabledButtons">
            <CIcon name="cil-ban"/>
            {{ $t('common.reset_button') }}
          </CButton>
        </CCardFooter>
      </CCard>
    </CCol>
    <template v-if="isLoading">
      <CElementCover
              :boundaries="[
            { sides: ['top'], query: 'form' },
            { sides: ['bottom'], query: 'form' }
          ]"
      />
    </template>
  </CRow>
</template>

<script>
  import {api, processState} from "../../constants/api";
  import { Translation } from '../../plugins/Translation'

  export default {
    name: "EditUser",
    data: () => {
      return {
        user: {
          firstName: '',
          lastName: '',
          username: '',
          password: '',
          confirmPassword: '',
          authority: 'ROLE_USER',
          groupIds: [],
          languageTag: Translation.currentLanguage,
          userType: 'INTERNAL'
        },
        userGroups: [],
        supportedLanguages: Translation.supportedLanguages,
        errorMessage: '',
        errorDetails: '',
        disabledButtons: false,
        displayError: false,
        isLoading: false
      }
    },
    created() {
      this.isLoading = true
      if (this.$route.params.id) {
        api.get(`user/${this.$route.params.id}`)
          .then(response => {
            this.user = response.data.payload
            processState(this, "onSuccess")
            if (this.user.userType === 'EXTERNAL') {
              this.disabledButtons = true
            }
          })
          .catch(err => {
            processState(this, "onError", err)
          })
      }
      api.get('user/groups')
        .then(response => {
          this.userGroups = response.data.payload
          this.isLoading = false
        })
        .catch()
    },
    methods: {
      goBack() {
        this.$router.go(-1)
      },
      resetForm: function (event) {
        let form = this.$el.querySelector('form')
        form.reset()
      },
      submitForm: function (event) {
        processState(this, "beforeSubmit")

        let form = this.$el.querySelector('form')
        if (!form.checkValidity()) {
          form.classList.add('was-validated')
          this.disabledButtons = false
          processState(this, "invalidForm")
          return false
        }

        if (this.$route.params.id) {
          api.put(`user/${this.$route.params.id}`, this.user)
            .then(response => {
              this.$router.push('/users')
            })
            .catch(err => {
              processState(this, "onError", err)
            });
        } else {
          api.post('user', this.user)
            .then(response => {
              this.$router.push('/users')
            })
            .catch(err => {
              processState(this, "onError", err)
            });
        }
      }
    }
  }
</script>

<style scoped>

</style>