<template>
  <div>
    <CRow>
      <CCol md="6">
        <CCard>
          <CCardHeader>
            {{ $t('system.upload_file_header') }}
          </CCardHeader>
          <CCardBody>
            <CAlert :show="displayError" color="danger">
              <div>{{ $t('common.error_message_template', { msg: errorMessage}) }}</div>
              <div v-if="errorDetails">{{ $t('common.error_details_template', { msg: errorDetails }) }}</div>
            </CAlert>
            <CForm class="needs-validation" novalidate>
              <CInput
                      :description="$t('system.filename_description')"
                      :label="$t('system.filename_label')"
                      horizontal
                      :placeholder="$t('system.filename_placeholder')"
                      v-model="filename"
              />
              <CSelect
                  :value.sync="fileType"
                  :options='["JAR", "KEYTAB", "JAAS", "JKS", "RESOURCE", "OTHER"]'
                  :value="fileType"
                  :label="$t('system.type_label')"
                  :placeholder="$t('system.select_one_placeholder')"
                  :title="$t('system.select_type_title')"
                  horizontal
                  required
              ></CSelect>
              <CTextarea
                  :label="$t('system.content_label')"
                  :placeholder="$t('system.content_placeholder')"
                  horizontal
                  rows="9"
                  v-model="content"
                  :invalid-feedback="$t('common.feedback_not_empty')"
              />
              <CInputFile
                      class="form-group"
                      :label="$t('system.filename_input')"
                      horizontal
                      custom
                      @change="files = $event"
              />
              {{ (files.length > 0) ? files[0].name : '' }}
            </CForm>
          </CCardBody>
          <CCardFooter>
            <CButton type="submit" size="sm" class="mr-1" color="primary" @click="(files.length > 0) ? submitFile() : submitForm()">
              <CIcon name="cil-check-circle"/>
              {{ $t('common.submit_button') }}
            </CButton>
            <CButton type="reset" size="sm" color="danger"><CIcon name="cil-ban"/> {{ $t('common.reset_button') }}</CButton>
          </CCardFooter>
        </CCard>
      </CCol>
    </CRow>
  </div>
</template>

<script>
  import {Translation} from "../../plugins/Translation";
  import {api, getWorkspaceOptions, processState, getComponents} from "../../constants/api";

  export default {
    name: "UploadFile",
    data() {
      return {
        filename: '',
        fileType: '',
        content: '',
        files: [],
        displayError: false,
        errorMessage: null,
        errorDetails: null,
      }
    },
    mounted() {
    },
    methods: {
      submitFile() {
        let form = this.$el.querySelector('form')
        if (!form.checkValidity()) {
          form.classList.add('was-validated')
          return
        }

        let formData = new FormData();
        formData.append('filename', this.filename)
        formData.append('file', this.files[0])
        formData.append('type', this.fileType)
        console.log('>> formData >> ', formData)

        api.post(`/files/generate-from-file`,
            formData, {
              headers: {
                'Content-Type': 'multipart/form-data'
              }
            }
        ).then(response => {
          processState(this, "onSuccess")
          this.$router.push("/system/files")
        })
        .catch(err => {
          processState(this, "onError", err)
        })
      },
      submitForm() {
        let form = this.$el.querySelector('form')
        if (!form.checkValidity()) {
          form.classList.add('was-validated')
          return
        }

        let formData = new FormData()
        formData.append('name', this.filename)
        formData.append('type', this.fileType)
        formData.append('content', this.content)

        api.post(`/files/generate-from-content`, formData)
        .then(response => {
          processState(this, "onSuccess")
          this.$router.push("/system/files")
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