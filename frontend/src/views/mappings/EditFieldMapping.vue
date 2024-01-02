<template>
  <CRow>
    <CCol col="12" lg="6">
      <CCard>
        <CCardHeader>
          Edit <strong>Field Mapping</strong> Form
          <span v-html="$t('mappings.edit_header')"></span>
        </CCardHeader>
        <CCardBody>
          <CAlert :show="displayError" color="danger">
            <div>{{ $t('common.error_message_template', { msg: errorMessage}) }}</div>
            <div v-if="errorDetails">{{ $t('common.error_details_template', { msg: errorDetails }) }}</div>
          </CAlert>
          <FieldMappingForm :field-mapping="fieldMapping" :is-loading="isLoading" :items="items"></FieldMappingForm>
        </CCardBody>
        <CCardFooter>
          <CButton type="submit" size="sm" color="primary" @click="submitForm" class="mr-1" :disabled="disabledButtons"><CIcon name="cil-check-circle"/> {{ $t('common.submit_button') }}</CButton>
          <CButton type="reset" size="sm" color="danger" @click="resetForm" :disabled="disabledButtons"><CIcon name="cil-ban"/> {{ $t('common.reset_button') }}</CButton>
        </CCardFooter>
      </CCard>
    </CCol>
  </CRow>
</template>

<script>
  import {api, processState} from "../../constants/api";
  import { Translation } from '../../plugins/Translation'
  import FieldMappingForm from './FieldMappingForm'

  export default {
    name: "EditFieldMapping",
    components: {
      FieldMappingForm
    },
    data () {
      return {
        fieldMapping: {},
        items: [],
        isLoading: false,
        errorMessage: '',
        errorDetails: '',
        disabledButtons: false,
        displayError: false,
        workspaces: []
      }
    },
    mounted() {
      let externalId = this.$route.params.id
      processState(this, "beforeSubmit")
      api.get(`/field-mappings/${externalId}`)
      .then(response => {
        this.fieldMapping = response.data.payload
        this.items.push(this.fieldMapping)
        processState(this, "onSuccess")
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

        let externalId = this.$route.params.id

        api.put(`/field-mappings/${externalId}`, this.fieldMapping)
        .then(response => {
          this.$router.push('/mappings')
          processState(this, "onSuccess")
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