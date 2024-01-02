<template>
  <CRow>
    <CCol col="12" lg="6">
      <CCard>
        <CCardHeader>
          <strong>Dataset</strong> Form
        </CCardHeader>
        <CCardBody>
          <CAlert :show="displayError" color="danger">
            <div>{{ $t('common.error_message_template', { msg: errorMessage}) }}</div>
            <div v-if="errorDetails">{{ $t('common.error_details_template', { msg: errorDetails }) }}</div>
          </CAlert>
          <template v-if="dataset != null">
            <DatasetForm :dataset="dataset" :is-loading="isLoading"></DatasetForm>
          </template>
        </CCardBody>
        <CCardFooter>
          <CButton type="submit" size="sm" color="primary" @click="submitForm" class="mr-1" :disabled="disabledButtons"><CIcon name="cil-check-circle"/> {{ $t('common.submit_button') }}</CButton>
          <CButton type="reset" size="sm" color="danger" @click="resetForm" :disabled="disabledButtons"><CIcon name="cil-ban"/> {{ $t('common.reset_button') }}</CButton>
          <CButton v-if="dataset != null" type="submit" size="sm" color="info" v-bind:to="{path:'../fields/' + dataset.externalId, query: {name: dataset.name, workspaceId: dataset.workspaceId}}" class="ml-1" :disabled="disabledButtons">
            <CIcon name="cil-check-circle"/> {{ $t('common.fields_button') }}
          </CButton>
        </CCardFooter>
      </CCard>
    </CCol>
  </CRow>
</template>

<script>
  import {api, getWorkspaceOptions, processState} from "../../constants/api";
  import { Translation } from '../../plugins/Translation'
  import DatasetForm from './DatasetForm'

  export default {
    name: 'EditDataset',
    components: {
      DatasetForm
    },
    data () {
      return {
        dataset: null,
        isLoading: false,
        errorMessage: '',
        errorDetails: '',
        disabledButtons: false,
        displayError: false,
        language: Translation.currentLanguage,
        workspaces: []
      }
    },
    mounted() {
      let id = this.$route.params.id
      api.get("/dataset/" + id)
      .then(response => {
        this.dataset = response.data.payload
        this.disabledButtons = false
      })
      .catch(err => {
        processState(this, "onError", err)
      })
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
          processState(this, "invalidForm")
          return false
        }

        this.dataset.nullablePercentLimit = parseFloat(this.dataset.nullablePercentLimit)

        let id = this.$route.params.id

        api.put('/dataset/' + id, this.dataset)
        .then(response => {
          this.$router.push('/datasets')
          //processState(this, "onSuccess")
        })
        .catch(err => {
          processState(this, "onError", err)
        })
      }
    }
  }
</script>

<style>
  input[type=range] {
    padding: 0 0;
  }
</style>
