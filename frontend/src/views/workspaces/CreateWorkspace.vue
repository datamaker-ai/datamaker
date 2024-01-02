<template>
  <CRow>
    <CCol col="12" lg="6">
      <CCard>
        <CCardHeader>
          {{ $t('workspaces.create_header') }}
        </CCardHeader>
        <CCardBody>
          <CAlert :show="displayError" color="danger">
            <div>{{ $t('common.error_message_template', { msg: errorMessage}) }}</div>
            <div v-if="errorDetails">{{ $t('common.error_details_template', { msg: errorDetails }) }}</div>
          </CAlert>
          <WorkspaceForm :is-loading="isLoading" :workspace="workspace"></WorkspaceForm>
        </CCardBody>
        <CCardFooter>
          <CButton type="submit" size="sm" color="primary" @click="submitForm" class="mr-1" :disabled="disabledButtons">
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
  </CRow>
</template>

<script>
import {api, processState} from "../../constants/api";
import WorkspaceForm from "./WorkspaceForm"

export default {
  name: 'CreateWorkspace',
  components: {
    WorkspaceForm
  },
  data () {
    return {
      options: ['NONE', 'READ_ONLY', 'READ_EXECUTE', 'READ_WRITE', 'FULL'],
      workspace: {name:'', description:'', owner:'', group: null, groupPermissions:'NONE'},
      errorMessage: '',
      errorDetails: '',
      disabledButtons: false,
      displayError: false,
      isLoading: false
    }
  },
  mounted() {
    this.workspace.owner = this.getCurrentUser()
  },
  methods: {
    resetForm: function (event) {
      let form = this.$el.querySelector('form')
      form.reset()
    },
    getCurrentUser() {
      return sessionStorage.getItem("username")
    },
    submitForm: function (event) {
      processState(this, "beforeSubmit")

      let form = this.$el.querySelector('form')
      if (!form.checkValidity()) {
        form.classList.add('was-validated')
        processState(this, "invalidForm")
        return false
      }

      api.post('/workspace',this.workspace)
      .then(response => {
        processState(this, "onSuccess")
        this.$router.push('/workspaces')
      })
      .catch(err => {
        processState(this, "onError", err)
      })
    }
  }
}
</script>
