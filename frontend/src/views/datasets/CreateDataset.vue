<template>
  <CRow>
    <CCol lg="8">
      <CCard>
        <CCardHeader>
          <span v-html="$t('dataset.create_title')"></span>
        </CCardHeader>
        <CCardBody>
          <CAlert :show="displayError" color="danger">
            <div>{{ $t('common.error_message_template', { msg: errorMessage}) }}</div>
            <div v-if="errorDetails">{{ $t('common.error_details_template', { msg: errorDetails }) }}</div>
          </CAlert>
          <DatasetForm :dataset="dataset" :is-loading="isLoading"></DatasetForm>
        </CCardBody>
        <CCardFooter>
          <CButton type="submit" size="sm" color="primary" @click="submitForm" class="mr-1" :disabled="disabledButtons">
            <CIcon name="cil-check-circle"/> {{ $t('common.submit_button') }}
          </CButton>
          <CButton type="reset" size="sm" color="danger" @click="resetForm" :disabled="disabledButtons">
            <CIcon name="cil-ban"/> {{ $t('common.reset_button') }}
          </CButton>
        </CCardFooter>
      </CCard>
    </CCol>
  </CRow>
</template>

<script>
  import {api, processState} from "../../constants/api";
  import { Translation } from '../../plugins/Translation'
  import DatasetForm from './DatasetForm'

  export default {
    name: 'CreateDataset',
    components: {
      DatasetForm
    },
    data () {
      return {
        dataset: {
          name: '',
          description: '',
          workspaceId: '',
          languageTag: Translation.currentLanguage,
          tags: [],
          exportHeader: true,
          nullablePercentLimit: 10,
          duplicatesPercentLimit: 0,
          allowDuplicates: true,
          flushOnEveryRecord: true,
          randomizeNumberRecords: false,
          numberOfRetries: 10
        },
        isLoading: false,
        errorMessage: '',
        errorDetails: '',
        disabledButtons: false,
        displayError: false,
        workspaces: []
      }
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

        api.post('/dataset', this.dataset)
        .then(response => {
          this.$router.push('/datasets')
          // processState(this, "onSuccess")
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
