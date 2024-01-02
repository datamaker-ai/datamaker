<template>
  <CForm id="dataset">
    <CInput
        :description="$t('dataset.name_description')"
        :label="$t('dataset.name_label')"
        horizontal
        :placeholder="$t('dataset.name_placeholder')"
        v-model="dataset.name"
        required
    />
    <CSelect
        :value.sync="dataset.workspaceId"
        :options="workspaces"
        :label="$t('dataset.workspace_label')"
        horizontal
        :title="$t('dataset.workspace_title')"
        required
    >
    </CSelect>
    <CInput
        :description="$t('dataset.description_description')"
        :label="$t('dataset.description_label')"
        horizontal
        :placeholder="$t('dataset.description_placeholder')"
        v-model="dataset.description"
    />
    <CSelect
        :value.sync="dataset.languageTag"
        :label="$t('dataset.locale_label')"
        horizontal
        :options="supportedLanguages"
        required
    >
    </CSelect>
    <CInput
        :description="$t('dataset.tags_description')"
        :label="$t('dataset.tags_label')"
        horizontal
        :placeholder="$t('dataset.tags_placeholder')"
        :value="dataset.tags.join(',')"
        @update:value="dataset.tags = $event.split(',')"
    />
    <CFormGroup class="form-group form-row">
      <template #label>
        <label for="exportHeader" class="col-form-label col-sm-3">
          {{ $t('dataset.export_header_label') }}
        </label>
      </template>
      <template #input>
        <CSwitch class="mx-1"
                 id="exportHeader"
                 color="primary"
                 :value="dataset.exportHeader"
                 @update:checked="dataset.exportHeader = $event"
                 :checked="dataset.exportHeader === true"
                 variant="3d"
                 label-on="ON"
                 label-off="OFF" />
      </template>
    </CFormGroup>
    <CInput
        type="range"
        :label="$t('dataset.null_percent_label') + dataset.nullablePercentLimit + '%'"
        v-model="dataset.nullablePercentLimit"
        min="0" max="100"
        horizontal
        custom
    />
    <CFormGroup class="form-group form-row">
      <template #label>
        <label for="allowDuplicates" class="col-form-label col-sm-3">
          {{ $t('dataset.allow_duplicates_label') }}
        </label>
      </template>
      <template #input>
        <CSwitch class="mx-1"
                 id="allowDuplicates"
                 color="primary"
                 :value="dataset.allowDuplicates"
                 @update:checked="dataset.allowDuplicates = $event"
                 :checked="dataset.allowDuplicates === true"
                 variant="3d"
                 label-on="ON"
                 label-off="OFF" />
      </template>
    </CFormGroup>
    <CInput
        type="range"
        :label="$t('dataset.duplicates_percent_label') + dataset.duplicatesPercentLimit + '%'"
        v-model="dataset.duplicatesPercentLimit"
        min="0" max="100"
        horizontal
        custom
    />
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
  import { Translation } from '../../plugins/Translation'
  import {SUPPORTED_LOCALES} from "../../constants/globals";
  import { getWorkspaceOptions } from "../../constants/api";

  export default {
    name: 'DatasetForm',
    props: {
      isLoading: {
        type: Boolean,
        default: false
      },
      dataset: {
        type: Object,
        // Object or array defaults must be returned from
        // a factory function
        default: function () {
          return {
            name: '',
            description: '',
            workspaceId: '',
            languageTag: '',
            tags: [],
            exportHeader: true,
            nullablePercentLimit: 10,
            duplicatesPercentLimit: 0,
            allowDuplicates: true,
            numberOfRetries: 10
          }
        }
      },
      options: {
        type: Array,
        default: () => ['NONE', 'READ_ONLY', 'READ_EXECUTE', 'READ_WRITE', 'FULL']
      }
    },
    mounted() {
      getWorkspaceOptions()
      .then(value => this.workspaces = value)
      .catch(err => {})
    },
    data () {
      return {
        workspaces: [],
        supportedLanguages: SUPPORTED_LOCALES
      }
    },
    methods: {
    }
  }
</script>

<style>
  input[type=range] {
    padding: 0 0;
  }
</style>
