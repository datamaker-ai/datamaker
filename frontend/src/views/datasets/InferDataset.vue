<template>
  <div>
    <CRow>
      <CCol md="6">
        <CCard>
          <CCardHeader>
            {{ $t('dataset.infer_content_header') }}
          </CCardHeader>
          <CCardBody>
            <CAlert :show="displayError" color="danger">
              <div>{{ $t('common.error_message_template', { msg: errorMessage}) }}</div>
              <div v-if="errorDetails">{{ $t('common.error_details_template', { msg: errorDetails }) }}</div>
            </CAlert>
            <CForm class="needs-validation" novalidate>
              <CInput
                      :description="$t('dataset.name_description')"
                      :label="$t('dataset.name_label')"
                      horizontal
                      :placeholder="$t('dataset.name_placeholder')"
                      v-model="name"
              />
              <CSelect
                  :value.sync="workspace"
                  :options="workspaces"
                  :value="workspace"
                  :label="$t('dataset.workspace_label')"
                  :placeholder="$t('dataset.workspace_placeholder')"
                  :title="$t('dataset.workspace_title')"
                  horizontal
                  required
              ></CSelect>
              <CTextarea
                  :label="$t('dataset.content_sample_label')"
                  :placeholder="$t('dataset.content_sample_placeholder')"
                  horizontal
                  rows="9"
                  v-model="content"
                  :invalid-feedback="$t('common.feedback_not_empty')"
              />
              <CInputFile
                  class="form-group"
                  :label="$t('dataset.content_file_label')"
                  horizontal
                  custom
                  @change="files = $event"
              />
              {{ (files.length > 0) ? files[0].name : '' }}
              <CSelect
                  :label="$t('dataset.content_type_label')"
                  horizontal
                  :options="[
                      {'label': $t('common.select'), 'value': '', 'disabled': 'disabled'},
                      {'label': 'AVRO', 'value': 'application/avro-binary'},
                      {'label': 'CSV', 'value': 'text/csv'},
                      {'label': 'EXCEL', 'value': 'application/x-excel'},
                      {'label': 'JSON', 'value': 'application/json'},
                      {'label': 'JSON Schema', 'value': 'application/schema+json'},
                      {'label': 'PARQUET', 'value': 'application/parquet'},
                      {'label': 'TEXT', 'value': 'text/plain'},
                      {'label': 'SQL', 'value': 'application/sql'},
                      {'label': 'XML', 'value': 'application/xml'},
                      {'label': 'XSD', 'value': 'application/xsd'}
                    ]"
                  :placeholder="$t('common.select')"
                  v-on:update:value="changeContent"
                  required
              />
              <template v-if="processorConfigs.length > 0">
                <span><strong>{{ $t('common.field_options') }}</strong></span>
                <FieldOptions :item="{className: '', config: config}"
                              :current-item="0"
                              :items="[{config: config}]"
                              :fieldOptions="processorConfigs"
                              @config-change="(key, value) => addToProcessorConfig(key, value)">
                </FieldOptions>
              </template>
              <template v-if="isLoading">
                <CElementCover
                    :boundaries="[
            { sides: ['top'], query: 'form' },
            { sides: ['bottom'], query: 'form' }
          ]"
                />
              </template>
            </CForm>
            <!--
            {{ config }}
            {{ componentsData.processors }}
            -->
          </CCardBody>
          <CCardFooter>
            <CButton type="submit" size="sm" class="mr-1" color="primary" @click="(files.length > 0) ? submitFile() : submitForm()"><CIcon name="cil-check-circle"/> {{ $t('common.submit_button') }}</CButton>
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
  import FieldOptions from "./FieldOptions";

  const TYPES_TO_PROCESSOR = {
    'application/parquet': 'ParquetProcessor',
    'application/avro-binary': 'AvroProcessor',
    'text/csv': 'CsvProcessor',
    'application/x-excel': 'ExcelProcessor',
    'application/json': 'JsonProcessor',
    'application/schema+json': 'JsonSchemaProcessor',
    'text/plain': 'CsvProcessor',
    'application/sql': 'SqlProcessor',
    'application/xml': 'XmlProcessor',
    'application/xsd': 'XmlSchemaProcessor'
  }

  export default {
    name: "InferDataset",
    components: {
      FieldOptions
    },
    data() {
      return {
        componentsData: {processors: []},
        name: '',
        contentType: '',
        content: '',
        description: '',
        workspace: '',
        config: {},
        files: [],
        workspaces: [],
        displayError: false,
        errorMessage: null,
        errorDetails: null,
        processorConfigs: [],
        isLoading: false
      }
    },
    mounted() {
      getWorkspaceOptions()
        .then(value => this.workspaces = value)
        .catch(err => {})
      getComponents()
        .then(response => {
          this.$set(this.componentsData, "processors", response.processors)
        })
        .catch(err => {})
    },
    methods: {
      addToProcessorConfig(key, value) {
        //this.config[key] = value
        this.$forceUpdate()
        // let copyConfig = {...this.config}
        // copyConfig[key] = value
        //this.$set(this.config, key, value)
      },
      changeContent(content) {
        if (this.contentType !== content) {
          this.config = {}
          this.processorConfigs = []
        }
        this.contentType = content

        let className = TYPES_TO_PROCESSOR[content]
        let processor = this.componentsData.processors.find(element => element.className.endsWith(className))

        for (let input of processor.configProperties) {
          let item = input
          if (input.key in this.config) {
            input.value = this.config[input.key]
          } else {
            input.value = input.defaultValue
            if (input.type === 'LIST') {
              this.config[input.key] = []
            }
          }
          this.processorConfigs.push(item)
          //configs.push(input)
          //configs.push({value: input.defaultValue.toString(), key: input.key, label: input.description, placeholder: input.description})
        }
        //this.dataJob.generator = generator.value
        //this.generatorConfigs = configs
      },
      submitFile() {
        let form = this.$el.querySelector('form')
        if (!form.checkValidity()) {
          form.classList.add('was-validated')
          return
        }
        this.isLoading = true

        let formData = new FormData();
        formData.append('name', this.name)
        formData.append('file', this.files[0])
        formData.append('mediaType', this.contentType)
        formData.append('config', JSON.stringify(this.config))
        // console.log('>> formData >> ', formData)

        api.post(`/dataset/generate-from-file/${this.workspace}`,
            formData, {
              headers: {
                'Content-Type': 'multipart/form-data'
              }
            }
        ).then(response => {
          processState(this, "onSuccess")
          let item = response.data
          this.$router.push({path:'edit/' + item.externalId, query: {name: item.name}})
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
        this.isLoading = true

        let formData = new FormData()
        formData.append('name', this.name)
        formData.append('config', JSON.stringify(this.config))
        formData.append('content', this.content)
        formData.append('mediaType', this.contentType)

        api.post(`/dataset/generate-from-content/${this.workspace}`, formData)
        .then(response => {
          processState(this, "onSuccess")
          let item = response.data
          this.$router.push({path:'edit/' + item.externalId, query: {name: item.name}})
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