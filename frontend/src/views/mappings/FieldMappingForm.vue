<template>
  <CForm>
    <CInput v-model="fieldMapping.name"
            :label="$t('mappings.name_label')"
            horizontal
            :placeholder="$t('mappings.change_name_placeholder')"
            required
    />
    <CSelect
        :value.sync="fieldMapping.languageTag"
        :label="$t('common.language')"
        horizontal
        :options="supportedLanguages"
        required
    />
    <CFormGroup class="form-group form-row">
      <template #label>
        <label for="primaryKey" class="col-form-label col-sm-3">
          {{ $t('mappings.primary_key_label') }}
        </label>
      </template>
      <template #input>
        <CSwitch
          v-c-tooltip="{content: $t('mappings.invalid_reference_tooltip')}"
          class="mx-1"
          id="primaryKey"
          color="primary"
          :value="fieldMapping.isPrimaryKey"
          @update:checked="fieldMapping.isPrimaryKey = $event"
          :checked="fieldMapping.isPrimaryKey === true"
          variant="3d"
          label-on="ON"
          label-off="OFF" />
      </template>
    </CFormGroup>
    <CFormGroup class="form-group form-row">
      <template #label>
        <label for="isNullable" class="col-form-label col-sm-3">
          {{ $t('mappings.nullable_label') }}
        </label>
      </template>
      <template #input>
        <CSwitch
            v-c-tooltip="{content: $t('mappings.null_value_placeholder')}"
            class="mx-1"
            id="isNullable"
            color="primary"
            :value="fieldMapping.isNullable"
            @update:checked="fieldMapping.isNullable = $event"
            :checked="fieldMapping.isNullable === true"
            variant="3d"
            label-on="ON"
            label-off="OFF" />
      </template>
    </CFormGroup>
    <CFormGroup class="form-group form-row">
      <template #label>
        <label class="col-form-label col-sm-3">
          {{ $t('mappings.data_type_label') }}
        </label>
      </template>
      <template #input>
        <div class="col-sm-9">
          <Select2 :value="fieldMapping.className"
                   :options="getFieldOptionsGroups()"
                   :settings="{theme: 'bootstrap'}"
                   @select="mySelectEvent($event)"
                   :placeholder="$t('mappings.select_data_type_placeholder')"
                   required
                   theme="bootstrap"
          />
        </div>
      </template>
    </CFormGroup>
    <CSelect
        v-on:update:value="(value, args) => setFormatterItem(value, args)"
        :options="getFormatterOptions()"
        :value="fieldMapping.formatterClassName"
        title="Select data type"
        class="form-control-10"
        :label="$t('mappings.formatter_label')"
        v-c-tooltip="{content: $t('mappings.formatter_tooltip')}"
        horizontal
    />
    <template v-if="fieldMapping.className && fieldOptions.length > 0">
      <div>
        <span><strong>{{ $t('common.field_options') }}</strong></span>
      </div>
      <FieldOptions :item="fieldMapping"
                    :current-item="0"
                    :items="items"
                    v-on:config-change="(key, value) => configChange(key, value)"
                    :fieldOptions="fieldOptions">
      </FieldOptions>
    </template>
    <template v-if="fieldMapping.formatterClassName && formatterOptions.length > 0">
      <div>
        <span><strong>{{ $t('common.formatter_options') }}</strong></span>
      </div>
      <FieldOptions :item="fieldMapping"
                    :current-item="0"
                    :items="items"
                    v-on:config-change="(key, value) => configChange(key, value)"
                    :fieldOptions="formatterOptions">

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
</template>

<script>
  import { api, getComponents } from "../../constants/api";
  import FieldOptions from "../datasets/FieldOptions"
  import { Translation } from '../../plugins/Translation'
  import Select2 from 'v-select2-component';
  import 'select2-bootstrap-theme/dist/select2-bootstrap.css'
  import {SUPPORTED_LOCALES} from "../../constants/globals";

  export default {
    name: "FieldMappingForm",
    components: {
      FieldOptions,
      Select2
    },
    props: {
      isLoading: {
        type: Boolean,
        default: false
      },
      fieldMapping: {
        type: Object
      },
      items: {
        type: Array
      }
    },
    data() {
      return {
        disabledButtons: false,
        dangerModal: false,
        fieldConfigs: [],
        fieldOptions: [],
        formatterOptions: [],
        supportedLanguages: SUPPORTED_LOCALES,
        componentsData: {sinks: [], generators: [], fields: [], formatters: []}
      }
    },
    beforeUpdate() {
      if (this.fieldMapping.className || this.fieldMapping.formatterClassName) {
        this.showDetailsRow()
      }
    },
    mounted() {
      getComponents()
      .then(response => {
        this.$set(this.componentsData, "fields", response.fields)
        this.$set(this.componentsData, "formatters", response.formatters)
      })
      .catch(err => {})
    },
    methods: {
      getOptionsFromConfig(componentFound) {
        for (let input of componentFound.configProperties) {
          if (input.key in this.fieldMapping.config) {
            input.value = this.fieldMapping.config[input.key]
          } else {
            input.value = input.defaultValue
            this.fieldMapping.config[input.key] = input.defaultValue
          }
          input.possibleValues =  input.possibleValues.sort()
        }
        return componentFound.configProperties
      },
      getFieldOptionsGroups() {
        let options = []
        let groups = {}
        for (let field of this.componentsData.fields) {
          if (!(field.grouping in groups)) {
            groups[field.grouping] = []
          }
          groups[field.grouping].push({id: field.className, text: field.name})
        }
        for (let group in groups) {
          groups[group].sort(function(a, b){
            if ( a.text < b.text ){
              return -1
            }
            if ( a.text > b.text ){
              return 1
            }
            return 0
          })
          options.push({"children": groups[group], "text": group.toUpperCase()})
        }
        options.sort(function(a, b){
          if ( a.text < b.text ){
            return -1
          }
          if ( a.text > b.text ){
            return 1
          }
          return 0
        })
        options.unshift({id: null, text: "Select one...", disabled: "disabled", selected: "selected"})
        return options
      },
      getFieldOptions() {
        let options = []

        for (let field of this.componentsData.fields) {
          options.push({label: field.name, value:field.className, config: field.configProperties })
        }
        options.sort(function(a, b){
          if ( a.label < b.label ){
            return -1;
          }
          if ( a.label > b.label ){
            return 1;
          }
          return 0;
        });
        options.unshift({value: null, label: "Select one...", config: [], disabled: "disabled", selected: "selected"})
        return options
      },
      getFormatterOptions() {
        let options = []

        for (let formatter of this.componentsData.formatters) {
          options.push({label: formatter.name, value: formatter.className, config: formatter.configProperties })
        }
        options.sort(function(a, b){
          if ( a.label < b.label ){
            return -1;
          }
          if ( a.label > b.label ){
            return 1;
          }
          return 0;
        });
        options.unshift({value: "None", label: "None", config: [], selected: "selected"})
        return options
      },
      mySelectEvent({id, text, element}){
        if (this.fieldMapping.className !== id) {
          this.$set(this.fieldMapping, 'config', {})
        }
        if (!this.fieldMapping.name) {
          this.fieldMapping.name = text
        }
        this.fieldMapping.className = id
        this.showDetailsRow()
      },
      setItem(value, args) {
        if (this.fieldMapping.className !== value) {
          this.$set(this.fieldMapping, 'config', {})
        }
        if (!this.fieldMapping.name) {
          this.fieldMapping.name = args.target.selectedOptions[0].label
        }
        this.fieldMapping.className = value
        this.showDetailsRow()
      },
      setFormatterItem(value) {
        this.fieldMapping.formatterClassName = value === 'None' ? null : value
        this.showDetailsRow()
      },
      showDetailsRow() {
        let fieldAndFormatterOptions = []

        const found = this.componentsData.fields.find(field => field.className === this.fieldMapping.className)

        if (found) {
          fieldAndFormatterOptions = this.getOptionsFromConfig(found)
        }

        const formatterFound = this.componentsData.formatters.find(field => field.className === this.fieldMapping.formatterClassName)

        if (formatterFound) {
          this.formatterOptions = this.getOptionsFromConfig(formatterFound)
        }

        this.fieldOptions = fieldAndFormatterOptions
      },
      configChange(key, value) {
        // this.fieldMapping.config[key] = value
        let copyConfig = {...this.fieldMapping.config}
        copyConfig[key] = value
        this.$set(this.fieldMapping, 'config', copyConfig)
      }
    }
  }
</script>

<style scoped>

</style>