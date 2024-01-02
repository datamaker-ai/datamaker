<template>
  <CRow>
    <CCol col="12" lg="6">
      <CCard>
        <CCardHeader>
          {{ $t('workspaces.edit_header') }}
        </CCardHeader>
        <CCardBody>
          <CAlert :show="displayError" color="danger">
            <div>{{ $t('common.error_message_template', { msg: errorMessage}) }}</div>
            <div v-if="errorDetails">{{ $t('common.error_details_template', { msg: errorDetails }) }}</div>
          </CAlert>
          <WorkspaceForm :workspace="workspace" :is-loading="isLoading"></WorkspaceForm>
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
  name: 'EditWorkspace',
  components: {
    WorkspaceForm
  },
  data () {
    return {
      options: ['NONE', 'READ_ONLY', 'READ_EXECUTE', 'READ_WRITE', 'FULL'],
      selected: [], // Must be an array reference!
      show: true,
      workspace: {name:'', description:'', owner:'', groupPermissions:'', group: null},
      isLoading: false,
      errorMessage: '',
      errorDetails: '',
      disabledButtons: false,
      displayError: false
    }
  },
  mounted() {
    let id = this.$route.params.id
    api.get("/workspace/" + id)
    .then(response => {
      this.workspace = response.data.payload
      processState(this)
    })
    .catch(err => {
      processState(this, "onError", err)
    })
  },
  methods: {
    goBack() {
      this.$router.go(-1)
    },
    setGroupPermissions(event, input) {
      this.workspace.groupPermissions = input.target.value
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
        processState(this, "invalidForm")
        return false
      }

      api.put(`/workspace/${this.$route.params.id}`, this.workspace)
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
